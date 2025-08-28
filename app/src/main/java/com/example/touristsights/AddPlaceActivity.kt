package com.example.touristsights

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestLocatonPermissionLauncher: ActivityResultLauncher<String>

    // リアルタイム位置取得用の変数
    private var locationManager: LocationManager? = null
    private var isLocationUpdating = false
    private var currentLocation: Location? = null

    // LocationListener の実装
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            currentLocation = location
            updateLocationFields(location)
        }

        override fun onProviderEnabled(provider: String) {
            Toast.makeText(this@AddPlaceActivity, "位置情報プロバイダーが有効になりました: $provider", Toast.LENGTH_SHORT).show()
        }

        override fun onProviderDisabled(provider: String) {
            Toast.makeText(this@AddPlaceActivity, "位置情報プロバイダーが無効になりました: $provider", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActivityResultLaunchers()
        setupSpinner()

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

        binding.autoInputLocationButton.setOnClickListener {
            if (checkLocationPermission()) {
                setLocationFields()
            } else {
                requestLocationPermission()
            }
        }

        binding.confirmButton.setOnClickListener {
            if (validateInput().isEmpty()) {
                addNewSight()
            } else {
                // エラーをまとめてダイアログ表示
                val errorMessage = validateInput().joinToString("\n")
                AlertDialog.Builder(this)
                    .setTitle("入力エラー")
                    .setMessage("入力に誤りがあります:\n$errorMessage")
                    .setPositiveButton("OK", { dialog, id ->
                        dialog.dismiss()
                    })
                    .show()
            }
        }
    }

    private fun setupSpinner() {
        val sightKinds = resources.getStringArray(R.array.sight_kinds)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sightKinds)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.kindsSpinner.adapter = adapter
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

        requestCameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(this, "カメラの権限が必要です", Toast.LENGTH_SHORT).show()
            }
        }

        requestLocatonPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                setLocationFields()
            } else {
                Toast.makeText(this, "位置情報の権限が必要です", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun requestLocationPermission() {
        requestLocatonPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }


    private fun setLocationFields() {
        val location = getLocation(this)
        if (location != null) {
            binding.latEditText.setText(location.latitude.toString())
            binding.lngEditText.setText(location.longitude.toString())
            Toast.makeText(this, "位置情報を取得しました", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "位置情報の取得に失敗しました", Toast.LENGTH_SHORT).show()
        }
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

    // 画像ファイルを作成しパスをcurrentPhotoPathに保存
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

    // 撮影した画像をプレビュー表示
    private fun displayImagePreview(imagePath: String) {
        try {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 200, 150, true)
            binding.imagePreview.setImageBitmap(scaledBitmap)
        } catch (e: Exception) {
            Toast.makeText(this, "画像の表示に失敗しました", Toast.LENGTH_SHORT).show()
        }
    }

    // 位置情報を取得
    fun getLocation(context: Context): Location? {
        val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
        return if (checkLocationPermission()) {
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            location
        } else {
            null
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

        if (binding.imageNameEditText.text.toString().trim().isEmpty()) {
            errors.add("写真を撮影してください")
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
        val imageName = binding.imageNameEditText.text.toString().trim()
        val kind = binding.kindsSpinner.selectedItem.toString()
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

    private fun updateLocationFields(location: Location) {
        binding.latEditText.setText(location.latitude.toString())
        binding.lngEditText.setText(location.longitude.toString())
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    // リアルタイム位置情報更新の開始
    private fun startLocationUpdates() {
        if (checkLocationPermission()) {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            isLocationUpdating = true
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000, // 5秒ごとに更新
                10f, // 10メートル以上移動したら更新
                locationListener,
                Looper.getMainLooper()
            )
        }
    }

    private fun stopLocationUpdates() {
        if (isLocationUpdating) {
            locationManager?.removeUpdates(locationListener)
            isLocationUpdating = false
        }
    }
}