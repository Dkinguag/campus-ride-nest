package com.booknest.campusridenest.ui.posts

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.booknest.campusridenest.R
import com.booknest.campusridenest.model.RideRequest
import com.booknest.campusridenest.ui.MatchedRidesActivity
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
        return VH(v, onEdit, onDelete, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(itemView: View,
             private val onEdit: (PostUi) -> Unit,
             private val onDelete: (PostUi) -> Unit,
             private val onClick: (PostUi) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvRoute: TextView = itemView.findViewById(R.id.tvRoute)
        private val tvMeta: TextView = itemView.findViewById(R.id.tvMeta)
        private val btnOverflow: ImageButton = itemView.findViewById(R.id.btnOverflow)
        private val btnFindMatches: Button = itemView.findViewById(R.id.btnFindMatches)

        fun bind(item: PostUi) {
            tvRoute.text = "${item.from} → ${item.to}"

            val seatsText = item.seats?.let { " • $it seats" } ?: ""

            val metaDate = try {
                val dt = item.dateTime
                if (dt.toString().contains("Timestamp")) {
                    val secondsMatch = Regex("seconds=(\\d+)").find(dt.toString())
                    val seconds = secondsMatch?.groupValues?.get(1)?.toLongOrNull() ?: 0L
                    (seconds * 1000).toShortDateTime()
                } else when (dt) {
                    is Long -> dt.toShortDateTime()
                    is String -> dt.toLongOrNull()?.toShortDateTime() ?: dt
                    else -> ""
                }
            } catch (e: Exception) {
                ""
            }

            tvMeta.text = "$metaDate • ${item.type}$seatsText"

            val isMine = Firebase.auth.currentUser?.uid == item.ownerUid
            btnOverflow.visibility = if (isMine) View.VISIBLE else View.INVISIBLE

            // Show "Find Matches" button only for requests that belong to current user
            val isMyRequest = isMine && item.type == "request"
            btnFindMatches.visibility = if (isMyRequest) View.VISIBLE else View.GONE

            // Handle Find Matches click
            btnFindMatches.setOnClickListener {
                launchMatchingActivity(item)
            }

            // Launch PostDetailActivity with proper data
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, com.booknest.campusridenest.ui.posts.PostDetailActivity::class.java)
                intent.putExtra("type", item.type)
                intent.putExtra("postId", item.id)
                intent.putExtra("from", item.from)
                intent.putExtra("to", item.to)
                intent.putExtra("dateTime", item.timeMillis ?: 0L)
                intent.putExtra("seats", item.seats ?: 0)
                intent.putExtra("price", item.price ?: 0)
                intent.putExtra("ownerUid", item.ownerUid)
                intent.putExtra("status", item.status ?: "open")
                itemView.context.startActivity(intent)
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

        private fun launchMatchingActivity(item: PostUi) {
            val intent = Intent(itemView.context, MatchedRidesActivity::class.java)
            intent.putExtra("request_id", item.id)
            intent.putExtra("from", item.from)
            intent.putExtra("to", item.to)
            intent.putExtra("timeMillis", item.timeMillis ?: 0L)
            intent.putExtra("seats", item.seats ?: 1)
            intent.putExtra("maxBudget", item.maxBudget ?: 0.0)
            intent.putExtra("pickupLat", item.pickupLocation?.latitude ?: 0.0)
            intent.putExtra("pickupLon", item.pickupLocation?.longitude ?: 0.0)
            intent.putExtra("dropoffLat", item.dropoffLocation?.latitude ?: 0.0)
            intent.putExtra("dropoffLon", item.dropoffLocation?.longitude ?: 0.0)
            intent.putExtra("needsNonSmoking", item.needsNonSmoking ?: false)
            intent.putExtra("needsNoPets", item.needsNoPets ?: false)
            intent.putExtra("musicPreference", item.musicPreference)
            intent.putExtra("conversationLevel", item.conversationLevel)
            intent.putExtra("ownerUid", item.ownerUid)
            itemView.context.startActivity(intent)
        }
    }
}