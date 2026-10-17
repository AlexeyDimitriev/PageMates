package com.example.pagemates.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.transform.CircleCropTransformation
import com.example.pagemates.PageMatesApp
import com.example.pagemates.R
import com.example.pagemates.databinding.FragmentProfileBinding
import com.example.pagemates.ui.ViewModelFactory
import com.example.pagemates.ui.util.persistReadPermission
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ViewModelFactory((requireActivity().application as PageMatesApp).container)
    }

    private val adapter = BooksAdapter { book ->
        findNavController().navigate(
            R.id.action_profile_to_bookDetail,
            bundleOf("bookId" to book.id)
        )
    }

    private val pickAvatar = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            requireContext().persistReadPermission(uri)
            viewModel.updateAvatar(uri.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.booksRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.booksRecycler.adapter = adapter

        binding.addBookFab.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_addBook)
        }

        binding.editToggleButton.setOnClickListener {
            val state = viewModel.uiState.value
            if (state.editing) {
                viewModel.saveProfile(
                    binding.nameInput.text?.toString().orEmpty(),
                    binding.bioInput.text?.toString().orEmpty(),
                    null
                )
            } else {
                viewModel.toggleEditing()
            }
        }

        binding.changeAvatarButton.setOnClickListener {
            pickAvatar.launch(arrayOf("image/*"))
        }

        binding.filterAll.setOnClickListener { viewModel.setFilter(BookFilter.ALL) }
        binding.filterReading.setOnClickListener { viewModel.setFilter(BookFilter.READING) }
        binding.filterUnread.setOnClickListener { viewModel.setFilter(BookFilter.UNREAD) }
        binding.filterRead.setOnClickListener { viewModel.setFilter(BookFilter.READ) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.avatarImage.load(state.profile.avatarUri) {
                        transformations(CircleCropTransformation())
                        crossfade(true)
                    }
                    binding.nameText.text = state.profile.name
                    binding.bioText.text = state.profile.bio
                    binding.booksCountText.text = state.booksCount.toString()
                    binding.finishedCountText.text = state.finishedCount.toString()
                    binding.pagesReadCountText.text = "%,d".format(state.pagesReadCount)

                    binding.editFields.isVisible = state.editing
                    binding.profileReadOnly.isVisible = !state.editing
                    binding.changeAvatarButton.isVisible = state.editing
                    binding.editToggleIcon.setImageResource(
                        if (state.editing) R.drawable.ic_check else R.drawable.ic_pencil
                    )
                    binding.editToggleText.setText(if (state.editing) R.string.done else R.string.edit)

                    if (state.editing) {
                        if (binding.nameInput.text?.toString() != state.profile.name) {
                            binding.nameInput.setText(state.profile.name)
                        }
                        if (binding.bioInput.text?.toString() != state.profile.bio) {
                            binding.bioInput.setText(state.profile.bio)
                        }
                    }

                    styleFilter(binding.filterAll, state.filter == BookFilter.ALL)
                    styleFilter(binding.filterReading, state.filter == BookFilter.READING)
                    styleFilter(binding.filterUnread, state.filter == BookFilter.UNREAD)
                    styleFilter(binding.filterRead, state.filter == BookFilter.READ)

                    adapter.submitList(state.filteredBooks)
                    binding.emptyText.isVisible = state.filteredBooks.isEmpty()
                    binding.booksRecycler.isVisible = state.filteredBooks.isNotEmpty()
                }
            }
        }
    }

    private fun styleFilter(view: TextView, selected: Boolean) {
        if (selected) {
            view.setBackgroundResource(R.drawable.bg_chip_selected)
            view.setTextColor(ContextCompat.getColor(requireContext(), R.color.background))
        } else {
            view.setBackgroundResource(R.drawable.bg_chip_outline)
            view.setTextColor(ContextCompat.getColor(requireContext(), R.color.foreground))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
