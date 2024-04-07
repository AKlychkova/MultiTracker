package ru.hse.multitracker.ui.elements

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
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
        // define binding
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        // handle the back button event
        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack(R.id.settingsFragment, false)
        }
        callback.isEnabled = true
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        observeViewModel()

        // get session id
        val sessionId = arguments?.getLong("s_id")

        // check if session is train(id <= 0)
        if (sessionId != null) {
            if (sessionId <= 0L) {
                // get results from arguments and set them to TextViews
                val accuracy = arguments?.getFloat("accuracy")
                val time = arguments?.getInt("time")
                if (accuracy != null && time != null) {
                    setResultsText((accuracy * 100).roundToInt(), time)
                }
            } else {
                // get test session info from database
                viewModel.getSession(sessionId)
            }
        }
    }

    /**
     * set results to TextViews
     */
    private fun setResultsText(accuracyPercent: Int, time: Int) {
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
            setResultsText((it.accuracy * 100).roundToInt(), it.reactionTime)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}