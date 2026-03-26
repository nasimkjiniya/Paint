package com.example.shapepaint.ui.reference

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.shapepaint.R
import com.example.shapepaint.ShapePaintApplication
import com.example.shapepaint.databinding.FragmentReferenceSearchBinding
import com.example.shapepaint.ui.common.ReferenceSearchViewModelFactory
import kotlinx.coroutines.launch

class ReferenceSearchFragment : Fragment() {
    private var _binding: FragmentReferenceSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReferenceSearchViewModel by viewModels {
        val container = (requireActivity().application as ShapePaintApplication).appContainer
        ReferenceSearchViewModelFactory(
            referenceRepository = container.referenceRepository,
            projectRepository = container.projectRepository,
            projectId = arguments?.getLong("projectId")?.takeIf { it > 0L }
        )
    }

    private val adapter = ReferenceArtworkAdapter { artwork ->
        viewModel.importReference(artwork.objectId, artwork.importUrl)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReferenceSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.referenceRecyclerView.adapter = adapter
        binding.searchButton.setOnClickListener { viewModel.search() }
        binding.referenceQueryInput.doAfterTextChanged {
            viewModel.updateQuery(it?.toString().orEmpty())
        }
        binding.referenceQueryInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.search()
                true
            } else {
                false
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            ReferenceSearchEvent.ReferenceImported -> {
                                Toast.makeText(requireContext(), R.string.reference_imported, Toast.LENGTH_SHORT).show()
                                findNavController().popBackStack()
                            }

                            ReferenceSearchEvent.ImportFailed -> {
                                Toast.makeText(requireContext(), R.string.reference_import_failed, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun render(state: ReferenceSearchUiState) {
        if (binding.referenceQueryInput.text?.toString() != state.query) {
            binding.referenceQueryInput.setText(state.query)
            binding.referenceQueryInput.setSelection(binding.referenceQueryInput.text?.length ?: 0)
        }
        adapter.submitList(state.results)
        adapter.updateImportInFlightId(state.importInFlightId)
        binding.referenceProgress.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        binding.referenceRecyclerView.visibility = if (state.results.isNotEmpty()) View.VISIBLE else View.GONE
        binding.referenceEmptyView.visibility =
            if (!state.isLoading && state.results.isEmpty() && state.emptyMessageRes != null) View.VISIBLE else View.GONE
        state.emptyMessageRes?.let { binding.referenceEmptyView.setText(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
