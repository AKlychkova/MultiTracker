package ru.hse.multitracker.data.repositories

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.hse.multitracker.data.database.PatientDao
import ru.hse.multitracker.data.database.entities.Patient
import ru.hse.multitracker.data.database.entities.PatientWithTestSessions

class PatientRepository(private val patientDao: PatientDao) {

    /**
     * ordered list of patients' full names
     */
    val allPatients: Flow<List<PatientFullName>> = patientDao.getPatients()
        // transform flow of List<Patient> to flow of List<PatientFullName>
        .map { patients -> patients.map { PatientFullName(it) } }

    /**
     * insert patient to the database
     * @param patient patient to insert
     * @return identifier of inserted patient
     */
    @WorkerThread
    suspend fun insert(patient: Patient): Long {
        return patientDao.insertPatient(patient)
    }

    /**
     * delete patient from the database
     * @param patient patient to delete
     */
    @WorkerThread
    suspend fun delete(patient: Patient) {
        patientDao.deletePatient(patient)
    }

    /**
     * update patient data in the database
     * @param patient patient to update
     */
    @WorkerThread
    suspend fun update(patient: Patient) {
        patientDao.updatePatient(patient)
    }

    /**
     * @param id patient's identifier
     * @return patient associated with this id with his test sessions
     */
    @WorkerThread
    suspend fun getWithTestSessions(id: Long): PatientWithTestSessions {
        return patientDao.getPatientWithTestSessions(id)
    }

    /**
     * @return list of all patients with their test sessions
     */
    @WorkerThread
    suspend fun getAllPatientsWithTestSessions(): List<PatientWithTestSessions> {
        return patientDao.getPatientsWithTestSessions()
    }

    /**
     * @param id patient's identifier
     * @return patient associated with this id
     */
    @WorkerThread
    suspend fun getPatient(id: Long): Patient {
        return patientDao.getPatient(id)
    }
}