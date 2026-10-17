package com.example.pagemates.data.local

import android.content.Context
import com.example.pagemates.data.model.Book
import com.example.pagemates.data.model.ReadingStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class BookLocalDataSource(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val booksFlow: MutableStateFlow<List<Book>>

    init {
        val raw = prefs.getString(KEY_BOOKS, null)
        val initial = if (raw.isNullOrBlank()) {
            writePrefs(SeedData.books)
            SeedData.books
        } else {
            decode(raw).ifEmpty {
                writePrefs(SeedData.books)
                SeedData.books
            }
        }
        booksFlow = MutableStateFlow(initial)
    }

    fun observeAll(): Flow<List<Book>> = booksFlow.asStateFlow()

    fun observeById(id: String): Flow<Book?> =
        booksFlow.map { list -> list.find { it.id == id } }

    fun getById(id: String): Book? = booksFlow.value.find { it.id == id }

    fun upsert(book: Book): Book {
        val withId = if (book.id.isBlank()) book.copy(id = UUID.randomUUID().toString()) else book
        val current = booksFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == withId.id }
        if (index >= 0) current[index] = withId else current.add(0, withId)
        persist(current)
        return withId
    }

    fun delete(id: String) {
        persist(booksFlow.value.filterNot { it.id == id })
    }

    private fun persist(books: List<Book>) {
        writePrefs(books)
        booksFlow.value = books
    }

    private fun writePrefs(books: List<Book>) {
        prefs.edit().putString(KEY_BOOKS, encode(books)).apply()
    }

    private fun encode(books: List<Book>): String {
        val array = JSONArray()
        books.forEach { book ->
            array.put(
                JSONObject().apply {
                    put("id", book.id)
                    put("title", book.title)
                    put("author", book.author)
                    put("pages", book.pages)
                    put("read", book.read)
                    put("status", book.status.name)
                    put("coverUri", book.coverUri)
                    put("hue", book.hue)
                    put("tags", JSONArray(book.tags))
                    put("notes", book.notes)
                    put("quotes", JSONArray(book.quotes))
                }
            )
        }
        return array.toString()
    }

    private fun decode(raw: String): List<Book> = try {
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    Book(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        author = obj.getString("author"),
                        pages = obj.getInt("pages"),
                        read = obj.getInt("read"),
                        status = ReadingStatus.fromStorage(obj.optString("status")),
                        coverUri = obj.optString("coverUri").takeIf { it.isNotBlank() && it != "null" },
                        hue = obj.optString("hue", SeedData.hues.first()),
                        tags = obj.optJSONArray("tags").toStringList(),
                        notes = obj.optString("notes"),
                        quotes = obj.optJSONArray("quotes").toStringList()
                    )
                )
            }
        }
    } catch (_: Exception) {
        emptyList()
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (i in 0 until length()) {
                val value = optString(i)
                if (value.isNotBlank()) add(value)
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "books_prefs"
        private const val KEY_BOOKS = "books_json"
    }
}
