package com.admin.kasironline.activity

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.admin.kasironline.GlideApp
import com.admin.kasironline.R
import com.admin.kasironline.utils.Constant.PLAY_SERVICES_RESOLUTION_REQUEST
import com.admin.kasironline.utils.Validation.Companion.validateEmail
import com.admin.kasironline.utils.Validation.Companion.validateFields
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mAuthListener: FirebaseAuth.AuthStateListener
    private lateinit var mAnalytics: FirebaseAnalytics

    private lateinit var mEmail: String
    private lateinit var mPassword: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        prepare()
        checkPlayServices()
        checkUser()
    }

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthListener)
    }

    override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener(mAuthListener)
    }

    private fun prepare() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        mAnalytics = FirebaseAnalytics.getInstance(this)
        mAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null)
        mAuth = FirebaseAuth.getInstance()

        GlideApp.with(this)
            .load(R.drawable.royal_truss)
            .into(img_logo)

        bindProgressButton(btn_login)
        btn_login.attachTextChangeAnimator()

        btn_login.setOnClickListener {
            mEmail = input_email.text.toString().trim()
            mPassword = input_password.text.toString().trim()

            if (validateFields(mEmail) || validateFields(mPassword)) {
                Toast.makeText(this, getString(R.string.email_password_null), Toast.LENGTH_SHORT)
                    .show()
            } else if (validateEmail(mEmail)) {
                Toast.makeText(this, getString(R.string.email_not_valid), Toast.LENGTH_SHORT).show()
            } else {
                btn_login.showProgress { progressColor = Color.WHITE }
                login()
            }
        }
    }

    private fun login() {
        val mAuth = FirebaseAuth.getInstance()

        mAuth.signInWithEmailAndPassword(mEmail, mPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    btn_login.hideProgress(R.string.btn_login)
                } else {
                    btn_login.hideProgress(R.string.btn_login)
                    when ((task.exception as FirebaseAuthException).errorCode) {
                        "ERROR_USER_NOT_FOUND" -> Toast.makeText(
                            this, getString(R.string.error_user_not_found),
                            Toast.LENGTH_SHORT
                        ).show()

                        "ERROR_WRONG_PASSWORD" -> Toast.makeText(
                            this, getString(R.string.error_wrong_password),
                            Toast.LENGTH_SHORT
                        ).show()

                        else -> Toast.makeText(
                            this, getString(R.string.request_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    private fun checkPlayServices() {
        val googleAPI = GoogleApiAvailability.getInstance()
        val result = googleAPI.isGooglePlayServicesAvailable(this)
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(
                    this, result,
                    PLAY_SERVICES_RESOLUTION_REQUEST
                ).show()
            }
        }
    }

    private fun checkUser() {
        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
            } else {
                login_layout.visibility = VISIBLE
            }
        }
    }
}