package com.bandi.textwar.data.datasource

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * 실제 Supabase 연동 데이터 소스 구현체
 */
class AuthDataSource @Inject constructor(
    private val auth: Auth // Hilt로 주입받는 Auth 인스턴스
) {
    /**
     * Supabase 로그아웃 처리
     * - suspend fun으로 안전하게 로그아웃 처리
     * - 예외 발생 시 Result.failure로 반환
     */
    suspend fun logout(): Result<Unit> {
        return try {
            auth.signOut() // 실제 로그아웃 처리 (suspend)
            Result.success(Unit)
        } catch (e: Exception) {
            // 로그아웃 실패 시 Result.failure 반환
            Result.failure(e)
        }
    }

    /**
     * Supabase Edge Function을 이용한 회원탈퇴 처리
     * - Edge Function: https://oyciepiokulbececwgns.supabase.co/functions/v1/authentication-withdrawal
     * - Authorization 헤더에 accessToken 포함
     * - 성공 시 Result.success(Unit), 실패 시 Result.failure(e)
     */
    suspend fun withdraw(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // 현재 로그인된 사용자의 accessToken 획득
                val session = auth.currentSessionOrNull()
                val accessToken = session?.accessToken ?: throw Exception("로그인 세션이 없습니다.")

                // Edge Function 호출
                val url = "https://oyciepiokulbececwgns.supabase.co/functions/v1/authentication-withdrawal"
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS) // 연결 타임아웃 30초
                    .readTimeout(30, TimeUnit.SECONDS)    // 읽기 타임아웃 30초
                    .writeTimeout(30, TimeUnit.SECONDS)   // 쓰기 타임아웃 30초
                    .build()

                val request = Request.Builder()
                    .url(url)
                    .post(ByteArray(0).toRequestBody(null)) // 빈 바디
                    .addHeader("Authorization", "Bearer $accessToken")
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw Exception("회원탈퇴 실패: ${response.code} ${response.message} ${response.body?.string()}")
                }
                // 성공 응답 파싱(필요시)
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e.toString())
                Result.failure(e)
            }
        }
    }

    /**
     * Supabase 로그인 처리
     * @param email 사용자 이메일
     * @param password 사용자 비밀번호
     * @return 성공 시 Result.success(Unit), 실패 시 Result.failure(e)
     */
    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            // Supabase Auth 이메일 로그인
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            // 로그인 실패 시 예외를 Result.failure로 반환
            Result.failure(e)
        }
    }

    /**
     * Supabase 회원가입 처리
     * @param email 사용자 이메일
     * @param password 사용자 비밀번호
     * @param nickname 닉네임(추가 정보)
     * @return 성공 시 Result.success(Unit), 실패 시 Result.failure(e)
     */
    suspend fun signup(email: String, password: String, nickname: String): Result<Unit> {
        return try {
            // Supabase Auth 이메일 회원가입
            val result = auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject { put("display_name", JsonPrimitive(nickname)) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            // 회원가입 실패 시 예외를 Result.failure로 반환
            Result.failure(e)
        }
    }

    /**
     * 현재 세션 존재 여부 반환
     * @return 로그인 상태 여부 (true: 로그인됨, false: 비로그인)
     */
    suspend fun getSessionState(): Result<Boolean> {
        return try {
            val session = auth.currentSessionOrNull()
            Result.success(session != null)
        } catch (e: Exception) {
            // 세션 조회 실패 시 예외를 Result.failure로 반환
            Result.failure(e)
        }
    }
}
