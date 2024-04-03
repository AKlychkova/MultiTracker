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
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.activity.addCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
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
    private var totalAttemptCount: Int = 5
    private var attemptCount: Int = 0
    private var objects: ArrayList<ImageButton> = ArrayList()
    private var rightAnswersCount: Int = 0
    private var sumReactionTime: Int = 0
    private var rightBorder: Int = 0
    private var bottomBorder: Int = 0
    private var objectsClicked: Int = 0
    private var currentAnimators: ArrayList<ObjectAnimator> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestingBinding.inflate(inflater, container, false)
        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.deleteSession()
            currentAnimators.forEach { animator -> animator.cancel() }
            findNavController().popBackStack()
        }
        callback.isEnabled = true
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewTreeObserver = binding.field.viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.field.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    rightBorder =
                        binding.field.right - resources.getDimensionPixelSize(R.dimen.object_size)
                    bottomBorder =
                        binding.field.bottom - resources.getDimensionPixelSize(R.dimen.object_size)
                    observeViewModel()
                    setListeners()
                }
            })
        }
        createSession()
    }

    private fun setListeners() {
        binding.goButton.setOnClickListener {
            binding.goButton.visibility = View.GONE
            binding.chronometer.visibility = View.GONE
            animate()
        }
    }

    private fun randomPath(startX: Float, startY: Float, pointsAmount: Int): Path {
        val path = Path()
        path.moveTo(startX, startY)

        for (i in 1..pointsAmount) {
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
        for (i in 0..<objects.size) {
            objects[i].setBackgroundResource(R.drawable.ordinary_object_button)
            objects[i].setImageDrawable(null)
            val pointsAmount: Int = max((viewModel.currentSession.value!!.movementTime *
                    viewModel.currentSession.value!!.speed / 30.0).toInt(), 1)
            val path = randomPath(objects[i].x, objects[i].y, pointsAmount)
            val animator = ObjectAnimator.ofFloat(objects[i], View.X, View.Y, path).apply {
                duration = (viewModel.currentSession.value!!.movementTime * 1000).toLong()
                interpolator = LinearInterpolator()
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(p0: Animator) {}

                    override fun onAnimationEnd(p0: Animator) {
                        objects[i].isClickable = true
                        if (i == 0) {
                            binding.chronometer.visibility = View.VISIBLE
                            binding.chronometer.base = SystemClock.elapsedRealtime()
                            binding.chronometer.start()
                        }
                    }

                    override fun onAnimationCancel(p0: Animator) {}

                    override fun onAnimationRepeat(p0: Animator) {}
                })
                start()
            }
            currentAnimators.add(animator)
        }
    }

    private fun createSession() {
        val pId = arguments?.getLong("p_id")
        val total = arguments?.getInt("total")
        val target = arguments?.getInt("target")
        val speed = arguments?.getInt("speed")
        val time = arguments?.getInt("time")
        if (pId == 0L) {
            totalAttemptCount = 1
            if (total != null && target != null && speed != null && time != null) {
                viewModel.createTrainingSession(total, target, speed, time)
            }
        } else if (pId != null && total != null && target != null && speed != null && time != null) {
            viewModel.createSession(pId, total, target, speed, time)
        }
    }

    private fun observeViewModel() {
        viewModel.currentSession.observe(viewLifecycleOwner) {
            setUpTest()
        }
    }

    private fun createButton(): ImageButton {
        val button = ImageButton(requireContext())
        button.layoutParams = RelativeLayout.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.object_size),
            resources.getDimensionPixelSize(R.dimen.object_size)
        )
        button.setBackgroundResource(R.drawable.ordinary_object_button)
        button.setPadding(resources.getDimensionPixelSize(R.dimen.object_padding))
        button.scaleType = ImageView.ScaleType.FIT_CENTER
        button.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)
        button.contentDescription = getString(R.string.`object`)
        button.translationX = Random.nextInt(rightBorder).toFloat()
        button.translationY = Random.nextInt(bottomBorder).toFloat()
        button.visibility = View.VISIBLE
        binding.field.addView(button)
        return button
    }

    private fun setUpTest() {
        val total = viewModel.currentSession.value!!.totalAmount
        val target = viewModel.currentSession.value!!.targetAmount
        objectsClicked = 0
        binding.chronometer.visibility = View.GONE
        for (i in 0..<total) {
            objects.add(createButton())
            if (i < target) {
                objects[i].setBackgroundResource(R.drawable.target_object_button)
                objects[i].setOnClickListener {
                    objects[i].setBackgroundResource(R.drawable.target_object_button)
                    objects[i].setImageDrawable(
                        AppCompatResources.getDrawable(
                            requireContext(),
                            R.drawable.baseline_check_24
                        )
                    )
                    rightAnswersCount += 1
                    objectsClicked += 1
                    if (objectsClicked >= target) {
                        finishAttempt(it)
                    }
                }
            } else {
                objects[i].setOnClickListener {
                    objects[i].setBackgroundResource(R.drawable.mistake_object_button)
                    objects[i].setImageDrawable(
                        AppCompatResources.getDrawable(
                            requireContext(),
                            R.drawable.baseline_clear_24
                        )
                    )
                    objectsClicked += 1
                    if (objectsClicked >= target) {
                        finishAttempt(it)
                    }
                }
            }
            objects[i].isClickable = false
        }
        binding.goButton.visibility = View.VISIBLE
    }

    private fun finishAttempt(view: View) {
        objects.forEach { it.isClickable = false }
        binding.chronometer.stop()
        sumReactionTime += ((SystemClock.elapsedRealtime() - binding.chronometer.base) / 1000).toInt()
        currentAnimators.clear()
        attemptCount += 1
        if (attemptCount >= totalAttemptCount) {
            countMeanResults()
            val bundle = Bundle()
            bundle.putLong("s_id", viewModel.currentSession.value!!.id)
            bundle.putInt("time", viewModel.currentSession.value!!.reactionTime)
            bundle.putFloat("accuracy", viewModel.currentSession.value!!.accuracy.toFloat())
            view.findNavController().navigate(
                R.id.action_testingFragment_to_reportFragment,
                bundle
            )
        } else {
            objectsClicked = 0
            for (i in 0..<viewModel.currentSession.value!!.targetAmount) {
                objects[i].setBackgroundResource(R.drawable.target_object_button)
            }
            binding.goButton.visibility = View.VISIBLE
        }
    }

    private fun countMeanResults() {
        val meanReactionTime: Int = sumReactionTime / totalAttemptCount
        val accuracy: Double =
            rightAnswersCount.toDouble() / (viewModel.currentSession.value!!.targetAmount * totalAttemptCount)
        viewModel.setResults(meanReactionTime, accuracy)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}