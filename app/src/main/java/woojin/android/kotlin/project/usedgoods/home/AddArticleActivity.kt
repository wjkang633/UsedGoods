package woojin.android.kotlin.project.usedgoods.home

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import woojin.android.kotlin.project.usedgoods.DBKey.Companion.DB_ARTICLES
import woojin.android.kotlin.project.usedgoods.R
import woojin.android.kotlin.project.usedgoods.databinding.ActivityAddArticleBinding

class AddArticleActivity : AppCompatActivity() {

    private var selectedUri: Uri? = null

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    private val storage: FirebaseStorage by lazy {
        Firebase.storage
    }

    private val articleDB: DatabaseReference by lazy {
        Firebase.database.reference.child(DB_ARTICLES)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_article)

        findViewById<Button>(R.id.imageAddButton).setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    //권한이 잘 부여되어 있으면 갤러리에서 사진 선택하도록
                    startContentProvider()
                }

                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    //교육용 UI 보여준 후 다시 권한 팝업 띄우기
                    showPermissionContextPopup()
                }

                else -> {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        1000
                    )
                }
            }
        }

        findViewById<Button>(R.id.submitButton).setOnClickListener {
            val title = findViewById<EditText>(R.id.titleEditText).text.toString()
            val price = findViewById<EditText>(R.id.priceEditText).text.toString()
            val sellerId = auth.currentUser?.uid.orEmpty()

            showProgressBar()

            //이미지가 있으면 storage에 업로드 하는 과정 추가
            if (selectedUri != null) {
                uploadPhoto(selectedUri!!,
                    successHandler = { url ->
                        uploadArticle(sellerId, title, price, url) },
                    errorHandler = {
                        Toast.makeText(
                            this,
                            "사진 업로드에 실패했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()

                        hideProgressBar()
                    })
            } else {
                uploadArticle(sellerId, title, price, "")
            }
        }
    }

    private fun uploadPhoto(uri: Uri, successHandler: (String) -> Unit, errorHandler: () -> Unit) {
        val fileName = "${System.currentTimeMillis()}.png"
        storage.reference
            .child("article/photo")
            .child(fileName)
            .putFile(uri)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    storage.reference
                        .child("article/photo")
                        .child(fileName)
                        .downloadUrl
                        .addOnCompleteListener { uri ->
                            successHandler(uri.toString())
                        }
                        .addOnFailureListener {
                            errorHandler()
                        }
                } else {
                    errorHandler()
                }
            }
    }

    private fun uploadArticle(sellerId: String, title: String, price: String, imageUrl: String) {
        val model = ArticleModel(sellerId, title, System.currentTimeMillis(), "$price 원", imageUrl)
        articleDB.push().setValue(model)

        hideProgressBar()

        finish()
    }

    private fun startContentProvider() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, 2000)
    }

    private fun showPermissionContextPopup() {
        AlertDialog.Builder(this).setTitle("권한이 필요합니다.").setMessage("사진을 가져오기 위해 필요합니다.")
            .setPositiveButton("동의") { _, _ ->
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    1000
                )
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            1000 -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startContentProvider()
            } else {
                Toast.makeText(
                    this,
                    "권한을 거부하셨습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            2000 -> {
                val uri = data?.data
                if (uri != null) {
                    findViewById<ImageView>(R.id.photoImageView).setImageURI(uri)
                    selectedUri = uri
                } else {
                    Toast.makeText(
                        this,
                        "사진을 가져오지 못했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else -> {
                Toast.makeText(
                    this,
                    "사진을 가져오지 못했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showProgressBar(){
        findViewById<ProgressBar>(R.id.progressBar).isVisible = true
    }

    private fun hideProgressBar(){
        findViewById<ProgressBar>(R.id.progressBar).isVisible = false
    }
}