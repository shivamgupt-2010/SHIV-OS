package com.example.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.dao.MemoryDao
import com.example.data.local.dao.WorkflowDao
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.WorkflowEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val workflowDao: WorkflowDao,
    private val memoryDao: MemoryDao
) : ViewModel() {

    private val _activeWorkflows = MutableStateFlow<List<WorkflowEntity>>(emptyList())
    val activeWorkflows: StateFlow<List<WorkflowEntity>> = _activeWorkflows.asStateFlow()

    private val _recentMemories = MutableStateFlow<List<MemoryEntity>>(emptyList())
    val recentMemories: StateFlow<List<MemoryEntity>> = _recentMemories.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _activeWorkflows.value = workflowDao.getActiveWorkflows()
            memoryDao.getAllMemories().collect {
                _recentMemories.value = it.take(5) // Limit to 5 for dashboard
            }
        }
    }
}
