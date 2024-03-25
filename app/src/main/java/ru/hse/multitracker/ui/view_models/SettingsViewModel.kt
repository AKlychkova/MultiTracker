package ru.hse.multitracker.ui.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ru.hse.multitracker.MultiTrackerApplication
import ru.hse.multitracker.data.database.entities.TestSession
import ru.hse.multitracker.data.repositories.PatientFullName
import ru.hse.multitracker.data.repositories.PatientRepository
import ru.hse.multitracker.data.repositories.TestSessionRepository
import java.util.Date

class SettingsViewModel(
    private val pRepository: PatientRepository,
    private val tRepository: TestSessionRepository
) : ViewModel() {

    val allPatientNames: LiveData<List<PatientFullName>> = pRepository.allPatients.asLiveData()
    val lastSession = MutableLiveData<TestSession?>()
    private var currentPatientId: Long? = null

    fun onPatientClicked(patientFullName: PatientFullName) = viewModelScope.launch(Dispatchers.IO) {
        val session = tRepository.getLastSession(patientFullName.id)
        launch(Dispatchers.Main) {
            lastSession.value = session
            currentPatientId = patientFullName.id
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