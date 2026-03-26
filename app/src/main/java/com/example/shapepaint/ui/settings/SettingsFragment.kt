package com.example.shapepaint.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.shapepaint.R
import com.example.shapepaint.ShapePaintApplication
import com.example.shapepaint.databinding.FragmentSettingsBinding
import com.example.shapepaint.ui.common.SettingsViewModelFactory
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private var suppressListeners = false

    private val viewModel: SettingsViewModel by viewModels {
        val container = (requireActivity().application as ShapePaintApplication).appContainer
        SettingsViewModelFactory(container.settingsRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.showGridSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!suppressListeners) viewModel.updateShowGrid(isChecked)
        }
        binding.showLabelsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!suppressListeners) viewModel.updateShowShapeLabels(isChecked)
        }
        binding.defaultSizeSlider.addOnChangeListener { _, value, _ ->
            if (!suppressListeners) viewModel.updateDefaultSize(value.toInt())
        }
        binding.artistNameInput.doAfterTextChanged { text ->
            if (!suppressListeners) viewModel.updateArtistName(text?.toString().orEmpty())
        }
        binding.saveSettingsButton.setOnClickListener {
            viewModel.save()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch {
                    viewModel.events.collect {
                        Toast.makeText(requireContext(), R.string.settings_saved, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun render(state: SettingsUiState) {
        suppressListeners = true
        if (binding.artistNameInput.text?.toString() != state.artistName) {
            binding.artistNameInput.setText(state.artistName)
            binding.artistNameInput.setSelection(binding.artistNameInput.text?.length ?: 0)
        }
        binding.showGridSwitch.isChecked = state.showGrid
        binding.showLabelsSwitch.isChecked = state.showShapeLabels
        binding.defaultSizeSlider.value = state.defaultSize.toFloat()
        binding.defaultSizeValue.text = state.defaultSize.toString()
        suppressListeners = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
