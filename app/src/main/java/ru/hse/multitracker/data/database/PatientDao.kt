package ru.hse.multitracker.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.hse.multitracker.data.database.entities.Patient
import ru.hse.multitracker.data.database.entities.PatientWithTestSessions

@Dao
interface PatientDao {
    /**
     * insert patient to the database
     * @param patient patient to insert
     * @return identifier of inserted patient
     */
    @Insert
    suspend fun insertPatient(patient: Patient): Long

    /**
     * update patient data in the database
     * @param patient patient to update
     */
    @Update
    suspend fun updatePatient(patient: Patient)

    /**
     * delete patient from the database
     * @param patient patient to delete
     */
    @Delete
    suspend fun deletePatient(patient: Patient)

    /**
     * @return list of patients ordered by full name
     */
    @Query("SELECT * FROM patients ORDER BY surname, name, patronymic")
    fun getPatients(): Flow<List<Patient>>

    /**
     * @param id patient's identifier
     * @return patient associated with this id
     */
    @Query("SELECT * FROM patients WHERE id = :id")
    suspend fun getPatient(id: Long) : Patient

    /**
     * @return list of all patients with their test sessions
     */
    @Transaction
    @Query("SELECT * FROM patients")
    suspend fun getPatientsWithTestSessions(): List<PatientWithTestSessions>

    /**
     * @param id patient's identifier
     * @return patient associated with this id with his test sessions
     */
    @Transaction
    @Query("SELECT * FROM patients WHERE id = :id")
    suspend fun getPatientWithTestSessions(id: Long): PatientWithTestSessions
}