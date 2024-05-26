package ru.hse.multitracker.ui.elements

import android.animation.Animator
import android.animation.TimeAnimator
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class TestObjectAnimator {
    fun animate(
        testSystem: TestSystem,
        objects: List<TestObject>,
        xFrom: Int,
        xUntil: Int,
        yFrom: Int,
        yUntil: Int,
    ): List<Animator> {
        val animators: ArrayList<Animator> = ArrayList()
        // get movement time and speed of current session
        val time = testSystem.session.movementTime * 1000
        val speed = testSystem.session.speed

        for (i in objects.indices) {
            // generate direction
            objects[i].direction = Random.nextDouble(-Math.PI, Math.PI)
            // create animator
            val animator = TimeAnimator()
            // add listener for every animation frame
            animator.setTimeListener { _, totalTime, _ ->
                // check if time has run out
                if (totalTime >= time) {
                    animator.end()
                } else {
                    // calculate speed projections on the x and y axes
                    val dx = speed * cos(objects[i].direction).toFloat()
                    val dy = speed * sin(objects[i].direction).toFloat()

                    // move objects
                    objects[i].button.x += dx
                    objects[i].button.y += dy

                    // check for collisions with borders
                    // left border
                    if (objects[i].button.x <= xFrom && (objects[i].direction > Math.PI / 2 || objects[i].direction < -Math.PI / 2)) {
                        objects[i].direction = Math.PI - objects[i].direction
                    }
                    // right border
                    if (objects[i].button.x >= xUntil && (objects[i].direction > -Math.PI / 2 && objects[i].direction < Math.PI / 2)) {
                        objects[i].direction = Math.PI - objects[i].direction
                    }
                    // top border
                    if (objects[i].button.y <= yFrom && (objects[i].direction < 0 && objects[i].direction > -Math.PI)) {
                        objects[i].direction = -objects[i].direction
                    }
                    // bottom border
                    if (objects[i].button.y >= yUntil && (objects[i].direction > 0 && objects[i].direction < Math.PI)) {
                        objects[i].direction = -objects[i].direction
                    }
                    // set up the direction so that it is between -pi and pi
                    while (objects[i].direction > Math.PI) objects[i].direction -= 2 * Math.PI
                    while (objects[i].direction < -Math.PI) objects[i].direction += 2 * Math.PI

                    // check for collisions with another objects
                    for (j in i + 1..<objects.size) {
                        // calculate distance between objects' centres
                        val xDiff = objects[j].getCentreX() - objects[i].getCentreX()
                        val yDiff = objects[j].getCentreY() - objects[i].getCentreY()
                        val distance = sqrt(xDiff * xDiff + yDiff * yDiff)
                        if (distance <= objects[i].getRadius() + objects[j].getRadius()) {

                            // calculate the angle of the beam (w) from i object center to j object center
                            var angleW = atan(yDiff.toDouble() / xDiff)
                            if (objects[j].getCentreX() - objects[i].getCentreX() < 0) {
                                angleW += Math.PI
                                while (angleW > Math.PI) angleW -= 2 * Math.PI
                                while (angleW < -Math.PI) angleW += 2 * Math.PI
                            }
                            // angles between w and objects' directions
                            var w1 = objects[i].direction - angleW
                            var w2 = objects[j].direction - angleW

                            // speed projections onto the beam w before the collision
                            var dw1 = speed * cos(w1)
                            var dw2 = speed * cos(w2)

                            // speed projections onto the perpendicular to the w beam before the collision
                            val dwt1 = speed * sin(w1)
                            val dwt2 = speed * sin(w2)

                            // speed projections onto the beam w after the collision are swapped
                            val temp = dw1
                            dw1 = dw2
                            dw2 = temp

                            // calculate angles between w and new directions
                            w1 = atan(dwt1 / dw1)
                            if (dw1 < 0) w1 += Math.PI
                            w2 = atan(dwt2 / dw2)
                            if (dw2 < 0) w2 += Math.PI

                            // change objects' directions
                            objects[i].direction = angleW + w1
                            while (objects[i].direction > Math.PI) objects[i].direction -= 2 * Math.PI
                            while (objects[i].direction < -Math.PI) objects[i].direction += 2 * Math.PI

                            objects[j].direction = angleW + w2
                            while (objects[j].direction > Math.PI) objects[j].direction -= 2 * Math.PI
                            while (objects[j].direction < -Math.PI) objects[j].direction += 2 * Math.PI
                        }
                    }
                }
            }
            animators.add(animator)
        }
        return animators
    }
}