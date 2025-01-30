package com.example.khushu.ui.home
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.khushu.lib.Place
import com.example.khushu.utils.PreferencesRepository


class HomeViewModel(private val preferencesRepository: PreferencesRepository) : ViewModel() {

    // Backing property for the list of items
    private val _items = MutableLiveData<List<Place>?>(preferencesRepository.getPlaces()?.toList())
    val items: LiveData<List<Place>?> = _items

    // Add an item to the list
    fun addItem(item: Place) {
        val updatedList = _items.value?.toMutableList() ?: mutableListOf()
        updatedList.add(item)
        _items.value = updatedList
        preferencesRepository.savePlaces(updatedList.toMutableSet())
    }

    // Remove an item from the list
    fun removeItem(item: Place) {
        val updatedItems = _items.value?.toMutableList()?.apply {
            remove(item)
        }
        _items.value = updatedItems
    }
}