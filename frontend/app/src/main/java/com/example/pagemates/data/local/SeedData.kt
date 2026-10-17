package com.example.pagemates.data.local

import com.example.pagemates.data.model.Book
import com.example.pagemates.data.model.ReadingStatus
import com.example.pagemates.data.model.UserProfile

object SeedData {
    val hues = listOf("#b84a33", "#e3ad3f", "#4f7a8a", "#7a4e6d", "#6b8f4e", "#d97b5a")

    val profile = UserProfile(
        name = "Даша",
        bio = "Читаю преимущественно художественную литературу.",
        avatarUri = ""
    )

    val books = listOf(
        Book(
            id = "1",
            title = "Охота на овец",
            author = "Харуки Мураками",
            pages = 448,
            read = 448,
            status = ReadingStatus.READ,
            hue = "#7a4e6d",
            tags = listOf("фантастика"),
            notes = "Не забыть прочитать остальные 3 книги из цикла",
            quotes = listOf("Даже про самую скучную жизнь никогда не расскажешь всё полностью."),
            coverUri = "https://www.1c-interes.ru/images/2023/03/Okhot_big.jpg"
        ),
        Book(
            id = "2",
            title = "Преступление и наказание",
            author = "Ф.М. Достоевский",
            pages = 432,
            read = 387,
            status = ReadingStatus.READING,
            hue = "#6b8f4e",
            tags = listOf("психология", "детектив", "роман"),
            notes = "Интересно, что книгу сложно отнести к какому-то одному жанру, в ней переплетаются самые разные жанровые разновидности.",
            quotes = listOf("Тварь ли я дрожащая или право имею?", "Человек он умный, но чтоб умно поступать - одного ума мало."),
            coverUri = "https://i.pinimg.com/originals/14/7e/ce/147ece17d5cea50159145737612fb878.jpg"
        ),
        Book(
            id = "3",
            title = "Граф Монте-Кристо",
            author = "Александр Дюма",
            pages = 1086,
            read = 0,
            status = ReadingStatus.UNREAD,
            hue = "#4f7a8a",
            tags = listOf("приключения", "история"),
            coverUri = "https://fkniga.ru/media/product/04/040409/KA-00194301_02.jpg"
        )
    )
}
