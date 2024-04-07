package ru.hse.multitracker.ui.elements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
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
    private val TOTAL_MIN = 2
    private val TOTAL_MAX = 10
    private val TOTAL_STEP = 1
    private val TOTAL_DEFAULT = 5

    private val TARGET_MIN = 1
    private val TARGET_MAX = 5
    private val TARGET_STEP = 1
    private val TARGET_DEFAULT = 3

    private val SPEED_MIN = 1
    private val SPEED_MAX = 10
    private val SPEED_STEP = 1
    private val SPEED_DEFAULT = 5

    private val TIME_MIN = 5
    private val TIME_MAX = 60
    private val TIME_STEP = 5
    private val TIME_DEFAULT = 30

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

        // adjust seekbars
        binding.totalSeekbar.max = (TOTAL_MAX - TOTAL_MIN) / TOTAL_STEP
        binding.targetSeekbar.max = (TARGET_MAX - TARGET_MIN) / TARGET_STEP
        binding.speedSeekbar.max = (SPEED_MAX - SPEED_MIN) / SPEED_STEP
        binding.timeSeekbar.max = (TIME_MAX - TIME_MIN) / TIME_STEP

        // set listeners
        setListeners()
        // set observers
        observeViewModel()
    }

    private fun setListeners() {
        // set listeners for settings seekbars
        binding.totalSeekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value: Int = TOTAL_MIN + progress
                // max target amount depend on total amount of objects (cannot be bigger)
                binding.targetSeekbar.max = min(TARGET_MAX, value) - TARGET_MIN / TARGET_STEP
                binding.total.text = value.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
        binding.targetSeekbar.setOnSeekBarChangeListener(
            getSeekBarListener(
                TARGET_MIN,
                TARGET_STEP,
                binding.target,
                R.string.number
            )
        )
        binding.speedSeekbar.setOnSeekBarChangeListener(
            getSeekBarListener(
                SPEED_MIN,
                SPEED_STEP,
                binding.speed,
                R.string.number
            )
        )
        binding.timeSeekbar.setOnSeekBarChangeListener(
            getSeekBarListener(
                TIME_MIN,
                TIME_STEP,
                binding.time,
                R.string.time
            )
        )

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
            bundle.putInt("total", TOTAL_MIN + binding.totalSeekbar.progress * TOTAL_STEP)
            bundle.putInt("target", TARGET_MIN + binding.targetSeekbar.progress * TARGET_STEP)
            bundle.putInt("speed", SPEED_MIN + binding.speedSeekbar.progress * SPEED_STEP)
            bundle.putInt("time", TIME_MIN + binding.timeSeekbar.progress * TIME_STEP)
            view.findNavController().navigate(
                R.id.action_settingsFragment_to_testingFragment,
                bundle
            )
        }
    }

    /**
     * Create OnSeekBarChangeListener that calculate value from min, step and progress
     * and then show it in the textView
     */
    private fun getSeekBarListener(
        min: Int,
        step: Int,
        textView: TextView,
        stringId: Int
    ): OnSeekBarChangeListener {
        return object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value: Int = min + (progress * step)
                textView.text = getString(stringId, value)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
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
                binding.totalSeekbar.progress =
                    ((viewModel.lastSession?.totalAmount ?: TOTAL_DEFAULT) - TOTAL_MIN) / TOTAL_STEP
                binding.targetSeekbar.progress =
                    ((viewModel.lastSession?.targetAmount ?: TARGET_DEFAULT) - TARGET_MIN) / TARGET_STEP
                binding.speedSeekbar.progress =
                    ((viewModel.lastSession?.speed ?: SPEED_DEFAULT) - SPEED_MIN) / SPEED_STEP
                binding.timeSeekbar.progress =
                    ((viewModel.lastSession?.movementTime ?: TIME_DEFAULT) - TIME_MIN) / TIME_STEP
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}