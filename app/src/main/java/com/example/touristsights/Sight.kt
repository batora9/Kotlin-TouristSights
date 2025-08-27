package com.example.touristsights
import android.content.Context
import android.content.res.Resources
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

@Serializable
class Sight (
    val name: String,
    val imageName: String,
    val description: String,
    val kind: String,
    val lat: Double,
    val lng: Double
)

fun getSights(context: Context): List<Sight> {
    val file = File(context.filesDir, "sights.json")

    return if (file.exists()) {
        // 内部ストレージからデータを読み込み
        val jsonStr = file.readText()
        Json.decodeFromString<List<Sight>>(jsonStr)
    } else {
        // assetsから初期データを読み込んで内部ストレージにコピー
        val assetManager = context.assets
        val inputStream = assetManager.open("sights.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val jsonStr = bufferedReader.readText()
        bufferedReader.close()

        // 初期データを内部ストレージに保存
        file.writeText(jsonStr)
        Json.decodeFromString<List<Sight>>(jsonStr)
    }
}

fun getSights(resources: Resources): List<Sight> {
    val assetManager = resources.assets
    val inputStream = assetManager.open("sights.json")
    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
    val str: String = bufferedReader.readText()
    bufferedReader.close()
    val obj = Json.decodeFromString<List<Sight>>(str)
    return obj
}

// 新しい観光地を追加する関数
fun addSight(context: Context, sight: Sight) {
    val sights = getSights(context).toMutableList()
    sights.add(sight)
    val jsonStr = Json.encodeToString(ListSerializer(Sight.serializer()), sights)
    val file = File(context.filesDir, "sights.json")
    file.writeText(jsonStr) // 内部ストレージに保存
}
