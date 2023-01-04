package com.nzdeveloper009.android.firebaseandroid.ui

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.auth.FirebaseAuth
import com.nzdeveloper009.android.firebaseandroid.R
import com.nzdeveloper009.android.firebaseandroid.databinding.ActivitySignInBinding


class SignInActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, SignInActivity::class.java))
        }
    }

    private lateinit var binding: ActivitySignInBinding
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpViews()
    }

    private fun setUpViews() {
        binding = ActivitySignInBinding.inflate(layoutInflater)
        initListener();
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle(resources.getString(R.string.please_wait))
        progressDialog.setMessage(resources.getString(R.string.logging_account))
        progressDialog.setCancelable(false)
    }

    private fun initListener() {
        binding.signInButton.setOnClickListener(this);
        binding.signUpButton.setOnClickListener(this);
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.signInButton -> {
                if (isDataValid()) {
                    doSignInWithEmailAndPassword();
                }
            }
            R.id.signUpButton -> {
                SignUpActivity.start(this)
            }
        }
    }

    private fun isDataValid(): Boolean {
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

    private fun doSignInWithEmailAndPassword() {
        progressDialog.show()
        FirebaseAuth.getInstance().signInWithEmailAndPassword(
            binding.emailTxt.text.toString().trim(),
            binding.passwordTxt.text.toString().trim()
        ).addOnCompleteListener { task ->
            progressDialog.dismiss()
            if (task.isSuccessful) {
                if (FirebaseAuth.getInstance().currentUser!!.isEmailVerified) {
                    Toast.makeText(
                        this@SignInActivity,
                        resources.getString(R.string.sign_in_successful),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    MainActivity.start(this)
                } else {
                    Toast.makeText(
                        this@SignInActivity,
                        resources.getString(R.string.verify_email_first),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this@SignInActivity, task.exception.toString(), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun sendVerificationEmail() {
        FirebaseAuth.getInstance().currentUser
            ?.sendEmailVerification()
            ?.addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(
                    this@SignInActivity,
                    resources.getString(R.string.email_verification_msg),
                    Toast.LENGTH_SHORT
                ).show()
            }
            ?.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(
                    this@SignInActivity,
                    resources.getString(R.string.email_not_sent),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}