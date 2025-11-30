package com.booknest.campusridenest.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import com.booknest.campusridenest.R
import com.booknest.campusridenest.ui.profile.ProfileActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.BuildConfig
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupToolbar()
        setupSettings()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"

        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupSettings() {
        // Edit Profile - pass userId
        findViewById<android.widget.LinearLayout>(R.id.settingEditProfile).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("user_id", FirebaseAuth.getInstance().currentUser?.uid)
            startActivity(intent)
        }

        // Notifications toggle
        val notifSwitch = findViewById<SwitchCompat>(R.id.switchNotifications)
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        notifSwitch.isChecked = prefs.getBoolean("notifications_enabled", true)

        notifSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
        }

        // Version
        val versionText = findViewById<TextView>(R.id.textVersion)
        try {
            versionText.text = "Version ${BuildConfig.VERSION_NAME}"
        } catch (e: Exception) {
            versionText.text = "Version 1.0.0"
        }

        // Logout
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnLogout).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}