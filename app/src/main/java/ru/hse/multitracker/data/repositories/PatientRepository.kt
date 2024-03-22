package ru.hse.multitracker.data.repositories

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.hse.multitracker.data.database.PatientDao
import ru.hse.multitracker.data.database.entities.Patient
import ru.hse.multitracker.data.database.entities.PatientWithTestSessions

class PatientRepository (private val patientDao: PatientDao) {
    val allPatients: Flow<List<PatientFullName>> = patientDao.getPatients().map {
        patients -> patients.map{PatientFullName(it)}
    }

    @WorkerThread
    suspend fun insert(patient: Patient): Long {
        return patientDao.insertPatient(patient)
    }

    @WorkerThread
    suspend fun delete(patient: Patient) {
        patientDao.deletePatient(patient)
    }

    @WorkerThread
    suspend fun update(patient: Patient) {
        patientDao.updatePatient(patient)
    }

    @WorkerThread
    suspend fun getWithTestSessions(id: Long): PatientWithTestSessions {
        return patientDao.getPatientWithTestSessions(id)
    }

    @WorkerThread
    suspend fun getAllPatientsWithTestSessions(): List<PatientWithTestSessions> {
        return patientDao.getPatientsWithTestSessions()
    }

    @WorkerThread
    suspend fun getPatient(id: Long): Patient {
        return patientDao.getPatient(id)
    }
}