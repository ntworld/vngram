package com.example.vngram

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    var auth : FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        email_login_button.setOnClickListener {

            // editText 이메일, 비밀번호 유효성 체크
            when {
                TextUtils.isEmpty(email_edittext.text.toString()) -> Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                TextUtils.isEmpty(password_edittext.text.toString()) -> Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                else -> signinAndSignup()
            }
        }
    }

    // 로그인 및 회원가입
    fun signinAndSignup() {
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())
            ?.addOnCompleteListener {
                task ->
                    when {
                        // 로그인 및 생성
                        task.isSuccessful -> moveMainPage(task.result?.user)

                        // 계정이 이미 있을 경우
                        else -> signinEmail()
                    }
            }
    }

    // 이메일로 로그인
    fun signinEmail() {
        auth?.signInWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())
            ?.addOnCompleteListener {
                task ->
                    when {
                        // 로그인
                        task.isSuccessful -> moveMainPage(task.result?.user)

                        // 로그인 에러 메세지
                        else -> Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show();
                    }
            }
    }

    fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
