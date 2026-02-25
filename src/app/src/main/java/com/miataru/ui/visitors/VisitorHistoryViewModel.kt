package com.miataru.ui.visitors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miataru.domain.model.VisitorEvent
import com.miataru.domain.repository.VisitorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VisitorHistoryUiState(
    val visitors: List<VisitorEvent> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class VisitorHistoryViewModel @Inject constructor(
    private val visitorRepository: VisitorRepository,
) : ViewModel() {

    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<VisitorHistoryUiState> = combine(
        visitorRepository.visitors,
        error,
    ) { visitors, errorMessage ->
        VisitorHistoryUiState(
            visitors = visitors,
            errorMessage = errorMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VisitorHistoryUiState(),
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val result = visitorRepository.refreshVisitorHistory()
            if (result.isFailure) {
                error.value = result.exceptionOrNull()?.message ?: "Failed to refresh visitor history"
            }
        }
    }

    fun setIgnored(visitorEventId: Long, ignored: Boolean) {
        viewModelScope.launch {
            val result = visitorRepository.setVisitorIgnored(visitorEventId, ignored)
            if (result.isFailure) {
                error.value = result.exceptionOrNull()?.message ?: "Failed to update visitor"
            }
        }
    }

    fun clearError() {
        error.value = null
    }
}
