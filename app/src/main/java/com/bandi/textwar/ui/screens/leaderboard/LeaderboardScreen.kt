@file:OptIn(ExperimentalMaterial3Api::class)

package com.bandi.textwar.ui.screens.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bandi.textwar.data.models.LeaderboardItem
import com.bandi.textwar.presentation.viewmodels.leaderboard.LeaderboardUiState
import com.bandi.textwar.presentation.viewmodels.leaderboard.LeaderboardViewModel

// 순위별 색상 정의
val goldColor = Color(0xFFFFD700)
val silverColor = Color(0xFFC0C0C0)
val bronzeColor = Color(0xFFCD7F32)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    navController: NavController,
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold() { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val state = uiState) {
                is LeaderboardUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is LeaderboardUiState.Success -> {
                    if (state.leaderboardData.isEmpty()) {
                        Text("리더보드 데이터가 없습니다.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LeaderboardList(leaderboardItems = state.leaderboardData)
                    }
                }
                is LeaderboardUiState.Error -> {
                    Text(
                        text = "오류가 발생했습니다: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    // TODO: 재시도 버튼 추가 고려
                }
            }
        }
    }
}

@Composable
fun LeaderboardList(leaderboardItems: List<LeaderboardItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 헤더
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("순위", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("유저", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("캐릭터", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("승리", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.End)
                Text("패배", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.End)
                Text("레이팅", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.End)
            }
            HorizontalDivider(thickness = 2.dp)
        }

        itemsIndexed(leaderboardItems) { index, item ->
            LeaderboardCard(rank = index + 1, item = item)
            HorizontalDivider(thickness = 1.dp)
        }
    }
}

@Composable
fun LeaderboardCard(rank: Int, item: LeaderboardItem) {
    val backgroundColor = when (rank) {
        1 -> goldColor.copy(alpha = 0.3f)
        2 -> silverColor.copy(alpha = 0.3f)
        3 -> bronzeColor.copy(alpha = 0.3f)
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier.fillMaxWidth().background(backgroundColor).padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(0.8f), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$rank",
                fontWeight = FontWeight.Bold,
                fontSize = if (rank <= 3) 15.sp else 13.sp,
                color = if (rank == 1) Color.Black else Color.Unspecified
            )
            if (rank == 1) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = "1등",
                    tint = goldColor,
                    modifier = Modifier.size(24.dp).padding(start = 4.dp)
                )
            }
        }
        Text(
            item.userDisplayName,
            modifier = Modifier.weight(2f),
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(item.characterName, modifier = Modifier.weight(2f), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(item.wins.toString(), modifier = Modifier.weight(0.8f), fontSize = 13.sp, textAlign = TextAlign.End)
        Text(item.losses.toString(), modifier = Modifier.weight(0.8f), fontSize = 13.sp, textAlign = TextAlign.End)
        Text(item.rating.toString(), modifier = Modifier.weight(1f), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.End)
    }
} 