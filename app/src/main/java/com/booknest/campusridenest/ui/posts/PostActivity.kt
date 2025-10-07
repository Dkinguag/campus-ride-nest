package com.booknest.campusridenest.ui.posts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.booknest.campusridenest.R

class PostsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, PostsFragment())
                .commit()
        }
    }
}
