package com.example.touristsights
import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

@Serializable
data class Sight (
    val id: Int,
    val name: String,
    val imageName: String,
    val description: String,
    val kind: String,
    val lat: Double,
    val lng: Double,
    var visible: Boolean = true  // デフォルト値をtrueに設定
)

// すべての観光地を取得する関数（visibleフィルタなし）
fun getAllSights(context: Context): List<Sight> {
    val file = File(context.filesDir, "sights.json")

    return if (file.exists()) {
        // 内部ストレージに保存されたデータを読み込み
        val jsonStr = file.readText()
        Json.decodeFromString(ListSerializer(Sight.serializer()), jsonStr)
    } else {
        // 初回起動時のみassetsから読み込んで内部ストレージにコピー
        val assetManager = context.assets
        val inputStream = assetManager.open("sights.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val jsonStr = bufferedReader.readText()
        bufferedReader.close()

        // 初期データを内部ストレージに保存（visibleフィールドを含む形式で保存）
        val sights = Json.decodeFromString(ListSerializer(Sight.serializer()), jsonStr)
        val updatedJsonStr = Json.encodeToString(ListSerializer(Sight.serializer()), sights)
        file.writeText(updatedJsonStr)
        sights
    }
}

fun getSights(context: Context): List<Sight> {
    // visibleがtrueの観光地のみを返す
    return getAllSights(context).filter { it.visible }
}

// 新しい観光地を追加する関数
fun addSight(context: Context, sight: Sight) {
    val allSights = getAllSights(context).toMutableList()
    allSights.add(sight)
    val jsonStr = Json.encodeToString(ListSerializer(Sight.serializer()), allSights)
    val file = File(context.filesDir, "sights.json")
    file.writeText(jsonStr)
    println("観光地を追加しました: $sight")
}

fun invisibleSight(context: Context, sightId: Int) {
    val allSights = getAllSights(context).toMutableList()
    val index = allSights.indexOfFirst { it.id == sightId }
    if (index != -1) {
        allSights[index] = allSights[index].copy(visible = false)
        val jsonStr = Json.encodeToString(ListSerializer(Sight.serializer()), allSights)
        val file = File(context.filesDir, "sights.json")
        file.writeText(jsonStr)
    }
}

