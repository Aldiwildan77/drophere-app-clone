package com.papbl.drophereclone

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.papbl.drophereclone.utils.UserCredential

class ManagePageActivity : AppCompatActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener {

    companion object {
        val homeFragment = HomeFragment()
        val profileFragment = ProfileFragment()
    }

    private var active: Fragment = homeFragment

    private val credential = UserCredential()
    private val mFragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_page)
        val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        mFragmentManager.beginTransaction()
            .add(R.id.nav_host_fragment, profileFragment, "profile")
            .hide(profileFragment)
            .commit()
        mFragmentManager.beginTransaction()
            .add(R.id.nav_host_fragment, homeFragment, "home")
            .commit()

        navView.setOnNavigationItemSelectedListener(this)
    }

    override fun onStart() {
        super.onStart()
        credential.setOnBoardingViewed(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_home -> {
                mFragmentManager.beginTransaction()
                    .hide(active)
                    .show(homeFragment)
                    .commit()
                active = homeFragment
                return true
            }
            R.id.menu_item_add -> {
                startActivity(Intent(this, CreatePageActivity::class.java))
            }
            R.id.menu_item_user -> {
                mFragmentManager.beginTransaction()
                    .hide(active)
                    .show(profileFragment)
                    .commit()
                active = profileFragment
                return true
            }
        }
        return false
    }
}
