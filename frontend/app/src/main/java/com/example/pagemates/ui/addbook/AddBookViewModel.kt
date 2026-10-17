package com.example.pagemates.ui.addbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagemates.data.local.SeedData
import com.example.pagemates.data.model.Book
import com.example.pagemates.data.model.ReadingStatus
import com.example.pagemates.data.repository.BookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AddBookUiState(
    val title: String = "",
    val author: String = "",
    val pages: String = "",
    val pagesRead: String = "",
    val status: ReadingStatus = ReadingStatus.UNREAD,
    val coverUri: String? = null,
    val tags: List<String> = emptyList(),
    val notes: String = "",
    val quotes: List<String> = emptyList(),
    val hue: String = SeedData.hues.random(),
    val saved: Boolean = false
) {
    val canSave: Boolean
        get() = title.trim().isNotEmpty() && (pages.toIntOrNull() ?: 0) > 0
}

class AddBookViewModel(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddBookUiState())
    val uiState: StateFlow<AddBookUiState> = _uiState.asStateFlow()

    fun updateTitle(value: String) = _uiState.update { it.copy(title = value) }
    fun updateAuthor(value: String) = _uiState.update { it.copy(author = value) }
    fun updatePages(value: String) = _uiState.update { it.copy(pages = value.filter(Char::isDigit)) }
    fun updatePagesRead(value: String) = _uiState.update { it.copy(pagesRead = value.filter(Char::isDigit)) }
    fun updateStatus(value: ReadingStatus) = _uiState.update { it.copy(status = value) }
    fun updateCover(uri: String?) = _uiState.update { it.copy(coverUri = uri) }
    fun updateNotes(value: String) = _uiState.update { it.copy(notes = value) }

    fun addTag(raw: String) {
        val tag = raw.trim().lowercase()
        if (tag.isEmpty()) return
        _uiState.update { state ->
            if (state.tags.contains(tag)) state else state.copy(tags = state.tags + tag)
        }
    }

    fun removeTag(tag: String) {
        _uiState.update { it.copy(tags = it.tags - tag) }
    }

    fun addQuote(raw: String) {
        val quote = raw.trim()
        if (quote.isEmpty()) return
        _uiState.update { it.copy(quotes = it.quotes + quote) }
    }

    fun removeQuote(index: Int) {
        _uiState.update { state ->
            state.copy(quotes = state.quotes.filterIndexed { i, _ -> i != index })
        }
    }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return
        val total = state.pages.toInt()
        var read = 0
        if (state.status == ReadingStatus.READ) read = total
        if (state.status == ReadingStatus.READING) {
            read = (state.pagesRead.toIntOrNull() ?: 1).coerceIn(1, (total - 1).coerceAtLeast(1))
        }
        val status = if (state.status == ReadingStatus.READING && total == 1) {
            ReadingStatus.READ
        } else {
            state.status
        }
        val book = Book(
            id = "",
            title = state.title.trim(),
            author = state.author.trim().ifBlank { "Автор неизвестен" },
            pages = total,
            read = if (status == ReadingStatus.READ) total else read,
            status = status,
            coverUri = state.coverUri,
            hue = state.hue,
            tags = state.tags,
            notes = state.notes,
            quotes = state.quotes
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) { bookRepository.saveBook(book) }
            _uiState.update { it.copy(saved = true) }
        }
    }
}
