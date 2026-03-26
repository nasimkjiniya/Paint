package com.example.shapepaint.ui.export

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.shapepaint.R
import com.example.shapepaint.databinding.ActivityExportBinding

class ExportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { getString(R.string.untitled_project) }
        val artist = intent.getStringExtra(EXTRA_ARTIST).orEmpty().ifBlank { getString(R.string.default_artist_name) }
        val shapeCount = intent.getIntExtra(EXTRA_SHAPE_COUNT, 0)
        val projectId = intent.getLongExtra(EXTRA_PROJECT_ID, -1)

        binding.exportTitle.text = title
        binding.exportMeta.text = getString(R.string.project_owner, artist)
        binding.exportSummary.text = buildString {
            appendLine(getString(R.string.project_summary))
            appendLine(getString(R.string.export_project_id, projectId))
            appendLine(getString(R.string.export_project_title, title))
            appendLine(getString(R.string.export_project_artist, artist))
            append(getString(R.string.project_count, shapeCount))
        }

        binding.shareButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject))
                .putExtra(Intent.EXTRA_TEXT, binding.exportSummary.text.toString())
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_summary)))
        }

        binding.doneButton.setOnClickListener { finish() }
    }

    companion object {
        const val EXTRA_PROJECT_ID = "extra_project_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ARTIST = "extra_artist"
        const val EXTRA_SHAPE_COUNT = "extra_shape_count"
    }
}
