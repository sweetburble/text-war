package com.bandi.textwar.presentation.viewmodels.shared

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * 여러 화면에서 공유하는 이벤트 전달용 ViewModel
 * - 리더보드 새로고침 등 글로벌 UI 이벤트 관리
 */
@HiltViewModel
class SharedEventViewModel @Inject constructor() : ViewModel() {
    // 리더보드 새로고침 필요 여부
    private val _refreshLeaderboard = MutableStateFlow(false)
    val refreshLeaderboard: StateFlow<Boolean> = _refreshLeaderboard.asStateFlow()

    /**
     * 리더보드 새로고침 이벤트 트리거
     */
    fun triggerRefreshLeaderboard() {
        _refreshLeaderboard.value = true
    }

    /**
     * 리더보드 새로고침 이벤트 소비(초기화)
     */
    fun consumeRefreshLeaderboard() {
        _refreshLeaderboard.value = false
    }
}
