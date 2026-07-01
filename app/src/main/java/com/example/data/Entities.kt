package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val password: String,
    val role: String, // "Admin", "Manager", "Team Leader", "Staff"
    val designation: String,
    val salary: Double,
    val incentives: Double = 0.0,
    val aadhaar: String = "",
    val pan: String = "",
    val bankDetails: String = "",
    val joiningDate: String = "",
    val photoUri: String? = null
)

@Entity(tableName = "leads")
data class Lead(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val status: String, // "New", "Fresh Data", "Hot Leads", "Warm Leads", "Cold Leads", "Converted", "Rejected", "DND"
    val followUpCategory: String? = null, // "Call Back", "Interested", "Not Interested", "Busy", "Switched Off", "Wrong Number", "Follow-up Tomorrow", "Follow-up Next Week", "Meeting Fixed", "Policy Purchased"
    val insuranceCategory: String, // "Motor Insurance", "Bike Insurance", "Car Insurance", "Health Insurance", "Life Insurance", "Commercial Vehicle", "Travel Insurance", "Home Insurance"
    val premiumAmount: Double = 0.0,
    val renewalDate: String? = null, // yyyy-MM-dd
    val assignedStaffId: Int? = null,
    val assignedStaffName: String? = null,
    val callNotes: String? = null,
    val lastCallTime: Long? = null,
    val reminderTime: Long? = null,
    val policyNumber: String? = null
)

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    val employeeName: String,
    val date: String, // yyyy-MM-dd
    val checkInTime: String? = null, // HH:mm
    val checkOutTime: String? = null, // HH:mm
    val latitude: Double? = null,
    val longitude: Double? = null,
    val selfieUri: String? = null,
    val status: String = "Absent" // "Present", "Late Arrival", "Half Day", "Absent"
)

@Entity(tableName = "leave_requests")
data class LeaveRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    val employeeName: String,
    val startDate: String,
    val endDate: String,
    val reason: String,
    val status: String = "Pending" // "Pending", "Approved", "Rejected"
)

@Entity(tableName = "salary_slips")
data class SalarySlip(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    val employeeName: String,
    val month: String, // yyyy-MM
    val basicSalary: Double,
    val incentives: Double,
    val bonus: Double = 0.0,
    val deductions: Double = 0.0,
    val netSalary: Double
)

@Entity(tableName = "targets")
data class Target(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    val period: String, // "Daily", "Weekly", "Monthly", "Renewal"
    val targetAmount: Double,
    val achievedAmount: Double = 0.0
)

@Entity(tableName = "call_logs")
data class CallLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val leadId: Int,
    val leadName: String,
    val callerId: Int,
    val callerName: String,
    val timestamp: Long,
    val outcome: String, // "No Answer", "Busy", "Not Interested", "Interested", "Follow-up Scheduled", "Converted", "Rejected"
    val durationSeconds: Int,
    val notes: String,
    val nextFollowUpDate: String? = null // yyyy-MM-dd
)
