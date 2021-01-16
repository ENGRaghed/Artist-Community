package com.safcsp.android.artistcommunity.ui

import android.content.DialogInterface
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseUser
import com.safcsp.android.artistcommunity.R
import com.safcsp.android.artistcommunity.viewmodel.LoginViewModel
import org.w3c.dom.Text


class LoginFragment : Fragment() {

    lateinit var email: EditText
    lateinit var password: EditText
    lateinit var loginButton: Button
    lateinit var registerButton: TextView
    lateinit var loginViewModel:LoginViewModel
    lateinit var forgetPassword:TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view= inflater.inflate(R.layout.fragment_login, container, false)

        if((requireActivity() as AppCompatActivity).supportActionBar!=null){
            (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }
        forgetPassword=view.findViewById(R.id.forgetpassword)
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
        registerButton =view.findViewById(R.id.registerbtn) as TextView

        registerButton.setOnClickListener {

            getView()?.let { it1 -> Navigation.findNavController(it1)
                .navigate(R.id.action_loginFragment_to_registerFragment) }
        }
        forgetPassword=view.findViewById(R.id.forgetpassword) as TextView

        forgetPassword.setOnClickListener {

                forgetPassword(email)

        }
        loginButton.setOnClickListener {
            val emailedt = email.text.toString().trim()
            val passwordedt = password.text.toString()

            if (emailedt.length > 0 && passwordedt.length > 0) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    loginViewModel.login(emailedt, passwordedt)
                }
            }
        }

        return view
    }
private fun forgetPassword(email:EditText){
    if(email.text.toString().isEmpty()){
        email.error = "ادخل البريد الالكتروني"
        email.requestFocus()

        return
    }
    if(!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()){
        return
    }
    loginViewModel.appRepository.firebaseAuth
        .sendPasswordResetEmail(email.text.toString().trim()).addOnCompleteListener {task->
        if(task.isSuccessful){
            Toast.makeText(context,"تم ارسال رسالة",Toast.LENGTH_LONG).show()
        }

    }
}

}