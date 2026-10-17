package com.example.pagemates.data.repository

import com.example.pagemates.data.local.ProfileDataStore
import com.example.pagemates.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

class ProfileRepository(
    private val profileDataStore: ProfileDataStore
) {
    fun observeProfile(): Flow<UserProfile> = profileDataStore.observe()

    fun getProfile(): UserProfile = profileDataStore.get()

    fun saveProfile(profile: UserProfile) {
        profileDataStore.save(profile)
    }
}
