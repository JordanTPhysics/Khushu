package com.example.khushu.lib

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.khushu.ui.mapview.MapsViewModel
import com.example.khushu.utils.PreferencesRepository

class MapsViewModelFactory(
    private val preferencesRepository: PreferencesRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapsViewModel::class.java)) {
            return MapsViewModel(preferencesRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}