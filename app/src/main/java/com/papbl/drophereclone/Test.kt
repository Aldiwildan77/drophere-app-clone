package com.papbl.drophereclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.papbl.drophereclone.fragments.UserProfile

class Test : AppCompatActivity() {

    companion object {
        val userProfile: Fragment = UserProfile()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fl_attach, userProfile, "fragment_name")
                .commit()
        }
    }

}
