package com.example.dogcatsquare.ui.map.location

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource // 수정된 부분
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target // 수정된 부분
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.map.DetailImg
import com.example.dogcatsquare.databinding.ItemDetailImgBinding

class DetailImgRVAdapter(private val imgList: List<DetailImg>) :
    RecyclerView.Adapter<DetailImgRVAdapter.ViewHolder>() {

    companion object {
        private const val TAG = "DetailImgRVAdapter"
    }

    inner class ViewHolder(private val binding: ItemDetailImgBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(img: DetailImg) {
            if (img.isUrlImage()) {
                loadImageUrl(img.imageUrl!!)
            } else {
                loadResourceImage(img.detailImg ?: img.getDefaultResource())
            }
        }

        private fun loadImageUrl(url: String) {
            Log.d(TAG, "이미지 URL 로딩 시작: $url")

            Glide.with(binding.root.context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_place_img_default)
                .error(R.drawable.ic_place_img_default)
                .fitCenter()  // centerCrop 대신 fitCenter 사용
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e(TAG, "URL에서 이미지 로딩 실패: $url", e)
                        e?.logRootCauses(TAG)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d(TAG, "이미지 로딩 성공: $url")
                        return false
                    }
                })
                .into(binding.detailImg)
        }

        private fun loadResourceImage(resourceId: Int) {
            Log.d(TAG, "리소스 이미지 로딩: $resourceId")
            binding.detailImg.setImageResource(resourceId)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemDetailImgBinding = ItemDetailImgBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(imgList[position])
    }

    override fun getItemCount(): Int = imgList.size
}