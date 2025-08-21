package com.bruno.bookbuddy.data.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.bruno.bookbuddy.data.model.Book
import com.bruno.bookbuddy.data.repository.BookRepository
import com.bruno.bookbuddy.data.repository.getBookRepository

private const val AUTHORITY = "com.bruno.bookbuddy.provider"
private const val PATH = "books"
val BOOK_PROVIDER_CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$PATH")

private const val BOOKS = 10
private const val BOOK_ID = 20

private val URI_MATCHER = with(UriMatcher(UriMatcher.NO_MATCH)) {
    addURI(AUTHORITY, PATH, BOOKS)
    addURI(AUTHORITY, "$PATH/#", BOOK_ID)
    this
}

class BookContentProvider : ContentProvider() {

    private lateinit var repository: BookRepository

    override fun onCreate(): Boolean {
        repository = getBookRepository(context)
        return true
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return when (URI_MATCHER.match(uri)) {
            BOOKS -> repository.delete(selection, selectionArgs)
            BOOK_ID -> {
                val id = uri.lastPathSegment ?: return 0
                repository.delete("${Book::_id.name}=?", arrayOf(id))
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun getType(uri: Uri): String? {
        return when (URI_MATCHER.match(uri)) {
            BOOKS -> "vnd.android.cursor.dir/vnd.bookbuddy.book"
            BOOK_ID -> "vnd.android.cursor.item/vnd.bookbuddy.book"
            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val id = repository.insert(values)
        return ContentUris.withAppendedId(BOOK_PROVIDER_CONTENT_URI, id)
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        return repository.query(projection, selection, selectionArgs, sortOrder)
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        return when (URI_MATCHER.match(uri)) {
            BOOKS -> repository.update(values, selection, selectionArgs)
            BOOK_ID -> {
                val id = uri.lastPathSegment ?: return 0
                repository.update(values, "${Book::_id.name}=?", arrayOf(id))
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }
}