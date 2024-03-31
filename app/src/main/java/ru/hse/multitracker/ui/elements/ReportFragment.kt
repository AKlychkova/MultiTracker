package ru.hse.multitracker.ui.elements

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import ru.hse.multitracker.R
import ru.hse.multitracker.databinding.FragmentReportBinding
import ru.hse.multitracker.ui.view_models.ReportViewModel
import kotlin.math.roundToInt

class ReportFragment : Fragment() {
    private val viewModel: ReportViewModel by viewModels { ReportViewModel.Factory }
    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { }
        callback.isEnabled = true
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        observeViewModel()
        val sId = arguments?.getLong("s_id")
        if (sId == -1L) {
            val accuracy = arguments?.getFloat("accuracy")
            val time = arguments?.getInt("time")
            setText((accuracy!! * 100).roundToInt(), time!!)
        } else {
            viewModel.getSession(sId!!)
        }
    }

    private fun setText(accuracyPercent: Int, time: Int) {
        binding.reportAccuracyTextview.text = getString(R.string.percent, accuracyPercent)
        binding.reportReactionTextview.text = getString(R.string.time, time)
    }

    private fun setListeners() {
        binding.homeButton.setOnClickListener(
            Navigation.createNavigateOnClickListener(R.id.action_reportFragment_to_homeFragment)
        )
        binding.statisticsButton2.setOnClickListener(
            Navigation.createNavigateOnClickListener(R.id.action_reportFragment_to_statisticsFragment)
        )
    }

    private fun observeViewModel() {
        viewModel.currentSession.observe(viewLifecycleOwner) {
            setText((it.accuracy * 100).roundToInt(), it.reactionTime)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}