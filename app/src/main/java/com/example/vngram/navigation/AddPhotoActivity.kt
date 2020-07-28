package com.example.vngram.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vngram.R
import com.example.vngram.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    private var PICK_IMAGE_FROM_ALBUM = 0
    var storage : FirebaseStorage? = null
    var photoUri : Uri? = null
    var firestore : FirebaseFirestore? = null
    var auth : FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)
        progress_bar.visibility = View.GONE

        // 초기값
        storage = FirebaseStorage.getInstance()
        // Firebase Database
        firestore = FirebaseFirestore.getInstance()
        // Firebase Auth
        auth = FirebaseAuth.getInstance()

        // 앨범 화면 열기
        startPhotoPicker()

        addphoto_image.setOnClickListener {
            startPhotoPicker()
        }

        // 사진 추가
        addphoto_btn_upload.setOnClickListener {
            contentUpload()
        }
    }

    // 앨범 화면 열기
    private fun startPhotoPicker() {
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            if(resultCode == Activity.RESULT_OK) {
                // 선택된 이미지 path
                photoUri = data?.data
                addphoto_image.setImageURI(photoUri)
            }
            else {
                // 선택한 이미지 없이 addPhotoActivity 를 빠져 나올 경우
                finish()
            }
        }
    }

    private fun contentUpload() {
        progress_bar.visibility = View.VISIBLE

        // 파일 이름 생성
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "IMAGE_" + timeStamp + "_.png"
        val storageRef = storage?.reference?.child("images")?.child(imageFileName)

        // 파일 업로드
        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {

            // 성공 시
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                progress_bar.visibility = View.GONE
                Toast.makeText(this, getString(R.string.upload_success), Toast.LENGTH_SHORT).show()

                //데이터베이스에 바인딩할 위치 생성 및 컬렉션(테이블)에 데이터 집합 생성
                val contentDTO = ContentDTO()

                //이미지 주소
                contentDTO.imageUrl = uri.toString()

                //유저의 UID
                contentDTO.uid = auth?.currentUser?.uid

                //게시물의 설명
                contentDTO.explain = addphoto_edit_explain.text.toString()

                //유저의 아이디
                contentDTO.userId = auth?.currentUser?.email

                //게시물 업로드 시간
                contentDTO.timestamp = System.currentTimeMillis()

                //게시물에 데이터 생성 및 액티비티 종료
                firestore?.collection("images")?.document()?.set(contentDTO)
                setResult(Activity.RESULT_OK)
                finish()

            // 실패 시
            }.addOnFailureListener {
                progress_bar.visibility = View.GONE
                Toast.makeText(this, getString(R.string.upload_fail), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
