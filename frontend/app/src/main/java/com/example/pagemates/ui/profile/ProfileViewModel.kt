package com.example.pagemates.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagemates.data.model.Book
import com.example.pagemates.data.model.ReadingStatus
import com.example.pagemates.data.model.UserProfile
import com.example.pagemates.data.repository.BookRepository
import com.example.pagemates.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

enum class BookFilter { ALL, READING, UNREAD, READ }

data class ProfileUiState(
    val profile: UserProfile = UserProfile("", "", ""),
    val books: List<Book> = emptyList(),
    val filteredBooks: List<Book> = emptyList(),
    val filter: BookFilter = BookFilter.ALL,
    val editing: Boolean = false,
    val booksCount: Int = 0,
    val finishedCount: Int = 0,
    val pagesReadCount: Int = 0
)

class ProfileViewModel(
    bookRepository: BookRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val filter = MutableStateFlow(BookFilter.ALL)
    private val editing = MutableStateFlow(false)

    val uiState: StateFlow<ProfileUiState> = combine(
        profileRepository.observeProfile(),
        bookRepository.observeBooks(),
        filter,
        editing
    ) { profile, books, selectedFilter, isEditing ->
        val filtered = when (selectedFilter) {
            BookFilter.ALL -> books
            BookFilter.READING -> books.filter { it.status == ReadingStatus.READING }
            BookFilter.UNREAD -> books.filter { it.status == ReadingStatus.UNREAD }
            BookFilter.READ -> books.filter { it.status == ReadingStatus.READ }
        }
        ProfileUiState(
            profile = profile,
            books = books,
            filteredBooks = filtered,
            filter = selectedFilter,
            editing = isEditing,
            booksCount = books.size,
            finishedCount = books.count { it.status == ReadingStatus.READ },
            pagesReadCount = books.sumOf { it.read }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun setFilter(value: BookFilter) {
        filter.value = value
    }

    fun toggleEditing() {
        editing.update { !it }
    }

    fun saveProfile(name: String, bio: String, avatarUri: String?) {
        val current = profileRepository.getProfile()
        profileRepository.saveProfile(
            current.copy(
                name = name.trim().ifBlank { current.name },
                bio = bio.trim(),
                avatarUri = avatarUri ?: current.avatarUri
            )
        )
        editing.value = false
    }

    fun updateAvatar(uri: String) {
        val current = profileRepository.getProfile()
        profileRepository.saveProfile(current.copy(avatarUri = uri))
    }
}
