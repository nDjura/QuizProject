package com.example.quizproject.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Questions(
    var id: String = "",
    val imageSrc: String = "",
    val imageFileName: String = "",
    val question: String = "",
    val option1: String = "",
    val option2: String = "",
    val option3: String = "",
    val option4: String = "",
    val correctAnswer: Int = 0
) : Parcelable
