package com.example.pagemates.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pagemates.R
import com.example.pagemates.data.model.Book
import com.example.pagemates.databinding.ItemBookBinding
import com.example.pagemates.ui.util.label

class BooksAdapter(
    private val onClick: (Book) -> Unit
) : ListAdapter<Book, BooksAdapter.Holder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding, onClick)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    class Holder(
        private val binding: ItemBookBinding,
        private val onClick: (Book) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(book: Book) {
            binding.coverView.bind(book)
            binding.titleText.text = book.title
            binding.authorText.text = book.author
            binding.statusText.text = book.status.label(binding.root.context)
            binding.pagesText.text = binding.root.context.getString(
                R.string.pages_short_format,
                book.read,
                book.pages
            )
            binding.progressBar.progress = book.progressPercent
            binding.root.setOnClickListener { onClick(book) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Book, newItem: Book) = oldItem == newItem
    }
}
