package com.example.pagemates.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.pagemates.R
import com.example.pagemates.data.model.ReadingStatus
import com.example.pagemates.databinding.ViewStatusPickerBinding

class StatusPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding = ViewStatusPickerBinding.inflate(LayoutInflater.from(context), this, true)
    private var current = ReadingStatus.UNREAD
    private var listener: ((ReadingStatus) -> Unit)? = null

    init {
        binding.statusUnread.setOnClickListener { select(ReadingStatus.UNREAD) }
        binding.statusReading.setOnClickListener { select(ReadingStatus.READING) }
        binding.statusRead.setOnClickListener { select(ReadingStatus.READ) }
        render()
    }

    fun setOnStatusChangeListener(listener: (ReadingStatus) -> Unit) {
        this.listener = listener
    }

    fun setStatus(status: ReadingStatus) {
        current = status
        render()
    }

    fun getStatus(): ReadingStatus = current

    private fun select(status: ReadingStatus) {
        if (current == status) return
        current = status
        render()
        listener?.invoke(status)
    }

    private fun render() {
        style(binding.statusUnread, current == ReadingStatus.UNREAD)
        style(binding.statusReading, current == ReadingStatus.READING)
        style(binding.statusRead, current == ReadingStatus.READ)
    }

    private fun style(view: TextView, selected: Boolean) {
        if (selected) {
            view.setBackgroundResource(R.drawable.bg_status_selected)
            view.setTextColor(ContextCompat.getColor(context, R.color.card))
        } else {
            view.background = null
            view.setTextColor(ContextCompat.getColor(context, R.color.card_foreground_70))
        }
    }
}
