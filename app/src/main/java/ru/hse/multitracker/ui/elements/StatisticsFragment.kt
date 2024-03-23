package ru.hse.multitracker.ui.elements

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import ru.hse.multitracker.R
import ru.hse.multitracker.data.database.entities.Patient
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = patientNameAdapter
        observeViewModel()
        setListeners()
    }

    private fun setListeners() {
        binding.deleteButton.setOnClickListener {
            viewModel.onDeleteClicked()
        }
        binding.updateButton.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                R.id.action_statisticsFragment_to_formFragment
                // TODO send patient
            )
        )
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}