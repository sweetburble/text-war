package com.bandi.textwar.ui.screens.character

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bandi.textwar.data.models.CharacterDetail
import com.bandi.textwar.presentation.viewmodels.character.CharacterDetailViewModel
import com.bandi.textwar.presentation.viewmodels.character.CharacterDetailViewModel.DeleteResult
import com.bandi.textwar.presentation.viewmodels.shared.SharedEventViewModel
import com.bandi.textwar.ui.utils.toFormattedBattleTime

@Composable
fun CharacterDetailScreen(
    navController: NavController,
    sharedEventViewModel: SharedEventViewModel,
    viewModel: CharacterDetailViewModel = hiltViewModel()
) {
    val characterDetail by viewModel.characterDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val deleteResult by viewModel.deleteResult.collectAsState()

    // 삭제 다이얼로그 노출 여부 상태
    remember { mutableStateOf(false) }.let { showDeleteDialog ->
        // 삭제 성공 시 이전 화면으로 이동
        if (deleteResult is DeleteResult.Success) {
            // SharedEventViewModel을 통해 리더보드 새로고침 이벤트 전달
            sharedEventViewModel.triggerRefreshLeaderboard()
            navController.popBackStack()
        }

        Scaffold() { paddingValues ->
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator()
                    }
                    error != null -> {
                        Text(
                            text = error ?: "오류가 발생했습니다.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    characterDetail != null -> {
                        // 캐릭터 상세 정보와 삭제 버튼을 함께 전달
                        CharacterDetailContent(
                            detail = characterDetail!!,
                            onDeleteClick = { showDeleteDialog.value = true }
                        )
                    }
                    else -> {
                        Text("캐릭터 정보를 불러올 수 없습니다.", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                // 삭제 다이얼로그
                if (showDeleteDialog.value) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog.value = false },
                        title = { Text("캐릭터 삭제") },
                        text = { Text("정말로 이 캐릭터를 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.") },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.deleteCharacter()
                                showDeleteDialog.value = false
                            }) {
                                Text("삭제")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog.value = false }) {
                                Text("취소")
                            }
                        }
                    )
                }
                // 삭제 실패 시 에러 메시지 표시
                if (deleteResult is DeleteResult.Error) {
                    Text(
                        text = (deleteResult as DeleteResult.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun CharacterDetailContent(
    detail: CharacterDetail,
    onDeleteClick: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(detail.characterName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Text(detail.description, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        CharacterStatRow(label = "승리", value = detail.wins.toString())
        CharacterStatRow(label = "패배", value = detail.losses.toString())
        CharacterStatRow(label = "레이팅", value = detail.rating.toString())
        CharacterStatRow(label = "생성일", value = detail.createdAt.toFormattedBattleTime())

        // 마지막 전투 정보가 있을 경우만 표시
        detail.lastBattleTimestamp?.let { ts ->
            CharacterStatRow(label = "마지막 전투", value = ts.toFormattedBattleTime())
        }
        Spacer(modifier = Modifier.height(24.dp))

        // 삭제 버튼 추가
        Button(
            onClick = onDeleteClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("캐릭터 삭제", color = MaterialTheme.colorScheme.onError)
        }
    }
}


@Composable
fun CharacterStatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
