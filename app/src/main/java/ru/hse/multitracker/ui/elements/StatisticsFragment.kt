package ru.hse.multitracker.ui.elements

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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
    private lateinit var graph: ResultsGraph
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
        graph = ResultsGraph(binding.graphview, requireContext())
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

        viewModel.currentPatient.observe(viewLifecycleOwner) { patientWithTestSessions ->
            if (patientWithTestSessions == null) {
                binding.hintTextview.visibility = View.VISIBLE
                binding.statisticsGroup.visibility = View.INVISIBLE
            } else {
                binding.hintTextview.visibility = View.INVISIBLE
                binding.statisticsGroup.visibility = View.VISIBLE
                binding.infoNameTextview.text = getString(
                    R.string.full_name_info,
                    patientWithTestSessions.patient.surname,
                    patientWithTestSessions.patient.name,
                    patientWithTestSessions.patient.patronymic ?: ""
                )
                if (patientWithTestSessions.patient.age == null) {
                    binding.infoAgeTextview.visibility = View.GONE
                } else {
                    binding.infoAgeTextview.text =
                        getString(R.string.age_info, patientWithTestSessions.patient.age)
                }
                if (patientWithTestSessions.patient.diagnosis == null) {
                    binding.infoDiagnosisTextview.visibility = View.GONE
                } else {
                    binding.infoDiagnosisTextview.text = getString(
                        R.string.diagnosis_info,
                        patientWithTestSessions.patient.diagnosis
                    )
                }
                if (patientWithTestSessions.patient.sex == null) {
                    binding.infoSexTextview.visibility = View.GONE
                } else {
                    binding.infoSexTextview.text = getString(
                        if (patientWithTestSessions.patient.sex == false) {
                            R.string.female
                        } else {
                            R.string.male
                        }
                    )
                }
                if (patientWithTestSessions.sessions.isEmpty()) {
                    binding.testInfo.visibility = View.INVISIBLE
                    binding.hintNoTests.visibility = View.VISIBLE
                } else {
                    binding.testInfo.visibility = View.VISIBLE
                    binding.hintNoTests.visibility = View.INVISIBLE
                    calculateResults(patientWithTestSessions.sessions)
                    graph.updateData(
                        patientWithTestSessions.sessions.sortedBy { session -> session.date }
                    )
                }
            }
        }
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

