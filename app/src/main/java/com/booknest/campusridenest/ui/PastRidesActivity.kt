package com.booknest.campusridenest.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.booknest.campusridenest.R
import com.booknest.campusridenest.ui.posts.PostDetailActivity
import com.booknest.campusridenest.ui.posts.PostUi
import com.booknest.campusridenest.ui.posts.PostsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import java.io.Serializable

class PastRidesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var adapter: PostsAdapter

    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_rides)

        setupToolbar()
        setupRecyclerView()
        loadPastRides()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Past Rides"

        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        emptyState = findViewById(R.id.emptyState)

        adapter = PostsAdapter(
            { post -> openPostDetail(post) },
            { /* edit - disabled for closed posts */ },
            { /* delete */ }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadPastRides() {
        if (currentUserId == null) return

        val pastRides = mutableListOf<PostUi>()

        // Load closed offers - FIXED: collection is "offers"
        db.collection("offers")
            .whereEqualTo("ownerUid", currentUserId)
            .whereEqualTo("status", "closed")
            .get()
            .addOnSuccessListener { offerSnapshots ->
                for (doc in offerSnapshots) {
                    val updatedAtValue: Any = doc.get("updatedAt")
                        ?: doc.get("createdAt")
                        ?: System.currentTimeMillis()

                    val post = PostUi(
                        id = doc.id,
                        type = "offer",
                        ownerUid = doc.getString("ownerUid") ?: "",
                        from = doc.getString("from") ?: "",
                        to = doc.getString("to") ?: "",
                        dateTime = doc.get("dateTime") as? java.io.Serializable,
                        seats = doc.getLong("seats")?.toInt(),
                        updatedAt = updatedAtValue,
                        status = "closed",
                        price = doc.getLong("pricePerSeat")?.toInt()
                    )
                    pastRides.add(post)
                }

                // Load closed requests - FIXED: collection is "requests"
                db.collection("requests")
                    .whereEqualTo("ownerUid", currentUserId)
                    .whereEqualTo("status", "closed")
                    .get()
                    .addOnSuccessListener { requestSnapshots ->
                        for (doc in requestSnapshots) {
                            val updatedAtValue: Any = doc.get("updatedAt")
                                ?: doc.get("createdAt")
                                ?: System.currentTimeMillis()

                            val post = PostUi(
                                id = doc.id,
                                type = "request",
                                ownerUid = doc.getString("ownerUid") ?: "",
                                from = doc.getString("from") ?: "",
                                to = doc.getString("to") ?: "",
                                dateTime = doc.get("dateTime") as? java.io.Serializable,
                                seats = doc.getLong("seats")?.toInt(),
                                updatedAt = updatedAtValue,
                                status = "closed",
                                price = null
                            )
                            pastRides.add(post)
                        }

                        // Update UI
                        if (pastRides.isEmpty()) {
                            emptyState.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        } else {
                            emptyState.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                            adapter.submitList(pastRides.sortedByDescending {
                                (it.updatedAt as? Number)?.toLong() ?: 0L
                            })
                        }
                    }
            }
    }

    private fun openPostDetail(post: PostUi) {
        val intent = Intent(this, PostDetailActivity::class.java).apply {
            putExtra("postId", post.id)
            putExtra("type", post.type)
            putExtra("from", post.from)
            putExtra("to", post.to)
            putExtra("seats", post.seats ?: 0)
            putExtra("ownerUid", post.ownerUid)
            putExtra("status", post.status ?: "closed")
            putExtra("price", post.price ?: 0)
        }
        startActivity(intent)
    }
}