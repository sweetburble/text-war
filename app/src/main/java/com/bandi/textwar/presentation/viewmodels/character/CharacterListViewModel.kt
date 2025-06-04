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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
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

    // 캐릭터별 쿨다운 상태 (캐릭터 ID -> Pair(쿨다운 중인가, 마지막 전투 시각 Unix timestamp (밀리초) 또는 null))
    private val _characterCooldowns = MutableStateFlow<Map<String, Pair<Boolean, Long?>>>(emptyMap())
    val characterCooldowns: StateFlow<Map<String, Pair<Boolean, Long?>>> = _characterCooldowns.asStateFlow()

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
     * @param updateUiImmediately UI에 즉시 반영할지 여부 (findOpponent 시에는 true)
     */
    fun loadCharacterCooldown(characterId: String, updateUiImmediately: Boolean = true) {
        viewModelScope.launch {
            checkBattleCooldownUseCase(characterId)
                .catch { e ->
                    Timber.e(e, "Error loading cooldown for $characterId")
                }
                .collect { cooldownStatus -> // Pair<Boolean, Long?>
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
            _error.value = null
            _isFindingOpponent.value = true // 먼저 로딩 상태 시작
            _myCharacterIdForBattle.value = myCharacterId
            _opponentCharacter.value = null

            // DB에서 최신 쿨다운 정보 확인
            val cooldownCheckResult = checkBattleCooldownUseCase(myCharacterId).firstOrNull() // Flow에서 첫번째 값만 가져옴

            if (cooldownCheckResult == null) { // 오류 등으로 정보 못 가져옴
                _error.value = "쿨다운 정보를 확인하지 못했습니다."
                _isFindingOpponent.value = false
                _myCharacterIdForBattle.value = null
                return@launch
            }

            val (isInCooldownDb, lastBattleTimestampMillisDb) = cooldownCheckResult

            if (isInCooldownDb && lastBattleTimestampMillisDb != null) {
                val currentTimeMillis = System.currentTimeMillis()
                val remainingTime = ((lastBattleTimestampMillisDb + (CheckBattleCooldownUseCase.COOLDOWN_SECONDS * 1000L)) - currentTimeMillis) / 1000
                _error.value = "현재 쿨다운 중입니다. ${remainingTime.coerceAtLeast(0L)}초 남음"
                _isFindingOpponent.value = false
                _myCharacterIdForBattle.value = null
                // ViewModel의 _characterCooldowns도 최신 DB 정보로 업데이트
                _characterCooldowns.value = _characterCooldowns.value.toMutableMap().apply {
                    this[myCharacterId] = Pair(true, lastBattleTimestampMillisDb)
                }
                return@launch
            }

            // 쿨다운 아니면 상대 찾기
            getRandomOpponentUseCase()
                .onSuccess { opponent ->
                    _opponentCharacter.value = opponent
                    // 전투 시작 성공 시, 두 캐릭터의 마지막 전투 시간 DB에 업데이트
                    updateCharactersLastBattleTimestampUseCase(myCharacterId, opponent.id)

                    // ViewModel의 쿨다운 상태도 즉시 업데이트 (낙관적 업데이트)
                    val nowMillis = System.currentTimeMillis()
                    _characterCooldowns.value = _characterCooldowns.value.toMutableMap().apply {
                        this[myCharacterId] = Pair(true, nowMillis) // 내가 방금 전투 했으니, 현재 시간을 마지막 전투 시간으로 설정
                        // 상대방은 DB 업데이트 후 loadCharacterCooldown으로 갱신될 것임 (선택사항)
                    }
                    // 상대방 캐릭터의 쿨다운 정보도 최신화 (UI 표시용)
                    loadCharacterCooldown(opponent.id)
                }
                .onFailure { e ->
                    _error.value = "상대 찾기 실패: ${e.message}"
                    _myCharacterIdForBattle.value = null
                }
            _isFindingOpponent.value = false
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