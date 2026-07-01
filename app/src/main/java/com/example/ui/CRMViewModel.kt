package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.Target
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class CrmScreen {
    Splash,
    Login,
    AdminDashboard,
    StaffDashboard,
    LeadsList,
    EmployeeManagement,
    Attendance,
    LeaveRequests,
    Salaries,
    Targets,
    Reports,
    Profile
}

class CRMViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CrmRepository
    
    // UI Navigation State
    private val _currentScreen = MutableStateFlow(CrmScreen.Splash)
    val currentScreen: StateFlow<CrmScreen> = _currentScreen.asStateFlow()

    // Screen navigation stack for going back
    private val navigationHistory = Stack<CrmScreen>()

    // Current Session State
    private val _currentUser = MutableStateFlow<Employee?>(null)
    val currentUser: StateFlow<Employee?> = _currentUser.asStateFlow()

    private val _otpRequired = MutableStateFlow(false)
    val otpRequired: StateFlow<Boolean> = _otpRequired.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // Selected Lead for Detail screen
    private val _selectedLead = MutableStateFlow<Lead?>(null)
    val selectedLead: StateFlow<Lead?> = _selectedLead.asStateFlow()

    // Reactive database data
    val employees: StateFlow<List<Employee>>
    val allLeads: StateFlow<List<Lead>>
    val allAttendance: StateFlow<List<Attendance>>
    val allLeaveRequests: StateFlow<List<LeaveRequest>>
    val allSalarySlips: StateFlow<List<SalarySlip>>
    val allTargets: StateFlow<List<Target>>
    val allCallLogs: StateFlow<List<CallLog>>

    // Current user's specific data flows
    private val _myLeads = MutableStateFlow<List<Lead>>(emptyList())
    val myLeads: StateFlow<List<Lead>> = _myLeads.asStateFlow()

    private val _myAttendance = MutableStateFlow<List<Attendance>>(emptyList())
    val myAttendance: StateFlow<List<Attendance>> = _myAttendance.asStateFlow()

    private val _myLeaveRequests = MutableStateFlow<List<LeaveRequest>>(emptyList())
    val myLeaveRequests: StateFlow<List<LeaveRequest>> = _myLeaveRequests.asStateFlow()

    private val _mySalarySlips = MutableStateFlow<List<SalarySlip>>(emptyList())
    val mySalarySlips: StateFlow<List<SalarySlip>> = _mySalarySlips.asStateFlow()

    private val _myTargets = MutableStateFlow<List<Target>>(emptyList())
    val myTargets: StateFlow<List<Target>> = _myTargets.asStateFlow()

    // Attendance State of Today for Currently Logged in Employee
    private val _todayAttendance = MutableStateFlow<Attendance?>(null)
    val todayAttendance: StateFlow<Attendance?> = _todayAttendance.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        val dao = database.crmDao()
        repository = CrmRepository(dao)

        // Prepopulate database
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }

        // Initialize flows
        employees = repository.allEmployees.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        allLeads = repository.allLeads.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        allAttendance = repository.allAttendance.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        allLeaveRequests = repository.allLeaveRequests.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        allSalarySlips = repository.allSalarySlips.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        allTargets = repository.allTargets.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        allCallLogs = repository.allCallLogs.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

        // Set up observation of currentUser to load user-specific datasets
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    // Load leads
                    repository.getLeadsByStaff(user.id).collect { leadsList ->
                        _myLeads.value = leadsList
                    }
                } else {
                    _myLeads.value = emptyList()
                    _todayAttendance.value = null
                }
            }
        }

        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    repository.getAttendanceForEmployee(user.id).collect { attList ->
                        _myAttendance.value = attList
                        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        _todayAttendance.value = attList.find { it.date == todayStr }
                    }
                } else {
                    _myAttendance.value = emptyList()
                }
            }
        }

        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    repository.getLeaveRequestsForEmployee(user.id).collect { requests ->
                        _myLeaveRequests.value = requests
                    }
                } else {
                    _myLeaveRequests.value = emptyList()
                }
            }
        }

        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    repository.getSalarySlipsForEmployee(user.id).collect { slips ->
                        _mySalarySlips.value = slips
                    }
                } else {
                    _mySalarySlips.value = emptyList()
                }
            }
        }

        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    repository.getTargetsForEmployee(user.id).collect { targetsList ->
                        _myTargets.value = targetsList
                    }
                } else {
                    _myTargets.value = emptyList()
                }
            }
        }
    }

    // --- Navigation ---
    fun navigateTo(screen: CrmScreen) {
        navigationHistory.push(_currentScreen.value)
        _currentScreen.value = screen
    }

    fun navigateBack() {
        if (navigationHistory.isNotEmpty()) {
            _currentScreen.value = navigationHistory.pop()
        } else {
            _currentScreen.value = CrmScreen.Login
        }
    }

    fun selectLead(lead: Lead?) {
        _selectedLead.value = lead
    }

    // --- Authentication ---
    fun requestOtp(phone: String, password: String) {
        viewModelScope.launch {
            _loginError.value = null
            val employee = repository.getEmployeeByPhone(phone)
            if (employee != null && employee.password == password) {
                if (employee.role == "Admin" || employee.role == "Manager" || employee.role == "Team Leader") {
                    _otpRequired.value = true
                } else {
                    // Regular staff bypasses OTP for fast direct access, or we can use regular password login
                    _currentUser.value = employee
                    navigateTo(CrmScreen.StaffDashboard)
                }
            } else {
                _loginError.value = "Invalid phone number or password"
            }
        }
    }

    fun verifyOtpAndLogin(phone: String, otp: String) {
        viewModelScope.launch {
            _loginError.value = null
            if (otp == "1234" || otp == "123456" || otp.isNotEmpty()) { // standard OTP for demonstration
                val employee = repository.getEmployeeByPhone(phone)
                if (employee != null) {
                    _currentUser.value = employee
                    _otpRequired.value = false
                    if (employee.role == "Admin" || employee.role == "Manager") {
                        navigateTo(CrmScreen.AdminDashboard)
                    } else {
                        navigateTo(CrmScreen.StaffDashboard)
                    }
                } else {
                    _loginError.value = "Login failed. Employee not found."
                }
            } else {
                _loginError.value = "Invalid OTP code"
            }
        }
    }

    fun sendPasswordRecoveryOtp(phone: String, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            val employee = repository.getEmployeeByPhone(phone)
            if (employee != null) {
                // Generate a random 4-digit OTP for simulation
                val randomOtp = (1000..9999).random().toString()
                onSuccess(randomOtp)
            } else {
                onFailure("Phone number not registered. Please contact support.")
            }
        }
    }

    fun resetPassword(phone: String, newPassword: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            if (newPassword.length < 4) {
                onFailure("Please enter a secure password containing at least 4 characters.")
                return@launch
            }
            val employee = repository.getEmployeeByPhone(phone)
            if (employee != null) {
                val updatedEmployee = employee.copy(password = newPassword)
                repository.insertEmployee(updatedEmployee)
                _currentUser.value = updatedEmployee
                _loginError.value = null
                _otpRequired.value = false
                
                if (updatedEmployee.role == "Admin" || updatedEmployee.role == "Manager") {
                    navigateTo(CrmScreen.AdminDashboard)
                } else {
                    navigateTo(CrmScreen.StaffDashboard)
                }
                onSuccess()
            } else {
                onFailure("Failed to reset password. Employee record mismatch.")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _otpRequired.value = false
        _loginError.value = null
        _currentScreen.value = CrmScreen.Login
        navigationHistory.clear()
    }

    // --- Lead Management ---
    fun addOrUpdateLead(
        id: Int = 0,
        name: String,
        phone: String,
        status: String,
        followUpCategory: String?,
        insuranceCategory: String,
        premiumAmount: Double,
        renewalDate: String?,
        assignedStaffId: Int?,
        assignedStaffName: String?,
        callNotes: String?,
        policyNumber: String? = null
    ) {
        viewModelScope.launch {
            val lead = Lead(
                id = id,
                name = name,
                phone = phone,
                status = status,
                followUpCategory = followUpCategory,
                insuranceCategory = insuranceCategory,
                premiumAmount = premiumAmount,
                renewalDate = renewalDate,
                assignedStaffId = assignedStaffId,
                assignedStaffName = assignedStaffName,
                callNotes = callNotes,
                policyNumber = policyNumber,
                lastCallTime = if (callNotes != null) System.currentTimeMillis() else null
            )
            repository.insertLead(lead)
            // If lead selected is the one edited, update the selection
            if (_selectedLead.value?.id == id) {
                _selectedLead.value = lead
            }
        }
    }

    fun logCall(
        leadId: Int,
        outcome: String,
        durationSeconds: Int,
        notes: String,
        nextFollowUpDate: String?
    ) {
        val user = currentUser.value ?: return
        val lead = allLeads.value.find { it.id == leadId } ?: return
        viewModelScope.launch {
            val callLog = CallLog(
                leadId = leadId,
                leadName = lead.name,
                callerId = user.id,
                callerName = user.name,
                timestamp = System.currentTimeMillis(),
                outcome = outcome,
                durationSeconds = durationSeconds,
                notes = notes,
                nextFollowUpDate = nextFollowUpDate
            )
            repository.insertCallLog(callLog)

            // Propagate updates to the Lead record seamlessly
            var updatedStatus = lead.status
            var updatedFollowUp = lead.followUpCategory

            when (outcome) {
                "Converted" -> {
                    updatedStatus = "Converted"
                    updatedFollowUp = "Policy Purchased"
                }
                "Rejected" -> {
                    updatedStatus = "Rejected"
                    updatedFollowUp = "Not Interested"
                }
                "DND" -> {
                    updatedStatus = "DND"
                    updatedFollowUp = "Not Interested"
                }
                "Busy" -> {
                    updatedFollowUp = "Busy"
                }
                "No Answer" -> {
                    updatedFollowUp = "Switched Off"
                }
                "Interested" -> {
                    updatedStatus = if (lead.status == "New" || lead.status == "Fresh Data") "Hot Leads" else lead.status
                    updatedFollowUp = "Interested"
                }
                "Follow-up Scheduled" -> {
                    updatedStatus = if (lead.status == "New" || lead.status == "Fresh Data") "Warm Leads" else lead.status
                    updatedFollowUp = "Follow-up Tomorrow"
                }
            }

            val updatedLead = lead.copy(
                status = updatedStatus,
                followUpCategory = updatedFollowUp,
                callNotes = notes,
                lastCallTime = System.currentTimeMillis(),
                renewalDate = if (!nextFollowUpDate.isNullOrBlank() && outcome != "Converted") nextFollowUpDate else lead.renewalDate
            )
            repository.insertLead(updatedLead)

            if (_selectedLead.value?.id == leadId) {
                _selectedLead.value = updatedLead
            }
        }
    }

    fun deleteLead(lead: Lead) {
        viewModelScope.launch {
            repository.deleteLead(lead)
            if (_selectedLead.value?.id == lead.id) {
                _selectedLead.value = null
            }
        }
    }

    fun bulkAssignLeads(leadIds: List<Int>, staffId: Int?, staffName: String?) {
        viewModelScope.launch {
            repository.bulkAssignLeads(leadIds, staffId, staffName)
        }
    }

    fun autoAssignUnassignedLeads() {
        viewModelScope.launch {
            val unassignedLeads = allLeads.value.filter { it.assignedStaffId == null }
            val activeStaff = employees.value.filter { it.role == "Staff" }
            if (unassignedLeads.isNotEmpty() && activeStaff.isNotEmpty()) {
                var staffIndex = 0
                unassignedLeads.forEach { lead ->
                    val staff = activeStaff[staffIndex]
                    repository.insertLead(
                        lead.copy(
                            assignedStaffId = staff.id,
                            assignedStaffName = staff.name,
                            status = "Hot Leads" // move to hot once assigned
                        )
                    )
                    staffIndex = (staffIndex + 1) % activeStaff.size
                }
            }
        }
    }

    // --- CSV Import ---
    fun importLeadsFromCsv(
        csvText: String,
        assignedStaffId: Int? = null,
        assignedStaffName: String? = null,
        autoAssignRoundRobin: Boolean = false
    ): Boolean {
        return try {
            val lines = csvText.lineSequence().filter { it.isNotBlank() }.toList()
            if (lines.isEmpty()) return false

            val newLeads = mutableListOf<Lead>()
            // Expect Header: Name,Phone,InsuranceCategory,PremiumAmount,RenewalDate
            val hasHeader = lines.first().contains("Name", ignoreCase = true) || lines.first().contains("Phone", ignoreCase = true)
            val startIndex = if (hasHeader) 1 else 0

            val activeStaff = if (autoAssignRoundRobin) employees.value.filter { it.role == "Staff" } else emptyList()
            var staffIndex = 0

            for (i in startIndex until lines.size) {
                val tokens = lines[i].split(",").map { it.trim() }
                if (tokens.size >= 2) {
                    val name = tokens[0]
                    val phone = tokens[1]
                    val insuranceCategory = tokens.getOrNull(2) ?: "Motor Insurance"
                    val premiumAmount = tokens.getOrNull(3)?.toDoubleOrNull() ?: 5000.0
                    val renewalDate = tokens.getOrNull(4) ?: "2026-12-31"

                    var targetStaffId = assignedStaffId
                    var targetStaffName = assignedStaffName
                    var targetStatus = "Fresh Data"

                    if (autoAssignRoundRobin && activeStaff.isNotEmpty()) {
                        val staff = activeStaff[staffIndex]
                        targetStaffId = staff.id
                        targetStaffName = staff.name
                        targetStatus = "Hot Leads"
                        staffIndex = (staffIndex + 1) % activeStaff.size
                    } else if (targetStaffId != null) {
                        targetStatus = "Hot Leads"
                    }

                    newLeads.add(
                        Lead(
                            name = name,
                            phone = phone,
                            status = targetStatus,
                            insuranceCategory = insuranceCategory,
                            premiumAmount = premiumAmount,
                            renewalDate = renewalDate,
                            assignedStaffId = targetStaffId,
                            assignedStaffName = targetStaffName
                        )
                    )
                }
            }
            if (newLeads.isNotEmpty()) {
                viewModelScope.launch {
                    repository.insertLeads(newLeads)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // --- Backup & Restore ---
    fun getBackupString(): String {
        val leadsList = allLeads.value
        val sb = StringBuilder()
        sb.append("Name,Phone,InsuranceCategory,PremiumAmount,RenewalDate,Status,AssignedStaff\n")
        leadsList.forEach { lead ->
            sb.append("${lead.name},${lead.phone},${lead.insuranceCategory},${lead.premiumAmount},${lead.renewalDate ?: ""},${lead.status},${lead.assignedStaffName ?: ""}\n")
        }
        return sb.toString()
    }

    // --- Attendance Tracking ---
    fun checkIn(latitude: Double, longitude: Double, selfieUri: String?) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

            // Determine if Late (e.g. after 09:30 is late)
            val cal = Calendar.getInstance()
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)
            val status = if (hour > 9 || (hour == 9 && minute > 30)) "Late Arrival" else "Present"

            val attendance = Attendance(
                employeeId = user.id,
                employeeName = user.name,
                date = dateStr,
                checkInTime = timeStr,
                latitude = latitude,
                longitude = longitude,
                selfieUri = selfieUri,
                status = status
            )
            repository.insertAttendance(attendance)
        }
    }

    fun checkOut() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

            val currentToday = todayAttendance.value
            if (currentToday != null) {
                repository.insertAttendance(
                    currentToday.copy(checkOutTime = timeStr)
                )
            }
        }
    }

    // --- Leave Requests ---
    fun submitLeaveRequest(startDate: String, endDate: String, reason: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val request = LeaveRequest(
                employeeId = user.id,
                employeeName = user.name,
                startDate = startDate,
                endDate = endDate,
                reason = reason,
                status = "Pending"
            )
            repository.insertLeaveRequest(request)
        }
    }

    fun approveOrRejectLeave(requestId: Int, status: String) {
        viewModelScope.launch {
            repository.updateLeaveStatus(requestId, status)
        }
    }

    // --- Employee Management ---
    fun addOrUpdateEmployee(
        id: Int = 0,
        name: String,
        phone: String,
        password: String = "staff",
        role: String,
        designation: String,
        salary: Double,
        aadhaar: String,
        pan: String,
        bankDetails: String,
        joiningDate: String,
        photoUri: String? = null
    ) {
        viewModelScope.launch {
            val employee = Employee(
                id = id,
                name = name,
                phone = phone,
                password = password,
                role = role,
                designation = designation,
                salary = salary,
                aadhaar = aadhaar,
                pan = pan,
                bankDetails = bankDetails,
                joiningDate = joiningDate,
                photoUri = photoUri
            )
            repository.insertEmployee(employee)
        }
    }

    fun verifyOnboardingAndSetPassword(
        phone: String,
        aadhaar: String,
        pan: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            val employee = repository.getEmployeeByPhone(phone)
            if (employee == null) {
                onFailure("Phone number not registered. Please contact your administrator for onboarding.")
                return@launch
            }
            
            // Normalize spaces/dashes in Aadhaar
            val cleanInputAadhaar = aadhaar.replace(Regex("[\\s-]"), "")
            val cleanDbAadhaar = employee.aadhaar.replace(Regex("[\\s-]"), "")
            
            val cleanInputPan = pan.trim().uppercase()
            val cleanDbPan = employee.pan.trim().uppercase()

            if (cleanInputAadhaar != cleanDbAadhaar) {
                onFailure("The Aadhaar Number does not match our registered onboarding details.")
                return@launch
            }
            
            if (cleanInputPan != cleanDbPan) {
                onFailure("The PAN Number does not match our registered onboarding details.")
                return@launch
            }

            if (newPassword.length < 4) {
                onFailure("Please enter a secure password containing at least 4 characters.")
                return@launch
            }

            // Update employee password
            val updatedEmployee = employee.copy(password = newPassword)
            repository.insertEmployee(updatedEmployee)
            
            // Log in successfully
            _currentUser.value = updatedEmployee
            _loginError.value = null
            _otpRequired.value = false
            
            if (updatedEmployee.role == "Admin" || updatedEmployee.role == "Manager") {
                navigateTo(CrmScreen.AdminDashboard)
            } else {
                navigateTo(CrmScreen.StaffDashboard)
            }
            onSuccess()
        }
    }

    fun deleteEmployee(employee: Employee) {
        viewModelScope.launch {
            repository.deleteEmployee(employee)
        }
    }

    // --- Salary Slip Generation ---
    fun generateSalarySlip(
        employeeId: Int,
        month: String,
        bonus: Double,
        deductions: Double
    ) {
        viewModelScope.launch {
            val emp = employees.value.find { it.id == employeeId } ?: return@launch
            // Calculate actual incentives based on converted premium
            val staffLeads = allLeads.value.filter { it.assignedStaffId == employeeId && it.status == "Converted" }
            val totalPremium = staffLeads.sumOf { it.premiumAmount }
            val calculatedIncentive = totalPremium * 0.05 // 5% incentive default

            val net = emp.salary + calculatedIncentive + bonus - deductions

            val slip = SalarySlip(
                employeeId = employeeId,
                employeeName = emp.name,
                month = month,
                basicSalary = emp.salary,
                incentives = calculatedIncentive,
                bonus = bonus,
                deductions = deductions,
                netSalary = net
            )
            repository.insertSalarySlip(slip)
        }
    }

    // --- Target Allocations ---
    fun setEmployeeTarget(employeeId: Int, period: String, targetAmount: Double) {
        viewModelScope.launch {
            // Find current achieved amount (e.g. from total premiums converted)
            val staffLeads = allLeads.value.filter { it.assignedStaffId == employeeId && it.status == "Converted" }
            val totalAchieved = staffLeads.sumOf { it.premiumAmount }

            val target = Target(
                employeeId = employeeId,
                period = period,
                targetAmount = targetAmount,
                achievedAmount = totalAchieved
            )
            repository.insertTarget(target)
        }
    }
}
