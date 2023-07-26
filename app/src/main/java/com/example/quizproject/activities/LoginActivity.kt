package com.example.quizproject.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import com.example.quizproject.R
import com.example.quizproject.activities.user.AdminActivity
import com.example.quizproject.activities.user.QuizQuestionsActivity
import com.example.quizproject.databinding.ActivityLoginBinding
import com.example.quizproject.firestore.FirestoreClass
import com.example.quizproject.models.User
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        binding.btnLogin.setOnClickListener(this)
        binding.tvRegister.setOnClickListener(this)
        binding.tvForgotPassword.setOnClickListener(this)
    }

    // in login screen the clickable components are Login Button, ForgotPassword text and Register Text
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.tv_forgot_password -> {
                    val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
                    startActivity(intent)
                }
                R.id.btn_login -> {
                    logInRegisterUser()
                }
                R.id.tv_register -> {
                    val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    private fun validateLoginDetails(): Boolean {
        return when {
            TextUtils.isEmpty(binding.etEmailLogin.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.errorMessageEmail), true)
                false
            }
            TextUtils.isEmpty(binding.etPasswordLogin.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.errorMessagePassword), true)
                false
            }
            else -> {
                true
            }
        }
    }

    private fun logInRegisterUser() {
        if (validateLoginDetails()) {
            // get the text from editText and trim the space
            val email = binding.etEmailLogin.text.toString().trim() { it <= ' ' }
            val password = binding.etPasswordLogin.text.toString().trim() { it <= ' ' }

            // log-in using FirebaseAuth
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        FirestoreClass().getUserDetails(this@LoginActivity)
                    } else {
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                    }
                }
        }
    }

    fun userLoggedInSuccess(user: User) {
        if (user.admin == 1) {
            // if the user is an admin (admin = 1), start AdminActivity
            val intent = Intent(this@LoginActivity, AdminActivity::class.java)
            startActivity(intent)
        } else {
            // if the user is not an admin (admin = 0), start MainActivity
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
        }
        finish()
    }
}