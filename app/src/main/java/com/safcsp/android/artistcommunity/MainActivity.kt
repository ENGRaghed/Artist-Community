package com.safcsp.android.artistcommunity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigantionView: BottomNavigationView
    lateinit var toggle: ActionBarDrawerToggle
    lateinit var drawerLayout : DrawerLayout
    lateinit var navView : NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigantionView= findViewById(R.id.bottom_nav)
        navView = findViewById(R.id.navView)
        val navController= findNavController(R.id.fragment)
        bottomNavigantionView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment -> hideBottomNav()
                R.id.registerFragment -> hideBottomNav()
                else -> showBottomNav()
            }
        }

        //Slidable Menu

        drawerLayout = findViewById(R.id.drawerLayout)
        toggle = ActionBarDrawerToggle(this, drawerLayout ,R.string.open,R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.miItem1 -> Toast.makeText(applicationContext,"Clicked Item 1",Toast.LENGTH_SHORT).show()
                R.id.miItem2 -> Toast.makeText(applicationContext,"Clicked Item 2",Toast.LENGTH_SHORT).show()
                R.id.miItem3 -> Toast.makeText(applicationContext,"Clicked Item 3",Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showBottomNav() {
        bottomNavigantionView.visibility = View.VISIBLE
    }

    private fun hideBottomNav() {
        bottomNavigantionView.visibility = View.GONE
    }
}