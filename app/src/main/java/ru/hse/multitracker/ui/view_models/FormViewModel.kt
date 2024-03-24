package ru.hse.multitracker.ui.view_models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.hse.multitracker.MultiTrackerApplication
import ru.hse.multitracker.data.database.entities.Patient
import ru.hse.multitracker.data.repositories.PatientRepository

class FormViewModel(private val repository: PatientRepository) : ViewModel() {
    val currentPatient = MutableLiveData<Patient>()
    fun save(
        name: String,
        surname: String,
        patronymic: String?,
        age: Int?,
        sex: Boolean?,
        diagnosis: String?
    ) = viewModelScope.launch(Dispatchers.IO) {
        if (currentPatient.value == null) {
            repository.insert(Patient(
                id = 0,
                name = name,
                surname = surname,
                patronymic = patronymic,
                age = age,
                sex = sex,
                diagnosis = diagnosis
            ))
        } else {
            repository.update(
                Patient(
                    id = currentPatient.value?.id ?: -1,
                    name = name,
                    surname = surname,
                    patronymic = patronymic,
                    age = age,
                    sex = sex,
                    diagnosis = diagnosis
                )
            )
        }
    }

    fun getPatient(id: Long) = viewModelScope.launch(Dispatchers.IO) {
        val patient = repository.getPatient(id)
        launch(Dispatchers.Main) {
            currentPatient.value = patient
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
                val application =
                    checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                return FormViewModel(
                    (application as MultiTrackerApplication).patientRepository
                ) as T
            }
        }
    }
}