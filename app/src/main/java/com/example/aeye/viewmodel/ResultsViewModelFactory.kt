package com.example.aeye.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aeye.data.Firestore

class ResultsViewModelFactory(
    private val repository: Firestore
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ResultsViewModel::class.java)) {
            return ResultsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}