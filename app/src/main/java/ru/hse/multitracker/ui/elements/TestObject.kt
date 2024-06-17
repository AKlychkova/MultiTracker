package ru.hse.multitracker.ui.elements

import android.content.Context
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import ru.hse.multitracker.R
import kotlin.math.abs
import kotlin.random.Random

class TestObject private constructor(
    val button: ImageButton,
    val isTarget: Boolean,
    private var size: Int
) {
    //current direction (angle in radians from the x-axis clockwise)
    var direction: Double = 0.0

    /**
     * @return object's radius
     */
    fun getRadius(): Float {
        return size.toFloat() / 2
    }

    /**
     * @return x coordinate of object's centre
     */
    fun getCentreX(): Float {
        return button.x + getRadius()
    }

    /**
     * @return y coordinate of object's centre
     */
    fun getCentreY(): Float {
        return button.y + getRadius()
    }

    /**
     * Create a set of Objects
     * @param context Current context
     */
    class TestObjectListFactory(val context: Context) {
        // coordinates of already created objects
        private val usedCoordinates: MutableList<Pair<Float, Float>> = mutableListOf()

        /**
         * Create button
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
            do {
                button.x = Random.nextInt(xFrom, xUntil).toFloat()
                button.y = Random.nextInt(yFrom, yUntil).toFloat()
            }
            // check that objects do not overlap with each other
            while (usedCoordinates.any { coordinates ->
                    abs(coordinates.first - button.x) < context.resources.getDimensionPixelSize(R.dimen.object_size) &&
                            abs(coordinates.second - button.y) < context.resources.getDimensionPixelSize(
                        R.dimen.object_size
                    )
                })
            usedCoordinates.add(Pair(button.x, button.y))

            button.visibility = View.VISIBLE
            return button
        }

        fun createObjectList(
            total: Int,
            target: Int,
            xFrom: Int,
            xUntil: Int,
            yFrom: Int,
            yUntil: Int,
            targetListener: View.OnClickListener,
            notTargetListener: View.OnClickListener
        ): List<TestObject> {
            // create set
            val list: List<TestObject> = buildList {
                for (i in 1..total) {
                    add(
                        TestObject(
                            createButton(xFrom, xUntil, yFrom, yUntil), i <= target,
                            context.resources.getDimensionPixelSize(R.dimen.object_size)
                        )
                    )
                }
            }
            // set listeners
            list.forEach { obj ->
                if (obj.isTarget) {
                    obj.button.setOnClickListener(targetListener)
                    obj.button.setBackgroundResource(R.drawable.target_object_button)
                } else {
                    obj.button.setOnClickListener(notTargetListener)
                    obj.button.setBackgroundResource(R.drawable.ordinary_object_button)
                }
            }

            return list
        }
    }
}
