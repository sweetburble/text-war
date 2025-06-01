package com.bandi.textwar.data.remote

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.bandi.textwar.BuildConfig
import com.bandi.textwar.data.models.CharacterDetail
import timber.log.Timber

private const val OPENAI_CHAT_MODEL = "gpt-4.1-mini-2025-04-14"

/**
 * OpenAI API와 상호작용하기 위한 서비스 클래스입니다.
 * com.aallam.openai.client.OpenAI 인스턴스를 초기화하고 관리합니다.
*/
class OpenAIService {
    private var openAIClient: OpenAI? = null
    private var apiKey: String? = null

    init {
        initializeClient()
    }

    /**
     * OpenAI 클라이언트를 초기화합니다.
     * API 키는 OPENAI_API_KEY 환경 변수를 통해 자동으로 로드됩니다.
     * 클라이언트 초기화에 실패하면 에러를 로깅하고 client를 null로 설정합니다.
     */
    private fun initializeClient() {
        try {
            val apiKeyFromConfig = BuildConfig.OPENAI_API_KEY

            if (apiKeyFromConfig.isEmpty()) {
                Timber.e("OpenAI API 키가 BuildConfig에 올바르게 설정되지 않았습니다. local.properties 및 build.gradle 설정을 확인하세요.")
                this.apiKey = null
                openAIClient = null
            } else {
                this.apiKey = apiKeyFromConfig
                openAIClient = OpenAI(token = apiKeyFromConfig)
                Timber.i("OpenAI 클라이언트가 성공적으로 초기화되었습니다.")
            }
        } catch (e: Exception) {
            Timber.e(e, "OpenAI 클라이언트 초기화 중 오류 발생")
            this.apiKey = null
            openAIClient = null
        }
    }

    /**
     * 초기화된 OpenAI 인스턴스를 반환합니다.
     * 클라이언트가 성공적으로 초기화되지 않은 경우 null을 반환할 수 있습니다.
     * @return OpenAI 인스턴스 또는 null
     */
    fun getClient(): OpenAI? {
        if (openAIClient == null) {
            Timber.w("OpenAI 클라이언트가 초기화되지 않았습니다. API 키 및 설정을 확인하세요.")
        }
        return openAIClient
    }

    /**
     * 두 캐릭터 간의 전투 내용을 생성하고 결과를 반환합니다. (suspend 함수로 변경)
     * @param characterA 첫 번째 캐릭터의 상세 정보
     * @param characterB 두 번째 캐릭터의 상세 정보
     * @return BattleResult(전투 내용, 승자 이름) 또는 API 호출 실패 시 null
     */
    suspend fun generateBattleNarrative(characterA: CharacterDetail, characterB: CharacterDetail): BattleResult? {
        if (apiKey.isNullOrEmpty()) {
            Timber.e("OpenAI API 키가 설정되지 않았습니다. 전투 내용 생성을 스킵합니다.")
            return BattleResult("OpenAI API 키가 설정되지 않았습니다. 앱 설정을 확인해주세요.", null)
        }

        val currentClient = openAIClient ?: run {
            Timber.e("OpenAI 클라이언트가 초기화되지 않아 (API 키는 존재하나 클라이언트 생성 실패) 전투 내용 생성을 스킵합니다.")
            return BattleResult("OpenAI 클라이언트 초기화에 실패했습니다. 잠시 후 다시 시도해주세요.", null)
        }

        val prompt = createBattlePrompt(characterA, characterB)

        return try {
            val chatRequest = ChatCompletionRequest(
                model = ModelId(OPENAI_CHAT_MODEL),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.User,
                        content = prompt
                    )
                )
            )

            Timber.d("OpenAI 요청: 모델=$OPENAI_CHAT_MODEL, 프롬프트 길이=${prompt.length}")
            val completion = currentClient.chatCompletion(chatRequest)
            Timber.d("OpenAI 응답: ${completion.choices.firstOrNull()?.message?.content?.take(100)}...")
            val responseText = completion.choices.firstOrNull()?.message?.content
            parseBattleResult(responseText)
        } catch (e: Exception) {
            Timber.e("OpenAI API 호출 중 오류 발생 : ${e.toString()}")
            BattleResult("전투 내용 생성 중 오류가 발생했습니다: ${e.localizedMessage}", null)
        }
    }

    private fun createBattlePrompt(characterA: CharacterDetail, characterB: CharacterDetail): String {
        val characterADetails = "이름: ${characterA.characterName}, 설명: ${characterA.description}"
        val characterBDetails = "이름: ${characterB.characterName}, 설명: ${characterB.description}"

        return """
            두 명의 용감한 전사가 숙명의 대결을 펼칩니다!

            전사 A: $characterADetails
            전사 B: $characterBDetails

            이 두 전사의 치열한 전투 과정을 상세하고 흥미진진하게 묘사해주세요. 
            전투는 여러 턴에 걸쳐 진행될 수 있으며, 각 전사의 기술이나 특징이 드러나도록 해주세요.
            묘사는 한국어로 최소 500자 이상으로 작성해주세요.
            마지막에는 반드시 명확하게 승자를 선언해야 합니다. 
            승자 선언은 다음 형식으로 끝나야 합니다: "승자: [전사 A의 이름 또는 전사 B의 이름]"
            다른 말은 포함하지 말고 오직 "승자: [이름]" 형식으로만 끝나야 합니다.
            
            예시:
            ... (500자 이상의 전투 과정 묘사) ...
            승자: ${characterA.characterName} 
            
            또는
            
            ... (500자 이상의 전투 과정 묘사) ...
            승자: ${characterB.characterName}
        """.trimIndent()
    }

    data class BattleResult(val narrative: String?, val winnerName: String?)

    /**
     * OpenAI API 응답 텍스트에서 전투 내용과 승자 이름을 파싱합니다.
     * @param responseText 파싱할 전체 응답 문자열
     * @return BattleResult 객체 또는 파싱 실패 시 null
     */
    private fun parseBattleResult(responseText: String?): BattleResult? {
        if (responseText.isNullOrBlank()) {
            Timber.w("OpenAI 응답이 비어있거나 null입니다.")
            return BattleResult("전투 내용을 생성하지 못했습니다. (API 응답 없음)", null)
        }

        return try {
            val winnerMarker = "승자: "
            val winnerNameStartIndex = responseText.lastIndexOf(winnerMarker)

            if (winnerNameStartIndex == -1) {
                Timber.w("응답에서 승자 정보를 찾을 수 없습니다. 형식: '$winnerMarker[이름]'. 전체 응답: $responseText")
                return BattleResult(narrative = responseText, winnerName = null)
            }

            val narrative = responseText.substring(0, winnerNameStartIndex).trim()
            val winnerNameString = responseText.substring(winnerNameStartIndex + winnerMarker.length).trim()
            
            val actualWinnerName = winnerNameString.lineSequence().firstOrNull()?.trim().takeIf { !it.isNullOrEmpty() } ?: winnerNameString.trim()

            if (actualWinnerName.isEmpty()) {
                Timber.w("승자 이름이 비어있습니다. (파싱 후)")
                return BattleResult(narrative = narrative, winnerName = null)
            }

            BattleResult(narrative, actualWinnerName)
        } catch (e: Exception) {
            Timber.e(e, "OpenAI 응답 파싱 중 오류 발생")
            BattleResult(responseText, null)
        }
    }
}
