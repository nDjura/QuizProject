package com.example.quizproject.activities.user

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.quizproject.R
import com.example.quizproject.activities.BaseActivity
import com.example.quizproject.activities.MainActivity
import com.example.quizproject.databinding.ActivityAdminBinding
import com.example.quizproject.models.Questions
import com.example.quizproject.utils.Constants.GALLERY_REQUEST_CODE
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


class AdminActivity : BaseActivity() {

    private lateinit var binding: ActivityAdminBinding
    private val mFireStore = FirebaseFirestore.getInstance()

    private var fileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnSelectImage.setOnClickListener {
            selectImageFromGallery()
        }

        binding.btnSubmitQuestion.setOnClickListener {
            uploadQuestionToFirebase(fileUri)
        }

        binding.btnAdminStartGame.setOnClickListener {
            val intent = Intent(this@AdminActivity, MainActivity::class.java)
            startActivity(intent)
        }

    }

    private fun validateQuestionUpload(): Boolean {
        return when {
            TextUtils.isEmpty(binding.etQuestion.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.enterQuestion), true)
                false
            }
            TextUtils.isEmpty(binding.etAddOption1.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.enterOptions), true)
                false
            }
            TextUtils.isEmpty(binding.etAddOption2.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.enterOptions), true)
                false
            }
            TextUtils.isEmpty(binding.etAddOption3.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.enterOptions), true)
                false
            }
            TextUtils.isEmpty(binding.etAddOption4.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.enterOptions), true)
                false
            }
            TextUtils.isEmpty(binding.etAddCorrectAnswer.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.enterCorrectAnswer), true)
                false
            }
            else -> {
                true
            }
        }
    }

    fun questionsAddedSuccess() {
        Toast.makeText(
            this@AdminActivity,
            resources.getString(R.string.uploadSuccess),
            Toast.LENGTH_SHORT
        ).show()
    }

    fun questionsAddedFailed() {
        Toast.makeText(
            this,
            resources.getString(R.string.uploadFailed),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun selectImageFromGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(
                intent,
                "Please select..."
            ),
            GALLERY_REQUEST_CODE
        )
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (requestCode == GALLERY_REQUEST_CODE
            && resultCode == Activity.RESULT_OK
            && data != null
            && data.data != null
        ) {

            // get the Uri of data
            fileUri = data.data!!
            if (validateQuestionUpload()) {
                binding.ivImage.setImageURI(fileUri)
            }
        }
    }

    private fun uploadQuestionToFirebase(fileUri: Uri?) {
        if (validateQuestionUpload()) {
            if (fileUri != null) {
                val fileName = "${System.currentTimeMillis()}.${getFileExtension(fileUri)}"

                val refStorage = FirebaseStorage.getInstance().reference.child("images/$fileName")
                val uploadTask = refStorage.putFile(fileUri)

                uploadTask.continueWith { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    refStorage.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        val imageUrl = downloadUri.toString()
                        val correctAnswer = binding.etAddCorrectAnswer.text.toString().toInt()

                        val question = Questions(
                            "",
                            imageSrc = imageUrl,
                            imageFileName = fileName,
                            binding.etQuestion.text.toString().trim { it <= ' ' },
                            binding.etAddOption1.text.toString().trim { it <= ' ' },
                            binding.etAddOption2.text.toString().trim { it <= ' ' },
                            binding.etAddOption3.text.toString().trim { it <= ' ' },
                            binding.etAddOption4.text.toString().trim { it <= ' ' },
                            correctAnswer
                        )

                        val id = mFireStore.collection("questions").document().id
                        question.id = id

                        uploadQuestionToFirestore(question)
                        questionsAddedSuccess()
                    }
                    else {
                        questionsAddedFailed()
                    }
                }.addOnFailureListener {
                    // Handle any errors that occurred during the upload.
                    questionsAddedFailed()
                }
            }
        } else {
            Toast.makeText(
                this@AdminActivity,
                resources.getString(R.string.addQuestionsError),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun uploadQuestionToFirestore(question: Questions) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("questions")
            .add(question)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Question uploaded to Firestore", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                questionsAddedFailed()
                Log.e(TAG, "Error adding question document", e)
            }
    }

    private fun getFileExtension(uri: Uri) : String? {
        val contentResolver = contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }
}
