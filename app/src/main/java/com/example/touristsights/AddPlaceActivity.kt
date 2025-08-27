package com.example.touristsights

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.touristsights.databinding.ActivityAddPlaceBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddPlaceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPlaceBinding
    private var currentPhotoPath: String? = null
    private var currentImageFileName: String? = null

    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActivityResultLaunchers()

        binding.cancelButton.setOnClickListener {
            finish()
        }

        binding.cameraButton.setOnClickListener {
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }

        binding.confirmButton.setOnClickListener {
            if (validateInput().isEmpty()) {
                addNewSight()
            } else {
                // エラーをまとめてダイアログ表示
                val errorMessage = validateInput().joinToString("\n")
                val builder = android.app.AlertDialog.Builder(this)
                builder.setTitle("入力エラー")
                val messageView = android.widget.TextView(this)
                messageView.text = errorMessage
                messageView.setTextColor(android.graphics.Color.RED)
                builder.setView(messageView)
                builder.setPositiveButton("OK", null)
                builder.show()
            }
        }
    }

    private fun setupActivityResultLaunchers() {
        takePictureLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                currentPhotoPath?.let { path ->
                    displayImagePreview(path)
                    currentImageFileName?.let { fileName ->
                        binding.imageNameEditText.setText(fileName)
                    }
                }
            }
        }

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(this, "カメラの権限が必要です", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            )

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            takePictureLauncher.launch(takePictureIntent)
        } catch (ex: IOException) {
            Toast.makeText(this, "写真ファイルの作成に失敗しました", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = getExternalFilesDir("Pictures")

        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
            currentImageFileName = name
        }
    }

    private fun displayImagePreview(imagePath: String) {
        try {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 200, 150, true)
            binding.imagePreview.setImageBitmap(scaledBitmap)
        } catch (e: Exception) {
            Toast.makeText(this, "画像の表示に失敗しました", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInput(): List<String> {
        var errors = mutableListOf<String>()

        if (binding.nameEditText.text.toString().trim().isEmpty()) {
            errors.add("観光地名を入力してください")
        }

        if (binding.descriptionEditText.text.toString().trim().isEmpty()) {
            errors.add("説明を入力してください")
        }

        if (binding.kindEditText.text.toString().trim().isEmpty()) {
            errors.add("種類を入力してください")
        }

        if (binding.imageNameEditText.text.toString().trim().isEmpty()) {
            errors.add("画像名を入力してください")
        }

        val latText = binding.latEditText.text.toString().trim()
        if (latText.isEmpty()) {
            errors.add("緯度を入力してください")
        } else {
            try {
                val lat = latText.toDouble()
                if (lat < -90 || lat > 90) {
                    errors.add("緯度は-90から90の間で入力してください")
                }
            } catch (e: NumberFormatException) {
                errors.add("緯度は正しい数値を入力してください")
            }
        }

        val lngText = binding.lngEditText.text.toString().trim()
        if (lngText.isEmpty()) {
            errors.add("経度を入力してください")
        } else {
            try {
                val lng = lngText.toDouble()
                if (lng < -180 || lng > 180) {
                    errors.add("経度は-180から180の間で入力してください")
                }
            } catch (e: NumberFormatException) {
                errors.add("経度は正しい数値を入力してください")
            }
        }

        return errors
    }

    private fun addNewSight() {
        val name = binding.nameEditText.text.toString().trim()
        val description = binding.descriptionEditText.text.toString().trim()
        val kind = binding.kindEditText.text.toString().trim()  // kindAutoCompleteからkindEditTextに変更
        val imageName = binding.imageNameEditText.text.toString().trim()
        val lat = binding.latEditText.text.toString().trim().toDouble()
        val lng = binding.lngEditText.text.toString().trim().toDouble()

        val newSight = Sight(
            id = (getAllSights(this).maxOfOrNull { it.id } ?: 0) + 1,
            name = name,
            imageName = imageName,
            description = description,
            kind = kind,
            lat = lat,
            lng = lng,
            visible = true
        )

        try {
            addSight(this, newSight)
            Toast.makeText(this, "${name}が追加されました", Toast.LENGTH_SHORT).show()
            // 結果をメインActivityに通知するためのIntent設定
            setResult(RESULT_OK)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "追加に失敗しました: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}