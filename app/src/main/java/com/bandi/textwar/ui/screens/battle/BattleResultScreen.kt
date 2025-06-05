package com.bandi.textwar.ui.screens.battle

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.bandi.textwar.R
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
                // 전투 결과 로딩 중에는 실제 데이터 없이 껍데기 UI를 보여줍니다.
                TempBattleResultScreen()
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

                    // 전투 이미지 표시
                    if (state.result.imageUrl.isNullOrEmpty()) {
                        // 전투 이미지 영역(플레이스홀더 박스)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(200.dp).border(BorderStroke(2.dp, Color.Gray)).padding(8.dp),
                        ) {
                            // 실제 이미지는 로딩 후 표시되지만, 여기서는 빈 박스만 보여줍니다.
                            Text("이미지 로딩 중...", color = Color.Gray)
                        }
                    } else {
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(LocalContext.current)
                                    .data(data = state.result.imageUrl)
                                    .apply {
                                        crossfade(true)
                                        placeholder(R.drawable.ic_placeholder_image) // 플레이스홀더 이미지
                                        error(R.drawable.ic_broken_image) // 오류 시 이미지
                                    }
                                    .build()
                            ),
                            contentDescription = "전투 장면 이미지",
                            modifier = Modifier.fillMaxWidth().height(400.dp), // 이미지 높이 조절
                            contentScale = ContentScale.Fit // 이미지 스케일 조절
                        )
                    }
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

@Composable
fun TempBattleResultScreen() {
    // 실제 데이터 없이, 전투 결과 화면의 전체 구조만 보여주는 껍데기 UI
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 타이틀 영역
        Text("전투 결과", style = MaterialTheme.typography.headlineMedium, color = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))

        // 승패 결과 텍스트(플레이스홀더)
        Text("결과 대기 중...", style = MaterialTheme.typography.headlineSmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))

        // 승자 정보(플레이스홀더)
        Text("승자: -", style = MaterialTheme.typography.titleMedium, color = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))

        // 전투 이미지 영역(플레이스홀더 박스)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(200.dp).border(BorderStroke(2.dp, Color.Gray)).padding(8.dp),
        ) {
            // 실제 이미지는 로딩 후 표시되지만, 여기서는 빈 박스만 보여줍니다.
            Text("이미지 로딩 중...", color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 전투 해설 텍스트 영역(플레이스홀더)
        Text(
            text = "전투 해설을 불러오는 중입니다...",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.LightGray
        )
        Spacer(modifier = Modifier.height(24.dp))

        // 버튼 영역(비활성화된 상태로 표시)
        Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth(0.7f)) {
            Text("돌아가기", color = Color.Gray)
        }
    }
}
