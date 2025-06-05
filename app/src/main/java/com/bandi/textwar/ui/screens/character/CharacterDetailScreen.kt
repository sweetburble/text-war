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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect 
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue 
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
import com.bandi.textwar.ui.components.ConfirmActionDialog
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

    // 삭제 다이얼로그 노출 여부 상태 (remember 구조 수정)
    var showDeleteDialog by remember { mutableStateOf(false) }

    // 삭제 성공 시 이전 화면으로 이동 및 이벤트 전달
    // LaunchedEffect를 사용하여 deleteResult 상태 변경 시 일회성 액션 처리
    LaunchedEffect(deleteResult) {
        when (deleteResult) { // 명시적으로 result 변수에 할당하여 when 내부에서 사용
            is DeleteResult.Success -> {
                sharedEventViewModel.triggerRefreshLeaderboard()
                navController.popBackStack()
                viewModel.resetDeleteResultState() // 성공 처리 후 상태 초기화
            }
            is DeleteResult.Error -> {
                // 에러 메시지 표시는 Box 내부의 Text 컴포넌트에서 이미 처리하고 있다
                viewModel.resetDeleteResultState() // 에러 처리(인지) 후 상태 초기화
            }
            is DeleteResult.None -> {
                // 초기 상태이거나 이미 리셋된 상태이므로 별도 처리 없음
            }
        }
    }

    Scaffold { paddingValues ->
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
                    CharacterDetailContent(
                        detail = characterDetail!!,
                        onDeleteClick = { showDeleteDialog = true } // 다이얼로그 표시
                    )
                }
                else -> {
                    Text("캐릭터 정보를 불러올 수 없습니다.", style = MaterialTheme.typography.bodyLarge)
                }
            }

            // 삭제 다이얼로그 (ConfirmActionDialog 사용)
            if (showDeleteDialog) {
                ConfirmActionDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    onConfirmation = {
                        viewModel.deleteCharacter()
                        // ConfirmActionDialog 내부에서 onDismissRequest가 호출되므로
                        // 여기서 showDeleteDialog = false 를 명시적으로 호출할 필요는 없음.
                        // deleteCharacter() 호출 후 deleteResult 상태 변경에 따라 LaunchedEffect가 반응.
                    },
                    dialogTitle = "캐릭터 삭제",
                    dialogText = "정말로 이 캐릭터를 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.",
                    confirmButtonText = "삭제",
                    dismissButtonText = "취소"
                )
            }

            // 삭제 실패 시 에러 메시지 표시
            // deleteResult가 Error 상태일 때만 표시되도록 LaunchedEffect 외부에서 처리 가능
            // 또는 Snackbar 등을 활용하여 사용자에게 피드백 제공 가능
            if (deleteResult is DeleteResult.Error) {
                Text(
                    text = (deleteResult as DeleteResult.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
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

        detail.lastBattleTimestamp?.let { ts ->
            CharacterStatRow(label = "마지막 전투", value = ts.toFormattedBattleTime())
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDeleteClick, // 전달받은 콜백 실행
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) // 버튼 색상 명확히 지정
        ) {
            Text("캐릭터 삭제", color = MaterialTheme.colorScheme.onError) // onError 색상 사용 권장
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