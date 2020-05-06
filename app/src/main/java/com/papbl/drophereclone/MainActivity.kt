package com.papbl.drophereclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.papbl.drophereclone.fragments.UserProfile

class MainActivity : AppCompatActivity() {

    companion object {
        val userProfile : UserProfile = UserProfile()
    }

    private lateinit var fragmentManager : FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fragmentManager = supportFragmentManager



    }


}
