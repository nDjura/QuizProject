package com.example.quizproject.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.quizproject.R
import com.example.quizproject.databinding.ActivityQuizQuestionsBinding
import com.example.quizproject.databinding.ActivityResultBinding
import com.example.quizproject.utils.Constants

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private var mUserName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mUserName = intent.getStringExtra(Constants.NAME)
        binding.tvUsername.text = mUserName

        val totalQuestions = intent.getIntExtra(Constants.TOTAL_QUESTIONS, 0)
        val correctAnswer = intent.getIntExtra(Constants.CORRECT_ANSWERS, 0)

        binding.tvScore.text = "Your score is $correctAnswer out of $totalQuestions"

        binding.btnPlayAgain.setOnClickListener {
            val intent = Intent(this@ResultActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}