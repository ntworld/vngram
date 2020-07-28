package com.example.vngram

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    var auth : FirebaseAuth? = null
    var googleSignInClient : GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // 구글 인증 옵션
        // 토큰, 이메일 필요
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // 이메일 로그인 버튼 클릭 했을 때
        email_login_button.setOnClickListener {
            // 프로그레스바 SHOW
            progress_bar.visibility = View.VISIBLE
            // editText 이메일, 비밀번호 유효성 체크
            when {
                TextUtils.isEmpty(email_edittext.text.toString()) -> Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                TextUtils.isEmpty(password_edittext.text.toString()) -> Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                else -> signinAndSignup()
            }
        }

        // 구글 로그인 버튼 클릭 했을 때
        google_sign_button.setOnClickListener {
            // 프로그레스바 SHOW
            progress_bar.visibility = View.VISIBLE
            googleLogin()
        }
    }

    // 구글 로그인
    private fun googleLogin() {
        val signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    }

    // 페이지 이동 후
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_LOGIN_CODE) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

            // 개같은곳
            if (result!!.isSuccess) {
                val account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
            } else {
                progress_bar.visibility = View.GONE
            }
        }
    }

    // 파이어배이스 권한 및 구글
    private fun firebaseAuthWithGoogle (account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener {
                task ->
                    progress_bar.visibility = View.GONE
                    when {
                        // 로그인
                        task.isSuccessful -> moveMainPage(auth?.currentUser)

                        // 로그인 에러 메세지
                        else -> Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                    }
            }
    }

    // 로그인 및 회원가입
    private fun signinAndSignup() {
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())
            ?.addOnCompleteListener {
                task ->
                    when {
                        // 로그인 및 생성
                        task.isSuccessful -> moveMainPage(auth?.currentUser)

                        // 계정이 이미 있을 경우
                        else -> signinEmail()
                    }
            }
    }

    // 이메일로 로그인
    private fun signinEmail() {
        auth?.signInWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())
            ?.addOnCompleteListener {
                task ->
                    when {
                        // 로그인
                        task.isSuccessful -> moveMainPage(auth?.currentUser)

                        // 로그인 에러 메세지
                        else -> Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                    }
            }
    }

    // 메인페이지로 이동
    private fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}