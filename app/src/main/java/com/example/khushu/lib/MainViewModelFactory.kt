package com.example.khushu.lib

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.khushu.MainViewModel
import com.example.khushu.utils.PreferencesRepository

class MainViewModelFactory(private val preferencesRepository: PreferencesRepository, private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(preferencesRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
