package com.gz.jey.go4lunch.utils

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat

object SetImageColor{

        fun changeDrawableColor(context: Context, icon: Int, newColor: Int): Drawable {
            val mDrawable = ContextCompat.getDrawable(context, icon)?.mutate() as Drawable
            mDrawable.setColorFilter(newColor, PorterDuff.Mode.SRC_IN)
            return mDrawable
        }

        fun changeBitmapColor(sourceBitmap: Bitmap, color: Int) : Bitmap {
            val resultBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0,
                    sourceBitmap.width - 10, sourceBitmap.height - 10)
            val p = Paint()
            val filter = LightingColorFilter(color, 1)
            p.colorFilter = filter
            val canvas = Canvas(resultBitmap)
            canvas.drawBitmap(resultBitmap, 0f, 0f, p)

            return resultBitmap
        }

}