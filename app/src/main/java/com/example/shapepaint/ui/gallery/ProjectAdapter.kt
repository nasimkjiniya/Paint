package com.example.shapepaint.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shapepaint.R
import com.example.shapepaint.databinding.ItemProjectBinding
import com.example.shapepaint.model.ProjectSummary
import java.text.DateFormat
import java.util.Date

class ProjectAdapter(
    private val onClick: (ProjectSummary) -> Unit,
    private val onLongClick: (ProjectSummary) -> Unit
) : ListAdapter<ProjectSummary, ProjectAdapter.ProjectViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val binding = ItemProjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProjectViewHolder(binding, onClick, onLongClick)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ProjectViewHolder(
        private val binding: ItemProjectBinding,
        private val onClick: (ProjectSummary) -> Unit,
        private val onLongClick: (ProjectSummary) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProjectSummary) {
            binding.projectTitle.text = item.title
            binding.projectMeta.text = binding.root.context.getString(R.string.project_owner, item.artistName)
            binding.projectCount.text = binding.root.context.getString(R.string.project_count, item.shapeCount)
            binding.projectUpdated.text = binding.root.context.getString(
                R.string.last_updated,
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(item.updatedAt))
            )
            binding.root.setOnClickListener { onClick(item) }
            binding.root.setOnLongClickListener {
                onLongClick(item)
                true
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<ProjectSummary>() {
        override fun areItemsTheSame(oldItem: ProjectSummary, newItem: ProjectSummary): Boolean {
            return oldItem.projectId == newItem.projectId
        }

        override fun areContentsTheSame(oldItem: ProjectSummary, newItem: ProjectSummary): Boolean {
            return oldItem == newItem
        }
    }
}
