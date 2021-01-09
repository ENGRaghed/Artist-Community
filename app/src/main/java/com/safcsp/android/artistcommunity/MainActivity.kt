package com.safcsp.android.artistcommunity

import android.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.safcsp.android.artistcommunity.ui.EditProfile

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigantionView: BottomNavigationView
    lateinit var toggle: ActionBarDrawerToggle
    lateinit var drawerLayout : DrawerLayout
    lateinit var navView : NavigationView
    private var firebaseAuth = FirebaseAuth.getInstance()
    var logoutMutableLiveData = MutableLiveData<Boolean>()
    lateinit var userPhoto:ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottomNavigantionView= findViewById(R.id.bottom_nav)
        navView = findViewById(R.id.navView)
        val navController= findNavController(R.id.fragment)

        bottomNavigantionView.setupWithNavController(navController)
        navView.setupWithNavController(navController)
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
                R.id.editProfile ->findNavController(R.id.fragment).navigate(R.id.editProfile)
                R.id.miItem2 -> Toast.makeText(applicationContext,"Clicked Item 2",Toast.LENGTH_SHORT).show()
                R.id.miItem3 -> logout()

            }
            true
        }
        val header=navView.getHeaderView(0)
        userPhoto=header.findViewById(R.id.userPhoto) as ImageView
        FirebaseAuth.getInstance().currentUser?.let { user ->
            Glide.with(this)
                .load(user.photoUrl)
                .circleCrop()
                .into(userPhoto)
            val userName=header.findViewById<TextView>(R.id.userName)
            userName.setText(user.displayName)
//            edit_text_phone.text = user.phoneNumber
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

    private fun logout() {
        firebaseAuth.signOut()
        logoutMutableLiveData.postValue(true)
            findNavController(R.id.fragment)
                .navigate(R.id.loginFragment)
        }

}