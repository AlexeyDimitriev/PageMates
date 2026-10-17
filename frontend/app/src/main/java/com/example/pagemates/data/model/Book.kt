package com.example.pagemates.data.model

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val pages: Int,
    val read: Int = 0,
    val status: ReadingStatus = ReadingStatus.UNREAD,
    val coverUri: String? = null,
    val hue: String,
    val tags: List<String> = emptyList(),
    val notes: String = "",
    val quotes: List<String> = emptyList()
) {
    val progressPercent: Int
        get() = if (pages <= 0) 0 else minOf(100, ((read.toFloat() / pages) * 100).toInt())
}
