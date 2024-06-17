package ru.hse.multitracker.ui.elements

import android.animation.Animator
import android.animation.TimeAnimator
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class TestObjectAnimator(
    private val testSystem: TestSystem,
    private val xFrom: Int,
    private val xUntil: Int,
    private val yFrom: Int,
    private val yUntil: Int,
) {
    /**
     * @param alpha angle in radians
     * @return angle in [0; 2*pi)
     */
    private fun normalizeAngle(alpha: Double): Double {
        return (alpha % (2*Math.PI) + (2*Math.PI)) % (2*Math.PI)
    }

    private fun checkBorderCollisions(testObject: TestObject) {
        // left border
        if (testObject.button.x <= xFrom && (testObject.direction > Math.PI / 2 && testObject.direction < 1.5 * Math.PI)) {
            testObject.direction = normalizeAngle(Math.PI - testObject.direction)
        }
        // right border
        if (testObject.button.x >= xUntil && (testObject.direction > 1.5 * Math.PI || testObject.direction < Math.PI / 2)) {
            testObject.direction = normalizeAngle(Math.PI - testObject.direction)
        }
        // top border
        if (testObject.button.y <= yFrom && (testObject.direction > Math.PI && testObject.direction < 2 * Math.PI)) {
            testObject.direction = normalizeAngle(-testObject.direction)
        }
        // bottom border
        if (testObject.button.y >= yUntil && (testObject.direction > 0 && testObject.direction < Math.PI)) {
            testObject.direction = normalizeAngle(-testObject.direction)
        }
    }
    
    private fun checkObjectsCollision(objectA:TestObject, objectB:TestObject) {
        val speed = testSystem.session.speed
        // calculate distance between objects' centres
        val xDiff = objectB.getCentreX() - objectA.getCentreX()
        val yDiff = objectB.getCentreY() - objectA.getCentreY()
        val distance = sqrt(xDiff * xDiff + yDiff * yDiff)
        if (distance <= objectA.getRadius() + objectB.getRadius()) {
            // calculate the angle of the beam (w) from A center to B center
            var angleW = atan(yDiff.toDouble() / xDiff)
            if (objectB.getCentreX() - objectA.getCentreX() < 0) {
                angleW += Math.PI
            }
            angleW = normalizeAngle(angleW)

            // solve overlay
            if (distance < objectA.getRadius() + objectB.getRadius()) {
                val overlay = objectA.getRadius() + objectB.getRadius() - distance
                // move object B along w
                objectB.button.x += overlay * cos(angleW).toFloat()
                objectB.button.y += overlay * sin(angleW).toFloat()
            }

            // angles between w and objects' directions
            var w1 = objectA.direction - angleW
            var w2 = objectB.direction - angleW

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
            objectA.direction = normalizeAngle(angleW + w1)
            objectB.direction = normalizeAngle(angleW + w2)
        }
    }

    fun animate(
        objects: List<TestObject>,
    ): List<Animator> {
        val animators: ArrayList<Animator> = ArrayList()
        // get movement time and speed of current session
        val time = testSystem.session.movementTime * 1000
        val speed = testSystem.session.speed

        for (i in objects.indices) {
            // generate direction
            objects[i].direction = Random.nextDouble(0.0, 2 * Math.PI)
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
                    checkBorderCollisions(objects[i])

                    // check for collisions with another objects
                    for (j in i + 1..<objects.size) {
                        checkObjectsCollision(objects[i], objects[j])
                    }
                }
            }
            animators.add(animator)
        }
        return animators
    }
}