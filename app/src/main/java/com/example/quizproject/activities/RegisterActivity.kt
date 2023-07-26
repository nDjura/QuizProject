package com.example.quizproject.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.quizproject.R
import com.example.quizproject.databinding.ActivityRegisterBinding
import com.example.quizproject.firestore.FirestoreClass
import com.example.quizproject.models.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class RegisterActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            finish()
        }

        binding.btnRegister.setOnClickListener {
            registerUser()
        }
    }

    // a function to validate the entries of a new user
    private fun validateRegisterDetails(): Boolean {
        return when {
            TextUtils.isEmpty(binding.etNameRegister.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.errorMessageName), true)
                false
            }
            TextUtils.isEmpty(binding.etEmailRegister.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.errorMessageEmail), true)
                false
            }
            TextUtils.isEmpty(binding.etPasswordRegister.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.errorMessagePassword), true)
                false
            }
            TextUtils.isEmpty(
                binding.etConfirmPasswordRegister.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.errorMessageConfirmPassword), true)
                false
            }
            binding.etPasswordRegister.text.toString()
                .trim { it <= ' ' } != binding.etConfirmPasswordRegister.text.toString()
                .trim { it <= ' ' } -> {
                showErrorSnackBar(
                    resources.getString(R.string.errorMessagePasswordAndConfirmPassword),
                    true
                )
                false
            }
            else -> {
                true
            }
        }
    }

    private fun registerUser() {
        if (validateRegisterDetails()) {

            val email: String = binding.etEmailRegister.text.toString().trim { it <= ' ' }
            val password: String = binding.etPasswordRegister.text.toString().trim { it <= ' ' }

            // create an instance and create a register a user with email and password
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // firebase registered user
                        val firebaseUser: FirebaseUser = task.result!!.user!!

                        val user = User(
                            firebaseUser.uid,
                            binding.etNameRegister.text.toString().trim { it <= ' ' },
                            binding.etEmailRegister.text.toString().trim { it <= ' ' }
                        )
                        FirestoreClass().registerUser(this@RegisterActivity, user)
                        finish()
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                    } else {
                        // if the registering is not successful the show error message
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                    }
                })
        }
    }

    fun userRegistrationSuccess() {
            Toast.makeText(
                this@RegisterActivity,
                resources.getString(R.string.registerSuccess),
                Toast.LENGTH_SHORT
            ).show()
        }
}