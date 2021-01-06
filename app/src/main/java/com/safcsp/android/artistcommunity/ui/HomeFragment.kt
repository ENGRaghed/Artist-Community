package com.safcsp.android.artistcommunity.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.safcsp.android.artistcommunity.R
import com.safcsp.android.artistcommunity.viewmodel.LoginViewModel

class HomeFragment : Fragment() {

    lateinit var text: TextView
    lateinit var logout: Button
    val homeViewModel by lazy {
        ViewModelProvider(this).get(LoginViewModel::class.java)
    }

    private var firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)
        logout = view.findViewById(R.id.button2)
        text = view.findViewById(R.id.textView) as TextView
     text.text = firebaseAuth.currentUser?.email.toString()

        logout.setOnClickListener {
            homeViewModel.logout()
            getView()?.let { it1 ->
                Navigation.findNavController(it1)
                    .navigate(R.id.action_homeFragment_to_loginFragment)
            }
        }
        return view

    }
}