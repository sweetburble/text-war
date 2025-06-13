package com.bandi.textwar.ui.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

/**
 * 토스트 메시지를 간편하게 표시하기 위한 유틸리티 객체
 */
object ToastUtils {

    /**
     * 문자열 메시지로 토스트를 표시
     *
     * @param context 애플리케이션 또는 액티비티 컨텍스트
     * @param message 표시할 메시지 문자열
     * @param duration 토스트가 표시될 시간 (기본값: Toast.LENGTH_SHORT)
     */
    fun showToast(
        context: Context,
        message: String,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        Toast.makeText(context, message, duration).show()
    }

    /**
     * 문자열 리소스 ID로 토스트를 표시
     *
     * @param context 애플리케이션 또는 액티비티 컨텍스트
     * @param messageResId 표시할 메시지의 문자열 리소스 ID
     * @param duration 토스트가 표시될 시간 (기본값: Toast.LENGTH_SHORT)
     */
    fun showToast(
        context: Context,
        @StringRes messageResId: Int,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        Toast.makeText(context, messageResId, duration).show()
    }
}