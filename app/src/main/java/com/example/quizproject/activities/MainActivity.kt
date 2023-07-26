package com.example.quizproject.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.example.quizproject.activities.user.QuizQuestionsActivity
import com.example.quizproject.databinding.ActivityMainBinding
import com.example.quizproject.models.User
import com.example.quizproject.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // How to set fullscreen in Android R?
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        val sharedPreferences = getSharedPreferences(Constants.MYQUIZAPP_PREFERENCES, Context.MODE_PRIVATE)
        val username = sharedPreferences.getString(Constants.LOGGED_IN_NAME, "")!!

        if (username.isNullOrEmpty()) {
            val firestore = FirebaseFirestore.getInstance()
            val userCollection = firestore.collection("users")

            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            if (currentUserId != null) {
                val userDocument = userCollection.document(currentUserId)
                userDocument.get().addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(User::class.java)
                        val usernameFromFirestore = user?.name

                        if (!usernameFromFirestore.isNullOrEmpty()) {
                            sharedPreferences.edit().putString(Constants.LOGGED_IN_NAME, usernameFromFirestore).apply()
                            binding.tvMain.text = usernameFromFirestore
                        }
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(this@MainActivity, "Error retrieving data from Firestore", Toast.LENGTH_SHORT).show()
                    exception.printStackTrace()
                }
            }
        } else {
            binding.tvMain.text = username
        }

        binding.btnStartGame.setOnClickListener {
            val intent = Intent(this@MainActivity, QuizQuestionsActivity::class.java)
            intent.putExtra(Constants.NAME, binding.tvMain.text.toString())
            startActivity(intent)
            finish()
        }
    }
}