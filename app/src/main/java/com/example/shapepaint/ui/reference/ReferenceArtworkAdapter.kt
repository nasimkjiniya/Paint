package com.example.shapepaint.ui.reference

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shapepaint.R
import com.example.shapepaint.databinding.ItemReferenceArtworkBinding
import com.example.shapepaint.model.ReferenceArtwork

class ReferenceArtworkAdapter(
    private val onImportClick: (ReferenceArtwork) -> Unit
) : ListAdapter<ReferenceArtwork, ReferenceArtworkAdapter.ReferenceArtworkViewHolder>(DiffCallback) {

    private var importInFlightId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReferenceArtworkViewHolder {
        val binding = ItemReferenceArtworkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReferenceArtworkViewHolder(binding, onImportClick)
    }

    override fun onBindViewHolder(holder: ReferenceArtworkViewHolder, position: Int) {
        holder.bind(getItem(position), getItem(position).objectId == importInFlightId)
    }

    fun updateImportInFlightId(value: String?) {
        importInFlightId = value
        notifyDataSetChanged()
    }

    class ReferenceArtworkViewHolder(
        private val binding: ItemReferenceArtworkBinding,
        private val onImportClick: (ReferenceArtwork) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ReferenceArtwork, importing: Boolean) {
            binding.referenceTitle.text = item.title
            binding.referenceMeta.text = binding.root.context.getString(
                R.string.reference_meta,
                item.artistName,
                item.objectDate
            )
            binding.importReferenceButton.text = binding.root.context.getString(
                if (importing) R.string.reference_importing else R.string.reference_use
            )
            binding.importReferenceButton.isEnabled = !importing
            binding.importReferenceButton.setOnClickListener { onImportClick(item) }
            Glide.with(binding.referenceImage)
                .load(item.thumbnailUrl)
                .placeholder(R.drawable.bg_canvas)
                .error(R.drawable.bg_canvas)
                .centerCrop()
                .into(binding.referenceImage)
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<ReferenceArtwork>() {
        override fun areItemsTheSame(oldItem: ReferenceArtwork, newItem: ReferenceArtwork): Boolean {
            return oldItem.objectId == newItem.objectId
        }

        override fun areContentsTheSame(oldItem: ReferenceArtwork, newItem: ReferenceArtwork): Boolean {
            return oldItem == newItem
        }
    }
}
