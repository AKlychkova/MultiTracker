package ru.hse.multitracker.ui.elements

import android.content.Context
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import ru.hse.multitracker.R
import kotlin.random.Random

/**
 * Create a set of Objects
 * @param context Current context
 */
class ObjectSetFactory(val context: Context) {
    /**
     * Create object
     */
    private fun createButton(
        xFrom: Int,
        xUntil: Int,
        yFrom: Int,
        yUntil: Int
    ): ImageButton {
        val button = ImageButton(context)
        button.layoutParams = RelativeLayout.LayoutParams(
            context.resources.getDimensionPixelSize(R.dimen.object_size),
            context.resources.getDimensionPixelSize(R.dimen.object_size)
        )
        button.setPadding(context.resources.getDimensionPixelSize(R.dimen.object_padding))
        button.scaleType = ImageView.ScaleType.FIT_CENTER
        button.imageTintList = ContextCompat.getColorStateList(context, R.color.white)
        button.contentDescription = context.getString(R.string.`object`)

        // generate coordinates
        button.translationX = Random.nextInt(xFrom, xUntil).toFloat()
        button.translationY = Random.nextInt(yFrom, yUntil).toFloat()

        button.visibility = View.VISIBLE
        return button
    }

    fun createObjectSet(
        total: Int,
        target: Int,
        xFrom: Int,
        xUntil: Int,
        yFrom: Int,
        yUntil: Int,
        targetListener: View.OnClickListener,
        notTargetListener: View.OnClickListener
    ): Set<Object> {
        // create set
        val set: Set<Object> = buildSet {
            for (i in 1..total) {
                add(Object(createButton(xFrom, xUntil, yFrom, yUntil), i <= target))
            }
        }
        // set listeners
        set.forEach { obj ->
            if (obj.isTarget) {
                obj.button.setOnClickListener(targetListener)
                obj.button.setBackgroundResource(R.drawable.target_object_button)
            } else {
                obj.button.setOnClickListener(notTargetListener)
                obj.button.setBackgroundResource(R.drawable.ordinary_object_button)
            }
        }

        return set
    }
}