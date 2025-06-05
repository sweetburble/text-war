package com.bandi.textwar.ui.utils

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 스낵바를 표시하는 확장 함수입니다.
 *
 * @param message 표시할 메시지
 * @param actionLabel (선택 사항) 스낵바 액션 버튼에 표시될 텍스트
 * @param duration (선택 사항) 스낵바가 표시될 시간 (기본값: Short)
 * @param onActionPerformed (선택 사항) 스낵바 액션이 수행되었을 때 호출될 콜백
 * @param onDismissed (선택 사항) 스낵바가 사라졌을 때 호출될 콜백 (액션 수행 여부와 관계 없이 호출)
 */
fun SnackbarHostState.showSnackbar(
    scope: CoroutineScope,
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short,
    onActionPerformed: (() -> Unit)? = null,
    onDismissed: (() -> Unit)? = null
) {
    scope.launch {
        val result = showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = duration
        )
        when (result) {
            SnackbarResult.ActionPerformed -> {
                onActionPerformed?.invoke()
            }
            SnackbarResult.Dismissed -> {
                onDismissed?.invoke()
            }
        }
    }
}
