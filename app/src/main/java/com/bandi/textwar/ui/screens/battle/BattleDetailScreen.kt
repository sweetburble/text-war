package com.bandi.textwar.ui.screens.battle

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.bandi.textwar.R
import com.bandi.textwar.presentation.viewmodels.battle.BattleDetailUiState
import com.bandi.textwar.presentation.viewmodels.battle.BattleDetailViewModel
import com.bandi.textwar.ui.utils.toFormattedBattleTime

@Composable
fun BattleDetailScreen(
    navController: NavController,
    viewModel: BattleDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is BattleDetailUiState.Loading -> {
                CircularProgressIndicator()
            }
            is BattleDetailUiState.Success -> {
                val record = state.record
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // 참가자 정보
                    val characterAName = record.characterAName ?: record.characterAId
                    val characterBName = record.characterBName ?: record.characterBId
                    Text("참가자: $characterAName vs $characterBName", style = MaterialTheme.typography.titleLarge, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // 승자 정보
                    val winnerDisplayName = record.winnerName ?: record.winnerId
                    Text("승자: ${winnerDisplayName ?: "-"}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))

                    // 전투 이미지 표시
                    if (record.imageUrl.isNullOrEmpty()) {
                        // 전투 이미지 영역(플레이스홀더 박스)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(300.dp).border(BorderStroke(2.dp, Color.Gray)).padding(8.dp),
                        ) {
                            Text("생성된 이미지 없음", color = Color.Gray)
                        }
                    } else {
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(LocalContext.current)
                                    .data(data = record.imageUrl)
                                    .apply {
                                        crossfade(true)
                                        placeholder(R.drawable.ic_placeholder_image) // 플레이스홀더 이미지
                                        error(R.drawable.ic_broken_image) // 오류 시 이미지
                                    }
                                    .build()
                            ),
                            contentDescription = "전투 장면 이미지",
                            modifier = Modifier.fillMaxWidth().height(300.dp), // 이미지 높이 조절
                            contentScale = ContentScale.Fit // 이미지 스케일 조절
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // 전투 내러티브
                    Text("전투 내용", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        record.narrative ?: "전투 내용이 없습니다.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // 전투 시간
                    Text("전투 시간: ${record.createdAt.toFormattedBattleTime()}", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(onClick = { navController.popBackStack() }) {
                        Text("목록으로 돌아가기")
                    }
                }
            }
            is BattleDetailUiState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text("오류 발생", style = MaterialTheme.typography.headlineMedium, color = Color.Red)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(state.message, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadBattleDetail() }) {
                        Text("다시 시도")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { navController.popBackStack() }) {
                        Text("돌아가기")
                    }
                }
            }
            is BattleDetailUiState.NotFound -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text("전투 기록을 찾을 수 없습니다.", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) {
                        Text("돌아가기")
                    }
                }
            }
        }
    }
} 