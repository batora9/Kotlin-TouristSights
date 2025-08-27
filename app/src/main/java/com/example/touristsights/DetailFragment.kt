package com.example.touristsights

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.touristsights.databinding.FragmentDetailBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

const val ROW_POSITION = "ROW_POSITION"

class DetailFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private var position: Int = 0
    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            position = it.getInt(ROW_POSITION)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // backButtonで戻る
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // 削除ボタン
        binding.deleteSightButton.setOnClickListener {
            val sights = getSights(requireContext())
            val sightId = sights[position].id

            // 確認ダイアログ
             val builder = android.app.AlertDialog.Builder(requireContext())
             builder.setTitle("確認")
             builder.setMessage("本当に削除しますか？")
             builder.setPositiveButton("はい") { _, _ ->
                 val sights = getSights(requireContext())
                 val sightId = sights[position].id
                 invisibleSight(requireContext(), sightId)
                 // 削除確認のトースト表示
                 Toast.makeText(requireContext(), "観光地を削除しました", Toast.LENGTH_SHORT).show()
                 // 前の画面に戻る
                 requireActivity().onBackPressedDispatcher.onBackPressed()
             }
             builder.setNegativeButton("いいえ", null)
             builder.show()
        }

        val sights = getSights(requireContext())
        binding.detailKind.text = sights[position].kind
        binding.detailName.text = sights[position].name
        binding.detailDescription.text = sights[position].description
        val img = resources.getIdentifier(
            sights[position].imageName,
            "drawable",
            requireActivity().packageName
        )
        // 画像が見つからない場合のデフォルト処理
        if (img != 0) {
            binding.detailImage.setImageResource(img)
        } else {
            binding.detailImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // MapViewの初期化と設定
        try {
            mapView = binding.mapView
            mapView.onCreate(savedInstanceState)
            mapView.getMapAsync(this)
        } catch (e: Exception) {
            // MapView初期化に失敗した場合、MapViewを非表示にする
            binding.mapView.visibility = View.GONE
            e.printStackTrace()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        val sights = getSights(requireContext())
        googleMap = map

        val location = LatLng(sights[position].lat, sights[position].lng)
        googleMap?.addMarker(MarkerOptions().position(location).title(sights[position].name))
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    override fun onResume() {
        super.onResume()
        if (::mapView.isInitialized) {
            mapView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::mapView.isInitialized) {
            mapView.onPause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mapView.isInitialized) {
            mapView.onDestroy()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        if (::mapView.isInitialized) {
            mapView.onLowMemory()
        }
    }
}