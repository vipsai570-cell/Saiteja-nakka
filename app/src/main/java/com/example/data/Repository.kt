package com.example.data

import kotlinx.coroutines.flow.Flow

class CrmRepository(private val crmDao: CrmDao) {

    val allEmployees: Flow<List<Employee>> = crmDao.getAllEmployees()
    val allLeads: Flow<List<Lead>> = crmDao.getAllLeads()
    val allAttendance: Flow<List<Attendance>> = crmDao.getAllAttendance()
    val allLeaveRequests: Flow<List<LeaveRequest>> = crmDao.getAllLeaveRequests()
    val allSalarySlips: Flow<List<SalarySlip>> = crmDao.getAllSalarySlips()
    val allTargets: Flow<List<Target>> = crmDao.getAllTargets()
    val allCallLogs: Flow<List<CallLog>> = crmDao.getAllCallLogs()

    fun getLeadsByStaff(staffId: Int): Flow<List<Lead>> = crmDao.getLeadsByStaff(staffId)
    fun getAttendanceForEmployee(employeeId: Int): Flow<List<Attendance>> = crmDao.getAttendanceForEmployee(employeeId)
    fun getLeaveRequestsForEmployee(employeeId: Int): Flow<List<LeaveRequest>> = crmDao.getLeaveRequestsForEmployee(employeeId)
    fun getSalarySlipsForEmployee(employeeId: Int): Flow<List<SalarySlip>> = crmDao.getSalarySlipsForEmployee(employeeId)
    fun getTargetsForEmployee(employeeId: Int): Flow<List<Target>> = crmDao.getTargetsForEmployee(employeeId)
    fun getCallLogsForLead(leadId: Int): Flow<List<CallLog>> = crmDao.getCallLogsForLead(leadId)
    fun getCallLogsForCaller(callerId: Int): Flow<List<CallLog>> = crmDao.getCallLogsForCaller(callerId)

    suspend fun getEmployeeByPhone(phone: String): Employee? = crmDao.getEmployeeByPhone(phone)
    suspend fun getEmployeeById(id: Int): Employee? = crmDao.getEmployeeById(id)
    suspend fun insertEmployee(employee: Employee): Long = crmDao.insertEmployee(employee)
    suspend fun deleteEmployee(employee: Employee) = crmDao.deleteEmployee(employee)

    suspend fun insertLead(lead: Lead): Long = crmDao.insertLead(lead)
    suspend fun insertLeads(leads: List<Lead>) = crmDao.insertLeads(leads)
    suspend fun bulkAssignLeads(leadIds: List<Int>, staffId: Int?, staffName: String?) =
        crmDao.bulkAssignLeads(leadIds, staffId, staffName)
    suspend fun deleteLead(lead: Lead) = crmDao.deleteLead(lead)

    suspend fun insertAttendance(attendance: Attendance): Long = crmDao.insertAttendance(attendance)
    suspend fun getAttendanceForEmployeeDate(employeeId: Int, date: String): Attendance? =
        crmDao.getAttendanceForEmployeeDate(employeeId, date)

    suspend fun insertLeaveRequest(request: LeaveRequest): Long = crmDao.insertLeaveRequest(request)
    suspend fun updateLeaveStatus(requestId: Int, status: String) = crmDao.updateLeaveStatus(requestId, status)

    suspend fun insertSalarySlip(salarySlip: SalarySlip): Long = crmDao.insertSalarySlip(salarySlip)
    suspend fun insertTarget(target: Target): Long = crmDao.insertTarget(target)
    suspend fun insertCallLog(callLog: CallLog): Long = crmDao.insertCallLog(callLog)

