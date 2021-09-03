package com.kujira.homestay.ui.all_login.login_new

import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kujira.homestay.R
import com.kujira.homestay.ui.base.BaseViewModel
import com.kujira.homestay.utils.Constants

class LoginAccViewModel : BaseViewModel() {
    val emailLogin = ObservableField<String>()
    val passwordLogin = ObservableField<String>()
    val listenerClick = MutableLiveData<Int>()
    private var auth = FirebaseAuth.getInstance()
    val listener = MutableLiveData<Int>()

    companion object {
        const val LOGIN = 1
        const val FORGOT_PASSWORD = 2
        const val REGISTER_ACC = 3
        const val PERMISSION_1 = 4
        const val PERMISSION_2 = 5
    }

    fun click(view: View) {
        when (view.id) {
            R.id.btn_login_new -> {
                checkSignIn()
            }
            R.id.tv_forgot_password_new -> {
                listenerClick.value = FORGOT_PASSWORD
            }
            R.id.tv_register_new -> {
                listenerClick.value = REGISTER_ACC
            }
        }
    }

    private var listAcc = mutableListOf<Acc>()
    fun getListAcc(): MutableList<Acc> {
        val dataRef =
            FirebaseDatabase.getInstance().getReference(Constants.CLIENT).child(Constants.ACCOUNT)

        dataRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listAcc.clear()
                for (snap in snapshot.children) {
                    val mail = snap.child(Constants.MAIL).value.toString()
                    val permission = snap.child(Constants.PERMISSION).value.toString()
                    listAcc.add(Acc(mail, permission))
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        return listAcc
    }

    private fun checkSignIn() {
        val email = emailLogin.get()?.trim() ?: ""
        val password = passwordLogin.get()?.trim() ?: ""
        // val isCheck = listAcc.contains(email)

        if (email.isNotEmpty() && password.isNotEmpty()) {
            val acc = listAcc.firstOrNull {
                it.email == email
            }
            if (acc != null) {
                showLoading.onNext(true)
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val user = auth.currentUser
                            if (user!!.isEmailVerified) {
                                showLoading.onNext(false)
                                if (acc.permission == "1") {
                                    listener.value = PERMISSION_1
                                } else {
                                    listener.value = PERMISSION_2
                                }
                            } else {
                                showLoading.onNext(false)
                                listener.value = R.string.error_auth
                            }
                        }
                    }
                    .addOnCanceledListener {
                        showLoading.onNext(false)
                        listener.value = R.string.check_info
                    }
                    .addOnFailureListener {
                        showLoading.onNext(false)
                    }
            } else {
                listener.value = R.string.not_exit_account
            }

        } else {
            listener.value = R.string.error_isEmpty
        }
    }


}

data class Acc(
    val email: String,
    val permission: String
)