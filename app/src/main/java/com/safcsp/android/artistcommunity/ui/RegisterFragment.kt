package com.safcsp.android.artistcommunity.ui

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.safcsp.android.artistcommunity.R
import com.safcsp.android.artistcommunity.viewmodel.LoginViewModel


class RegisterFragment : Fragment() {
    lateinit var loginViewModel:LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_register, container, false)
        loginViewModel= ViewModelProvider(this).get(LoginViewModel::class.java)
        val name = view.findViewById<EditText>(R.id.name_et)
        val email = view.findViewById<EditText>(R.id.email_et)
        val password = view.findViewById<EditText>(R.id.password_et)
        val confirmPassword = view.findViewById<EditText>(R.id.confirm_password_et)
        val signUpButton = view.findViewById<Button>(R.id.sign_up_button)
        signUpButton.setOnClickListener {
            if (name.text.isNullOrEmpty()) {
                name.error = "enter name !!"
                name.requestFocus()
                return@setOnClickListener
            }
            if (email.text.isNullOrEmpty()) {
                email.error = "enter email !!"
                email.requestFocus()
                return@setOnClickListener
            }
//            if (!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()) {
//                email.error = "enter valid email !!"
//                email.requestFocus()
//                return@setOnClickListener
//            }
            if (password.text.isNullOrEmpty()) {
                password.error = "enter password !!"
                password.requestFocus()
                return@setOnClickListener
            }
            if (confirmPassword.text.isNullOrEmpty()) {
                confirmPassword.error = "enter password !!"
                confirmPassword.requestFocus()
                return@setOnClickListener
            }
            if (confirmPassword.text.length < 6) {
                confirmPassword.error = "enter  password !!"
                confirmPassword.requestFocus()
                return@setOnClickListener
            }
            if (confirmPassword.text.toString() != password.text.toString()) {
                confirmPassword.error = "enter confirem password !!"
                confirmPassword.requestFocus()
                return@setOnClickListener
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                loginViewModel.register(
                    email.text.toString(),
                    password.text.toString(),
                    name.text.toString()
                ).observe(viewLifecycleOwner, Observer {firebaseUser->
                    if(firebaseUser!=null)
                    getView()?.let { it1 -> Navigation.findNavController(it1).navigate(R.id.action_registerFragment_to_homeFragment) }

                })

            }
        }
        return view
    }
}