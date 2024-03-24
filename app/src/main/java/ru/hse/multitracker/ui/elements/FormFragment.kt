package ru.hse.multitracker.ui.elements

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import ru.hse.multitracker.R
import ru.hse.multitracker.databinding.FragmentFormBinding
import ru.hse.multitracker.ui.view_models.FormViewModel

class FormFragment : Fragment() {

    private val viewModel: FormViewModel by viewModels { FormViewModel.Factory }
    private var _binding: FragmentFormBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = arguments?.getLong("id")
        id?.let { viewModel.getPatient(it) }
        setListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.currentPatient.observe(viewLifecycleOwner) {
            binding.nameEdittext.setText(it.name)
            binding.surnameEdittext.setText(it.surname)
            binding.patronymicEdittext.setText(it.patronymic)
            binding.ageEdittext.setText(it.age.toString())
            if (it.sex == true) {
                binding.maleRadiobutton.isChecked = true
            }
            if (it.sex == false) {
                binding.femaleRadiobutton.isChecked = true
            }
            binding.diagnosisEdittext.setText(it.diagnosis)
        }
    }

    private fun setListeners() {
        binding.saveButton.setOnClickListener { view ->
            if (binding.nameEdittext.text.isEmpty()) {
                binding.nameEdittext.error = getString(R.string.empty_name_error)
                return@setOnClickListener
            }
            if (binding.surnameEdittext.text.isEmpty()) {
                binding.surnameEdittext.error = getString(R.string.empty_surname_error)
                return@setOnClickListener
            }
            val age = binding.ageEdittext.text.toString().toIntOrNull()
            if (age == null || age > 100 || age < 0) {
                binding.ageEdittext.error = getString(R.string.age_error)
                return@setOnClickListener
            }
            viewModel.save(
                name = binding.nameEdittext.text.toString(),
                surname = binding.surnameEdittext.text.toString(),
                age = age,
                diagnosis =
                if (binding.diagnosisEdittext.text.isNotEmpty()) {
                    binding.diagnosisEdittext.text.toString()
                } else {
                    null
                },
                patronymic =
                if (binding.patronymicEdittext.text.isNotEmpty()) {
                    binding.patronymicEdittext.text.toString()
                } else {
                    null
                },
                sex =
                if (binding.maleRadiobutton.isChecked) {
                    true
                } else if (binding.femaleRadiobutton.isChecked) {
                    false
                } else {
                    null
                }
            )
            view.findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}