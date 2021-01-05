package com.safcsp.android.artistcommunity.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseUser
import com.safcsp.android.artistcommunity.R
import com.safcsp.android.artistcommunity.viewmodel.LoginViewModel


class LoginFragment : Fragment() {
    lateinit var email: EditText
    lateinit var password: EditText
    lateinit var loginButton: Button
    lateinit var registerButton: Button
     lateinit var loginViewModel:LoginViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_login, container, false)
        loginViewModel= ViewModelProvider(this).get(LoginViewModel::class.java)
        loginViewModel.userMutableLiveData.observe(viewLifecycleOwner,Observer<FirebaseUser>(){
           if(it !=null){
               getView()?.let { it1 ->
                   Navigation.findNavController(it1).navigate(R.id.action_loginFragment_to_homeFragment)
               }
           }
        })
        email=view.findViewById(R.id.username) as EditText
        password=view.findViewById(R.id.password) as EditText
        loginButton=view.findViewById(R.id.loginbtn) as Button
        registerButton =view.findViewById(R.id.registerbtn) as Button

        registerButton.setOnClickListener {

            getView()?.let { it1 -> Navigation.findNavController(it1).navigate(R.id.action_loginFragment_to_registerFragment) }
        }
        loginButton.setOnClickListener {
            val emailedt = email.text.toString()
            val passwordedt = password.text.toString()
            if (emailedt.length > 0 && passwordedt.length > 0) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    loginViewModel.login(emailedt, passwordedt)
                }
            }
        }
        return view
    }

}