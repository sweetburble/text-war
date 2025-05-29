package com.bandi.textwar.data.remote
import com.bandi.textwar.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import timber.log.Timber

const val DEFAULT_MODEL_NAME = "gemini-2.5-flash-preview-05-20"
/**
 * Google Gemini API와 상호작용하기 위한 서비스 클래스입니다.
 * GenerativeModel 인스턴스를 초기화하고 관리합니다.
*/
class GeminiService {
    private var generativeModel: GenerativeModel? = null

    init {
        initializeModel()
    }

    /**
     * GenerativeModel을 초기화합니다.
     * API 키는 BuildConfig를 통해 안전하게 로드됩니다.
     * 모델 초기화에 실패하면 에러를 로깅하고 generativeModel을 null로 설정합니다.
     */
    private fun initializeModel() {
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isEmpty()) {
            Timber.e("Gemini API 키가 설정되지 않았습니다. local.properties 파일을 확인하세요.")
            return
        }

        try {
            generativeModel = GenerativeModel(
                modelName = DEFAULT_MODEL_NAME,
                apiKey = apiKey
            )
            Timber.i("Gemini 모델이 성공적으로 초기화되었습니다: $DEFAULT_MODEL_NAME")
        } catch (e: Exception) {
            Timber.e(e, "Gemini 모델 초기화 중 오류 발생")
            // 사용자에게 적절한 피드백을 제공하거나, 앱의 다른 부분에 영향을 주지 않도록 처리합니다.
            generativeModel = null // 모델 초기화 실패 시 null 로 설정
        }
    }

    /**
     * 초기화된 GenerativeModel 인스턴스를 반환합니다.
     * 모델이 성공적으로 초기화되지 않은 경우 null을 반환할 수 있습니다.
     * @return GenerativeModel 인스턴스 또는 null
     */
    fun getModel(): GenerativeModel? {
        if (generativeModel == null) {
            Timber.w("Gemini 모델이 초기화되지 않았거나 실패했습니다.")
        }
        return generativeModel
    }

    // TODO: 여기에 전투 프롬프트를 생성하고 API를 호출하는 함수 추가 (Task 7.4, 7.5)
    // suspend fun generateBattleNarrative(characterA: Character, characterB: Character): BattleResult? {}

    // TODO: 전투 결과를 담을 데이터 클래스 정의 (Task 7.6 관련)
    // data class BattleResult(val narrative: String?, val winnerName: String?) {}
}
