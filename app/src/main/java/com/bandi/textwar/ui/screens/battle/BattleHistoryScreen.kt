@file:OptIn(ExperimentalMaterial3Api::class)

package com.bandi.textwar.ui.screens.battle

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.bandi.textwar.domain.models.BattleRecord
import com.bandi.textwar.presentation.viewmodels.battle.BattleHistoryUiState
import com.bandi.textwar.presentation.viewmodels.battle.BattleHistoryViewModel
import com.bandi.textwar.R // R 임포트 추가 (플레이스홀더 이미지 등)

@Composable
fun BattleHistoryScreen(
    navController: NavController,
    viewModel: BattleHistoryViewModel = hiltViewModel(),
    characterId: String? = null, // 특정 캐릭터의 기록을 보려면 ID 전달
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = characterId) {
        viewModel.loadBattleHistory(characterId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (characterId == null) "전체 전투 기록" else "캐릭터 전투 기록") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로 가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is BattleHistoryUiState.Loading -> {
                    CircularProgressIndicator()
                }

                is BattleHistoryUiState.Success -> {
                    if (state.records.isEmpty()) {
                        Text("아직 전투 기록이 없습니다.", textAlign = TextAlign.Center)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.records) { record ->
                                BattleRecordItem(record = record, navController = navController)
                            }
                        }
                    }
                }

                is BattleHistoryUiState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "오류 발생",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(state.message, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = { viewModel.loadBattleHistory(characterId) }) {
                            Text("다시 시도")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BattleRecordItem(
    record: BattleRecord,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("전투 시간: ${record.createdAt}", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(8.dp))

            // 캐릭터 정보 (A vs B) - 실제 캐릭터 이름을 표시하려면 추가적인 데이터 로드가 필요할 수 있습니다.
            Text("참가자: ${record.characterAId} vs ${record.characterBId}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("승자: ${record.winnerId ?: "-"}", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(12.dp))

            if (!record.imageUrl.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current).data(data = record.imageUrl)
                            .apply(
                                block = fun ImageRequest.Builder.() {
                                    crossfade(true)
                                    placeholder(R.drawable.ic_placeholder_image) // 플레이스홀더 이미지
                                    error(R.drawable.ic_broken_image) // 오류 시 이미지
                                }
                            )
                            .build()
                    ),
                    contentDescription = "전투 장면 이미지",
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("전투 내용:", style = MaterialTheme.typography.titleSmall)
                Text(
                    record.narrative ?: "내용 없음",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        // TODO: 전투 상세 보기 화면으로 이동 (선택 사항)
                        // navController.navigate("battle_detail/${record.id}")
                    },
                ) {
                    Text("더 보기")
                }
            }
        }
    }
}
