package com.example.pagemates.di

import android.content.Context
import com.example.pagemates.data.local.BookLocalDataSource
import com.example.pagemates.data.local.ProfileDataStore
import com.example.pagemates.data.repository.BookRepository
import com.example.pagemates.data.repository.ProfileRepository

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val bookLocalDataSource = BookLocalDataSource(appContext)
    private val profileDataStore = ProfileDataStore(appContext)

    val bookRepository = BookRepository(bookLocalDataSource)
    val profileRepository = ProfileRepository(profileDataStore)
}
