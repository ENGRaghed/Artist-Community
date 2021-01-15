package com.safcsp.android.artistcommunity.ui

import android.app.ProgressDialog.show
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.Navigation
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.safcsp.android.artistcommunity.R




class ChangePassword : Fragment() {


    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_change_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutPassword=view.findViewById<LinearLayout>(R.id.layoutPassword)
        val layoutUpdatePassword=view.findViewById<LinearLayout>(R.id.layoutUpdatePassword)
        val button_authenticate=view.findViewById<Button>(R.id.button_authenticate)
        val edit_text_password=view.findViewById<EditText>(R.id.edit_text_password)
        val progressbar=view.findViewById<ProgressBar>(R.id.progressbar)
        val button_update=view.findViewById<Button>(R.id.button_update)
        val edit_text_new_password=view.findViewById<EditText>(R.id.edit_text_new_password)
        val edit_text_new_password_confirm=view.findViewById<EditText>(R.id.edit_text_new_password_confirm)

        layoutPassword.visibility = View.VISIBLE
        layoutUpdatePassword.visibility = View.GONE

        button_authenticate.setOnClickListener {

            val password = edit_text_password.text.toString().trim()

            if (password.isEmpty()) {
                edit_text_password.error = "Password required"
                edit_text_password.requestFocus()
                return@setOnClickListener
            }


            currentUser?.let { user ->
                val credential = EmailAuthProvider.getCredential(user.email!!, password)
                progressbar.visibility = View.VISIBLE
                user.reauthenticate(credential)
                    .addOnCompleteListener { task ->
                        progressbar.visibility = View.GONE
                        when {
                            task.isSuccessful -> {
                                layoutPassword.visibility = View.GONE
                                layoutUpdatePassword.visibility = View.VISIBLE
                            }
                            task.exception is FirebaseAuthInvalidCredentialsException -> {
                                edit_text_password.error = "Invalid Password"
                                edit_text_password.requestFocus()
                            }
                            else -> Toast.makeText(context,task.exception?.message!!.toString(),Toast.LENGTH_LONG).show()
                        }
                    }
            }

        }

        button_update.setOnClickListener {

            val password = edit_text_new_password.text.toString().trim()

            if(password.isEmpty() || password.length < 6){
                edit_text_new_password.error = "atleast 6 char password required"
                edit_text_new_password.requestFocus()
                return@setOnClickListener
            }

            if(password != edit_text_new_password_confirm.text.toString().trim()){
                edit_text_new_password_confirm.error = "password did not match"
                edit_text_new_password_confirm.requestFocus()
                return@setOnClickListener
            }

            currentUser?.let{ user ->
                progressbar.visibility = View.VISIBLE
                user.updatePassword(password)
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful){
//                            val action = UpdatePasswordFragmentDirections.actionPasswordUpdated()
//                            Navigation.findNavController(it).navigate(action)
                            getView()?.let { it1 -> Navigation.findNavController(it1).navigate(R.id.changePassword) }
                            Toast.makeText(context,"Password Updated",Toast.LENGTH_LONG).show()

                        }else{
                            Toast.makeText(context,task.exception?.message!!,Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
    }

}