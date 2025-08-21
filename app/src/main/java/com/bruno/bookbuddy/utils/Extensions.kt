package com.bruno.bookbuddy.utils

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import com.bruno.bookbuddy.data.model.Book
import com.bruno.bookbuddy.data.model.ReadingStatus

val BOOK_PROVIDER_CONTENT_URI: Uri = Uri.parse("content://com.bruno.bookbuddy.provider/books")

@SuppressLint("Range")
fun Context.fetchBooksFromProvider(): MutableList<Book> {
    val books = mutableListOf<Book>()

    val cursor = contentResolver?.query(
        BOOK_PROVIDER_CONTENT_URI,
        null,
        null,
        null,
        "${Book::createdAt.name} DESC"
    )

    while (cursor != null && cursor.moveToNext()) {
        books.add(
            Book(
                _id = cursor.getLong(cursor.getColumnIndexOrThrow(Book::_id.name)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(Book::title.name)),
                author = cursor.getString(cursor.getColumnIndexOrThrow(Book::author.name)),
                year = cursor.getInt(cursor.getColumnIndexOrThrow(Book::year.name)),
                genre = cursor.getString(cursor.getColumnIndexOrThrow(Book::genre.name)),
                status = ReadingStatus.valueOf(
                    cursor.getString(cursor.getColumnIndexOrThrow(Book::status.name))
                ),
                rating = cursor.getFloat(cursor.getColumnIndexOrThrow(Book::rating.name)),
                coverPath = cursor.getString(cursor.getColumnIndexOrThrow(Book::coverPath.name)),
                createdAt = cursor.getString(cursor.getColumnIndexOrThrow(Book::createdAt.name))
            )
        )
    }

    cursor?.close()
    return books
}

@SuppressLint("Range")
fun Context.getBookByIdFromProvider(bookId: Long): Book? {
    val uri = Uri.withAppendedPath(BOOK_PROVIDER_CONTENT_URI, bookId.toString())
    val cursor = contentResolver?.query(uri, null, null, null, null)

    return if (cursor != null && cursor.moveToFirst()) {
        val book = Book(
            _id = cursor.getLong(cursor.getColumnIndexOrThrow(Book::_id.name)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(Book::title.name)),
            author = cursor.getString(cursor.getColumnIndexOrThrow(Book::author.name)),
            year = cursor.getInt(cursor.getColumnIndexOrThrow(Book::year.name)),
            genre = cursor.getString(cursor.getColumnIndexOrThrow(Book::genre.name)),
            status = ReadingStatus.valueOf(
                cursor.getString(cursor.getColumnIndexOrThrow(Book::status.name))
            ),
            rating = cursor.getFloat(cursor.getColumnIndexOrThrow(Book::rating.name)),
            coverPath = cursor.getString(cursor.getColumnIndexOrThrow(Book::coverPath.name)),
            createdAt = cursor.getString(cursor.getColumnIndexOrThrow(Book::createdAt.name))
        )
        cursor.close()
        book
    } else {
        cursor?.close()
        null
    }
}

fun Context.insertBookViaProvider(book: Book): Long {
    val values = ContentValues().apply {
        put(Book::title.name, book.title)
        put(Book::author.name, book.author)
        put(Book::year.name, book.year)
        put(Book::genre.name, book.genre)
        put(Book::status.name, book.status.name)
        put(Book::rating.name, book.rating)
        put(Book::coverPath.name, book.coverPath)
        put(Book::createdAt.name, book.createdAt)
    }

    val uri = contentResolver.insert(BOOK_PROVIDER_CONTENT_URI, values)
    return uri?.lastPathSegment?.toLongOrNull() ?: -1
}

fun Context.updateBookViaProvider(book: Book): Int {
    val values = ContentValues().apply {
        put(Book::title.name, book.title)
        put(Book::author.name, book.author)
        put(Book::year.name, book.year)
        put(Book::genre.name, book.genre)
        put(Book::status.name, book.status.name)
        put(Book::rating.name, book.rating)
        put(Book::coverPath.name, book.coverPath)
        put(Book::createdAt.name, book.createdAt)
    }

    val uri = Uri.withAppendedPath(BOOK_PROVIDER_CONTENT_URI, book._id.toString())
    return contentResolver.update(uri, values, null, null)
}

fun Context.deleteBookViaProvider(bookId: Long): Int {
    val uri = Uri.withAppendedPath(BOOK_PROVIDER_CONTENT_URI, bookId.toString())
    return contentResolver.delete(uri, null, null)
}