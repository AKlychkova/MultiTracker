package ru.hse.multitracker.ui.elements

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.hse.multitracker.R
import ru.hse.multitracker.databinding.FragmentCreationFormBinding
import ru.hse.multitracker.ui.view_models.CreationFormViewModel

class CreationFormFragment : Fragment() {

    private val viewModel: CreationFormViewModel by viewModels()
    private var _binding: FragmentCreationFormBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreationFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}