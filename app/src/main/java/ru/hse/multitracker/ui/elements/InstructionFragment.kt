package ru.hse.multitracker.ui.elements

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import ru.hse.multitracker.R
import ru.hse.multitracker.databinding.FragmentInstructionBinding

class InstructionFragment : Fragment() {
    private var _binding: FragmentInstructionBinding? = null
    private val binding get() = _binding!!

    // Default settings for train test session
    private val TRAIN_TOTAL = 2
    private val TRAIN_TARGET = 1
    private val TRAIN_SPEED = 1
    private val TRAIN_TIME = 5

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInstructionBinding.inflate(inflater, container, false)

        // set listener to trainTestButton
        binding.trainTestButton.setOnClickListener{view ->
            // go to TrainingFragment and send train settings
            val bundle = Bundle()
            // patient id = -1, because train session is not associated with any patient
            bundle.putLong("p_id", -1L)
            bundle.putInt("total", TRAIN_TOTAL)
            bundle.putInt("target", TRAIN_TARGET)
            bundle.putInt("speed", TRAIN_SPEED)
            bundle.putInt("time", TRAIN_TIME)
            view.findNavController().navigate(
                R.id.action_instructionFragment_to_testingFragment,
                bundle
            )
        }

        // to perform the movement action
        binding.instructionTextview.movementMethod = ScrollingMovementMethod()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}