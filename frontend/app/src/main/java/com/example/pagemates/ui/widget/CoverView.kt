package com.example.pagemates.ui.widget

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import coil.load
import com.example.pagemates.R
import com.example.pagemates.data.model.Book
import com.example.pagemates.databinding.ViewCoverBinding

class CoverView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding = ViewCoverBinding.inflate(LayoutInflater.from(context), this, true)

    fun bind(book: Book) {
        contentDescription = context.getString(R.string.cover_of, book.title)
        val coverUri = resolveCoverUri(book.coverUri)
        if (coverUri != null) {
            binding.coverImage.isVisible = true
            binding.placeholderGroup.isVisible = false
            binding.coverImage.load(coverUri) {
                crossfade(true)
                listener(
                    onError = { _, _ -> showPlaceholder(book) }
                )
            }
        } else {
            showPlaceholder(book)
        }
    }

    private fun showPlaceholder(book: Book) {
        binding.coverImage.isVisible = false
        binding.placeholderGroup.isVisible = true
        binding.placeholderGroup.setBackgroundColor(Color.parseColor(book.hue))
        binding.placeholderTitle.text = book.title.ifBlank { context.getString(R.string.untitled) }
    }

    private fun resolveCoverUri(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return try {
            val uri = Uri.parse(raw)
            uri.getQueryParameter("img_url")?.takeIf { it.isNotBlank() } ?: raw
        } catch (_: Exception) {
            raw
        }
    }
}
