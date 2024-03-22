package ru.hse.multitracker.ui.elements

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import ru.hse.multitracker.R
import ru.hse.multitracker.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setListeners()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setListeners() {
        binding.testButton.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                R.id.action_homeFragment_to_settingsFragment
            )
        )
        binding.statisticsButton.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                R.id.action_homeFragment_to_statisticsFragment
            )
        )
    }
}