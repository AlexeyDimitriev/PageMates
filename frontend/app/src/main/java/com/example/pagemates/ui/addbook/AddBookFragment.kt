package com.example.pagemates.ui.addbook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.pagemates.PageMatesApp
import com.example.pagemates.R
import com.example.pagemates.data.model.ReadingStatus
import com.example.pagemates.databinding.FragmentAddBookBinding
import com.example.pagemates.ui.ViewModelFactory
import com.example.pagemates.ui.util.bindQuotes
import com.example.pagemates.ui.util.bindRemovableTags
import com.example.pagemates.ui.util.persistReadPermission
import kotlinx.coroutines.launch

class AddBookFragment : Fragment() {

    private var _binding: FragmentAddBookBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddBookViewModel by viewModels {
        ViewModelFactory((requireActivity().application as PageMatesApp).container)
    }

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
        _binding = FragmentAddBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        binding.coverPicker.setOnClickListener { pickCover.launch(arrayOf("image/*")) }
        binding.saveButton.setOnClickListener { viewModel.save() }

        binding.titleInput.doAfterTextChanged { viewModel.updateTitle(it?.toString().orEmpty()) }
        binding.authorInput.doAfterTextChanged { viewModel.updateAuthor(it?.toString().orEmpty()) }
        binding.pagesInput.doAfterTextChanged { viewModel.updatePages(it?.toString().orEmpty()) }
        binding.pagesReadInput.doAfterTextChanged { viewModel.updatePagesRead(it?.toString().orEmpty()) }
        binding.notesInput.doAfterTextChanged { viewModel.updateNotes(it?.toString().orEmpty()) }

        binding.statusPicker.setOnStatusChangeListener(viewModel::updateStatus)

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
                viewModel.uiState.collect { state ->
                    binding.pagesReadInput.isVisible = state.status == ReadingStatus.READING
                    binding.saveButton.alpha = if (state.canSave) 1f else 0.4f
                    binding.saveButton.isEnabled = state.canSave

                    val hasCover = !state.coverUri.isNullOrBlank()
                    binding.coverPreview.isVisible = hasCover
                    binding.coverPlaceholder.isVisible = !hasCover
                    if (hasCover) binding.coverPreview.load(state.coverUri)

                    binding.tagsChipGroup.bindRemovableTags(state.tags, viewModel::removeTag)
                    binding.quotesContainer.bindQuotes(state.quotes, viewModel::removeQuote)
                    binding.statusPicker.setStatus(state.status)

                    if (state.saved) findNavController().navigateUp()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
