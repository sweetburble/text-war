@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bandi.textwar.data.models.CharacterDetail
import com.bandi.textwar.presentation.viewmodels.character.CharacterDetailViewModel
import timber.log.Timber

@Composable
fun CharacterDetailScreen(
    navController: NavController,
    viewModel: CharacterDetailViewModel = hiltViewModel()
) {
    val characterDetail by viewModel.characterDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(characterDetail?.characterName ?: "캐릭터 상세") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로 가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
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
                    CharacterDetailContent(detail = characterDetail!!)
                }
                else -> {
                    Text("캐릭터 정보를 불러올 수 없습니다.", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun CharacterDetailContent(detail: CharacterDetail) {
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
        CharacterStatRow(label = "생성일", value = simpleDateFormat(detail.createdAt))

        detail.lastBattleTimestamp?.let {
            CharacterStatRow(label = "마지막 전투", value = simpleDateFormat(it))
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

// 간단한 날짜 포맷팅 함수 (실제 앱에서는 더 견고한 방식 사용 권장)
fun simpleDateFormat(timestamp: String): String {
    return try {
        timestamp.substringBefore("T") // "YYYY-MM-DDTHH:mm:ss.sssZ" 형식에서 날짜 부분만 추출
    } catch (e: Exception) {
        Timber.e(e.toString())
        timestamp // 파싱 실패 시 원본 반환
    }
}
