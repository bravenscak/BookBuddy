package com.bruno.bookbuddy.data.repository

import android.content.ContentValues
import android.database.Cursor
import com.bruno.bookbuddy.data.model.Book

interface BookRepository {
    fun delete(selection: String?, selectionArgs: Array<String>?): Int
    fun insert(values: ContentValues?): Long
    fun query(
        projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor
    fun update(
        values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int

    fun getAllBooks(): List<Book>
    fun getBookById(id: Long): Book?
    fun insertBook(book: Book): Long
    fun updateBook(book: Book): Int
    fun deleteBook(id: Long): Int
}