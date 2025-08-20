package com.bruno.bookbuddy.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bruno.bookbuddy.R
import com.bruno.bookbuddy.data.model.Book
import com.bruno.bookbuddy.data.model.ReadingStatus
import com.bruno.bookbuddy.data.repository.getBookRepository
import com.google.android.material.chip.Chip
import java.io.File

class BookAdapter(
    private val context: Context,
    private val books: MutableList<Book>,
    private val onBookClick: (Book, Int) -> Unit
) : RecyclerView.Adapter<BookAdapter.ViewHolder>() {

    private val repository = getBookRepository(context)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivBookCover: ImageView = itemView.findViewById(R.id.ivBookCover)
        val tvBookTitle: TextView = itemView.findViewById(R.id.tvBookTitle)
        val tvBookAuthor: TextView = itemView.findViewById(R.id.tvBookAuthor)
        val tvBookYear: TextView = itemView.findViewById(R.id.tvBookYear)
        val chipGenre: Chip = itemView.findViewById(R.id.chipGenre)
        val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)
        val tvRating: TextView = itemView.findViewById(R.id.tvRating)

        fun bind(book: Book) {
            tvBookTitle.text = book.title
            tvBookAuthor.text = book.author
            tvBookYear.text = book.year.toString()
            chipGenre.text = book.genre

            chipStatus.text = when (book.status) {
                ReadingStatus.WANT_TO_READ -> "Want to Read"
                ReadingStatus.CURRENTLY_READING -> "Reading"
                ReadingStatus.FINISHED -> "Finished"
                ReadingStatus.ABANDONED -> "Abandoned"
            }

            chipStatus.setChipBackgroundColorResource(
                when (book.status) {
                    ReadingStatus.WANT_TO_READ -> R.color.status_want_to_read
                    ReadingStatus.CURRENTLY_READING -> R.color.status_currently_reading
                    ReadingStatus.FINISHED -> R.color.status_finished
                    ReadingStatus.ABANDONED -> R.color.status_abandoned
                }
            )

            tvRating.text = if (book.rating > 0) {
                String.format("%.1f", book.rating)
            } else {
                "N/A"
            }

            loadBookCover(book.coverPath)
        }

        private fun loadBookCover(coverPath: String) {
            if (coverPath.isNotEmpty() && File(coverPath).exists()) {
                ivBookCover.setImageURI(android.net.Uri.fromFile(File(coverPath)))
            } else {
                ivBookCover.setImageResource(R.drawable.ic_book)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.book_item, parent, false)
        )
    }

    override fun getItemCount() = books.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books[position]

        holder.itemView.setOnClickListener {
            onBookClick(book, position)
        }

        holder.itemView.setOnLongClickListener {
            showDeleteDialog(position)
            true
        }

        holder.bind(book)
    }

    private fun showDeleteDialog(position: Int) {
        val book = books[position]
        AlertDialog.Builder(context).apply {
            setTitle(context.getString(R.string.delete))
            setMessage(context.getString(R.string.sure_to_delete, book.title))
            setIcon(R.drawable.ic_delete)
            setCancelable(true)
            setPositiveButton(android.R.string.ok) { _, _ ->
                deleteBook(position)
            }
            setNegativeButton(context.getString(R.string.cancel), null)
            show()
        }
    }

    private fun deleteBook(position: Int) {
        val book = books[position]
        book._id?.let { bookId ->
            repository.deleteBook(bookId)
            books.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, books.size)

            if (book.coverPath.isNotEmpty()) {
                File(book.coverPath).delete()
            }
        }
    }

    fun updateBooks(newBooks: List<Book>) {
        books.clear()
        books.addAll(newBooks)
        notifyDataSetChanged()
    }
}