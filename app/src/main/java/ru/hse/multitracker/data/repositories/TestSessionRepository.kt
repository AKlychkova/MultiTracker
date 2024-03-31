package ru.hse.multitracker.data.repositories

import androidx.annotation.WorkerThread
import ru.hse.multitracker.data.database.TestSessionDao
import ru.hse.multitracker.data.database.entities.TestSession

class TestSessionRepository(private val testSessionDao: TestSessionDao) {
    @WorkerThread
    suspend fun insert(session: TestSession): Long {
        return testSessionDao.insertTestSession(session)
    }

    @WorkerThread
    suspend fun getSession(id: Long): TestSession {
        return testSessionDao.getSession(id)
    }

    @WorkerThread
    suspend fun getLastSession(patientId: Long): TestSession? {
        return testSessionDao.getLastSession(patientId)
    }

    @WorkerThread
    suspend fun delete(session: TestSession) {
        testSessionDao.deleteTestSession(session)
    }

    @WorkerThread
    suspend fun update(session: TestSession) {
        testSessionDao.updateTestSession(session)
    }
}