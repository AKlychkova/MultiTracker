package ru.hse.multitracker.ui.elements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import ru.hse.multitracker.R
import ru.hse.multitracker.databinding.FragmentSettingsBinding
import ru.hse.multitracker.ui.adapters.PatientNameAdapter
import ru.hse.multitracker.ui.view_models.SettingsViewModel
import kotlin.math.min


class SettingsFragment : Fragment() {
    private val viewModel: SettingsViewModel by viewModels { SettingsViewModel.Factory }
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var patientNameAdapter: PatientNameAdapter

    // constants used to adjust seekbars
    private val TOTAL_DEFAULT = 5.0f
    private val TARGET_MAX = 5.0f   // target amount cannot be bigger than 5
    private val TARGET_DEFAULT = 3.0f
    private val SPEED_DEFAULT = 5.0f
    private val TIME_DEFAULT = 30.0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // define binding
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        // define adapter for recycler view
        patientNameAdapter = PatientNameAdapter(listOf()) { fullName ->
            viewModel.onPatientClicked(fullName)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // define recycler view
        binding.recyclerViewSettings.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewSettings.adapter = patientNameAdapter

        binding.timeSlider.setLabelFormatter { value ->
            getString(R.string.time, value.toInt())
        }

        // set listeners
        setListeners()
        // set observers
        observeViewModel()
    }

    private fun setListeners() {
        // max target amount depend on total amount of objects (cannot be bigger)
        binding.totalSlider.addOnChangeListener { slider, value, fromUser ->
            val valueTo = min(TARGET_MAX, value)
            if (binding.targetSlider.value > valueTo) {
                binding.targetSlider.value = valueTo
            }
            binding.targetSlider.valueTo = valueTo
        }

        binding.addFab.setOnClickListener { view ->
            // go to formFragment with id = 0 to create new patient profile
            val bundle = Bundle()
            bundle.putLong("id", 0)
            view.findNavController().navigate(
                R.id.action_settingsFragment_to_formFragment,
                bundle
            )
        }

        binding.helpFab.setOnClickListener(
            // go to InstructionFragment
            Navigation.createNavigateOnClickListener(
                R.id.action_settingsFragment_to_instructionFragment
            )
        )

        binding.readyButton.setOnClickListener { view ->
            // go to TestingFragment and send settings to it
            val bundle = Bundle()
            bundle.putLong("p_id", viewModel.currentPatientFullName.value!!.id)
            bundle.putInt("total", binding.totalSlider.value.toInt())
            bundle.putInt("target", binding.targetSlider.value.toInt())
            bundle.putInt("speed", binding.speedSlider.value.toInt())
            bundle.putInt("time", binding.timeSlider.value.toInt())
            view.findNavController().navigate(
                R.id.action_settingsFragment_to_testingFragment,
                bundle
            )
        }
    }

    private fun observeViewModel() {
        // observe list of patient profiles
        viewModel.allPatientNames.observe(viewLifecycleOwner) {
            // show hint that there are not profiles, if list of profiles is empty
            binding.hintNoUsersSettingsTextview.visibility = if (it.isEmpty()) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
            // update data in recyclerView
            it.let { patientNameAdapter.setData(it) }
        }

        // observe current patient value
        viewModel.currentPatientFullName.observe(viewLifecycleOwner) {
            if (it != null) {
                // make views from settings group visible and hide the hint
                binding.hintSettingsTextview.visibility = View.INVISIBLE
                binding.settingsGroup.visibility = View.VISIBLE
                binding.fullNameTextview.text = getString(
                    R.string.full_name_info,
                    it.surname,
                    it.name,
                    it.patronymic ?: ""
                )

                // set seekbars' progresses to last session's value or to default value
                binding.totalSlider.value =
                    viewModel.lastSession?.totalAmount?.toFloat() ?: TOTAL_DEFAULT
                binding.targetSlider.value =
                    viewModel.lastSession?.targetAmount?.toFloat() ?: TARGET_DEFAULT
                binding.speedSlider.value =
                    viewModel.lastSession?.speed?.toFloat() ?: SPEED_DEFAULT
                binding.timeSlider.value =
                    viewModel.lastSession?.movementTime?.toFloat() ?: TIME_DEFAULT
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}