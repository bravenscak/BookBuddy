package com.bruno.bookbuddy.utils

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import com.bruno.bookbuddy.data.model.Book
import com.bruno.bookbuddy.data.model.ReadingStatus

val BOOK_PROVIDER_CONTENT_URI: Uri = Uri.parse("content://com.bruno.bookbuddy.provider/books")

@SuppressLint("Range")
fun Context.fetchBooksFromProvider(): MutableList<Book> {
    val books = mutableListOf<Book>()
    Log.d("Extensions", "fetchBooksFromProvider: Starting to fetch books")

    val cursor = contentResolver?.query(
        BOOK_PROVIDER_CONTENT_URI,
        null,
        null,
        null,
        "${Book::createdAt.name} DESC"
    )

    if (cursor == null) {
        Log.e("Extensions", "fetchBooksFromProvider: Cursor is null")
        return books
    }

    Log.d("Extensions", "fetchBooksFromProvider: Cursor count = ${cursor.count}")

    while (cursor.moveToNext()) {
        try {
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
            books.add(book)
            Log.d("Extensions", "fetchBooksFromProvider: Added book ID=${book._id}, Title=${book.title}")
        } catch (e: Exception) {
            Log.e("Extensions", "fetchBooksFromProvider: Error parsing book", e)
        }
    }

    cursor.close()
    Log.d("Extensions", "fetchBooksFromProvider: Final count = ${books.size}")
    return books
}

@SuppressLint("Range")
fun Context.getBookByIdFromProvider(bookId: Long): Book? {
    Log.d("Extensions", "getBookByIdFromProvider: Looking for book ID = $bookId")

    val uri = Uri.withAppendedPath(BOOK_PROVIDER_CONTENT_URI, bookId.toString())
    Log.d("Extensions", "getBookByIdFromProvider: URI = $uri")

    val cursor = contentResolver?.query(uri, null, null, null, null)

    if (cursor == null) {
        Log.e("Extensions", "getBookByIdFromProvider: Cursor is null for ID $bookId")
        return null
    }

    Log.d("Extensions", "getBookByIdFromProvider: Cursor count = ${cursor.count}")

    return if (cursor.moveToFirst()) {
        try {
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

            // FIX: Provjeri da li smo dobili pravu knjigu
            if (book._id == bookId) {
                Log.d("Extensions", "getBookByIdFromProvider: Found CORRECT book: ID=${book._id}, Title=${book.title}")
                book
            } else {
                Log.e("Extensions", "getBookByIdFromProvider: WRONG book returned! Expected ID=$bookId, got ID=${book._id}, Title=${book.title}")
                null
            }
        } catch (e: Exception) {
            Log.e("Extensions", "getBookByIdFromProvider: Error parsing book for ID $bookId", e)
            cursor.close()
            null
        }
    } else {
        cursor.close()
        Log.e("Extensions", "getBookByIdFromProvider: No book found for ID $bookId")
        null
    }
}

fun Context.insertBookViaProvider(book: Book): Long {
    Log.d("Extensions", "insertBookViaProvider: Inserting book: ${book.title}")

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
    val id = uri?.lastPathSegment?.toLongOrNull() ?: -1

    Log.d("Extensions", "insertBookViaProvider: Inserted with ID = $id, URI = $uri")
    return id
}

fun Context.updateBookViaProvider(book: Book): Int {
    Log.d("Extensions", "updateBookViaProvider: Updating book ID=${book._id}, Title=${book.title}")

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
    val rows = contentResolver.update(uri, values, null, null)

    Log.d("Extensions", "updateBookViaProvider: Updated $rows rows for book ID=${book._id}")
    return rows
}

fun Context.deleteBookViaProvider(bookId: Long): Int {
    Log.d("Extensions", "deleteBookViaProvider: Deleting book ID = $bookId")

    val uri = Uri.withAppendedPath(BOOK_PROVIDER_CONTENT_URI, bookId.toString())
    val rows = contentResolver.delete(uri, null, null)

    Log.d("Extensions", "deleteBookViaProvider: Deleted $rows rows for book ID = $bookId")
    return rows
}