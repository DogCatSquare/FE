package com.example.dogcatsquare.ui.map.walking

import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.walk.Walk
import com.example.dogcatsquare.databinding.ItemMapwalkingBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import java.text.SimpleDateFormat
import java.util.Locale

class WalkRVAdapter(
    private val onItemClick: (Int) -> Unit,
    private val onMenuClick: (Walk, View) -> Unit,
    private var walkList: ArrayList<Walk>
) : RecyclerView.Adapter<WalkRVAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemMapwalkingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private fun dpToPx(dp: Float): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                itemView.resources.displayMetrics
            ).toInt()
        }

        fun bind(walk: Walk) {
            binding.apply {
                userName.text = walk.createdBy.nickname
                petType.text = walk.createdBy.breed
                Glide.with(userImg)
                    .load(walk.createdBy.profileImageUrl)
                    .fallback(R.drawable.ic_profile_img_default)
                    .error(R.drawable.ic_profile_img_default)
                    .into(userImg)

                walkTime.text = "소요시간 ${walk.time}h"
                walkKm.text = "| ${String.format("%.1f", walk.distance)}km"
                walkText.text = walk.description

                imageContainer.removeAllViews()

                if (walk.walkImageUrl.isNotEmpty()) {
                    walk.walkImageUrl.forEach { url ->
                        val cardView = CardView(root.context).apply {
                            radius = dpToPx(12f).toFloat()
                            cardElevation = 0f
                            val params = LinearLayout.LayoutParams(
                                dpToPx(120f),
                                dpToPx(120f)
                            )
                            params.marginEnd = dpToPx(8f)
                            layoutParams = params
                        }

                        val imageView = ImageView(root.context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        }

                        Glide.with(imageView)
                            .load(url)
                            .placeholder(R.drawable.ic_place_img_default)
                            .error(R.drawable.ic_place_img_default)
                            .into(imageView)

                        cardView.addView(imageView)
                        imageContainer.addView(cardView)
                    }
                } else {
                    addDefaultPlaceholder()
                }

                mapView.onCreate(null)
                mapView.getMapAsync { googleMap ->
                    googleMap.uiSettings.apply {
                        isMapToolbarEnabled = false
                        setAllGesturesEnabled(false)
                    }

                    googleMap.clear()

                    if (!walk.coordinates.isNullOrEmpty()) {
                        val boundsBuilder = LatLngBounds.Builder()
                        val polylineOptions = PolylineOptions()
                            .width(10f)
                            .color(Color.parseColor("#FFB200"))

                        walk.coordinates.forEach { coord ->
                            val latLng = LatLng(coord.latitude, coord.longitude)
                            polylineOptions.add(latLng)
                            boundsBuilder.include(latLng)
                        }

                        googleMap.addPolyline(polylineOptions)

                        try {
                            val bounds = boundsBuilder.build()
                            val padding = dpToPx(30f)
                            googleMap.setOnMapLoadedCallback {
                                googleMap.moveCamera(
                                    CameraUpdateFactory.newLatLngBounds(bounds, padding)
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("WalkRVAdapter", "Bounds 렌더링 에러", e)
                        }
                    }
                }

                try {
                    val dateStr = walk.createdAt.split(".")[0]
                    val inputFormat =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("yy.MM.dd", Locale.getDefault())
                    val date = inputFormat.parse(dateStr)
                    walkDate.text = date?.let { outputFormat.format(it) } ?: ""
                } catch (e: Exception) {
                    walkDate.text = ""
                }

                val profileDrawable = when (walk.difficulty.lowercase()) {
                    "low" -> R.drawable.ic_easy
                    "high" -> R.drawable.ic_difficulty
                    else -> R.drawable.ic_normal
                }
                profileIv.setImageResource(profileDrawable)

                root.setOnClickListener {
                    onItemClick(walk.walkId)
                }

                ivMenu.setOnClickListener { view ->
                    onMenuClick(walk, view)
                }
            }
        }

        private fun addDefaultPlaceholder() {
            val cardView = CardView(itemView.context).apply {
                radius = dpToPx(12f).toFloat()
                cardElevation = 0f
                val params = LinearLayout.LayoutParams(dpToPx(120f), dpToPx(120f))
                params.marginEnd = dpToPx(8f)
                layoutParams = params
            }

            val iv = ImageView(itemView.context).apply {
                setImageResource(R.drawable.ic_place_img_default)
                scaleType = ImageView.ScaleType.CENTER_CROP
                setBackgroundColor(Color.parseColor("#F2F2F2"))
            }

            cardView.addView(iv)
            binding.imageContainer.addView(cardView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemMapwalkingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(walkList[position])
    }

    override fun getItemCount(): Int = walkList.size

    fun updateData(newWalks: ArrayList<Walk>) {
        walkList = newWalks
        notifyDataSetChanged()
    }
}