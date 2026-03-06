package com.example.aeye.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aeye.data.Firestore
import com.example.aeye.data.model.TestResult
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ResultsViewModel(
    private val repository: Firestore
) : ViewModel() {

    val results: StateFlow<List<TestResult>> =
        repository.observeResults()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveResult(result: TestResult) {
        viewModelScope.launch {
            repository.addResult(result)
        }
    }

    fun deleteResult(resultId: String) {
        viewModelScope.launch {
            repository.deleteResult(resultId)
        }
    }
}