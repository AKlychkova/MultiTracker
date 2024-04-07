package ru.hse.multitracker.data.repositories

import androidx.annotation.WorkerThread
import ru.hse.multitracker.data.database.TestSessionDao
import ru.hse.multitracker.data.database.entities.TestSession

class TestSessionRepository(private val testSessionDao: TestSessionDao) {
    /**
     * insert test session to the database
     * @param session test session to insert
     */
    @WorkerThread
    suspend fun insert(session: TestSession): Long {
        return testSessionDao.insertTestSession(session)
    }

    /**
     * @param id test session identifier
     * @return test session associated with this identifier
     */
    @WorkerThread
    suspend fun getSession(id: Long): TestSession {
        return testSessionDao.getSession(id)
    }

    /**
     * @param patientId patient's identifier
     * @return patient's last test session or null if there are not this patient's test sessions
     */
    @WorkerThread
    suspend fun getLastSession(patientId: Long): TestSession? {
        return testSessionDao.getLastSession(patientId)
    }

    /**
     * delete test session from the database
     * @param session test session to delete
     */
    @WorkerThread
    suspend fun delete(session: TestSession) {
        testSessionDao.deleteTestSession(session)
    }

    /**
     * update test session data in the database
     * @param session test session to update
     */
    @WorkerThread
    suspend fun update(session: TestSession) {
        testSessionDao.updateTestSession(session)
    }
}