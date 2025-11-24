package com.booknest.campusridenest.ui.posts

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.booknest.campusridenest.R
import com.booknest.campusridenest.ui.LoginActivity
import com.booknest.campusridenest.ui.OfferCreateActivity
import com.booknest.campusridenest.ui.RequestCreateActivity
import com.booknest.campusridenest.ui.profile.ProfileActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PostsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    private val mAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        // Set up toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Campus Ride Nest"

        // Set up drawer
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        // Set up drawer toggle (hamburger icon)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Load user info in drawer header
        loadUserInfo()

        // Load PostsFragment if first time
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PostsFragment())
                .commit()

            // Set default selected item
            navigationView.setCheckedItem(R.id.nav_posts)
        }
    }

    private fun loadUserInfo() {
        val currentUser = mAuth.currentUser ?: return

        val headerView = navigationView.getHeaderView(0)
        val tvUserName: TextView = headerView.findViewById(R.id.tvUserName)
        val tvUserEmail: TextView = headerView.findViewById(R.id.tvUserEmail)

        // Set email
        tvUserEmail.text = currentUser.email

        // Load name from Firestore
        db.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name")
                    tvUserName.text = name?.takeIf { it.isNotEmpty() } ?: "Campus Rider"
                } else {
                    tvUserName.text = "Campus Rider"
                }
            }
            .addOnFailureListener {
                tvUserName.text = "Campus Rider"
            }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_posts -> {
                // Already on posts feed - refresh or do nothing
                Toast.makeText(this, "Posts Feed", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_profile -> {
                // Navigate to profile
                startActivity(Intent(this, ProfileActivity::class.java))
            }
            R.id.nav_my_posts -> {
                // Filter to show only user's posts
                Toast.makeText(this, "My Posts - Coming soon!", Toast.LENGTH_SHORT).show()
                // TODO: Implement filtering in PostsFragment
            }
            R.id.nav_past_rides -> {
                // Past rides
                Toast.makeText(this, "Past Rides - Coming soon!", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_create_offer -> {
                // Create offer
                startActivity(Intent(this, OfferCreateActivity::class.java))
            }
            R.id.nav_create_request -> {
                // Create request
                startActivity(Intent(this, RequestCreateActivity::class.java))
            }
            R.id.nav_settings -> {
                // Settings
                Toast.makeText(this, "Settings - Coming soon!", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_logout -> {
                // Logout
                logout()
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun logout() {
        mAuth.signOut()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}