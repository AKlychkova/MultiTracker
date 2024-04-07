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

    /**
     * Create training test session (not storing in database)
     * @param total total amount of objects
     * @param target amount of target objects
     * @param speed speed of objects movement
     * @param time time of objects movement in seconds
     */
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

    /**
     * Create test session object and insert it to database
     * @param patientId identifier of patient who is tested
     * @param total total amount of objects
     * @param target amount of target objects
     * @param speed speed of objects movement
     * @param time time of objects movement in seconds
     */
    fun createSession(
        patientId: Long,
        total: Int,
        target: Int,
        speed: Int,
        time: Int
    ) = viewModelScope.launch(Dispatchers.IO) {
        // create a session and insert it to database
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
                patientId = patientId
            )
        )
        // update currentSession value
        val session = repository.getSession(sessionId)
        launch(Dispatchers.Main) {
            _currentSession.value = session
        }
    }

    /**
     * Delete current session from database
      */
    fun deleteSession() = viewModelScope.launch(Dispatchers.IO) {
        currentSession.value?.let {
            repository.delete(it)
        }
    }

    /**
     * Set results to current session
     * @param reactionTime mean reaction time by attempts in seconds
     * @param accuracy mean answer accuracy by attempts
     */
    fun setResults(reactionTime: Int, accuracy: Double) {
        currentSession.value?.let { session ->
            session.accuracy = accuracy
            session.reactionTime = reactionTime

            // if current session is not training update database record
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