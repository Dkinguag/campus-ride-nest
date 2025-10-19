package com.booknest.campusridenest.ui.posts

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.booknest.campusridenest.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class PostsAdapter(
    private val onEdit: (PostUi) -> Unit,
    private val onDelete: (PostUi) -> Unit,
    private val onClick: (PostUi) -> Unit
) : ListAdapter<PostUi, PostsAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<PostUi>() {
            override fun areItemsTheSame(oldItem: PostUi, newItem: PostUi) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: PostUi, newItem: PostUi) = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return VH(v, onEdit, onDelete, onClick)  // ← Pass onClick
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(itemView: View,
             private val onEdit: (PostUi) -> Unit,
             private val onDelete: (PostUi) -> Unit,
             private val onClick: (PostUi) -> Unit  // ← NEW
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvRoute: TextView = itemView.findViewById(R.id.tvRoute)
        private val tvMeta: TextView = itemView.findViewById(R.id.tvMeta)
        private val btnOverflow: ImageButton = itemView.findViewById(R.id.btnOverflow)

        fun bind(item: PostUi) {
            tvRoute.text = "${item.from} → ${item.to}"

            val seatsText = item.seats?.let { " • $it seats" } ?: ""

            val metaDate = when (val dt = item.dateTime) {
                is String  -> dt.toLongOrNull()?.toShortDateTime() ?: dt
                is Number  -> dt.toLong().toShortDateTime()
                else       -> dt?.toString() ?: ""
            }

            tvMeta.text = "$metaDate • ${item.type}$seatsText"

            val isMine = Firebase.auth.currentUser?.uid == item.ownerUid
            btnOverflow.visibility = if (isMine) View.VISIBLE else View.INVISIBLE

            itemView.setOnClickListener {
                onClick(item)
            }

            btnOverflow.setOnClickListener { v ->
                val menu = PopupMenu(v.context, v)
                menu.menu.add(0, 1, 0, "Edit")
                menu.menu.add(0, 2, 1, "Delete")
                menu.setOnMenuItemClickListener { mi ->
                    when (mi.itemId) {
                        1 -> onEdit(item)
                        2 -> onDelete(item)
                    }
                    true
                }
                menu.show()
            }
        }
    }
}
