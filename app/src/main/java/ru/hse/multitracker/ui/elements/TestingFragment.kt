package ru.hse.multitracker.ui.elements

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.Path
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.LinearInterpolator
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
import kotlin.math.max
import kotlin.random.Random


class TestingFragment : Fragment() {

    private val viewModel: TestingViewModel by viewModels { TestingViewModel.Factory }
    private var _binding: FragmentTestingBinding? = null
    private val binding get() = _binding!!

    private lateinit var testSystem: TestSystem

//    // total amount of attempts
//    private var totalAttemptCount: Int = 5
//
//    // the number of attempts that have passed
//    private var attemptCount: Int = 0

    // array of objects
    private lateinit var objects: Set<Object>

//    // the number of right answers
//    private var rightAnswersCount: Int = 0
//
//    // sum of reaction time in seconds
//    private var sumReactionTime: Int = 0

    // screen borders for objects
    private var rightBorder: Int = 0
    private var bottomBorder: Int = 0

//    // amount of objects have been clicked in this attempt
//    private var objectsClicked: Int = 0

    // animators that are running at the moment
    private var currentAnimators: ArrayList<ObjectAnimator> = ArrayList()

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
            objects.forEach {obj ->
                obj.button.setBackgroundResource(R.drawable.ordinary_object_button)
                obj.button.setImageDrawable(null)
            }
            animate()
        }
    }

    /**
     * Generate random path consisting of cubic bezier curves
     * @param startX starting point X coordinate
     * @param startY starting point Y coordinate
     * @curveAmount amount of curves in the path
     * @return random path
     */
    private fun randomPath(startX: Float, startY: Float, curveAmount: Int): Path {
        val path = Path()
        // move to starting point
        path.moveTo(startX, startY)

        for (i in 1..curveAmount) {
            // add random curve
            path.cubicTo(
                Random.nextInt(rightBorder).toFloat(),
                Random.nextInt(bottomBorder).toFloat(),
                Random.nextInt(rightBorder).toFloat(),
                Random.nextInt(bottomBorder).toFloat(),
                Random.nextInt(rightBorder).toFloat(),
                Random.nextInt(bottomBorder).toFloat()
            )
        }
        return path
    }

    private fun animate() {
        viewModel.currentSession.value?.let { currentSession ->
            // get movement time and speed of current session
            val time = currentSession.movementTime
            val speed = currentSession.speed
            // calculate amount of curves
            val curveAmount: Int = max((time * speed / 30.0).toInt(), 1)

            objects.forEach{obj ->
                // generate path
                val path = randomPath(obj.button.x, obj.button.y, curveAmount)
                // create animator
                val animator = ObjectAnimator.ofFloat(obj.button, View.X, View.Y, path).apply {
                    // set duration to movementTime
                    duration = (time * 1000).toLong()
                    // set linear interpolator to make speed linear
                    interpolator = LinearInterpolator()
                }
                currentAnimators.add(animator)
            }
            // set animation end listener
            currentAnimators.getOrNull(0)?.addListener(object : Animator.AnimatorListener {
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
            val factory = ObjectSetFactory(requireContext())
            objects = factory.createObjectSet(
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