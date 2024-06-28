package ru.hse.multitracker.ui.view_models

import android.content.Context
import android.net.Uri
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
import ru.hse.multitracker.data.database.entities.TestSession
import ru.hse.multitracker.data.repositories.PatientFullName
import ru.hse.multitracker.data.repositories.PatientRepository
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class StatisticsViewModel(private val repository: PatientRepository) : ViewModel() {
    val allPatientNames: LiveData<List<PatientFullName>> = repository.allPatients.asLiveData()
    private val _currentPatient = MutableLiveData<PatientWithTestSessions?>()
    val currentPatient: LiveData<PatientWithTestSessions?> get() = _currentPatient

    /**
     * If patient is already chosen, update his data
     */
    fun updateCurrentPatient() = viewModelScope.launch(Dispatchers.IO) {
        // get current patient id
        val currentId = currentPatient.value?.patient?.id
        // check if it exists
        if (currentId != null) {
            // get actual data from database
            val patient = repository.getWithTestSessions(currentId)
            launch(Dispatchers.Main) {
                _currentPatient.value = patient
            }
        }
    }

    /**
     * Called when a patient item in recycler view has been clicked
     */
    fun onPatientClicked(patientFullName: PatientFullName) = viewModelScope.launch(Dispatchers.IO) {
        // check if chosen patient differ from current
        if (patientFullName.id != currentPatient.value?.patient?.id) {
            // get data of chosen patient
            val patient = repository.getWithTestSessions(patientFullName.id)
            // change currentPatient value to chosen one
            launch(Dispatchers.Main) {
                _currentPatient.value = patient
            }
        }
    }

    /**
     * Called when the delete button has been clicked
     */
    fun onDeleteClicked() = viewModelScope.launch(Dispatchers.IO) {
        // delete chosen patient
        currentPatient.value?.let { repository.delete(it.patient) }
        // set currentPatient value to null
        launch(Dispatchers.Main) {
            _currentPatient.value = null
        }
    }

    fun editDocument(uri: Uri, context: Context) = viewModelScope.launch(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        try {
            contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { out ->
                    out.write(CsvGenerator.generateCsv(
                        currentPatient.value?.sessions ?: listOf<TestSession>()
                    ).toByteArray())
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
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