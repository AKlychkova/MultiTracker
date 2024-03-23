package ru.hse.multitracker.ui.elements

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import ru.hse.multitracker.data.database.entities.Patient
import ru.hse.multitracker.databinding.FragmentStatisticsBinding
import ru.hse.multitracker.ui.adapters.PatientNameAdapter
import ru.hse.multitracker.ui.view_models.StatisticsViewModel

class StatisticsFragment : Fragment() {

    private val viewModel: StatisticsViewModel by viewModels {StatisticsViewModel.Factory}
    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private lateinit var patientNameAdapter: PatientNameAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        patientNameAdapter = PatientNameAdapter(listOf())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = patientNameAdapter
        viewModel.allPatientNames.observe(viewLifecycleOwner) {
            it.let{patientNameAdapter.setData(it)}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}