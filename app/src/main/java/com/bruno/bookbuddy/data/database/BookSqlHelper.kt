package com.bruno.bookbuddy.data.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.bruno.bookbuddy.data.model.Book
import com.bruno.bookbuddy.data.model.ReadingStatus
import com.bruno.bookbuddy.data.repository.BookRepository

private const val DB_NAME = "books.db"
private const val DB_VERSION = 1
private const val TABLE_NAME = "books"

private val CREATE_TABLE = "create table $TABLE_NAME( " +
        "${Book::_id.name} integer primary key autoincrement, " +
        "${Book::title.name} text not null, " +
        "${Book::author.name} text not null, " +
        "${Book::year.name} integer not null, " +
        "${Book::genre.name} text not null, " +
        "${Book::status.name} text not null, " +
        "${Book::rating.name} real not null, " +
        "${Book::coverPath.name} text not null, " +
        "${Book::createdAt.name} text not null" +
        ")"

private const val DROP_TABLE = "drop table if exists $TABLE_NAME"

class BookSqlHelper(context: Context?) : SQLiteOpenHelper(
    context,
    DB_NAME,
    null,
    DB_VERSION
), BookRepository {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(DROP_TABLE)
        onCreate(db)
    }

    override fun delete(selection: String?, selectionArgs: Array<String>?) =
        writableDatabase.delete(TABLE_NAME, selection, selectionArgs)

    override fun insert(values: ContentValues?) =
        writableDatabase.insert(TABLE_NAME, null, values)

    override fun query(
        projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor = readableDatabase.query(
        TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder
    )

    override fun update(
        values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ) = writableDatabase.update(TABLE_NAME, values, selection, selectionArgs)

    override fun getAllBooks(): List<Book> {
        val books = mutableListOf<Book>()
        val cursor = query(null, null, null, "${Book::createdAt.name} DESC")

        while (cursor.moveToNext()) {
            books.add(createBookFromCursor(cursor))
        }
        cursor.close()
        return books
    }

    override fun getBookById(id: Long): Book? {
        val cursor = query(
            null,
            "${Book::_id.name}=?",
            arrayOf(id.toString()),
            null
        )

        return if (cursor.moveToFirst()) {
            val book = createBookFromCursor(cursor)
            cursor.close()
            book
        } else {
            cursor.close()
            null
        }
    }

    override fun insertBook(book: Book): Long {
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
        return insert(values)
    }

    override fun updateBook(book: Book): Int {
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
        return update(values, "${Book::_id.name}=?", arrayOf(book._id.toString()))
    }

    override fun deleteBook(id: Long): Int {
        return delete("${Book::_id.name}=?", arrayOf(id.toString()))
    }

    private fun createBookFromCursor(cursor: Cursor): Book {
        return Book(
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
    }
}