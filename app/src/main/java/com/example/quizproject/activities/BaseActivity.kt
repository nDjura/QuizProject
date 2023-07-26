package com.example.quizproject.activities

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.quizproject.R
import com.google.android.material.snackbar.Snackbar

open class BaseActivity : AppCompatActivity() {

    fun showErrorSnackBar(message: String, errorMessage: Boolean){
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackBarView = snackbar.view

        if (errorMessage) {
            snackBarView.setBackgroundColor(
                ContextCompat.getColor(this@BaseActivity, R.color.colorSnackBarError)
            )
        } else {
            snackBarView.setBackgroundColor(
                ContextCompat.getColor(this@BaseActivity, R.color.colorSnackBarSuccess)
            )
        }
        snackbar.show()
    }


}