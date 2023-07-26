package com.example.quizproject.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User (
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val profileCompleted: Int = 0,
    val admin: Int = 0
) : Parcelable