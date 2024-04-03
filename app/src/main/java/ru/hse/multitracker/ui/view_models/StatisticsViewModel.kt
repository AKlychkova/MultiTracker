package ru.hse.multitracker.ui.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.hse.multitracker.MultiTrackerApplication
import ru.hse.multitracker.data.database.entities.PatientWithTestSessions
import ru.hse.multitracker.data.repositories.PatientFullName
import ru.hse.multitracker.data.repositories.PatientRepository

class StatisticsViewModel(private val repository: PatientRepository) : ViewModel() {
    val allPatientNames: LiveData<List<PatientFullName>> = repository.allPatients.asLiveData()
    private val _currentPatient = MutableLiveData<PatientWithTestSessions?>()
    val currentPatient: LiveData<PatientWithTestSessions?> get() = _currentPatient

    fun updateCurrentPatient() = viewModelScope.launch(Dispatchers.IO) {
        val currentId = currentPatient.value?.patient?.id
        if (currentId != null) {
            val patient = repository.getWithTestSessions(currentId)
            launch(Dispatchers.Main) {
                _currentPatient.value = patient
            }
        }
    }

    fun onPatientClicked(patientFullName: PatientFullName) = viewModelScope.launch(Dispatchers.IO) {
        val patient = repository.getWithTestSessions(patientFullName.id)
        launch(Dispatchers.Main) {
            _currentPatient.value = patient
        }
    }

    fun onDeleteClicked() = viewModelScope.launch(Dispatchers.IO) {
        currentPatient.value?.let { repository.delete(it.patient) }
        launch(Dispatchers.Main) {
            _currentPatient.value = null
        }
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