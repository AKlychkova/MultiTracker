package ru.hse.multitracker.ui.elements

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.LegendRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import ru.hse.multitracker.R
import ru.hse.multitracker.data.database.entities.TestSession
import ru.hse.multitracker.databinding.FragmentStatisticsBinding
import ru.hse.multitracker.ui.adapters.PatientNameAdapter
import ru.hse.multitracker.ui.view_models.StatisticsViewModel
import kotlin.math.roundToInt


class StatisticsFragment : Fragment() {

    private val viewModel: StatisticsViewModel by viewModels { StatisticsViewModel.Factory }
    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private lateinit var patientNameAdapter: PatientNameAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        patientNameAdapter = PatientNameAdapter(listOf()) { fullName ->
            viewModel.onPatientClicked(fullName)
        }
        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_statisticsFragment_to_homeFragment)
        }
        callback.isEnabled = true
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = patientNameAdapter
        viewModel.updateCurrentPatient()
        observeViewModel()
        setListeners()
    }

    private fun setListeners() {
        binding.deleteButton.setOnClickListener {
            showDeleteAlertDialog()
        }

        binding.toUpdateFormButton.setOnClickListener { view ->
            val bundle = Bundle()
            val currentId = viewModel.currentPatient.value?.patient?.id
            currentId?.let {
                bundle.putLong("id", it)
                view.findNavController().navigate(
                    R.id.action_statisticsFragment_to_formFragment,
                    bundle
                )
            }
        }
    }

    private fun showDeleteAlertDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.delete_alert_message))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> viewModel.onDeleteClicked() }
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .show()
    }

    private fun observeViewModel() {
        viewModel.allPatientNames.observe(viewLifecycleOwner) {
            binding.hintNoUsersTextview.visibility = if (it.isEmpty()) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
            it.let { patientNameAdapter.setData(it) }
        }

        viewModel.currentPatient.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.hintTextview.visibility = View.VISIBLE
                binding.statisticsGroup.visibility = View.INVISIBLE
            } else {
                binding.hintTextview.visibility = View.INVISIBLE
                binding.statisticsGroup.visibility = View.VISIBLE
                binding.infoNameTextview.text = getString(
                    R.string.full_name_info,
                    it.patient.surname,
                    it.patient.name,
                    it.patient.patronymic ?: ""
                )
                if (it.patient.age == null) {
                    binding.infoAgeTextview.visibility = View.GONE
                } else {
                    binding.infoAgeTextview.text = getString(R.string.age_info, it.patient.age)
                }
                if (it.patient.diagnosis == null) {
                    binding.infoDiagnosisTextview.visibility = View.GONE
                } else {
                    binding.infoDiagnosisTextview.text = getString(
                        R.string.diagnosis_info,
                        it.patient.diagnosis
                    )
                }
                if (it.patient.sex == null) {
                    binding.infoSexTextview.visibility = View.GONE
                } else {
                    binding.infoSexTextview.text =
                        getString(if (it.patient.sex == false) R.string.female else R.string.male)
                }
                if (it.sessions.isEmpty()) {
                    binding.testInfo.visibility = View.INVISIBLE
                    binding.hintNoTests.visibility = View.VISIBLE
                } else {
                    binding.testInfo.visibility = View.VISIBLE
                    binding.hintNoTests.visibility = View.INVISIBLE
                    calculateResults(it.sessions)
                    showGraphs(it.sessions)
                }
            }
        }
    }

    private fun showGraphs(sessions: List<TestSession>) {
        binding.graphview.removeAllSeries()
        binding.graphview.secondScale.removeAllSeries()

        val sortedSessions = sessions.sortedBy { it.date }

        val accuracySeries = LineGraphSeries(
            sortedSessions.map { session -> DataPoint(session.date, session.accuracy * 100) }
                .toTypedArray()
        )
        accuracySeries.color = ContextCompat.getColor(requireContext(), R.color.green)
        accuracySeries.title = getString(R.string.accuracy)
        accuracySeries.isDrawDataPoints = true
        accuracySeries.isDrawBackground = true
        accuracySeries.backgroundColor =
            ContextCompat.getColor(requireContext(), R.color.green_semitransparent)
        binding.graphview.addSeries(accuracySeries)

        val reactionSeries = LineGraphSeries(
            sortedSessions.map { session ->
                DataPoint(
                    session.date,
                    session.reactionTime.toDouble()
                )
            }
                .toTypedArray()
        )
        reactionSeries.color = ContextCompat.getColor(requireContext(), R.color.red)
        reactionSeries.title = getString(R.string.reaction_time)
        reactionSeries.isDrawDataPoints = true
        reactionSeries.isDrawBackground = true
        reactionSeries.backgroundColor =
            ContextCompat.getColor(requireContext(), R.color.red_semitransparent)
        binding.graphview.getSecondScale().addSeries(reactionSeries)

        // set label formatter
        binding.graphview.gridLabelRenderer.setLabelFormatter(object : DefaultLabelFormatter() {
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                return if (isValueX) {
                    // show dates for x values
                    val dateFormat = android.text.format.DateFormat.getDateFormat(context)
                    dateFormat.format(value.toLong())
                } else {
                    // show percent for accuracy values
                    super.formatLabel(value, isValueX) + "%"
                }
            }
        })

        // set manual y bounds
        binding.graphview.getSecondScale().setMinY(0.0)
        binding.graphview.getSecondScale()
            .setMaxY(sortedSessions.maxOf { it.reactionTime }.toDouble())
        binding.graphview.viewport.setMinY(0.0)
        binding.graphview.viewport.setMaxY(100.0)
        binding.graphview.viewport.isYAxisBoundsManual = true

        // set manual x bounds
        binding.graphview.viewport.setMinX(sortedSessions.first().date.time.toDouble())
        binding.graphview.viewport.setMaxX(sortedSessions.last().date.time.toDouble())
        binding.graphview.viewport.isXAxisBoundsManual = true

        // activate horizontal zooming and scaling
        binding.graphview.viewport.isScalable = true

        // activate horizontal scrolling
        binding.graphview.viewport.isScrollable = true

        // as we use dates as labels, the human rounding to nice readable numbers is not necessary
        binding.graphview.gridLabelRenderer.setHumanRounding(false)

        // set number of horizontal labels
        //binding.graphview.gridLabelRenderer.numHorizontalLabels = min(4,sortedSessions.size)

        // display legend
        binding.graphview.legendRenderer.isVisible = true
        binding.graphview.legendRenderer.align = LegendRenderer.LegendAlign.TOP
    }

    private fun calculateResults(sessions: List<TestSession>) {
        val bestAccuracy = (sessions.maxOf { it.accuracy } * 100).roundToInt()
        val worstAccuracy = (sessions.minOf { it.accuracy } * 100).roundToInt()
        val meanAccuracy = (sessions.sumOf { it.accuracy } / sessions.size * 100).roundToInt()
        val bestReactionTime = sessions.minOf { it.reactionTime }
        val worstReactionTime = sessions.maxOf { it.reactionTime }
        val meanReactionTime =
            (sessions.sumOf { it.reactionTime }.toDouble() / sessions.size).roundToInt()

        binding.accuracyBestTextview.text = getString(R.string.percent, bestAccuracy)
        binding.accuracyWorstTextview.text = getString(R.string.percent, worstAccuracy)
        binding.accuracyMeanTextview.text = getString(R.string.percent, meanAccuracy)
        binding.reactionBestTextview.text = getString(R.string.time, bestReactionTime)
        binding.reactionWorstTextview.text = getString(R.string.time, worstReactionTime)
        binding.reactionMeanTextview.text = getString(R.string.time, meanReactionTime)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}