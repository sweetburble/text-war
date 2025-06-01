package com.bandi.textwar.ui.screens.battle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bandi.textwar.presentation.viewmodels.battle.BattleResultUiState
import com.bandi.textwar.presentation.viewmodels.battle.BattleResultViewModel

@Composable
fun BattleResultScreen(
    navController: NavController,
    viewModel: BattleResultViewModel = hiltViewModel() // ViewModel 주입
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is BattleResultUiState.Loading -> {
                CircularProgressIndicator()
            }
            is BattleResultUiState.Success -> {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("전투 결과", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 승패 결과 표시
                    val winnerName = state.result.winnerName
                    val myName = state.myCharacterName
                    val resultText = when (winnerName) {
                        null -> "무승부 또는 결과를 알 수 없습니다."
                        myName -> "승리했습니다!"
                        else -> "패배했습니다..."
                    }
                    val resultColor = when (winnerName) {
                        null -> Color.Gray
                        myName -> Color(0xFF4CAF50) // Green
                        else -> Color.Red
                    }
                    Text(resultText, style = MaterialTheme.typography.headlineSmall, color = resultColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("승자: ${winnerName ?: "-"}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("전투 기록", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(state.result.narrative ?: "전투 기록을 가져올 수 없습니다.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { navController.popBackStack() }) {
                        Text("돌아가기")
                    }
                }
            }
            is BattleResultUiState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("오류 발생", style = MaterialTheme.typography.headlineMedium, color = Color.Red)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(state.message, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) {
                        Text("돌아가기")
                    }
                }
            }
        }
    }
} 