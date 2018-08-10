package com.gz.jey.go4lunch.utils

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import com.gz.jey.go4lunch.R

class SetBottomMenuTab{

    companion object {
        fun onTabSelected(c: Context, v : AppCompatActivity, index: Int) {
            val map_i = v.findViewById(R.id.map_button_img) as ImageView
            val map_t = v.findViewById(R.id.map_button_txt) as TextView
            val list_i = v.findViewById(R.id.list_button_img) as ImageView
            val list_t = v.findViewById(R.id.list_button_txt) as TextView
            val people_i = v.findViewById(R.id.people_button_img) as ImageView
            val people_t = v.findViewById(R.id.people_button_txt) as TextView
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
                }
                1 -> {
                    list_i.setColorFilter(prim)
                    list_t.setTextColor(prim)
                }
                2 -> {
                    people_i.setColorFilter(prim)
                    people_t.setTextColor(prim)
                }
            }
        }

        fun changeDrawableColor(context: Context, icon: Int, newColor: Int): Drawable {
            val mDrawable = ContextCompat.getDrawable(context, icon)?.mutate() as Drawable
            mDrawable?.setColorFilter(newColor, PorterDuff.Mode.SRC_IN)
            return mDrawable
        }
    }
}