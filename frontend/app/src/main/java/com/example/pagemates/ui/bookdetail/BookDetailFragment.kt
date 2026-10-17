package com.example.pagemates.ui.bookdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.pagemates.PageMatesApp
import com.example.pagemates.R
import com.example.pagemates.data.model.Book
import com.example.pagemates.databinding.FragmentBookDetailBinding
import com.example.pagemates.ui.ViewModelFactory
import com.example.pagemates.ui.util.bindHeaderTags
import com.example.pagemates.ui.util.bindQuotes
import com.example.pagemates.ui.util.bindRemovableTags
import com.example.pagemates.ui.util.persistReadPermission
import kotlinx.coroutines.launch

class BookDetailFragment : Fragment() {

    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookDetailViewModel by viewModels {
        ViewModelFactory((requireActivity().application as PageMatesApp).container)
    }

    private val bookId: String by lazy { requireArguments().getString(ARG_BOOK_ID).orEmpty() }
    private var updatingUi = false

    private val pickCover = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            requireContext().persistReadPermission(uri)
            viewModel.updateCover(uri.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.load(bookId)

        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        binding.deleteButton.setOnClickListener {
            viewModel.delete { findNavController().navigateUp() }
        }
        binding.changeCoverButton.setOnClickListener {
            pickCover.launch(arrayOf("image/*"))
        }

        binding.minusButton.setOnClickListener {
            val current = viewModel.book.value?.read ?: return@setOnClickListener
            viewModel.setPagesRead(current - 1)
        }
        binding.plusButton.setOnClickListener {
            val current = viewModel.book.value?.read ?: return@setOnClickListener
            viewModel.setPagesRead(current + 1)
        }

        binding.statusPicker.setOnStatusChangeListener(viewModel::setStatus)

        binding.pagesReadInput.doAfterTextChanged {
            if (updatingUi) return@doAfterTextChanged
            viewModel.setPagesRead(it?.toString()?.toIntOrNull() ?: 0)
        }

        binding.pagesSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) viewModel.setPagesRead(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        binding.notesInput.doAfterTextChanged {
            if (updatingUi) return@doAfterTextChanged
            viewModel.updateNotes(it?.toString().orEmpty())
        }

        binding.addTagButton.setOnClickListener {
            viewModel.addTag(binding.tagInput.text?.toString().orEmpty())
            binding.tagInput.text = null
        }
        binding.tagInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.addTag(binding.tagInput.text?.toString().orEmpty())
                binding.tagInput.text = null
                true
            } else false
        }

        binding.addQuoteButton.setOnClickListener {
            viewModel.addQuote(binding.quoteInput.text?.toString().orEmpty())
            binding.quoteInput.text = null
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.book.collect { book ->
                    if (book != null) bindBook(book)
                }
            }
        }
    }

    private fun bindBook(book: Book) {
        updatingUi = true
        binding.coverView.bind(book)
        binding.titleText.text = book.title
        binding.authorText.text = book.author
        binding.pagesOfText.text = getString(R.string.pages_of_format, book.read, book.pages)
        binding.topProgressBar.progress = book.progressPercent
        binding.topProgressPercent.text = getString(R.string.percent_format, book.progressPercent)
        binding.statusPicker.setStatus(book.status)

        binding.pagesSeekBar.max = book.pages
        if (binding.pagesSeekBar.progress != book.read) {
            binding.pagesSeekBar.progress = book.read
        }
        if (!binding.pagesReadInput.isFocused) {
            val text = book.read.toString()
            if (binding.pagesReadInput.text?.toString() != text) {
                binding.pagesReadInput.setText(text)
            }
        }
        if (!binding.notesInput.isFocused && binding.notesInput.text?.toString() != book.notes) {
            binding.notesInput.setText(book.notes)
        }

        binding.headerTags.bindHeaderTags(book.tags)
        binding.tagsChipGroup.bindRemovableTags(book.tags, viewModel::removeTag)
        binding.quotesContainer.bindQuotes(book.quotes, viewModel::removeQuote)
        updatingUi = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_BOOK_ID = "bookId"
    }
}
