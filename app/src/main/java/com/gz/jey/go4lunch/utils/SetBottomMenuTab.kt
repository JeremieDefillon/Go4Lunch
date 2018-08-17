package com.gz.jey.go4lunch.utils

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import com.gz.jey.go4lunch.R

object SetBottomMenuTab{

        fun onTabSelected(c: Context, v : AppCompatActivity, index: Int) {

            val map_i = v.findViewById<ImageView>(R.id.map_button_img)
            val map_t = v.findViewById<TextView>(R.id.map_button_txt)
            val list_i = v.findViewById<ImageView>(R.id.list_button_img)
            val list_t = v.findViewById<TextView>(R.id.list_button_txt)
            val people_i = v.findViewById<ImageView>(R.id.people_button_img)
            val people_t = v.findViewById<TextView>(R.id.people_button_txt)
            val prim = ContextCompat.getColor(c, R.color.colorPrimary)
            val black = ContextCompat.getColor(c, R.color.colorBlack)

            map_i.setImageDrawable(changeDrawableColor(c, R.drawable.map, black))
            map_t.setText(c.resources.getString(R.string.map))
            map_t.setTextColor(black)
            list_i.setImageDrawable(changeDrawableColor(c, R.drawable.list, black))
            list_t.setText(c.resources.getString(R.string.restaurants))
            list_t.setTextColor(black)
            people_i.setImageDrawable(changeDrawableColor(c, R.drawable.people, black))
            people_t.setText(c.resources.getString(R.string.workmates))
            people_t.setTextColor(black)

            when (index) {
                0 -> {
                    map_i.setColorFilter(prim)
                    map_t.setTextColor(prim)
                    list_i.setColorFilter(black)
                    people_i.setColorFilter(black)
                }
                1 -> {
                    list_i.setColorFilter(prim)
                    list_t.setTextColor(prim)
                    map_i.setColorFilter(black)
                    people_i.setColorFilter(black)
                }
                2 -> {
                    people_i.setColorFilter(prim)
                    people_t.setTextColor(prim)
                    map_i.setColorFilter(black)
                    list_i.setColorFilter(black)
                }
            }
        }

        fun changeDrawableColor(context: Context, icon: Int, newColor: Int): Drawable {
            val mDrawable = ContextCompat.getDrawable(context, icon)?.mutate() as Drawable
            mDrawable?.setColorFilter(newColor, PorterDuff.Mode.SRC_IN)
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