package ru.hse.multitracker.ui.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.launch
import ru.hse.multitracker.MultiTrackerApplication
import ru.hse.multitracker.data.database.entities.Patient
import ru.hse.multitracker.data.repositories.PatientFullName
import ru.hse.multitracker.data.repositories.PatientRepository

class StatisticsViewModel(private val repository: PatientRepository) : ViewModel() {
    val allPatientNames: LiveData<List<PatientFullName>> = repository.allPatients.asLiveData()

    // TODO delete insert
    fun insert(patient:Patient) = viewModelScope.launch {
        repository.insert(patient)
    }

    fun delete(patient: Patient) = viewModelScope.launch {
        repository.delete(patient)
    }

    // Define ViewModel factory in a companion object
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get the Application object from extras
                val application = checkNotNull(extras[APPLICATION_KEY])
                return StatisticsViewModel(
                    (application as MultiTrackerApplication).patientRepository
                ) as T
            }
        }
    }
}