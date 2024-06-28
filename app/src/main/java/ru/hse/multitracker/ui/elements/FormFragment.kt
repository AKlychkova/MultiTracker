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
        id?.let {
            if (it != 0L) {
                viewModel.getPatient(it)
            }
        }
        setListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.currentPatient.observe(viewLifecycleOwner) {
            binding.nameTextfield.editText?.setText(it?.name)
            binding.surnameTextfield.editText?.setText(it?.surname)
            binding.patronymicTextfield.editText?.setText(it?.patronymic)
            binding.ageTextfield.editText?.setText(it?.age?.toString())
            if (it?.sex == true) {
                binding.maleRadiobutton.isChecked = true
            }
            if (it?.sex == false) {
                binding.femaleRadiobutton.isChecked = true
            }
            binding.diagnosisTextfield.editText?.setText(it?.diagnosis)
        }
    }

    /**
     * Check that all fields filled correct and set errors if not
     * @return true if all fields are filled correct else false
     */
    private fun checkFields(): Boolean {
        var check = true
        binding.nameTextfield.error = null
        binding.surnameTextfield.error = null
        binding.ageTextfield.error = null

        if (binding.nameTextfield.editText?.text.isNullOrBlank()) {
            binding.nameTextfield.error = getString(R.string.empty_name_error)
            check = false
        }
        if (binding.surnameTextfield.editText?.text.isNullOrBlank()) {
            binding.surnameTextfield.error = getString(R.string.empty_surname_error)
            check = false
        }
        if (!binding.ageTextfield.editText?.text.isNullOrBlank()) {
            val age = binding.ageTextfield.editText?.text.toString().toIntOrNull()
            if (age == null || age > 100 || age < 0) {
                binding.ageTextfield.error = getString(R.string.age_error)
                check = false
            }
        }
        return check
    }

    private fun setListeners() {
        binding.saveButton.setOnClickListener { view ->
            if (checkFields()) {
                val age = binding.ageTextfield.editText?.text.toString().toIntOrNull()

                viewModel.onSaveClicked(
                    name = binding.nameTextfield.editText!!.text.toString(),
                    surname = binding.surnameTextfield.editText!!.text.toString(),
                    age = age,
                    diagnosis =
                    if (binding.diagnosisTextfield.editText?.text.isNullOrBlank()) {
                        null
                    } else {
                        binding.diagnosisTextfield.editText?.text.toString()
                    },
                    patronymic =
                    if (binding.patronymicTextfield.editText?.text.isNullOrBlank()) {
                        null
                    } else {
                        binding.patronymicTextfield.editText?.text.toString()
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}