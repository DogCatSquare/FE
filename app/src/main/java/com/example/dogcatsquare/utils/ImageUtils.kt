package com.example.dogcatsquare.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import java.io.InputStream

object ImageUtils {
    private const val TAG = "ImageUtils"

    /**
     * Inspects the EXIF metadata of the image specified by the Uri, and if there is a rotation tag,
     * rotates the input bitmap accordingly and returns the correctly-oriented bitmap.
     * The input bitmap is recycled if a new rotated bitmap is created.
     */
    fun getRotatedBitmap(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val exif = ExifInterface(inputStream)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                val degrees = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
                if (degrees != 0) {
                    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
                    val rotatedBitmap = Bitmap.createBitmap(
                        bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                    )
                    if (rotatedBitmap != bitmap) {
                        bitmap.recycle()
                    }
                    return rotatedBitmap
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking EXIF orientation: ${e.message}", e)
        } finally {
            try {
                inputStream?.close()
            } catch (e: Exception) {
                // ignore
            }
        }
        return bitmap
    }
}
