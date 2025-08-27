package com.example.touristsights

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.touristsights.databinding.ActivityAddPlaceBinding

class AddPlaceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPlaceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cancelButton.setOnClickListener {
            finish()
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
            name = name,
            imageName = imageName,
            description = description,
            kind = kind,
            lat = lat,
            lng = lng
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