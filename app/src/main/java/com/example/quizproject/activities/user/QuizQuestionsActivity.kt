package com.example.quizproject.activities.user

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.quizproject.R
import com.example.quizproject.activities.ResultActivity
import com.example.quizproject.databinding.ActivityQuizQuestionsBinding
import com.example.quizproject.models.Questions
import com.example.quizproject.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class QuizQuestionsActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityQuizQuestionsBinding

    private val mHandler = Handler()
    private val mFirestore = FirebaseFirestore.getInstance()
    private var mSelectedOptionPosition: Int = 0
    private var mCurrentPosition: Int = 1
    private var mQuestionsList: ArrayList<Questions>? = null
    private var mCorrectAnswer: Int = 0

    private var mUserName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizQuestionsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mUserName = intent.getStringExtra(Constants.NAME)
        setQuestion()

        binding.ivImage

        binding.tvOption1.setOnClickListener(this)
        binding.tvOption2.setOnClickListener(this)
        binding.tvOption3.setOnClickListener(this)
        binding.tvOption4.setOnClickListener(this)
        binding.btnSubmit.setOnClickListener(this)
    }


    private fun setQuestion() {
        val imageView = binding.ivImage

        val collectionRef = mFirestore.collection("questions")
        collectionRef.get().addOnSuccessListener { querySnapshot ->
            mQuestionsList = ArrayList()

            for (document in querySnapshot) {
                val questionText = document.getString("question")
                val option1 = document.getString("option1")
                val option2 = document.getString("option2")
                val option3 = document.getString("option3")
                val option4 = document.getString("option4")

                val imageUrl = document.getString("imageSrc") ?: ""
                val question = Questions(
                    id = document.id,
                    question = questionText ?: "",
                    option1 = option1 ?: "",
                    option2 = option2 ?: "",
                    option3 = option3 ?: "",
                    option4 = option4 ?: "",
                    correctAnswer = document.getLong("correctAnswer")?.toInt() ?: 0,
                    imageSrc = imageUrl,
                    imageFileName = document.getString("imageFileName") ?: ""
                )
                mQuestionsList?.add(question)
            }

            if (mQuestionsList?.isNotEmpty() == true) {

                val question = mQuestionsList!![mCurrentPosition - 1]
                defaultOptionsView()

                if (mCurrentPosition == mQuestionsList!!.size) {
                    binding.btnSubmit.text = "FINISH"
                } else {
                    binding.btnSubmit.text = "SUBMIT"
                }

                binding.progressBar.progress = mCurrentPosition
                binding.tvProgress.text = "$mCurrentPosition" + "/" + binding.progressBar.max

                binding.tvQuestion.text = question.question
                binding.tvOption1.text = question.option1
                binding.tvOption2.text = question.option2
                binding.tvOption3.text = question.option3
                binding.tvOption4.text = question.option4

                val imageUrl = question.imageSrc
                Log.d("TAG", "Image URL: $imageUrl")
                if (imageUrl.isNotEmpty()) {
                    val storage = FirebaseStorage.getInstance()
                    val storageRef = storage.reference.child("images").child(question.imageFileName)

                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        Glide.with(this@QuizQuestionsActivity)
                            .load(uri)
                            .into(imageView)
                    }.addOnFailureListener {
                        Toast.makeText(
                            this@QuizQuestionsActivity,
                            "Failed to load image",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                binding.ivImage.setImageResource(R.drawable.placeholder_image)
            } else {
                Toast.makeText(
                    this,
                    "Handle case when no questions are available",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this@QuizQuestionsActivity, "Failed to load questions", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onClick(v: View) {
        when (v?.id) {
            R.id.tv_option_1 -> {
                selectedOption(binding.tvOption1, 1)
            }
            R.id.tv_option_2 -> {
                selectedOption(binding.tvOption2, 2)
            }
            R.id.tv_option_3 -> {
                selectedOption(binding.tvOption3, 3)
            }
            R.id.tv_option_4 -> {
                selectedOption(binding.tvOption4, 4)
            }
            R.id.btnSubmit -> {
                if (mSelectedOptionPosition == 0) {
                    Toast.makeText(this, "Please choose your answer", Toast.LENGTH_SHORT).show()
                    return
                }

                val question = mQuestionsList?.get(mCurrentPosition - 1)
                if (question!!.correctAnswer != mSelectedOptionPosition) {
                    answerView(mSelectedOptionPosition, R.drawable.wrong_answer_background)
                } else {
                    mCorrectAnswer++
                }
                answerView(question.correctAnswer, R.drawable.correct_answer_background)

                mHandler.postDelayed({
                    mSelectedOptionPosition = 0
                    mCurrentPosition++
                    if (mCurrentPosition <= mQuestionsList!!.size) {
                        setQuestion()
                    } else {
                        val intent = Intent(this, ResultActivity::class.java)
                        intent.putExtra(Constants.TOTAL_QUESTIONS, mQuestionsList!!.size)
                        intent.putExtra(Constants.CORRECT_ANSWERS, mCorrectAnswer)
                        intent.putExtra(Constants.NAME, mUserName)
                        startActivity(intent)
                        finish()
                    }
                }, 700)
            }
        }
    }

    private fun answerView(answer: Int, drawableView: Int) {
        when (answer) {
            1 -> {
                binding.tvOption1.background = ContextCompat.getDrawable(
                    this, drawableView
                )
            }
            2 -> {
                binding.tvOption2.background = ContextCompat.getDrawable(
                    this, drawableView
                )
            }
            3 -> {
                binding.tvOption3.background = ContextCompat.getDrawable(
                    this, drawableView
                )
            }
            4 -> {
                binding.tvOption4.background = ContextCompat.getDrawable(
                    this, drawableView
                )
            }
        }
    }

    private fun defaultOptionsView() {
        val options = ArrayList<TextView>()
        options.add(0, binding.tvOption1)
        options.add(1, binding.tvOption2)
        options.add(2, binding.tvOption3)
        options.add(3, binding.tvOption4)

        for (option in options) {
            option.setTextColor(ContextCompat.getColor(this, R.color.white))
            option.typeface = Typeface.DEFAULT
            option.background = ContextCompat.getDrawable(
                this,
                R.drawable.question_button_background
            )
        }
    }

    private fun selectedOption(tv: TextView, selectedOptionNum: Int) {
        defaultOptionsView()
        mSelectedOptionPosition = selectedOptionNum

        tv.setTextColor(ContextCompat.getColor(this, R.color.white))
        tv.setTypeface(tv.typeface, Typeface.BOLD)
        tv.background = ContextCompat.getDrawable(
            this,
            R.drawable.select_option_border_bg
        )
    }

}