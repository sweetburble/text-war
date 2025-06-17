package com.bandi.textwar.data.datasource.remote

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.bandi.textwar.BuildConfig
import com.bandi.textwar.data.models.CharacterDetail
import timber.log.Timber
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val OPENAI_CHAT_MODEL = "gpt-4o-mini-2024-07-18"
private const val OPENAI_IMAGE_MODEL = "gpt-image-1"
private const val OPENAI_IMAGE_GENERATION_URL = "https://api.openai.com/v1/images/generations"

/**
 * OpenAI API와 상호작용하기 위한 서비스 클래스
 * com.aallam.openai.client.OpenAI 인스턴스를 초기화하고 관리
 * 이미지 생성은 Ktor HTTP 클라이언트를 사용
 */
class OpenAIService {
    private var openAIClient: OpenAI? = null
    private var apiKey: String? = null
    private val ktorHttpClient: HttpClient

    init {
        initializeClient()
        ktorHttpClient = HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 100000
                connectTimeoutMillis = 10000
                socketTimeoutMillis = 70000
            }
        }
    }

    /**
     * OpenAI 클라이언트를 초기화
     * API 키는 OPENAI_API_KEY 환경 변수를 통해 자동으로 로드됩니다.
     * 클라이언트 초기화에 실패하면 에러를 로깅하고 client를 null로 설정
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
                Timber.i("OpenAI SDK 클라이언트가 성공적으로 초기화되었습니다.")
            }
        } catch (e: Exception) {
            Timber.e(e, "OpenAI SDK 클라이언트 초기화 중 오류 발생")
            this.apiKey = null
            openAIClient = null
        }
    }

    /**
     * 초기화된 OpenAI 인스턴스를 반환
     * 클라이언트가 성공적으로 초기화되지 않은 경우 null을 반환할 수 있다.
     * @return OpenAI 인스턴스 또는 null
     */
    fun getClient(): OpenAI? {
        if (openAIClient == null) {
            Timber.w("OpenAI 클라이언트가 초기화되지 않았습니다. API 키 및 설정을 확인하세요.")
        }
        return openAIClient
    }

    /**
     * 두 캐릭터 간의 전투 내용을 생성하고 결과를 반환
     * @param characterA 첫 번째 캐릭터의 상세 정보
     * @param characterB 두 번째 캐릭터의 상세 정보
     * @return BattleResult(전투 내용, 승자 이름) 또는 API 호출 실패 시 null
     */
    suspend fun generateBattleNarrative(characterA: CharacterDetail, characterB: CharacterDetail): BattleResult? {
        if (apiKey.isNullOrEmpty()) {
            Timber.e("OpenAI API 키가 설정되지 않았습니다. 전투 내용 생성을 스킵합니다.")
            return BattleResult("OpenAI API 키가 설정되지 않았습니다. 앱 설정을 확인해주세요.", null, null)
        }

        val currentClient = openAIClient ?: run {
            Timber.e("OpenAI 클라이언트가 초기화되지 않아 (API 키는 존재하나 클라이언트 생성 실패) 전투 내용 생성을 스킵합니다.")
            return BattleResult("OpenAI 클라이언트 초기화에 실패했습니다. 잠시 후 다시 시도해주세요.", null, null)
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
            BattleResult("전투 내용 생성 중 오류가 발생했습니다: ${e.localizedMessage}", null, null)
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
            묘사는 한국어로 최소 300자 이상으로 작성해주세요.
            마지막에는 반드시 명확하게 승자를 선언해야 합니다. 
            승자 선언은 다음 형식으로 끝나야 합니다: "승자: [전사 A의 이름 또는 전사 B의 이름]"
            다른 말은 포함하지 말고 오직 "승자: [이름]" 형식으로만 끝나야 합니다.
            
            예시:
            ... (300자 이상의 전투 과정 묘사) ...
            승자: ${characterA.characterName} 
            
            또는
            
            ... (300자 이상의 전투 과정 묘사) ...
            승자: ${characterB.characterName}
        """.trimIndent()
    }

    data class BattleResult(val narrative: String?, val winnerName: String?, val imageUrl: String? = null)

    /**
     * OpenAI API 응답 텍스트에서 전투 내용과 승자 이름을 파싱
     * @param responseText 파싱할 전체 응답 문자열
     * @return BattleResult 객체 또는 파싱 실패 시 null
     */
    private fun parseBattleResult(responseText: String?): BattleResult? {
        if (responseText.isNullOrBlank()) {
            Timber.w("OpenAI 응답이 비어있거나 null입니다.")
            return BattleResult("전투 내용을 생성하지 못했습니다. (API 응답 없음)", null, null)
        }

        return try {
            val winnerMarker = "승자: "
            val winnerNameStartIndex = responseText.lastIndexOf(winnerMarker)

            if (winnerNameStartIndex == -1) {
                Timber.w("응답에서 승자 정보를 찾을 수 없습니다. 형식: '$winnerMarker[이름]'. 전체 응답: $responseText")
                return BattleResult(narrative = responseText, winnerName = null, imageUrl = null)
            }

            val narrative = responseText.substring(0, winnerNameStartIndex).trim()
            val winnerNameString = responseText.substring(winnerNameStartIndex + winnerMarker.length).trim()

            val actualWinnerName = winnerNameString.lineSequence().firstOrNull()?.trim().takeIf { !it.isNullOrEmpty() } ?: winnerNameString.trim()

            if (actualWinnerName.isEmpty()) {
                Timber.w("승자 이름이 비어있다. (파싱 후)")
                return BattleResult(narrative = narrative, winnerName = null, imageUrl = null)
            }

            BattleResult(narrative, actualWinnerName, null)
        } catch (e: Exception) {
            Timber.e(e, "OpenAI 응답 파싱 중 오류 발생")
            BattleResult(responseText, null, null)
        }
    }

    /**
     * 이미지 생성 결과를 담는 데이터 클래스
     * @param imageUrl 생성된 이미지의 URL (gpt-image-1에서는 data URI 형태)
     * @param errorMessage 오류 발생 시 메시지
     */
    data class ImageResult(val imageUrl: String?, val errorMessage: String?)

    /**
     * OpenAI gpt-image-1 모델용 이미지 생성 API 요청을 위한 데이터 클래스
     */
    @Serializable
    private data class OpenAIImageRequest(
        val model: String,
        val prompt: String,
        val n: Int = 1,
        val size: String = "1024x1024",
        val quality: String = "medium", // gpt-image-1: "high", "medium", "low"
        val background: String = "auto", // gpt-image-1 새 파라미터: "transparent", "opaque", "auto"
        @SerialName("output_format")
        val outputFormat: String = "png", // gpt-image-1 새 파라미터: "png", "jpeg", "webp"
        val moderation: String = "low" // gpt-image-1 새 파라미터: "low", "auto"
    )

    /**
     * OpenAI gpt-image-1 모델용 이미지 생성 API 응답을 위한 데이터 클래스
     */
    @Serializable
    private data class OpenAIImageResponse(
        val created: Long,
        val data: List<ImageData>
    )

    @Serializable
    private data class ImageData(
        val url: String? = null, // gpt-image-1에서는 사용되지 않음

        @SerialName("b64_json")
        val b64Json: String? = null, // gpt-image-1에서는 이것만 사용됨

        @SerialName("revised_prompt")
        val revisedPrompt: String? = null
    )

    /**
     * 전투 내용을 기반으로 이미지 생성 프롬프트를 생성
     * @param narrative 전투 내용
     * @param winnerName 승자 이름 (선택 사항)
     * @return 생성된 프롬프트 문자열
     */
    private fun createImagePrompt(narrative: String, winnerName: String?): String {
        val winnerDeclaration = winnerName?.let { "승자는 ${it}입니다." } ?: "승패가 명확하지 않은 치열한 전투였습니다."
        return """
            다음은 두 전사의 치열한 전투 장면입니다:
            $narrative
            $winnerDeclaration
            이 장면을 묘사하는 역동적이고 인상적인 이미지를 생성해주세요.
        """.trimIndent()
    }

    /**
     * 전투 내용을 기반으로 Ktor HTTP 클라이언트를 사용하여 이미지를 생성하고 이미지 URL을 반환
     * gpt-image-1 모델은 base64 형태로만 이미지를 반환하므로 data URI로 변환한다.
     * @param battleNarrative 전투 내용
     * @param winnerName 승자 이름 (선택 사항)
     * @return ImageResult(생성된 이미지 data URI, 오류 메시지) 또는 API 호출 실패 시 null
     */
    suspend fun generateBattleImage(battleNarrative: String, winnerName: String?): ImageResult? {
        if (apiKey.isNullOrEmpty()) {
            Timber.e("OpenAI API 키가 설정되지 않았습니다. 이미지 생성을 스킵합니다.")
            return ImageResult(null, "OpenAI API 키가 설정되지 않았습니다. 앱 설정을 확인해주세요.")
        }

        val prompt = createImagePrompt(battleNarrative, winnerName)

        val requestBody = OpenAIImageRequest(
            model = OPENAI_IMAGE_MODEL,
            prompt = prompt,
            n = 1,
            size = "1024x1024",
            quality = "medium",
            background = "auto", // gpt-image-1 새 파라미터
            outputFormat = "png", // gpt-image-1 새 파라미터
            moderation = "low" // gpt-image-1 새 파라미터
        )

        return try {
            Timber.d("OpenAI 이미지 생성 Ktor 요청: URL=$OPENAI_IMAGE_GENERATION_URL, 모델=$OPENAI_IMAGE_MODEL, 프롬프트 길이=${prompt.length}")

            val httpResponse = ktorHttpClient.post(OPENAI_IMAGE_GENERATION_URL) {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $apiKey")
                    append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                }
                setBody(requestBody)
            }

            if (!httpResponse.status.isSuccess()) {
                val errorBody = httpResponse.bodyAsText()
                Timber.e("OpenAI 이미지 생성 Ktor API 오류 응답 (상태 코드: ${httpResponse.status}): $errorBody")
                return ImageResult(null, "이미지 생성 API 오류 (코드: ${httpResponse.status}): $errorBody")
            }

            val response: OpenAIImageResponse = httpResponse.body()

            Timber.d("OpenAI 이미지 생성 Ktor 응답: base64 데이터 수신됨 (길이: ${response.data.firstOrNull()?.b64Json?.length ?: 0})")

            val base64Image = response.data.firstOrNull()?.b64Json
            if (base64Image.isNullOrEmpty()) {
                Timber.w("OpenAI 이미지 생성 Ktor 응답에서 base64 데이터를 찾을 수 없습니다. Revised Prompt: ${response.data.firstOrNull()?.revisedPrompt}")
                ImageResult(null, "이미지 base64 데이터를 받지 못했습니다. API 응답 확인 필요. 수정된 프롬프트: ${response.data.firstOrNull()?.revisedPrompt}")
            } else {
                // base64 데이터를 data URI로 변환
                val outputFormat = requestBody.outputFormat
                val dataUri = "data:image/$outputFormat;base64,$base64Image"
                ImageResult(dataUri, null)
            }
        } catch (e: Exception) {
            Timber.e(e, "OpenAI 이미지 생성 Ktor API 호출 중 오류 발생")
            val errorBody = if (e is ResponseException) {
                e.response.bodyAsText()
            } else {
                e.message ?: "알 수 없는 오류"
            }
            Timber.e("Ktor 오류 응답 본문 (catch 블록): $errorBody")
            ImageResult(null, "이미지 생성 중 오류가 발생했습니다: ${e.message}. 응답(catch): $errorBody")
        }
    }
}
