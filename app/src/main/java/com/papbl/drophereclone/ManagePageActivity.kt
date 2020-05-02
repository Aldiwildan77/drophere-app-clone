package com.papbl.drophereclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class ManagePageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_page)

        val navView : BottomNavigationView = findViewById(R.id.bottom_navigation)
        val navController = findNavController(R.id.nav_host_fragment)

        val appConfiguration = AppBarConfiguration.Builder(R.id.menu_item_home, R.id.menu_item_add, R.id.menu_item_user).build()

//        setupActionBarWithNavController(navController, appConfiguration)
        navView.setupWithNavController(navController)
    }
}
