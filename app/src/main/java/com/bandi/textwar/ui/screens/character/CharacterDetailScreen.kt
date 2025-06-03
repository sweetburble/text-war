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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.bandi.textwar.ui.utils.toFormattedBattleTime

@Composable
fun CharacterDetailScreen(
    navController: NavController,
    viewModel: CharacterDetailViewModel = hiltViewModel()
) {
    val characterDetail by viewModel.characterDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

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
        CharacterStatRow(label = "생성일", value = detail.createdAt.toFormattedBattleTime())

        detail.lastBattleTimestamp?.let {
            CharacterStatRow(label = "마지막 전투", value = it.toFormattedBattleTime())
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
