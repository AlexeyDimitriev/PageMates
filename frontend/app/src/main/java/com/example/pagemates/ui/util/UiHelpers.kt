package com.example.pagemates.ui.util

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import coil.load
import coil.transform.CircleCropTransformation
import com.example.pagemates.R
import com.example.pagemates.data.model.ReadingStatus
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

fun ReadingStatus.label(context: Context): String = when (this) {
    ReadingStatus.UNREAD -> context.getString(R.string.status_unread)
    ReadingStatus.READING -> context.getString(R.string.status_reading)
    ReadingStatus.READ -> context.getString(R.string.status_read)
}

fun ImageView.loadAvatar(uri: String?) {
    load(uri) {
        transformations(CircleCropTransformation())
        crossfade(true)
        placeholder(R.drawable.bg_avatar_ring)
        error(R.drawable.bg_avatar_ring)
    }
}

fun ChipGroup.bindRemovableTags(
    tags: List<String>,
    onRemove: (String) -> Unit
) {
    removeAllViews()
    tags.forEach { tag ->
        val chip = Chip(context).apply {
            text = "#$tag"
            isCloseIconVisible = true
            closeIcon = ContextCompat.getDrawable(context, R.drawable.ic_close)
            setChipBackgroundColorResource(R.color.card_foreground_10)
            setTextColor(ContextCompat.getColor(context, R.color.card_foreground))
            setOnCloseIconClickListener { onRemove(tag) }
        }
        addView(chip)
    }
}

fun ChipGroup.bindHeaderTags(tags: List<String>) {
    removeAllViews()
    val density = resources.displayMetrics.density
    tags.forEach { tag ->
        val label = TextView(context).apply {
            text = "#$tag"
            setTextColor(ContextCompat.getColor(context, R.color.foreground))
            textSize = 12f
            setBackgroundResource(R.drawable.bg_header_tag)
            setPadding(
                (10 * density).toInt(),
                (4 * density).toInt(),
                (10 * density).toInt(),
                (4 * density).toInt()
            )
            val params = ChipGroup.LayoutParams(
                ChipGroup.LayoutParams.WRAP_CONTENT,
                ChipGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams = params
        }
        addView(label)
    }
}

fun LinearLayout.bindQuotes(
    quotes: List<String>,
    onRemove: ((Int) -> Unit)?
) {
    removeAllViews()
    val inflater = LayoutInflater.from(context)
    quotes.forEachIndexed { index, quote ->
        val view = inflater.inflate(R.layout.item_quote, this, false)
        view.findViewById<TextView>(R.id.quoteText).text =
            context.getString(R.string.quote_format, quote)
        val remove = view.findViewById<android.widget.ImageButton>(R.id.removeQuoteButton)
        if (onRemove == null) {
            remove.visibility = android.view.View.GONE
        } else {
            remove.setOnClickListener { onRemove(index) }
        }
        addView(view)
    }
}

fun Context.persistReadPermission(uri: Uri) {
    try {
        contentResolver.takePersistableUriPermission(
            uri,
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    } catch (_: SecurityException) {
        // provider may not support persistable permissions
    }
}
