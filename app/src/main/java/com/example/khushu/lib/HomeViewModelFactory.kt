package com.example.khushu.lib

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.khushu.ui.home.HomeViewModel
import com.example.khushu.utils.PreferencesRepository

class HomeViewModelFactory(private val preferencesRepository: PreferencesRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(preferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
