package com.techne.vaultshield.ui.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techne.vaultshield.domain.audit.AuditLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuditViewModel @Inject constructor(
    private val auditLogRepository: AuditLogRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuditState())
    val state: StateFlow<AuditState> = _state.asStateFlow()

    init {
        handleIntent(AuditIntent.LoadAuditLogs)
    }

    fun handleIntent(intent: AuditIntent) {
        when (intent) {
            is AuditIntent.LoadAuditLogs -> loadAuditLogs()
        }
    }

    private fun loadAuditLogs() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            auditLogRepository.getLogs().collect { logs ->
                _state.update { it.copy(auditLogs = logs, isLoading = false) }
            }
        }
    }
}
