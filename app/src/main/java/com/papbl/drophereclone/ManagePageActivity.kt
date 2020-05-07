package com.papbl.drophereclone

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class ManagePageActivity : AppCompatActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener {

    companion object {
        val homeFragment = HomeFragment()
        val createPageFragment = CreatePageFragment()
        val profileFragment = ProfileFragment()
    }

    private var active: Fragment = homeFragment
    private val mFragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_page)
        val navView : BottomNavigationView = findViewById(R.id.bottom_navigation)

        mFragmentManager.beginTransaction()
            .add(R.id.nav_host_fragment, profileFragment, "profile")
            .hide(profileFragment)
            .commit()
        mFragmentManager.beginTransaction()
            .add(R.id.nav_host_fragment, createPageFragment, "create")
            .hide(createPageFragment)
            .commit()
        mFragmentManager.beginTransaction()
            .add(R.id.nav_host_fragment, homeFragment, "home")
            .commit()
        navView.setOnNavigationItemSelectedListener(this)
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
                mFragmentManager.beginTransaction()
                    .hide(active)
                    .show(createPageFragment)
                    .commit()
                active = createPageFragment
                return true
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
