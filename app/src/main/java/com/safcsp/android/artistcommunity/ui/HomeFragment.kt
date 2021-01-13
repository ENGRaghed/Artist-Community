package com.safcsp.android.artistcommunity.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

    val homeViewModel by lazy {
        ViewModelProvider(this).get(LoginViewModel::class.java)
    }
    private var firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        if((requireActivity() as AppCompatActivity).supportActionBar!=null){
            (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        text = view.findViewById(R.id.textView) as TextView
        text.text = firebaseAuth.currentUser?.email.toString()

        return view
    }
}