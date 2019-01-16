package com.hellowo.journey.ui.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.TextView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.hellowo.journey.*
import com.hellowo.journey.R
import io.realm.*
import kotlinx.android.synthetic.main.activity_welcome.*


class WelcomeActivity : BaseActivity() {
    private var realmAsyncTask: RealmAsyncTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        initTheme(rootLy)
        loginBtn.setOnClickListener { signInWithGoogle() }
        callAfterViewDrawed(rootLy, Runnable{ startShow() })
    }

    private fun startShow() {
        val handler =  Handler()
        handler.postDelayed({
            ObjectAnimator.ofFloat(firstText1, "alpha", 0f, 1f).let {
                it.duration = 2000
                it.start()
            }
        }, 1000)
        handler.postDelayed({
            ObjectAnimator.ofFloat(firstText2, "alpha", 0f, 1f).let {
                it.duration = 2000
                it.start()
            }
        }, 4000)
        handler.postDelayed({
            ObjectAnimator.ofFloat(firstText1, "alpha", 1f, 0f).let {
                it.duration = 1000
                it.start()
            }
        }, 7000)
        handler.postDelayed({
            ObjectAnimator.ofFloat(firstText2, "alpha", 1f, 0f).let {
                it.duration = 1000
                it.start()
            }
        }, 8000)
        handler.postDelayed({
            val animSet = AnimatorSet()
            animSet.playTogether(
                    ObjectAnimator.ofFloat(loginLy, "alpha", 0f, 1f),
                    ObjectAnimator.ofFloat(loginLy, "translationY", dpToPx(15f), 0f))
            animSet.duration = 1000
            animSet.interpolator = FastOutSlowInInterpolator()
            animSet.start()
        }, 9000)
    }

    var scriptNum = 0

    private fun startInitSettingLy() {
        prevBtn.setOnClickListener { setScript() }
        nextBtn.setOnClickListener { setScript() }

        val handler =  Handler()
        handler.postDelayed({
            val animSet = AnimatorSet()
            animSet.playTogether(
                    ObjectAnimator.ofFloat(loginLy, "alpha", 1f, 0f),
                    ObjectAnimator.ofFloat(loginLy, "translationY", 0f, dpToPx(15f)))
            animSet.duration = 1000
            animSet.interpolator = FastOutSlowInInterpolator()
            animSet.start()
        }, 0)

        handler.postDelayed({
            val animSet = AnimatorSet()
            animSet.playTogether(
                    ObjectAnimator.ofFloat(initSettingLy, "alpha", 0f, 1f),
                    ObjectAnimator.ofFloat(initSettingLy, "translationY", dpToPx(15f), 0f))
            animSet.duration = 1000
            animSet.interpolator = FastOutSlowInInterpolator()
            animSet.start()
        }, 1000)
    }

    private fun setScript() {
        val optionsTexts = arrayOf<TextView>(
                optionsLy.findViewById(R.id.option1Text),
                optionsLy.findViewById(R.id.option2Text),
                optionsLy.findViewById(R.id.option3Text),
                optionsLy.findViewById(R.id.option4Text),
                optionsLy.findViewById(R.id.option5Text))

        when(scriptNum) {

        }

        TransitionManager.beginDelayedTransition(optionsLy, makeFromBottomSlideTransition())
        optionsTexts.forEach { it.visibility = View.GONE }

        optionsLy.postDelayed({
            TransitionManager.beginDelayedTransition(optionsLy, makeFromBottomSlideTransition())
            optionsTexts.forEach { it.visibility = View.VISIBLE }
        }, 500)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progressBar.setProgress(50, true)
        }else {
            progressBar.progress = 50
        }
    }

    private fun loginRealm(account: GoogleSignInAccount) {
        val credentials = SyncCredentials.usernamePassword(account.email,
                account.id, false)
        SyncUser.logInAsync(credentials, AUTH_URL, object: SyncUser.Callback<SyncUser> {
            override fun onError(error: ObjectServerError?) {
                l("[로그인 실패]"+error?.errorCode?.intValue())
                hideProgressDialog()
            }
            override fun onSuccess(result: SyncUser?) {
                l("[로그인 성공]")
                result?.let { initRealm(it) }
            }
        })
        showProgressDialog(getString(R.string.plz_wait))
    }

    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("730968307119-mrr5n2rriuc9dqnejfmni6fkae7h4t13.apps.googleusercontent.com")
                .requestEmail()
                .build()
        GoogleSignIn.getClient(this, gso).signOut().addOnCompleteListener {
            startActivityForResult(GoogleSignIn.getClient(this, gso).signInIntent, 0)
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        showProgressDialog(null)
        val mAuth = FirebaseAuth.getInstance()
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        l("signInWithCredential:success")
                        val user = mAuth.currentUser
                    } else {
                        // If sign in fails, display a message to the user.
                        l("signInWithCredential:failure")
                    }
                    hideProgressDialog()
                    startInitSettingLy()
                }
    }

    private fun initRealm(result: SyncUser) {
        val config = result.createConfiguration(USER_URL)
                .fullSynchronization()
                .waitForInitialRemoteData()
                .build()
        realmAsyncTask = Realm.getInstanceAsync(config, object : Realm.Callback() {
            override fun onSuccess(realm: Realm) {
                if (isDestroyed) {
                    realm.close()
                } else {
                    l("Realm 준비 완료")
                    Realm.setDefaultConfiguration(config)
                    hideProgressDialog()
                    startMainActivity()
                }
            }

            override fun onError(exception: Throwable) {
                super.onError(exception)
                hideProgressDialog()
            }
        })
    }

    private fun startMainActivity() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                account?.let { firebaseAuthWithGoogle(account) }
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                l("Google sign in failed")
            }
        }
    }
}