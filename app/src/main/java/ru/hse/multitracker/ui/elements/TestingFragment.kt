package ru.hse.multitracker.ui.elements

import android.animation.Animator
import android.animation.TimeAnimator
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageButton
import androidx.activity.addCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import ru.hse.multitracker.R
import ru.hse.multitracker.databinding.FragmentTestingBinding
import ru.hse.multitracker.ui.view_models.TestingViewModel
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random


class TestingFragment : Fragment() {

    private val viewModel: TestingViewModel by viewModels { TestingViewModel.Factory }
    private var _binding: FragmentTestingBinding? = null
    private val binding get() = _binding!!

    private lateinit var testSystem: TestSystem

    // array of objects
    private lateinit var objects: List<Object>

    // screen borders for objects
    private var rightBorder: Int = 0
    private var bottomBorder: Int = 0

    // animators that are running at the moment
    private var currentAnimators: ArrayList<Animator> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //define binding
        _binding = FragmentTestingBinding.inflate(inflater, container, false)

        // handle the back button event
        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // delete current test session
            viewModel.deleteSession()
            // cancel current animations
            currentAnimators.forEach { animator -> animator.cancel() }
            // go back
            findNavController().popBackStack()
        }
        callback.isEnabled = true
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // calculate borders
        val viewTreeObserver = binding.field.viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.field.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    rightBorder =
                        binding.field.right - resources.getDimensionPixelSize(R.dimen.object_size)
                    bottomBorder =
                        binding.field.bottom - resources.getDimensionPixelSize(R.dimen.object_size)
                    // set observers
                    observeViewModel()
                    // set listeners
                    setListeners()
                }
            })
        }
        createSession()
    }

    private fun setListeners() {
        binding.goButton.setOnClickListener {
            // hide irrelevant views
            binding.goButton.visibility = View.GONE
            binding.chronometer.visibility = View.GONE
            // make all objects looks same
            objects.forEach { obj ->
                obj.button.setBackgroundResource(R.drawable.ordinary_object_button)
                obj.button.setImageDrawable(null)
            }
            animate()
        }
    }
    
    private fun animate() {
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
                    if (objects[i].button.x <= 0 && (objects[i].direction > Math.PI / 2 || objects[i].direction < -Math.PI / 2)) {
                        objects[i].direction = Math.PI - objects[i].direction
                    }
                    // right border
                    if (objects[i].button.x >= rightBorder && (objects[i].direction > -Math.PI / 2 && objects[i].direction < Math.PI / 2)) {
                        objects[i].direction = Math.PI - objects[i].direction
                    }
                    // top border
                    if (objects[i].button.y <= 0 && (objects[i].direction < 0 && objects[i].direction > -Math.PI)) {
                        objects[i].direction = -objects[i].direction
                    }
                    // bottom border
                    if (objects[i].button.y >= bottomBorder && (objects[i].direction > 0 && objects[i].direction < Math.PI)) {
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
            currentAnimators.add(animator)
        }

        // set animation end listener
        currentAnimators.getOrNull(0)?.addListener(
            object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator) {}
                override fun onAnimationCancel(p0: Animator) {}
                override fun onAnimationRepeat(p0: Animator) {}

                override fun onAnimationEnd(p0: Animator) {
                    // make objects clickable
                    objects.forEach { obj -> obj.button.isClickable = true }
                    // start chronometer
                    binding.chronometer.visibility = View.VISIBLE
                    binding.chronometer.base = SystemClock.elapsedRealtime()
                    binding.chronometer.start()
                }
            })
        // start animations
        currentAnimators.forEach { animator -> animator.start() }
    }

    /**
     * create current session instance
     */
    private fun createSession() {
        // get session settings from arguments
        val pId = arguments?.getLong("p_id")
        val total = arguments?.getInt("total")
        val target = arguments?.getInt("target")
        val speed = arguments?.getInt("speed")
        val time = arguments?.getInt("time")
        if (pId != null && total != null && target != null && speed != null && time != null) {
            // check if session is train(patient id <= 0) or not
            if (pId <= 0) {
                viewModel.createTrainingSession(total, target, speed, time)
            } else {
                viewModel.createSession(pId, total, target, speed, time)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.currentSession.observe(viewLifecycleOwner) { session ->
            // define test system
            testSystem =
                TestSystem(session, if (viewModel.isCurrentSessionTrain() == true) 1 else 5)
            // create objects
            val factory = Object.ObjectListFactory(requireContext())
            objects = factory.createObjectList(
                session.totalAmount,
                session.targetAmount,
                0,
                rightBorder,
                0,
                bottomBorder,
                {
                    // make green and draw check sign
                    it.setBackgroundResource(R.drawable.target_object_button)
                    (it as ImageButton).setImageDrawable(
                        AppCompatResources.getDrawable(
                            requireContext(),
                            R.drawable.baseline_check_24
                        )
                    )

                    val isFinished = testSystem.rightAnswer()
                    if (isFinished) {
                        finishAttempt(it)
                    }
                    it.isClickable = false
                },
                { // make red and draw cross sign
                    it.setBackgroundResource(R.drawable.mistake_object_button)
                    (it as ImageButton).setImageDrawable(
                        AppCompatResources.getDrawable(
                            requireContext(),
                            R.drawable.baseline_clear_24
                        )
                    )
                    // increment objects clicked number
                    val isFinished = testSystem.mistakeAnswer()
                    if (isFinished) {
                        finishAttempt(it)
                    }
                    it.isClickable = false
                }
            )

            // add objects to layout
            objects.forEach { obj -> binding.field.addView(obj.button) }

            // make objects not clickable
            objects.forEach { obj -> obj.button.isClickable = false }

            // make goButton visible
            binding.goButton.visibility = View.VISIBLE
        }
    }

    private fun finishAttempt(view: View) {
        // make objects not clickable
        objects.forEach { obj -> obj.button.isClickable = false }
        // stop chronometer
        binding.chronometer.stop()
        // calculate reaction time
        val reactionTime =
            ((SystemClock.elapsedRealtime() - binding.chronometer.base) / 1000).toInt()
        // clear list of current animators
        currentAnimators.clear()

        // check if all attempts are finished
        if (testSystem.finishAttempt(reactionTime)) {
            viewModel.currentSession.value?.let { currentSession ->
                // calculate mean results
                val meanReactionTime = testSystem.getMeanReactionTime()
                val accuracy = testSystem.getMeanAccuracy()
                // update current test session
                viewModel.setResults(meanReactionTime, accuracy)
                // go to reportFragment
                val bundle = Bundle()
                bundle.putLong("s_id", currentSession.id)
                bundle.putInt("time", currentSession.reactionTime)
                bundle.putFloat("accuracy", currentSession.accuracy.toFloat())
                view.findNavController().navigate(
                    R.id.action_testingFragment_to_reportFragment,
                    bundle
                )
            }
        } else {
            // show target objects
            objects.forEach { obj ->
                if (obj.isTarget)
                    obj.button.setBackgroundResource(R.drawable.target_object_button)
            }
            // make goButton visible
            binding.goButton.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}