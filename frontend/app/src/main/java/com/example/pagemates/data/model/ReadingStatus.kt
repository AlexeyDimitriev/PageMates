package com.example.pagemates.data.model

enum class ReadingStatus {
    UNREAD,
    READING,
    READ;

    companion object {
        fun fromStorage(value: String): ReadingStatus =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: UNREAD

        fun fromPages(read: Int, pages: Int): ReadingStatus = when {
            read <= 0 -> UNREAD
            read >= pages -> READ
            else -> READING
        }
    }
}
