package com.bandi.textwar.ui.screens.character

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bandi.textwar.presentation.viewmodels.character.CharacterCreationViewModel
import com.bandi.textwar.presentation.viewmodels.character.SaveCharacterState
import com.bandi.textwar.presentation.viewmodels.character.SlotCheckState
import com.bandi.textwar.ui.theme.TextWarTheme

const val MAX_CHAR_DESCRIPTION_LENGTH = 100

@Composable
fun CharacterCreationScreen(
    navController: NavController,
    viewModel: CharacterCreationViewModel = hiltViewModel(),
    onSaveSuccessNavigation: () -> Unit
) {
    val characterName by viewModel.characterName.collectAsState()
    val characterDescription by viewModel.characterDescription.collectAsState()
    val remainingChars by viewModel.remainingChars.collectAsState()
    val isSaveButtonEnabled by viewModel.isSaveButtonEnabled.collectAsState()
    val slotCheckState by viewModel.slotCheckState.collectAsState()
    val saveCharacterState by viewModel.saveCharacterState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(saveCharacterState) {
        when (val state = saveCharacterState) {
            is SaveCharacterState.Success -> {
                snackbarHostState.showSnackbar(message = state.message)
                onSaveSuccessNavigation()
            }
            is SaveCharacterState.Error -> {
                snackbarHostState.showSnackbar(message = state.message)
            }
            else -> { /* Idle or Loading, do nothing here for snackbar */ }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "당신만의 캐릭터를 창조하세요",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))

            when (slotCheckState) {
                is SlotCheckState.Loading -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                }
                is SlotCheckState.Success -> {
                    val state = slotCheckState as SlotCheckState.Success
                    if (state.message != null) {
                        Text(
                            text = state.message,
                            color = if (state.canCreateCharacter) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                is SlotCheckState.Error -> {
                    Text(
                        text = (slotCheckState as SlotCheckState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                is SlotCheckState.Idle -> { /* Do nothing or show placeholder */ }
            }

            OutlinedTextField(
                value = characterName,
                onValueChange = { viewModel.onCharacterNameChange(it) },
                label = { Text("캐릭터 이름 (예시 : 엄마)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                isError = saveCharacterState is SaveCharacterState.Error
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = characterDescription,
                onValueChange = { viewModel.onCharacterDescriptionChange(it) },
                label = { Text("캐릭터 설명 (예시 : 무척 강합니다.)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 4,
                supportingText = {
                    Text(
                        text = "$remainingChars / $MAX_CHAR_DESCRIPTION_LENGTH 글자",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                    )
                },
                isError = saveCharacterState is SaveCharacterState.Error
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (saveCharacterState is SaveCharacterState.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        viewModel.saveCharacter()
                    },
                    enabled = isSaveButtonEnabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("캐릭터 생성")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CharacterCreationScreenPreview() {
    TextWarTheme {
        CharacterCreationScreen(navController = NavController(LocalContext.current), onSaveSuccessNavigation = {})
    }
} 