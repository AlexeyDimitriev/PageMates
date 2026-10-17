package com.example.pagemates.data.local

import android.content.Context
import com.example.pagemates.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileDataStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val profileFlow: MutableStateFlow<UserProfile>

    init {
        val initial = if (prefs.getBoolean(KEY_INITIALIZED, false)) {
            UserProfile(
                name = prefs.getString(KEY_NAME, SeedData.profile.name) ?: SeedData.profile.name,
                bio = prefs.getString(KEY_BIO, SeedData.profile.bio) ?: SeedData.profile.bio,
                avatarUri = prefs.getString(KEY_AVATAR, SeedData.profile.avatarUri)
                    ?: SeedData.profile.avatarUri
            )
        } else {
            writePrefs(SeedData.profile)
            SeedData.profile
        }
        profileFlow = MutableStateFlow(initial)
    }

    fun observe(): Flow<UserProfile> = profileFlow.asStateFlow()

    fun get(): UserProfile = profileFlow.value

    fun save(profile: UserProfile) {
        writePrefs(profile)
        profileFlow.value = profile
    }

    private fun writePrefs(profile: UserProfile) {
        prefs.edit()
            .putString(KEY_NAME, profile.name)
            .putString(KEY_BIO, profile.bio)
            .putString(KEY_AVATAR, profile.avatarUri)
            .putBoolean(KEY_INITIALIZED, true)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "profile_prefs"
        private const val KEY_INITIALIZED = "initialized"
        private const val KEY_NAME = "name"
        private const val KEY_BIO = "bio"
        private const val KEY_AVATAR = "avatar"
    }
}
