package com.bruno.bookbuddy.data.model

data class Book(
    var _id: Long?,
    val title: String,
    val author: String,
    val year: Int,
    val genre: String,
    val status: ReadingStatus,
    val rating: Float,
    val coverPath: String,
    val createdAt: String
)