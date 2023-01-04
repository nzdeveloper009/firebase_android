package com.nzdeveloper009.android.firebaseandroid.ui

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.nzdeveloper009.android.firebaseandroid.R
import com.nzdeveloper009.android.firebaseandroid.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity(), OnClickListener {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, SignUpActivity::class.java))
        }
    }

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isUserAlreadySignIn()
        setUpViews()
    }

    private fun isUserAlreadySignIn() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            val intent = Intent(this@SignUpActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setUpViews() {
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        initListeners()
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle(resources.getString(R.string.please_wait))
        progressDialog.setMessage(resources.getString(R.string.creating_account_msg))
        progressDialog.setCancelable(false)
    }

    private fun initListeners() {
        binding.signUpButton.setOnClickListener(this)
        binding.signInButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.signUpButton -> {
                if (isDataValid()) {
                    signUpWithEmailAndPassword()
                }
            }
            R.id.signInButton -> {
                SignInActivity.start(this)

            }
        }
    }

    private fun isDataValid(): Boolean {
        if (binding.nameTxt.text.toString().isEmpty()) {
            binding.nameTxt.requestFocus()
            Toast.makeText(this, resources.getString(R.string.provide_name), Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (binding.passwordTxt.text.toString().isEmpty()) {
            binding.passwordTxt.requestFocus()
            Toast.makeText(this, resources.getString(R.string.provide_password), Toast.LENGTH_SHORT)
                .show()
            return false
        } else if (binding.emailTxt.text.toString().isEmpty()) {
            binding.emailTxt.requestFocus()
            Toast.makeText(this, resources.getString(R.string.provide_email), Toast.LENGTH_SHORT)
                .show()
            return false
        }
        return true
    }

    private fun signUpWithEmailAndPassword() {
        progressDialog.show()
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
            binding.emailTxt.text.toString().trim(),
            binding.passwordTxt.text.toString().trim()
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val map: MutableMap<String, Any> = HashMap()
                map["name"] = binding.nameTxt.text.toString().trim()
                map["email"] = binding.emailTxt.text.toString().trim()
                map["password"] = binding.passwordTxt.text.toString().trim()
                FirebaseDatabase.getInstance()
                    .reference
                    .child("Users")
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .updateChildren(map)
                    .addOnSuccessListener(OnSuccessListener<Void?> { //sent verification email
                        sendVerificationEmail()
                    })
                    .addOnFailureListener(OnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(
                            this@SignUpActivity,
                            e.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    })
                Toast.makeText(
                    this@SignUpActivity,
                    resources.getString(R.string.sign_up_successful),
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                progressDialog.dismiss()
                Toast.makeText(
                    this@SignUpActivity,
                    task.exception.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun sendVerificationEmail() {

        FirebaseAuth.getInstance().currentUser
            ?.sendEmailVerification()
            ?.addOnSuccessListener {
                progressDialog.dismiss()
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(
                    this@SignUpActivity,
                    resources.getString(R.string.email_verification_msg),
                    Toast.LENGTH_SHORT
                ).show()
            }
            ?.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(
                    this@SignUpActivity,
                    resources.getString(R.string.email_not_sent),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

}