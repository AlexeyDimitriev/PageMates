package com.example.pagemates.ui.bookdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagemates.data.model.Book
import com.example.pagemates.data.model.ReadingStatus
import com.example.pagemates.data.repository.BookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class BookDetailViewModel(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val bookId = MutableStateFlow<String?>(null)

    val book: StateFlow<Book?> = bookId
        .filterNotNull()
        .flatMapLatest(bookRepository::observeBook)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun load(id: String) {
        bookId.value = id
    }

    fun setPagesRead(pages: Int) {
        val id = bookId.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.setPagesRead(id, pages)
        }
    }

    fun setStatus(status: ReadingStatus) {
        val id = bookId.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.setStatus(id, status)
        }
    }

    fun updateCover(uri: String) {
        val current = book.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.updateBook(current.copy(coverUri = uri))
        }
    }

    fun updateNotes(notes: String) {
        val current = book.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.updateBook(current.copy(notes = notes))
        }
    }

    fun addTag(raw: String) {
        val current = book.value ?: return
        val tag = raw.trim().lowercase()
        if (tag.isEmpty() || current.tags.contains(tag)) return
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.updateBook(current.copy(tags = current.tags + tag))
        }
    }

    fun removeTag(tag: String) {
        val current = book.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.updateBook(current.copy(tags = current.tags - tag))
        }
    }

    fun addQuote(raw: String) {
        val current = book.value ?: return
        val quote = raw.trim()
        if (quote.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.updateBook(current.copy(quotes = current.quotes + quote))
        }
    }

    fun removeQuote(index: Int) {
        val current = book.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.updateBook(
                current.copy(quotes = current.quotes.filterIndexed { i, _ -> i != index })
            )
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val id = bookId.value ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) { bookRepository.deleteBook(id) }
            onDeleted()
        }
    }
}
