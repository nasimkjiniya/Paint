package com.example.shapepaint.ui.gallery

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.shapepaint.R
import com.example.shapepaint.ShapePaintApplication
import com.example.shapepaint.databinding.FragmentGalleryBinding
import com.example.shapepaint.model.ProjectSummary
import com.example.shapepaint.ui.common.GalleryViewModelFactory
import kotlinx.coroutines.launch

class GalleryFragment : Fragment() {
    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GalleryViewModel by viewModels {
        val container = (requireActivity().application as ShapePaintApplication).appContainer
        GalleryViewModelFactory(projectRepository = container.projectRepository)
    }

    private val projectAdapter = ProjectAdapter(::openProject, ::showProjectActions)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.savedRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.savedRecyclerView.adapter = projectAdapter
        binding.savedRecyclerView.isNestedScrollingEnabled = false

        binding.newProjectButton.setOnClickListener {
            showProjectNameDialog()
        }
        binding.settingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_galleryFragment_to_settingsFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: GalleryUiState) {
        projectAdapter.submitList(state.projects)
        binding.savedEmptyView.visibility = if (state.projects.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun openProject(project: ProjectSummary) {
        navigateToEditor(project.projectId)
    }

    private fun showProjectActions(project: ProjectSummary) {
        val options = arrayOf(
            getString(R.string.open_project_option),
            getString(R.string.delete_project_option)
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(project.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openProject(project)
                    1 -> confirmDeleteProject(project)
                }
            }
            .show()
    }

    private fun confirmDeleteProject(project: ProjectSummary) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_project_title)
            .setMessage(getString(R.string.delete_project_message, project.title))
            .setNegativeButton(R.string.no_label, null)
            .setPositiveButton(R.string.yes_label) { _, _ ->
                viewModel.deleteProject(project.projectId)
                Toast.makeText(requireContext(), R.string.project_deleted, Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showProjectNameDialog(defaultName: String = "") {
        val maxTitleLength = resources.getInteger(R.integer.project_title_max_length)
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            filters = arrayOf(InputFilter.LengthFilter(maxTitleLength))
            hint = getString(R.string.project_name_hint)
            setText(defaultName.take(maxTitleLength))
            setSelection(text.length)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.project_name_title)
            .setMessage(R.string.project_name_message)
            .setView(input)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.continue_label) { _, _ ->
                lifecycleScope.launch {
                    val name = input.text?.toString().orEmpty().take(maxTitleLength)
                    navigateToEditor(viewModel.createProject(name))
                }
            }
            .show()
    }

    private fun navigateToEditor(projectId: Long) {
        val args = Bundle().apply { putLong("projectId", projectId) }
        findNavController().navigate(R.id.action_galleryFragment_to_editorFragment, args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
