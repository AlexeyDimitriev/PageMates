package com.example.pagemates.data.repository

import com.example.pagemates.data.local.BookLocalDataSource
import com.example.pagemates.data.model.Book
import com.example.pagemates.data.model.ReadingStatus
import kotlinx.coroutines.flow.Flow

class BookRepository(
    private val dataSource: BookLocalDataSource
) {
    fun observeBooks(): Flow<List<Book>> = dataSource.observeAll()

    fun observeBook(id: String): Flow<Book?> = dataSource.observeById(id)

    fun getBook(id: String): Book? = dataSource.getById(id)

    fun saveBook(book: Book): Book = dataSource.upsert(book)

    fun updateBook(book: Book) {
        dataSource.upsert(book)
    }

    fun setPagesRead(bookId: String, pagesRead: Int) {
        val current = dataSource.getById(bookId) ?: return
        val read = pagesRead.coerceIn(0, current.pages)
        dataSource.upsert(
            current.copy(
                read = read,
                status = ReadingStatus.fromPages(read, current.pages)
            )
        )
    }

    fun setStatus(bookId: String, status: ReadingStatus) {
        val current = dataSource.getById(bookId) ?: return
        val read = when (status) {
            ReadingStatus.READ -> current.pages
            ReadingStatus.UNREAD -> 0
            ReadingStatus.READING -> when {
                current.read > 0 && current.read < current.pages -> current.read
                current.pages <= 1 -> 1
                else -> 1.coerceAtMost(current.pages - 1)
            }
        }
        dataSource.upsert(current.copy(status = status, read = read))
    }

    fun deleteBook(id: String) {
        dataSource.delete(id)
    }
}