    suspend fun seedInitialDataIfEmpty() {
        // Run check
        val adminExists = crmDao.getEmployeeByPhone("1234567890")
        if (adminExists == null) {
            // Seed Admin
            val adminId = crmDao.insertEmployee(
                Employee(
                    name = "Sree Venkateswara Admin",
                    phone = "1234567890",
                    password = "admin",
                    role = "Admin",
                    designation = "Principal Manager",
                    salary = 120000.0,
                    aadhaar = "1234-5678-9012",
                    pan = "ABCDE1234F",
                    bankDetails = "SBI A/C: 100200300400",
                    joiningDate = "2024-01-01"
                )
            ).toInt()

            // Seed Staff 1
            val staff1Id = crmDao.insertEmployee(
                Employee(
                    name = "Venkata Raman",
                    phone = "9876543210",
                    password = "staff",
                    role = "Staff",
                    designation = "Senior Telecaller",
                    salary = 25000.0,
                    aadhaar = "9999-8888-7777",
                    pan = "XYZWP9876Q",
                    bankDetails = "HDFC A/C: 50100200300",
                    joiningDate = "2024-03-15"
                )
            ).toInt()

            // Seed Staff 2
            val staff2Id = crmDao.insertEmployee(
                Employee(
                    name = "Sai Kumar",
                    phone = "8888888888",
                    password = "staff",
                    role = "Staff",
                    designation = "Telecaller Associate",
                    salary = 20000.0,
                    aadhaar = "1111-2222-3333",
                    pan = "LMNOP4321Z",
                    bankDetails = "ICICI A/C: 300400500600",
                    joiningDate = "2024-05-01"
                )
            ).toInt()

            // Seed initial Leads from the real Venkateswara Insurance dataset
            val initialLeads = listOf(
                Lead(
                    name = "MOHAMMED AFZAL",
                    phone = "9012345678",
                    status = "Hot Leads",
                    followUpCategory = "Interested",
                    insuranceCategory = "Car Insurance",
                    premiumAmount = 14200.0,
                    renewalDate = "2025-06-02",
                    assignedStaffId = staff1Id,
                    assignedStaffName = "Venkata Raman",
                    callNotes = "Client wants a call tomorrow morning to close the premium."
                ),
                Lead(
                    name = "VATTEPU MADHU",
                    phone = "9440123456",
                    status = "Hot Leads",
                    followUpCategory = "Interested",
                    insuranceCategory = "Car Insurance",
                    premiumAmount = 15500.0,
                    renewalDate = "2025-06-02",
                    assignedStaffId = staff2Id,
                    assignedStaffName = "Sai Kumar",
                    callNotes = "Interested in renewing Maruti Dzire insurance. Needs quotes."
                ),
                Lead(
                    name = "GEETHIKA RATHOD",
                    phone = "9848011223",
                    status = "Fresh Data",
                    insuranceCategory = "Car Insurance",
                    premiumAmount = 13800.0,
                    renewalDate = "2025-06-02",
                    assignedStaffId = staff1Id,
                    assignedStaffName = "Venkata Raman",
                    callNotes = "Fresh lead assigned automatically."
                ),
                Lead(
                    name = "SUSHEELA ESLAVATH",
                    phone = "9100223344",
                    status = "Warm Leads",
                    followUpCategory = "Call Back",
                    insuranceCategory = "Car Insurance",
                    premiumAmount = 14500.0,
                    renewalDate = "2025-06-02",
                    assignedStaffId = staff2Id,
                    assignedStaffName = "Sai Kumar",
                    callNotes = "Called, asked to call back next week."
                ),
                Lead(
                    name = "LAXMI PRASANNA PERELLI",
                    phone = "9000123123",
                    status = "Converted",
                    followUpCategory = "Policy Purchased",
                    insuranceCategory = "Car Insurance",
                    premiumAmount = 12900.0,
                    renewalDate = "2025-06-02",
                    assignedStaffId = staff1Id,
                    assignedStaffName = "Venkata Raman",
                    callNotes = "Policy successfully renewed.",
                    policyNumber = "CAR-889912"
                ),
                Lead(
                    name = "VENU GOPAL POTHAGONI",
                    phone = "9876543001",
                    status = "Cold Leads",
                    followUpCategory = "Busy",
                    insuranceCategory = "Commercial Vehicle",
                    premiumAmount = 21500.0,
                    renewalDate = "2025-06-02",
                    assignedStaffId = staff2Id,
                    assignedStaffName = "Sai Kumar",
                    callNotes = "Line was busy. Will call again."
                ),
                Lead(
                    name = "KUMBHA RAMARAO",
                    phone = "9441234567",
                    status = "Hot Leads",
                    followUpCategory = "Call Back",
                    insuranceCategory = "Car Insurance",
                    premiumAmount = 16200.0,
                    renewalDate = "2025-06-02",
                    assignedStaffId = staff1Id,
                    assignedStaffName = "Venkata Raman",
                    callNotes = "Wants to check and compare competitive rates before finalizing."
                ),
                Lead(
                    name = "PRAVEEN KUMAR VAMKUNAVATH",
                    phone = "9900112288",
                    status = "Converted",
                    followUpCategory = "Policy Purchased",
                    insuranceCategory = "Commercial Vehicle",
                    premiumAmount = 23000.0,
                    renewalDate = "2025-06-02",
                    assignedStaffId = staff2Id,
                    assignedStaffName = "Sai Kumar",
                    callNotes = "Ertiga Commercial vehicle policy renewed successfully.",
                    policyNumber = "COM-551122"
                ),
                Lead(
                    name = "SHIREESHA BANOTHU",
                    phone = "9121213141",
                    status = "Fresh Data",
                    insuranceCategory = "Commercial Vehicle",
                    premiumAmount = 22500.0,
                    renewalDate = "2025-06-02",
                    assignedStaffId = staff1Id,
                    assignedStaffName = "Venkata Raman",
                    callNotes = "New lead from Suryapet, Ertiga model."
                ),
                Lead(
                    name = "DILEEP KUMAR NAGULA",
                    phone = "9030112233",
                    status = "Hot Leads",
                    followUpCategory = "Interested",
                    insuranceCategory = "Commercial Vehicle",
                    premiumAmount = 21000.0,
                    renewalDate = "2025-06-02",
                    assignedStaffId = staff2Id,
                    assignedStaffName = "Sai Kumar",
                    callNotes = "Interested in third party + comprehensive commercial cover."
                ),
                Lead(
                    name = "KOMMU KAVITHA",
                    phone = "9543210987",
                    status = "Warm Leads",
                    followUpCategory = "Call Back",
                    insuranceCategory = "Car Insurance",
                    premiumAmount = 18500.0,
                    renewalDate = "2025-06-04",
                    assignedStaffId = staff1Id,
                    assignedStaffName = "Venkata Raman",
                    callNotes = "Requested call back during evening hours."
                ),
                Lead(
                    name = "SHABANA BEGUM",
                    phone = "9112233445",
                    status = "Fresh Data",
                    insuranceCategory = "Car Insurance",
                    premiumAmount = 19000.0,
                    renewalDate = "2025-06-06",
                    assignedStaffId = staff2Id,
                    assignedStaffName = "Sai Kumar",
                    callNotes = "Citroen C3 electric vehicle lead."
                )
            )
            crmDao.insertLeads(initialLeads)

            // Seed some default targets
            crmDao.insertTarget(Target(employeeId = staff1Id, period = "Monthly", targetAmount = 100000.0, achievedAmount = 33000.0))
            crmDao.insertTarget(Target(employeeId = staff2Id, period = "Monthly", targetAmount = 80000.0, achievedAmount = 20500.0))

            // Seed attendance for yesterday/today
            crmDao.insertAttendance(Attendance(employeeId = staff1Id, employeeName = "Venkata Raman", date = "2026-06-29", checkInTime = "09:15", checkOutTime = "18:00", status = "Present"))
            crmDao.insertAttendance(Attendance(employeeId = staff2Id, employeeName = "Sai Kumar", date = "2026-06-29", checkInTime = "09:45", checkOutTime = "18:05", status = "Late Arrival"))
        }
    }
}
