package com.bandi.textwar.data.datasource.local

import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import androidx.core.content.edit

/**
 * SharedPreferences를 사용하여 세션 관련 데이터를 로컬에 저장하고 불러오는 데이터 소스 구현체입니다.
 */
class SessionLocalDataSourceImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) : SessionLocalDataSource {

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    /**
     * 현재 로그인 상태를 반환합니다.
     *
     * @return 로그인 되어있으면 true, 아니면 false
     */
    override suspend fun isLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * 로그인 상태를 저장합니다.
     */
    override suspend fun saveLoginSession() = withContext(Dispatchers.IO) {
        sharedPreferences.edit { putBoolean(KEY_IS_LOGGED_IN, true) }
    }

    /**
     * 저장된 로그인 세션을 삭제합니다.
     */
    override suspend fun clearLoginSession() = withContext(Dispatchers.IO) {
        sharedPreferences.edit { remove(KEY_IS_LOGGED_IN) }
    }
}
