package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CrmDao {

    // --- Employee Queries ---
    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE phone = :phone LIMIT 1")
    suspend fun getEmployeeByPhone(phone: String): Employee?

    @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
    suspend fun getEmployeeById(id: Int): Employee?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee): Long

    @Delete
    suspend fun deleteEmployee(employee: Employee)

    // --- Lead Queries ---
    @Query("SELECT * FROM leads ORDER BY id DESC")
    fun getAllLeads(): Flow<List<Lead>>

    @Query("SELECT * FROM leads WHERE assignedStaffId = :staffId ORDER BY id DESC")
    fun getLeadsByStaff(staffId: Int): Flow<List<Lead>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLead(lead: Lead): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeads(leads: List<Lead>)

    @Query("UPDATE leads SET assignedStaffId = :staffId, assignedStaffName = :staffName WHERE id IN (:leadIds)")
    suspend fun bulkAssignLeads(leadIds: List<Int>, staffId: Int?, staffName: String?)

    @Delete
    suspend fun deleteLead(lead: Lead)

    // --- Attendance Queries ---
    @Query("SELECT * FROM attendance ORDER BY date DESC")
    fun getAllAttendance(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE employeeId = :employeeId ORDER BY date DESC")
    fun getAttendanceForEmployee(employeeId: Int): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE date = :date")
    suspend fun getAttendanceForDate(date: String): List<Attendance>

    @Query("SELECT * FROM attendance WHERE employeeId = :employeeId AND date = :date LIMIT 1")
    suspend fun getAttendanceForEmployeeDate(employeeId: Int, date: String): Attendance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance): Long

    // --- Leave Request Queries ---
    @Query("SELECT * FROM leave_requests ORDER BY id DESC")
    fun getAllLeaveRequests(): Flow<List<LeaveRequest>>

    @Query("SELECT * FROM leave_requests WHERE employeeId = :employeeId ORDER BY id DESC")
    fun getLeaveRequestsForEmployee(employeeId: Int): Flow<List<LeaveRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRequest(request: LeaveRequest): Long

    @Query("UPDATE leave_requests SET status = :status WHERE id = :requestId")
    suspend fun updateLeaveStatus(requestId: Int, status: String)

    // --- Salary Slip Queries ---
    @Query("SELECT * FROM salary_slips ORDER BY month DESC")
    fun getAllSalarySlips(): Flow<List<SalarySlip>>

    @Query("SELECT * FROM salary_slips WHERE employeeId = :employeeId ORDER BY month DESC")
    fun getSalarySlipsForEmployee(employeeId: Int): Flow<List<SalarySlip>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalarySlip(salarySlip: SalarySlip): Long

    // --- Target Queries ---
    @Query("SELECT * FROM targets")
    fun getAllTargets(): Flow<List<Target>>

    @Query("SELECT * FROM targets WHERE employeeId = :employeeId")
    fun getTargetsForEmployee(employeeId: Int): Flow<List<Target>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarget(target: Target): Long

    // --- Call Log Queries ---
    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    fun getAllCallLogs(): Flow<List<CallLog>>

    @Query("SELECT * FROM call_logs WHERE leadId = :leadId ORDER BY timestamp DESC")
    fun getCallLogsForLead(leadId: Int): Flow<List<CallLog>>

    @Query("SELECT * FROM call_logs WHERE callerId = :callerId ORDER BY timestamp DESC")
    fun getCallLogsForCaller(callerId: Int): Flow<List<CallLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLog(callLog: CallLog): Long
}
