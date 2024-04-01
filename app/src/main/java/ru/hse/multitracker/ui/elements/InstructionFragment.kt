package ru.hse.multitracker.ui.elements

import android.os.Bundle
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
        binding.trainTestButton.setOnClickListener{view ->
            val bundle = Bundle()
            bundle.putLong("p_id", 0L)
            bundle.putInt("total", TRAIN_TOTAL)
            bundle.putInt("target", TRAIN_TARGET)
            bundle.putInt("speed", TRAIN_SPEED)
            bundle.putInt("time", TRAIN_TIME)
            view.findNavController().navigate(
                R.id.action_instructionFragment_to_testingFragment,
                bundle
            )
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}