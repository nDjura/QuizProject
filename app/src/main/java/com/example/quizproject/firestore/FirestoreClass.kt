package com.example.quizproject.firestore

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.quizproject.activities.user.AdminActivity
import com.example.quizproject.activities.LoginActivity
import com.example.quizproject.activities.RegisterActivity
import com.example.quizproject.models.Questions
import com.example.quizproject.models.User
import com.example.quizproject.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: RegisterActivity, userInfo: User) {
        // the "users" is collection name. if the collection is already created then it will not create the same one again
        mFireStore.collection(Constants.USERS)
            // document ID for users fields. here the document it is the User ID
            .document(userInfo.id)
            // here the userInfo are Field and the SetOption is set to merge. it is for if we wants to merge later on instead of replacing the fields
            .set(userInfo, SetOptions.merge())
            .addOnCompleteListener {

                // here call a function of base activity for transferring the result to it
                activity.userRegistrationSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(activity.javaClass.simpleName, "Error while registering the user", e)
            }
    }

    private fun getCurrentUserID(): String {
        // an instance of currentUser using FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser

        // a variable to assign the currentUserId if it is not null or else it will be blank
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    fun getUserDetails(activity: Activity) {
        // here we pass the collection name from which we wants the data
        mFireStore.collection(Constants.USERS)
            // the document id to get the Fields of user
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->

                if (document == null) {
                    Log.e(activity.javaClass.simpleName, "Document snapshot is null.")
                } else {

                    Log.i(activity.javaClass.simpleName, document.toString())

                    // here we have received the document snapshot which is converted into the User Data model object
                    val user = document.toObject(User::class.java)!!

                    val sharedPreferences = activity.getSharedPreferences(
                        Constants.MYQUIZAPP_PREFERENCES,
                        Context.MODE_PRIVATE
                    )

                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                    // key: logged_in_name - value : Neca
                    editor.putString(
                        Constants.LOGGED_IN_NAME,
                        "${user.name}"
                    )
                    editor.apply()
                    (activity as LoginActivity).userLoggedInSuccess(user)
                }
            }
            .addOnFailureListener { e ->
                // hide the progress dialog if there is any error and print the error in log
                when (activity) {
                    is LoginActivity -> {
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting user details"
                )
            }
    }

    fun uploadQuestion(activity: AdminActivity, questionsInfo: Questions) {
        mFireStore.collection(Constants.QUESTIONS)
            .document(questionsInfo.id)
            .set(questionsInfo, SetOptions.merge())
            .addOnCompleteListener {
                activity.questionsAddedSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(activity.javaClass.simpleName, "Error while uploading questions", e)
            }
    }

}


