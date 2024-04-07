package ru.hse.multitracker.ui.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.hse.multitracker.MultiTrackerApplication
import ru.hse.multitracker.data.database.entities.TestSession
import ru.hse.multitracker.data.repositories.TestSessionRepository

class ReportViewModel(private val repository: TestSessionRepository) : ViewModel() {
    private val _currentSession = MutableLiveData<TestSession>()
    val currentSession: LiveData<TestSession> get() = _currentSession

    /**
     * get session data from database by id
     * @param id session identifier
     */
    fun getSession(id:Long) = viewModelScope.launch(Dispatchers.IO) {
        val session = repository.getSession(id)
        launch(Dispatchers.Main) {
            _currentSession.value = session
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
                return ReportViewModel(
                    (application as MultiTrackerApplication).testSessionRepository
                ) as T
            }
        }
    }
}