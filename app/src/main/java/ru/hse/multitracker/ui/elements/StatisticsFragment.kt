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

    /**
     * Represent statistics of test sessions' results
     */
    data class Results(
        val bestAccuracyPercent: Int,
        val worstAccuracyPercent: Int,
        val meanAccuracyPercent: Int,
        val bestReactionTime: Int,
        val worstReactionTime: Int,
        val meanReactionTime: Int
    ) {
        constructor(sessions: List<TestSession>) : this(
            bestAccuracyPercent = (sessions.maxOf { it.accuracy } * 100).roundToInt(),
            worstAccuracyPercent = (sessions.minOf { it.accuracy } * 100).roundToInt(),
            meanAccuracyPercent = (sessions.sumOf { it.accuracy } / sessions.size * 100).roundToInt(),
            bestReactionTime = sessions.minOf { it.reactionTime },
            worstReactionTime = sessions.maxOf { it.reactionTime },
            meanReactionTime = (sessions.sumOf { it.reactionTime }.toDouble() / sessions.size).roundToInt()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // define binding
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        // define adapter for recycler view
        patientNameAdapter = PatientNameAdapter(listOf()) { fullName ->
            viewModel.onPatientClicked(fullName)
        }
        // handle the back button event
        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack(R.id.homeFragment, false)
        }
        callback.isEnabled = true
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // define recyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = patientNameAdapter

        // define graph
        graph = ResultsGraph(binding.graphview, requireContext())

        // if patient was already chosen, update his data
        viewModel.updateCurrentPatient()

        // set observers
        observeViewModel()

        // set listeners
        setListeners()
    }

    private fun setListeners() {
        binding.deleteButton.setOnClickListener {
            // show alert dialog before deleting patient profile
            AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.delete_alert_message))
                // in case of a positive response, delete the profile
                .setPositiveButton(getString(R.string.yes)) { _, _ -> viewModel.onDeleteClicked() }
                // else do nothing
                .setNegativeButton(getString(R.string.no)) { _, _ -> }
                .show()
        }

        binding.toUpdateFormButton.setOnClickListener { view ->
            // go to formFragment and send id of current patient
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

    private fun observeViewModel() {
        // observe list of patient profiles
        viewModel.allPatientNames.observe(viewLifecycleOwner) { fullNamesList ->
            // show hint that there are not profiles, if list of profiles is empty
            binding.hintNoUsersTextview.visibility = if (fullNamesList.isEmpty()) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
            // update data in recyclerView
            fullNamesList.let { patientNameAdapter.setData(fullNamesList) }
        }

        // observe current patient value
        viewModel.currentPatient.observe(viewLifecycleOwner) { patientWithTestSessions ->
            if (patientWithTestSessions == null) {
                // show the hint that user needs to select a profile, if there not current patient
                binding.hintTextview.visibility = View.VISIBLE
                // make views from statistics group invisible
                binding.statisticsGroup.visibility = View.INVISIBLE
            } else {
                // make the hint invisible
                binding.hintTextview.visibility = View.INVISIBLE
                // show views from statistics group
                binding.statisticsGroup.visibility = View.VISIBLE

                // show patient info
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
                    // make views from test info group invisible
                    binding.testInfo.visibility = View.INVISIBLE
                    // show the hint that this patient have not finished test sessions
                    binding.hintNoTests.visibility = View.VISIBLE
                } else {
                    // show views from test info group
                    binding.testInfo.visibility = View.VISIBLE
                    // make the hint invisible
                    binding.hintNoTests.visibility = View.INVISIBLE

                    // show results
                    val results = Results(patientWithTestSessions.sessions)
                    binding.accuracyBestTextview.text =
                        getString(R.string.percent, results.bestAccuracyPercent)
                    binding.accuracyWorstTextview.text =
                        getString(R.string.percent, results.worstAccuracyPercent)
                    binding.accuracyMeanTextview.text =
                        getString(R.string.percent, results.meanAccuracyPercent)
                    binding.reactionBestTextview.text =
                        getString(R.string.time, results.bestReactionTime)
                    binding.reactionWorstTextview.text =
                        getString(R.string.time, results.worstReactionTime)
                    binding.reactionMeanTextview.text =
                        getString(R.string.time, results.meanReactionTime)

                    // update graph
                    graph.updateData(
                        patientWithTestSessions.sessions.sortedBy { session -> session.date }
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

