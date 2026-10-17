package com.example.pagemates.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pagemates.di.AppContainer
import com.example.pagemates.ui.addbook.AddBookViewModel
import com.example.pagemates.ui.bookdetail.BookDetailViewModel
import com.example.pagemates.ui.profile.ProfileViewModel

class ViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
                ProfileViewModel(container.bookRepository, container.profileRepository) as T
            modelClass.isAssignableFrom(AddBookViewModel::class.java) ->
                AddBookViewModel(container.bookRepository) as T
            modelClass.isAssignableFrom(BookDetailViewModel::class.java) ->
                BookDetailViewModel(container.bookRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel ${modelClass.name}")
        }
    }
}
