package com.bandi.textwar.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * 확인 작업을 위한 공통 다이얼로그
 */
@Composable
fun ConfirmActionDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    confirmButtonText: String = "확인",
    dismissButtonText: String = "취소"
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = dialogTitle, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Text(text = dialogText, style = MaterialTheme.typography.bodyMedium)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                    onDismissRequest() // 확인 후 다이얼로그 닫기
                }
            ) {
                Text(confirmButtonText, color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest() // 취소 시 다이얼로그 닫기
                }
            ) {
                Text(dismissButtonText, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}