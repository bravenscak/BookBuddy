package com.bruno.bookbuddy.data.repository

import android.content.Context
import com.bruno.bookbuddy.data.database.BookSqlHelper

fun getBookRepository(context: Context?): BookRepository = BookSqlHelper(context)