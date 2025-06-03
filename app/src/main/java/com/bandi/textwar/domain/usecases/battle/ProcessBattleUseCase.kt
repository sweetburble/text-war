package com.bandi.textwar.domain.usecases.battle

import android.util.Base64
import com.bandi.textwar.data.models.BattleRecordInput
import com.bandi.textwar.data.remote.OpenAIService
import com.bandi.textwar.domain.repository.BattleRecordsRepository
import com.bandi.textwar.domain.repository.CharacterRepository
import com.bandi.textwar.domain.repository.StorageRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

/**
 * 전투 결과를 가져오고, 캐릭터의 승패 정보를 업데이트하며, 전투 기록을 저장하는 UseCase
 */
class ProcessBattleUseCase @Inject constructor(
    private val characterRepository: CharacterRepository,
    private val openAIService: OpenAIService,
    private val saveBattleRecordUseCase: SaveBattleRecordUseCase,
    private val storageRepository: StorageRepository,
    private val supabaseClient: SupabaseClient,
    private val battleRecordsRepository: BattleRecordsRepository
) {
    /**
     * UseCase를 실행
     * @param myCharacterId 내 캐릭터의 ID
     * @param opponentId 상대방 캐릭터의 ID
     * @return 전투 결과와 내 캐릭터 이름을 담은 Pair를 Flow로 반환
     *         오류 발생 시 예외를 발생시킵니다.
     */
    operator fun invoke(myCharacterId: String, opponentId: String): Flow<Pair<OpenAIService.BattleResult, String>> = flow {
        Timber.d("ProcessBattleResultUseCase 시작: 내 캐릭터 ID=$myCharacterId, 상대 ID=$opponentId")
        // 1. 내 캐릭터 정보 가져오기
        val myCharacter = characterRepository.getCharacterDetail(myCharacterId).firstOrNull()
            ?: run {
                Timber.e("내 캐릭터 정보를 가져올 수 없습니다: $myCharacterId")
                throw IllegalStateException("내 캐릭터 정보를 가져올 수 없습니다: $myCharacterId")
            }

        // 2. 상대방 캐릭터 정보 가져오기
        val opponentCharacter = characterRepository.getCharacterDetail(opponentId).firstOrNull()
            ?: run {
                Timber.e("상대방 캐릭터 정보를 가져올 수 없습니다: $opponentId")
                throw IllegalStateException("상대방 캐릭터 정보를 가져올 수 없습니다: $opponentId")
            }

        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: run {
            Timber.e("현재 사용자 ID를 가져올 수 없어 이미지 저장이 불가능합니다.")
            throw IllegalStateException("로그인된 사용자 ID를 가져올 수 없습니다. 이미지 저장이 불가능합니다.")
        }

        // 3. OpenAI API로 전투 결과 생성
        Timber.d("OpenAI 전투 내용 생성 요청")
        val battleResultOpenAI = openAIService.generateBattleNarrative(myCharacter, opponentCharacter)
            ?: run {
                Timber.e("전투 결과를 생성하지 못했습니다.")
                throw IllegalStateException("전투 결과를 생성하지 못했습니다.")
            }
        Timber.i("OpenAI 전투 내용 생성 완료. 승자: ${battleResultOpenAI.winnerName}, 내용: ${battleResultOpenAI.narrative?.take(50)}...")

        // 4. 전투 결과에 따라 승패 업데이트
        battleResultOpenAI.winnerName?.let {
            winnerName ->
            Timber.d("승자: $winnerName. 승패 업데이트 시작")
            val myCharacterWon = winnerName == myCharacter.characterName
            val opponentWon = winnerName == opponentCharacter.characterName

            if (myCharacterWon) {
                characterRepository.updateCharacterBattleStats(myCharacter.id, true)
                characterRepository.updateCharacterBattleStats(opponentCharacter.id, false)
                Timber.i("${myCharacter.characterName} 승리, ${opponentCharacter.characterName} 패배 기록")
            } else if (opponentWon) {
                characterRepository.updateCharacterBattleStats(myCharacter.id, false)
                characterRepository.updateCharacterBattleStats(opponentCharacter.id, true)
                Timber.i("${myCharacter.characterName} 패배, ${opponentCharacter.characterName} 승리 기록")
            } else {
                Timber.w("승자 이름($winnerName)이 캐릭터 이름과 일치하지 않아 승패 기록 안함.")
            }
        } ?: Timber.w("승자 이름이 없어 승패를 기록할 수 없습니다.")

        // 5. OpenAI API로 전투 이미지 생성
        var generatedImageResultDataUri: String? = null
        if (!battleResultOpenAI.narrative.isNullOrBlank()) {
            Timber.d("OpenAI 전투 이미지 생성 요청")
            try {
                val imageGenResult = openAIService.generateBattleImage(battleResultOpenAI.narrative, battleResultOpenAI.winnerName)
                generatedImageResultDataUri = imageGenResult?.imageUrl
                if (generatedImageResultDataUri != null) {
                    Timber.i("OpenAI 전투 이미지 생성 완료 (데이터 URI): ${generatedImageResultDataUri.take(100)}...")
                } else {
                    Timber.w("OpenAI 전투 이미지 생성 실패 (데이터 URI 없음): ${imageGenResult?.errorMessage}")
                }
            } catch (e: Exception) {
                Timber.e(e, "OpenAI 전투 이미지 생성 중 예외 발생")
            }
        } else {
            Timber.w("전투 내용이 없어 이미지 생성을 건너뜁니다.")
        }

        // 6. 전투 기록 저장
        Timber.d("전투 기록 저장 시작")
        val recordInput = BattleRecordInput(
            characterAId = myCharacter.id,
            characterBId = opponentCharacter.id,
            winnerId = battleResultOpenAI.winnerName?.let { wn ->
                if (wn == myCharacter.characterName) myCharacter.id
                else if (wn == opponentCharacter.id) opponentCharacter.id
                else null
            },
            narrative = battleResultOpenAI.narrative,
            imageUrl = null // 초기에는 null로 설정
        )

        var battleRecordIdFromDb: String? = null
        var finalImageUrlForDisplayAndDb: String? = null

        saveBattleRecordUseCase(recordInput)
            .onSuccess { savedRecordId ->
                battleRecordIdFromDb = savedRecordId
                Timber.i("전투 기록 저장 성공, ID: $battleRecordIdFromDb")

                if (!generatedImageResultDataUri.isNullOrBlank()) {
                    val parts = generatedImageResultDataUri.split(',', limit = 2)
                    if (parts.size == 2) {
                        val header = parts[0]
                        val base64Data = parts[1]
                        val mimeTypePart = header.removePrefix("data:").removeSuffix(";base64")

                        if (mimeTypePart.isNotBlank() && mimeTypePart.startsWith("image/") && base64Data.isNotBlank()) {
                            Timber.d("데이터 URI 파싱 성공: MIME Type = $mimeTypePart, Base64 데이터 길이 = ${base64Data.length}")
                            try {
                                val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
                                val fileExtension = mimeTypePart.substringAfterLast('/', "png")
                                val uploadFileName = "$battleRecordIdFromDb.$fileExtension" // 파일명은 battle_records.id 사용
                                val uploadFilePath = "public/$userId/$uploadFileName"      // 경로는 public/사용자ID/battle_records.id.확장자

                                Timber.d("이미지 업로드 시도: 경로=$uploadFilePath, MIME=$mimeTypePart")
                                storageRepository.uploadImage(uploadFilePath, imageBytes, mimeTypePart)
                                    .fold(
                                        ifLeft = { throwable ->
                                            Timber.e(throwable, "Supabase Storage 이미지 업로드 실패")
                                            // 업로드 실패 시 finalImageUrlForDisplayAndDb는 null로 유지
                                        },
                                        ifRight = { uploadedUrl ->
                                            finalImageUrlForDisplayAndDb = uploadedUrl
                                            Timber.i("Supabase Storage 이미지 업로드 성공: $finalImageUrlForDisplayAndDb")
                                            // DB에 이미지 URL 업데이트
                                            battleRecordsRepository.updateImageUrl(
                                                battleRecordIdFromDb, finalImageUrlForDisplayAndDb!!)
                                                .onFailure { e -> 
                                                    Timber.e(e, "DB에 이미지 URL 업데이트 실패: recordId=$battleRecordIdFromDb, url=$finalImageUrlForDisplayAndDb")
                                                    finalImageUrlForDisplayAndDb = null // DB 업데이트 실패 시 UI에도 반영 안되도록 null 처리
                                                }
                                                .onSuccess { Timber.i("DB 이미지 URL 업데이트 성공")}
                                        }
                                    )
                            } catch (e: IllegalArgumentException) {
                                Timber.e(e, "Base64 디코딩 실패.")
                            } catch (e: Exception) {
                                Timber.e(e, "이미지 처리 또는 업로드 중 예외 발생")
                            }
                        } else {
                            Timber.w("Data URI 헤더 형식이 올바르지 않아 파싱 실패: Header='${header}'")
                        }
                    } else {
                        Timber.w("Data URI 형식이 올바르지 않아 ',' 기준으로 분리 실패: $generatedImageResultDataUri")
                    }
                }
            }
            .onFailure { e ->
                Timber.e(e, "전투 기록 저장 실패")
                // 전투 기록 저장 실패는 사용자에게 직접적인 오류로 표시하지 않을 수 있음 (백그라운드 작업)
                // 하지만 로깅은 중요
            }

        // 7. 결과 반환
        if (battleResultOpenAI.winnerName != null || battleResultOpenAI.narrative != null) {
            // 승자가 있거나, 서사가 있는 경우 (즉, API 호출이 어느정도 성공한 경우)
            val finalBattleResult = OpenAIService.BattleResult(
                narrative = battleResultOpenAI.narrative,
                winnerName = battleResultOpenAI.winnerName,
                imageUrl = finalImageUrlForDisplayAndDb // Storage에 업로드된 최종 URL 또는 실패 시 null
            )
            emit(Pair(finalBattleResult, myCharacter.characterName))
        } else {
            // API 호출 완전 실패 (승자도 없고, 서사도 없는 경우)
            Timber.e("최종 전투 결과 반환 실패: 승자도 없고 내용도 없음")
            throw IllegalStateException(battleResultOpenAI.narrative ?: "승자와 전투 내용을 모두 가져오지 못했습니다.")
        }
    }
} 