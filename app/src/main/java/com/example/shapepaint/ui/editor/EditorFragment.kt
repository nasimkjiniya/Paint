package com.example.shapepaint.ui.editor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.activity.addCallback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.shapepaint.R
import com.example.shapepaint.ShapePaintApplication
import com.example.shapepaint.databinding.FragmentEditorBinding
import com.example.shapepaint.model.PaintColor
import com.example.shapepaint.model.ShapeType
import com.example.shapepaint.ui.common.EditorViewModelFactory
import com.example.shapepaint.ui.export.ExportActivity
import kotlinx.coroutines.launch

class EditorFragment : Fragment() {
    private var _binding: FragmentEditorBinding? = null
    private val binding get() = _binding!!
    private var latestState = EditorUiState()
    private var navigateBackAfterSave = false

    private val cameraPreview = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let { viewModel.importBackground(it) }
    }

    private val cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            cameraPreview.launch(null)
        } else {
            Toast.makeText(requireContext(), R.string.camera_permission_required, Toast.LENGTH_SHORT).show()
        }
    }

    private val viewModel: EditorViewModel by viewModels {
        val container = (requireActivity().application as ShapePaintApplication).appContainer
        val projectId = arguments?.getLong("projectId")?.takeIf { it > 0L }
        EditorViewModelFactory(
            projectRepository = container.projectRepository,
            settingsRepository = container.settingsRepository,
            projectId = projectId
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            handleBackNavigation()
        }
        requireActivity().findViewById<Toolbar>(R.id.toolbar)?.setNavigationOnClickListener {
            handleBackNavigation()
        }
        binding.drawingCanvas.onCanvasTap = { x, y -> viewModel.addShapeAt(x, y) }
        binding.drawingCanvas.onStrokeFinished = viewModel::addStroke
        binding.drawingCanvas.onEraseAt = viewModel::eraseAt
        binding.toolsToggleButton.setOnClickListener { viewModel.toggleTools() }
        binding.overflowButton.setOnClickListener { showOverflowMenu() }
        binding.undoButton.setOnClickListener { viewModel.undoLastShape() }
        binding.clearButton.setOnClickListener { viewModel.clearCanvas() }
        binding.exportButton.setOnClickListener { viewModel.requestExport() }
        binding.cameraButton.setOnClickListener { requestCameraCapture() }
        binding.referenceButton.setOnClickListener { openReferenceSearch() }

        binding.shapeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val shape = when (checkedId) {
                R.id.shapeSquareButton -> ShapeType.SQUARE
                R.id.shapeRectangleButton -> ShapeType.RECTANGLE
                R.id.shapeCircleButton -> ShapeType.CIRCLE
                R.id.shapeOvalButton -> ShapeType.OVAL
                R.id.shapeTriangleButton -> ShapeType.TRIANGLE
                R.id.shapeFreehandButton -> ShapeType.FREEHAND
                else -> ShapeType.ERASER
            }
            viewModel.selectShape(shape)
        }

        binding.colorGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val color = when (checkedId) {
                R.id.colorGreenButton -> PaintColor.GREEN
                R.id.colorBlueButton -> PaintColor.BLUE
                R.id.colorOrangeButton -> PaintColor.ORANGE
                R.id.colorPurpleButton -> PaintColor.PURPLE
                R.id.colorCharcoalButton -> PaintColor.CHARCOAL
                else -> PaintColor.RED
            }
            viewModel.selectColor(color)
        }

        binding.sizeSlider.addOnChangeListener { _, value, _ -> viewModel.updateUniformSize(value) }
        binding.widthSlider.addOnChangeListener { _, value, _ -> viewModel.updateWidth(value) }
        binding.heightSlider.addOnChangeListener { _, value, _ -> viewModel.updateHeight(value) }
        binding.strokeSlider.addOnChangeListener { _, value, _ -> viewModel.updateStrokeWidth(value) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is EditorEvent.ProjectSaved -> {
                                Toast.makeText(requireContext(), R.string.project_saved, Toast.LENGTH_SHORT).show()
                                if (navigateBackAfterSave) {
                                    navigateBackAfterSave = false
                                    findNavController().popBackStack()
                                }
                            }

                            is EditorEvent.ExportRequested -> {
                                val intent = Intent(requireContext(), ExportActivity::class.java)
                                    .putExtra(ExportActivity.EXTRA_PROJECT_ID, event.projectId)
                                    .putExtra(ExportActivity.EXTRA_TITLE, latestState.title)
                                    .putExtra(ExportActivity.EXTRA_ARTIST, latestState.artistName)
                                    .putExtra(
                                        ExportActivity.EXTRA_SHAPE_COUNT,
                                        latestState.shapes.size + latestState.strokes.size
                                    )
                                startActivity(intent)
                            }

                            EditorEvent.BackgroundImported -> {
                                Toast.makeText(requireContext(), R.string.camera_imported, Toast.LENGTH_SHORT).show()
                            }

                            EditorEvent.ProjectDeleted -> {
                                Toast.makeText(requireContext(), R.string.project_deleted, Toast.LENGTH_SHORT).show()
                                findNavController().popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun confirmDeleteProject() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_project_title)
            .setMessage(getString(R.string.delete_project_message, latestState.title.ifBlank {
                getString(R.string.untitled_project)
            }))
            .setNegativeButton(R.string.no_label, null)
            .setPositiveButton(R.string.yes_label) { _, _ ->
                viewModel.deleteProject()
            }
            .show()
    }

    private fun requestCameraCapture() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                cameraPreview.launch(null)
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Toast.makeText(requireContext(), R.string.camera_permission_required, Toast.LENGTH_SHORT).show()
                cameraPermission.launch(Manifest.permission.CAMERA)
            }

            else -> cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun showOverflowMenu() {
        PopupMenu(requireContext(), binding.overflowButton).apply {
            menuInflater.inflate(R.menu.menu_editor_overflow, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_save_project -> {
                        viewModel.saveProject()
                        true
                    }

                    R.id.action_rename_project -> {
                        showRenameProjectDialog()
                        true
                    }

                    R.id.action_delete_project -> {
                        confirmDeleteProject()
                        true
                    }

                    else -> false
                }
            }
        }.show()
    }

    fun onNavigateUpRequested(): Boolean {
        handleBackNavigation()
        return true
    }

    private fun handleBackNavigation() {
        if (!viewModel.uiState.value.hasUnsavedChanges) {
            findNavController().popBackStack()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.unsaved_changes_title)
            .setMessage(R.string.unsaved_changes_message)
            .setNegativeButton(android.R.string.cancel, null)
            .setNeutralButton(R.string.discard_changes) { _, _ ->
                navigateBackAfterSave = false
                findNavController().popBackStack()
            }
            .setPositiveButton(R.string.save) { _, _ ->
                navigateBackAfterSave = true
                viewModel.saveProject()
            }
            .show()
    }

    private fun showRenameProjectDialog() {
        val maxTitleLength = resources.getInteger(R.integer.project_title_max_length)
        val currentTitle = latestState.title.ifBlank { "" }
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            filters = arrayOf(InputFilter.LengthFilter(maxTitleLength))
            hint = getString(R.string.project_name_hint)
            setText(currentTitle.take(maxTitleLength))
            setSelection(text.length)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.rename_project_title)
            .setMessage(R.string.rename_project_message)
            .setView(input)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = input.text?.toString().orEmpty().take(maxTitleLength)
                viewModel.updateTitle(name)
            }
            .show()
    }

    private fun openReferenceSearch() {
        val projectId = latestState.projectId ?: return
        val args = Bundle().apply { putLong("projectId", projectId) }
        findNavController().navigate(R.id.action_editorFragment_to_referenceSearchFragment, args)
    }

    private fun render(state: EditorUiState) {
        latestState = state
        binding.editorSubtitle.text = if (state.artistName.isBlank()) {
            getString(R.string.tap_hint)
        } else {
            getString(R.string.project_owner, state.artistName)
        }
        val headerTitle = state.title.ifBlank { getString(R.string.untitled_project) }
        if (binding.editorTitle.text?.toString() != headerTitle) {
            binding.editorTitle.text = headerTitle
        }
        binding.sizeValue.text = state.uniformSize.toInt().toString()
        binding.widthValue.text = state.width.toInt().toString()
        binding.heightValue.text = state.height.toInt().toString()
        binding.emptyCanvasLabel.visibility = if (
            state.shapes.isEmpty() && state.strokes.isEmpty() && state.backgroundImagePath == null
        ) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.drawingCanvas.render(
            shapes = state.shapes,
            strokes = state.strokes,
            showGrid = state.showGrid,
            showLabels = state.showLabels,
            backgroundImagePath = state.backgroundImagePath,
            freehandEnabled = state.selectedShape == ShapeType.FREEHAND,
            eraserEnabled = state.selectedShape == ShapeType.ERASER,
            selectedColor = state.selectedColor.colorInt,
            selectedStrokeWidth = state.strokeWidth
        )
        binding.shapeCountChip.text = getString(R.string.project_count, state.shapes.size + state.strokes.size)
        binding.toolsToggleButton.text = getString(
            if (state.toolsExpanded) R.string.collapse_tools else R.string.expand_tools
        )
        if (state.toolsExpanded) {
            binding.motionRoot.transitionToStart()
        } else {
            binding.motionRoot.transitionToEnd()
        }

        val selectedShapeButton = when (state.selectedShape) {
            ShapeType.SQUARE -> R.id.shapeSquareButton
            ShapeType.RECTANGLE -> R.id.shapeRectangleButton
            ShapeType.CIRCLE -> R.id.shapeCircleButton
            ShapeType.OVAL -> R.id.shapeOvalButton
            ShapeType.TRIANGLE -> R.id.shapeTriangleButton
            ShapeType.FREEHAND -> R.id.shapeFreehandButton
            ShapeType.ERASER -> R.id.shapeEraserButton
        }
        if (binding.shapeGroup.checkedButtonId != selectedShapeButton) {
            binding.shapeGroup.check(selectedShapeButton)
        }

        val selectedColorButton = when (state.selectedColor) {
            PaintColor.RED -> R.id.colorRedButton
            PaintColor.GREEN -> R.id.colorGreenButton
            PaintColor.BLUE -> R.id.colorBlueButton
            PaintColor.ORANGE -> R.id.colorOrangeButton
            PaintColor.PURPLE -> R.id.colorPurpleButton
            PaintColor.CHARCOAL -> R.id.colorCharcoalButton
        }
        if (binding.colorGroup.checkedButtonId != selectedColorButton) {
            binding.colorGroup.check(selectedColorButton)
        }

        if (binding.sizeSlider.value != state.uniformSize) {
            binding.sizeSlider.value = state.uniformSize
        }
        if (binding.widthSlider.value != state.width) {
            binding.widthSlider.value = state.width
        }
        if (binding.heightSlider.value != state.height) {
            binding.heightSlider.value = state.height
        }
        if (binding.strokeSlider.value != state.strokeWidth) {
            binding.strokeSlider.value = state.strokeWidth
        }

        val isFreehand = state.selectedShape == ShapeType.FREEHAND
        val isEraser = state.selectedShape == ShapeType.ERASER
        val isBrushTool = isFreehand || isEraser
        val lockAspect = state.selectedShape.lockAspectRatio
        binding.sizeLabel.visibility = if (!isBrushTool && lockAspect) View.VISIBLE else View.GONE
        binding.sizeSlider.visibility = if (!isBrushTool && lockAspect) View.VISIBLE else View.GONE
        binding.sizeValue.visibility = if (!isBrushTool && lockAspect) View.VISIBLE else View.GONE
        binding.widthLabel.visibility = if (!isBrushTool && !lockAspect) View.VISIBLE else View.GONE
        binding.widthSlider.visibility = if (!isBrushTool && !lockAspect) View.VISIBLE else View.GONE
        binding.widthValue.visibility = if (!isBrushTool && !lockAspect) View.VISIBLE else View.GONE
        binding.heightLabel.visibility = if (!isBrushTool && !lockAspect) View.VISIBLE else View.GONE
        binding.heightSlider.visibility = if (!isBrushTool && !lockAspect) View.VISIBLE else View.GONE
        binding.heightValue.visibility = if (!isBrushTool && !lockAspect) View.VISIBLE else View.GONE
        binding.strokeLabel.visibility = if (isBrushTool) View.VISIBLE else View.GONE
        binding.strokeSlider.visibility = if (isBrushTool) View.VISIBLE else View.GONE
        binding.strokeValue.visibility = if (isBrushTool) View.VISIBLE else View.GONE
        binding.strokeValue.text = state.strokeWidth.toInt().toString()
    }

    override fun onDestroyView() {
        requireActivity().findViewById<Toolbar>(R.id.toolbar)?.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        super.onDestroyView()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
    }
}
