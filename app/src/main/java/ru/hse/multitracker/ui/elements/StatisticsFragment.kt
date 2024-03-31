package ru.hse.multitracker.ui.elements

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import ru.hse.multitracker.R
import ru.hse.multitracker.data.repositories.PatientFullName
import ru.hse.multitracker.databinding.FragmentStatisticsBinding
import ru.hse.multitracker.ui.adapters.PatientNameAdapter
import ru.hse.multitracker.ui.view_models.StatisticsViewModel

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
        val patientClickListener = object : PatientNameAdapter.OnPatientClickListener {
            override fun onPatientClick(patientFullName: PatientFullName, position: Int) {
                viewModel.onPatientClicked(patientFullName)
            }
        }
        patientNameAdapter = PatientNameAdapter(listOf(), patientClickListener)
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
            viewModel.onDeleteClicked()
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

    private fun observeViewModel() {
        viewModel.allPatientNames.observe(viewLifecycleOwner) {
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
                    // TODO observe test statistics
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