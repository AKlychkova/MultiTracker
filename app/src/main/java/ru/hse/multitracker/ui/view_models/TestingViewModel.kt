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
import java.util.Date

class TestingViewModel(private val repository: TestSessionRepository) : ViewModel() {
    private val _currentSession: MutableLiveData<TestSession> = MutableLiveData<TestSession>()
    val currentSession: LiveData<TestSession> get() = _currentSession

    fun createTrainingSession(total: Int, target: Int, speed: Int, time: Int) {
        _currentSession.value = TestSession(
            id = -1,
            date = Date(),
            targetAmount = target,
            totalAmount = total,
            speed = speed,
            movementTime = time,
            accuracy = 0.0,
            reactionTime = 0,
            patientId = 0
        )
    }

    fun createSession(
        pId: Long,
        total: Int,
        target: Int,
        speed: Int,
        time: Int
    ) = viewModelScope.launch(Dispatchers.IO) {
        val sessionId = repository.insert(
            TestSession(
                id = 0,
                date = Date(),
                targetAmount = target,
                totalAmount = total,
                speed = speed,
                movementTime = time,
                accuracy = 0.0,
                reactionTime = 0,
                patientId = pId
            )
        )
        val session = repository.getSession(sessionId)
        launch(Dispatchers.Main) {
            _currentSession.value = session
        }
    }

    fun deleteSession() = viewModelScope.launch(Dispatchers.IO) {
        currentSession.value?.let { repository.delete(it) }
    }

    fun setResults(reactionTime: Int, accuracy: Double) {
        currentSession.value?.let { session ->
            session.accuracy = accuracy
            session.reactionTime = reactionTime
            if (session.id != -1L) {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.update(session)
                }
            }
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
                return TestingViewModel(
                    (application as MultiTrackerApplication).testSessionRepository
                ) as T
            }
        }
    }
}