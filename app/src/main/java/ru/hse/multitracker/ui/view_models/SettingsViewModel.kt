package ru.hse.multitracker.ui.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.hse.multitracker.MultiTrackerApplication
import ru.hse.multitracker.data.database.entities.TestSession
import ru.hse.multitracker.data.repositories.PatientFullName
import ru.hse.multitracker.data.repositories.PatientRepository
import ru.hse.multitracker.data.repositories.TestSessionRepository

class SettingsViewModel(
    private val pRepository: PatientRepository,
    private val tRepository: TestSessionRepository
) : ViewModel() {

    val allPatientNames: LiveData<List<PatientFullName>> = pRepository.allPatients.asLiveData()
    var lastSession: TestSession? = null
    private set

    private val _currentPatientFullName: MutableLiveData<PatientFullName?> = MutableLiveData()
    val currentPatientFullName: LiveData<PatientFullName?> get() = _currentPatientFullName


    /**
     * Called when a patient item in recycler view has been clicked
     */
    fun onPatientClicked(patientFullName: PatientFullName) = viewModelScope.launch(Dispatchers.IO) {
        // get last session of chosen patient
        val session = tRepository.getLastSession(patientFullName.id)
        // update current patient data
        launch(Dispatchers.Main) {
            lastSession = session
            _currentPatientFullName.value = patientFullName
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get the Application object from extras
                val application =
                    checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                return SettingsViewModel(
                    (application as MultiTrackerApplication).patientRepository,
                    (application as MultiTrackerApplication).testSessionRepository
                ) as T
            }
        }
    }
}