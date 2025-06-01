package com.bandi.textwar.presentation.viewmodels.character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bandi.textwar.data.models.CharacterSummary
import com.bandi.textwar.data.models.CharacterDetail
import com.bandi.textwar.domain.usecases.battle.CheckBattleCooldownUseCase
import com.bandi.textwar.domain.usecases.character.GetCharacterListUseCase
import com.bandi.textwar.domain.usecases.battle.GetRandomOpponentUseCase
import com.bandi.textwar.domain.usecases.battle.UpdateCharactersLastBattleTimestampUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * CharacterListScreen을 위한 ViewModel
 */
@HiltViewModel
class CharacterListViewModel @Inject constructor(
    private val charactersUseCase: GetCharacterListUseCase,
    private val getRandomOpponentUseCase: GetRandomOpponentUseCase,
    private val checkBattleCooldownUseCase: CheckBattleCooldownUseCase,
    private val updateCharactersLastBattleTimestampUseCase: UpdateCharactersLastBattleTimestampUseCase
) : ViewModel() {

    // UI 상태: 캐릭터 목록
    private val _characters = MutableStateFlow<List<CharacterSummary>>(emptyList())
    val characters: StateFlow<List<CharacterSummary>> = _characters.asStateFlow()

    // 로딩 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 오류 상태
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // 상대 캐릭터 상태
    private val _opponentCharacter = MutableStateFlow<CharacterDetail?>(null)
    val opponentCharacter: StateFlow<CharacterDetail?> = _opponentCharacter.asStateFlow()

    // 상대 캐릭터 검색 중 로딩 상태
    private val _isFindingOpponent = MutableStateFlow(false)
    val isFindingOpponent: StateFlow<Boolean> = _isFindingOpponent.asStateFlow()

    // 현재 전투를 시작하려는 나의 캐릭터 ID
    private val _myCharacterIdForBattle = MutableStateFlow<String?>(null)
    val myCharacterIdForBattle: StateFlow<String?> = _myCharacterIdForBattle.asStateFlow()

    // 캐릭터별 쿨다운 상태 (캐릭터 ID -> Pair(쿨다운 중인가, 남은 시간(초)))
    private val _characterCooldowns = MutableStateFlow<Map<String, Pair<Boolean, Long>>>(emptyMap())
    val characterCooldowns: StateFlow<Map<String, Pair<Boolean, Long>>> = _characterCooldowns.asStateFlow()

    init {
        loadCharacters()
    }

    fun loadCharacters() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            charactersUseCase()
                .catch { e ->
                    _error.value = "캐릭터 목록을 불러오는데 실패했습니다: ${e.message}"
                }
                .onEach { characterList ->
                    _characters.value = characterList
                    // 각 캐릭터의 쿨다운 정보도 함께 로드
                    characterList.forEach { character ->
                        loadCharacterCooldown(character.id)
                    }
                }
                .launchIn(viewModelScope) // collect 대신 launchIn 사용
            _isLoading.value = false
        }
    }

    /**
     * 특정 캐릭터의 쿨다운 정보를 로드하여 _characterCooldowns 상태를 업데이트합니다.
     */
    fun loadCharacterCooldown(characterId: String) {
        viewModelScope.launch {
            checkBattleCooldownUseCase(characterId)
                .catch { e ->
                    // 오류 발생 시 해당 캐릭터 쿨다운 정보는 업데이트하지 않거나 기본값으로 설정
                    // _error.value = "쿨다운 정보 로드 실패: ${e.message}" // 개별 에러보다는 전체적인 에러로 처리
                }
                .collect { cooldownStatus ->
                    _characterCooldowns.value = _characterCooldowns.value.toMutableMap().apply {
                        this[characterId] = cooldownStatus
                    }
                }
        }
    }

    /**
     * 전투 상대를 찾는다.
     * @param myCharacterId 전투를 시작하려는 나의 캐릭터 ID
     */
    fun findOpponent(myCharacterId: String) {
        viewModelScope.launch {
            _error.value = null // 이전 오류 초기화
            // 먼저 선택한 캐릭터의 쿨다운 확인
            checkBattleCooldownUseCase(myCharacterId)
                .onEach { (isInCooldown, remainingTime) ->
                    if (isInCooldown) {
                        _error.value = "현재 쿨다운 중입니다. ${remainingTime}초 남음"
                        // _isFindingOpponent.value = false // 로딩 상태를 변경할 필요 없음
                        return@onEach // 쿨다운 중이면 더 이상 진행하지 않음
                    }

                    // 쿨다운이 아니면 상대 찾기 진행
                    _myCharacterIdForBattle.value = myCharacterId // 나의 ID 저장
                    _isFindingOpponent.value = true
                    _opponentCharacter.value = null // 이전 상대 정보 초기화

                    getRandomOpponentUseCase()
                        .onSuccess { opponent ->
                            _opponentCharacter.value = opponent // 상대 정보 업데이트
                            // 전투 시작 성공 시, 두 캐릭터의 마지막 전투 시간 업데이트
                            updateCharactersLastBattleTimestampUseCase(myCharacterId, opponent.id)
                            // 상대방 캐릭터의 쿨다운 정보도 업데이트 (UI 표시용)
                            loadCharacterCooldown(opponent.id)
                            // 나의 캐릭터 쿨다운 정보도 전투 시작 직후이므로 다시 로드
                            loadCharacterCooldown(myCharacterId)
                        }
                        .onFailure { e ->
                            _error.value = "상대 찾기 실패: ${e.message}"
                            _myCharacterIdForBattle.value = null // 실패 시 나의 ID도 초기화
                        }
                    _isFindingOpponent.value = false
                }
                .catch { e ->
                    _error.value = "쿨다운 확인 중 오류: ${e.message}"
                    _isFindingOpponent.value = false
                }
                .launchIn(viewModelScope)
        }
    }

    /**
     * 검색된 상대 캐릭터 정보와 나의 캐릭터 ID를 초기화합니다.
     * 화면 이동 후 호출하여 이전 정보가 남아있지 않도록 합니다.
     */
    fun clearBattleContext() {
        _opponentCharacter.value = null
        _myCharacterIdForBattle.value = null
    }

    /**
     * 현재 표시되고 있는 오류 메시지를 초기화합니다.
     */
    fun clearError() {
        _error.value = null
    }
}