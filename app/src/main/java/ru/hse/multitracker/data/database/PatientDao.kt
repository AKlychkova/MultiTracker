package ru.hse.multitracker.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import ru.hse.multitracker.data.database.entities.Patient
import ru.hse.multitracker.data.database.entities.PatientWithTestSessions

@Dao
interface PatientDao {
    @Insert
    suspend fun insertPatient(patient: Patient)

    @Update
    suspend fun updatePatient(patient: Patient)

    @Delete
    suspend fun deletePatient(patient: Patient)

    @Query("SELECT * FROM patients")
    suspend fun getPatients(): List<Patient>

    @Transaction
    @Query("SELECT * FROM patients")
    suspend fun getPatientsWithTestSessions(): List<PatientWithTestSessions>

    @Transaction
    @Query("SELECT * FROM patients WHERE id = :id")
    suspend fun getPatientsWithTestSessions(id: Int): PatientWithTestSessions
}