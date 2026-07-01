@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.data.Target
import com.example.ui.CRMViewModel
import com.example.ui.CrmScreen
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalCrmDrawerController = staticCompositionLocalOf<() -> Unit> { { } }

@Composable
fun MainCrmApp(viewModel: CRMViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val drawerController = remember(drawerState, coroutineScope) {
        {
            coroutineScope.launch {
                if (drawerState.isClosed) drawerState.open() else drawerState.close()
            }
            Unit
        }
    }

    CompositionLocalProvider(LocalCrmDrawerController provides drawerController) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = currentScreen != CrmScreen.Login && currentScreen != CrmScreen.Splash,
            drawerContent = {
                if (currentScreen != CrmScreen.Login && currentScreen != CrmScreen.Splash && currentUser != null) {
                    CrmSidebarDrawerContent(
                        currentUser = currentUser!!,
                        currentScreen = currentScreen,
                        onNavigate = { screen ->
                            viewModel.navigateTo(screen)
                            coroutineScope.launch { drawerState.close() }
                        },
                        onLogout = {
                            viewModel.logout()
                            coroutineScope.launch { drawerState.close() }
                        }
                    )
                }
            }
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "screen_transition"
                ) { screen ->
                    when (screen) {
                        CrmScreen.Splash -> SplashScreen(viewModel)
                        CrmScreen.Login -> LoginScreen(viewModel)
                        CrmScreen.AdminDashboard -> AdminDashboardScreen(viewModel)
                        CrmScreen.StaffDashboard -> StaffDashboardScreen(viewModel)
                        CrmScreen.LeadsList -> LeadsListScreen(viewModel)
                        CrmScreen.EmployeeManagement -> EmployeeManagementScreen(viewModel)
                        CrmScreen.Attendance -> AttendanceScreen(viewModel)
                        CrmScreen.LeaveRequests -> LeaveRequestsScreen(viewModel)
                        CrmScreen.Salaries -> SalaryManagementScreen(viewModel)
                        CrmScreen.Targets -> TargetsScreen(viewModel)
                        CrmScreen.Reports -> ReportsScreen(viewModel)
                        CrmScreen.Profile -> ProfileScreen(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun CrmSidebarDrawerContent(
    currentUser: Employee,
    currentScreen: CrmScreen,
    onNavigate: (CrmScreen) -> Unit,
    onLogout: () -> Unit
) {
    val isAdmin = currentUser.role == "Admin" || currentUser.role == "Manager"

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // --- HEADER CARD ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = currentUser.name.take(1).uppercase(),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = currentUser.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = currentUser.role,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Designation: ${currentUser.designation}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Phone: ${currentUser.phone}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }
            }

            // Divider below header
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // --- SCROLLABLE NAVIGATION ITEMS ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "NAVIGATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
                )

                if (isAdmin) {
                    // 1. Dashboard
                    DrawerItem(
                        label = "Dashboard",
                        icon = Icons.Default.Home,
                        selected = currentScreen == CrmScreen.AdminDashboard,
                        onClick = { onNavigate(CrmScreen.AdminDashboard) }
                    )
                    // 2. Leads Management
                    DrawerItem(
                        label = "Leads Management",
                        icon = Icons.Default.List,
                        selected = currentScreen == CrmScreen.LeadsList,
                        onClick = { onNavigate(CrmScreen.LeadsList) }
                    )
                    // 3. Staff Attendance
                    DrawerItem(
                        label = "Staff Attendance",
                        icon = Icons.Default.Face,
                        selected = currentScreen == CrmScreen.Attendance,
                        onClick = { onNavigate(CrmScreen.Attendance) }
                    )
                    // 4. Leave Applications
                    DrawerItem(
                        label = "Leave Applications",
                        icon = Icons.Default.Assignment,
                        selected = currentScreen == CrmScreen.LeaveRequests,
                        onClick = { onNavigate(CrmScreen.LeaveRequests) }
                    )
                    // 5. Staff Directory
                    DrawerItem(
                        label = "Staff Directory",
                        icon = Icons.Default.People,
                        selected = currentScreen == CrmScreen.EmployeeManagement,
                        onClick = { onNavigate(CrmScreen.EmployeeManagement) }
                    )
                    // 6. Salary Administration
                    DrawerItem(
                        label = "Salary Payroll",
                        icon = Icons.Default.Edit,
                        selected = currentScreen == CrmScreen.Salaries,
                        onClick = { onNavigate(CrmScreen.Salaries) }
                    )
                    // 7. Target Setter
                    DrawerItem(
                        label = "Target Setter",
                        icon = Icons.Default.Star,
                        selected = currentScreen == CrmScreen.Targets,
                        onClick = { onNavigate(CrmScreen.Targets) }
                    )
                    // 8. Advanced Analytics
                    DrawerItem(
                        label = "Reports & Analytics",
                        icon = Icons.Default.Assessment,
                        selected = currentScreen == CrmScreen.Reports,
                        onClick = { onNavigate(CrmScreen.Reports) }
                    )
                } else {
                    // Staff navigation
                    // 1. My Dashboard
                    DrawerItem(
                        label = "My Dashboard",
                        icon = Icons.Default.Home,
                        selected = currentScreen == CrmScreen.StaffDashboard,
                        onClick = { onNavigate(CrmScreen.StaffDashboard) }
                    )
                    // 2. My Leads
                    DrawerItem(
                        label = "My Leads",
                        icon = Icons.Default.List,
                        selected = currentScreen == CrmScreen.LeadsList,
                        onClick = { onNavigate(CrmScreen.LeadsList) }
                    )
                    // 3. My Attendance
                    DrawerItem(
                        label = "My Attendance Calendar",
                        icon = Icons.Default.DateRange,
                        selected = currentScreen == CrmScreen.Attendance,
                        onClick = { onNavigate(CrmScreen.Attendance) }
                    )
                    // 4. Leave Applications
                    DrawerItem(
                        label = "My Leaves & Applications",
                        icon = Icons.Default.Assignment,
                        selected = currentScreen == CrmScreen.LeaveRequests,
                        onClick = { onNavigate(CrmScreen.LeaveRequests) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "USER PREFERENCES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
                )

                // Profile
                DrawerItem(
                    label = "My Profile",
                    icon = Icons.Default.Person,
                    selected = currentScreen == CrmScreen.Profile,
                    onClick = { onNavigate(CrmScreen.Profile) }
                )
            }

            // --- FOOTER SECTION ---
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "logout",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign Out Account",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DrawerItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp)
            )
        },
        selected = selected,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedContainerColor = Color.Transparent,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    )
}


// --- Welcome Banner drawing logo ---
@Composable
fun VenkateswaraEntranceLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(160.dp)) {
        val w = size.width
        val h = size.height

        // Define precise colors based on user's logo
        val royalBlue = Color(0xFF0F3B8C)
        val deepNavy = Color(0xFF0A2E6D)
        val goldAccent = Color(0xFFD4AF37)
        val redAccent = Color(0xFFD32F2F)
        val whiteColor = Color.White

        // 1. Draw the Cradling Hand (supporting the shield from underneath)
        val handPath = Path().apply {
            moveTo(w * 0.15f, h * 0.40f)
            // Left swoop down
            cubicTo(
                w * 0.15f, h * 0.60f,
                w * 0.30f, h * 0.85f,
                w * 0.50f, h * 0.85f
            )
            // Right swoop up to fingertips
            cubicTo(
                w * 0.70f, h * 0.85f,
                w * 0.85f, h * 0.62f,
                w * 0.88f, h * 0.52f
            )
            // Fingertip curve
            cubicTo(
                w * 0.89f, h * 0.49f,
                w * 0.85f, h * 0.50f,
                w * 0.80f, h * 0.55f
            )
            // Inner curve back to thickness
            cubicTo(
                w * 0.72f, h * 0.65f,
                w * 0.62f, h * 0.75f,
                w * 0.50f, h * 0.76f
            )
            cubicTo(
                w * 0.38f, h * 0.75f,
                w * 0.25f, h * 0.62f,
                w * 0.22f, h * 0.45f
            )
            close()
        }
        drawPath(path = handPath, color = deepNavy)

        // 2. Draw the Shield Outline (royal blue)
        val shieldPath = Path().apply {
            moveTo(w * 0.28f, h * 0.24f)
            // Dip in center of crest
            quadraticTo(w * 0.50f, h * 0.16f, w * 0.72f, h * 0.24f)
            // Right curve down
            quadraticTo(w * 0.71f, h * 0.50f, w * 0.50f, h * 0.74f)
            // Left curve up
            quadraticTo(w * 0.29f, h * 0.50f, w * 0.28f, h * 0.24f)
            close()
        }
        // Fill shield with dynamic white background so behind elements don't bleed
        drawPath(path = shieldPath, color = whiteColor)
        drawPath(
            path = shieldPath,
            color = royalBlue,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
        )

        // 3. Draw the giant stylized 'V' inside the shield
        val vPath = Path().apply {
            // Left top
            moveTo(w * 0.34f, h * 0.28f)
            // Bottom tip of V
            lineTo(w * 0.50f, h * 0.62f)
            // Right top of V
            lineTo(w * 0.66f, h * 0.28f)
            // Right stem width
            lineTo(w * 0.60f, h * 0.28f)
            // Inner V tip
            lineTo(w * 0.50f, h * 0.52f)
            // Left stem width
            lineTo(w * 0.40f, h * 0.28f)
            close()
        }
        drawPath(path = vPath, color = royalBlue.copy(alpha = 0.15f))

        // 4. Draw the Venkateswara Gopuram (Temple Crown) in Gold, centered at top
        // Let's draw the layers of the gopuram
        // Layer 1 (bottom base)
        drawRoundRect(
            color = goldAccent,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.44f, h * 0.36f),
            size = androidx.compose.ui.geometry.Size(w * 0.12f, h * 0.04f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
        )
        // Layer 2 (middle tier)
        drawRoundRect(
            color = goldAccent,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.46f, h * 0.31f),
            size = androidx.compose.ui.geometry.Size(w * 0.08f, h * 0.04f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
        )
        // Layer 3 (top dome)
        drawRoundRect(
            color = goldAccent,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.475f, h * 0.26f),
            size = androidx.compose.ui.geometry.Size(w * 0.05f, h * 0.04f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
        )
        // Kalasam spike
        val spikePath = Path().apply {
            moveTo(w * 0.50f, h * 0.22f)
            lineTo(w * 0.485f, h * 0.26f)
            lineTo(w * 0.515f, h * 0.26f)
            close()
        }
        drawPath(path = spikePath, color = goldAccent)

        // 5. Draw the Red Tilak (Namam) in the center of the Gopuram
        // Left white mark
        drawRect(
            color = Color.LightGray,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.485f, h * 0.32f),
            size = androidx.compose.ui.geometry.Size(w * 0.008f, h * 0.02f)
        )
        // Right white mark
        drawRect(
            color = Color.LightGray,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.507f, h * 0.32f),
            size = androidx.compose.ui.geometry.Size(w * 0.008f, h * 0.02f)
        )
        // Red center mark (bindu)
        drawRect(
            color = redAccent,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.497f, h * 0.32f),
            size = androidx.compose.ui.geometry.Size(w * 0.006f, h * 0.025f)
        )

        // 6. Draw Icons in lower half of Shield (House, Family, Car)
        
        // --- HOUSE (left) ---
        // Roof
        val houseRoof = Path().apply {
            moveTo(w * 0.35f, h * 0.54f)
            lineTo(w * 0.39f, h * 0.50f)
            lineTo(w * 0.43f, h * 0.54f)
            close()
        }
        drawPath(path = houseRoof, color = royalBlue)
        // Body
        drawRect(
            color = royalBlue,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.36f, h * 0.54f),
            size = androidx.compose.ui.geometry.Size(w * 0.06f, h * 0.06f)
        )
        // Door (white)
        drawRect(
            color = whiteColor,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.38f, h * 0.57f),
            size = androidx.compose.ui.geometry.Size(w * 0.02f, h * 0.03f)
        )

        // --- FAMILY SILHOUETTES (center) ---
        // Father
        drawCircle(color = royalBlue, radius = w * 0.012f, center = androidx.compose.ui.geometry.Offset(w * 0.48f, h * 0.49f))
        drawRoundRect(
            color = royalBlue,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.47f, h * 0.51f),
            size = androidx.compose.ui.geometry.Size(w * 0.02f, h * 0.07f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
        )
        // Mother
        drawCircle(color = royalBlue, radius = w * 0.011f, center = androidx.compose.ui.geometry.Offset(w * 0.52f, h * 0.50f))
        val motherPath = Path().apply {
            moveTo(w * 0.51f, h * 0.52f)
            lineTo(w * 0.53f, h * 0.52f)
            lineTo(w * 0.535f, h * 0.58f)
            lineTo(w * 0.505f, h * 0.58f)
            close()
        }
        drawPath(path = motherPath, color = royalBlue)
        // Children (between or sides)
        // Child 1 (left)
        drawCircle(color = royalBlue, radius = w * 0.008f, center = androidx.compose.ui.geometry.Offset(w * 0.45f, h * 0.53f))
        drawRoundRect(
            color = royalBlue,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.443f, h * 0.545f),
            size = androidx.compose.ui.geometry.Size(w * 0.014f, h * 0.045f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx())
        )
        // Child 2 (right)
        drawCircle(color = royalBlue, radius = w * 0.008f, center = androidx.compose.ui.geometry.Offset(w * 0.55f, h * 0.53f))
        drawRoundRect(
            color = royalBlue,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.543f, h * 0.545f),
            size = androidx.compose.ui.geometry.Size(w * 0.014f, h * 0.045f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx())
        )

        // --- CAR (right) ---
        // Cabin
        drawRoundRect(
            color = royalBlue,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.58f, h * 0.52f),
            size = androidx.compose.ui.geometry.Size(w * 0.06f, h * 0.04f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
        )
        // Body
        drawRoundRect(
            color = royalBlue,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.57f, h * 0.545f),
            size = androidx.compose.ui.geometry.Size(w * 0.08f, h * 0.04f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
        )
        // Wheels
        drawCircle(color = whiteColor, radius = w * 0.01f, center = androidx.compose.ui.geometry.Offset(w * 0.59f, h * 0.585f))
        drawCircle(color = deepNavy, radius = w * 0.006f, center = androidx.compose.ui.geometry.Offset(w * 0.59f, h * 0.585f))
        drawCircle(color = whiteColor, radius = w * 0.01f, center = androidx.compose.ui.geometry.Offset(w * 0.63f, h * 0.585f))
        drawCircle(color = deepNavy, radius = w * 0.006f, center = androidx.compose.ui.geometry.Offset(w * 0.63f, h * 0.585f))
    }
}

@Composable
fun BrandLogoHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VenkateswaraEntranceLogo(
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "VENKATESWARA",
            fontSize = 24.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0F3B8C),
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Divider(color = Color(0xFFD4AF37), modifier = Modifier.width(36.dp).height(1.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "INSURANCES",
                fontSize = 16.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD4AF37),
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(8.dp))
            Divider(color = Color(0xFFD4AF37), modifier = Modifier.width(36.dp).height(1.dp))
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "SECURING TODAY  •  PROTECTING TOMORROW",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0F3B8C).copy(alpha = 0.8f),
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )
    }
}

// --- Login Screen ---
@Composable
fun LoginScreen(viewModel: CRMViewModel) {
    val context = LocalContext.current
    var phone by remember { mutableStateOf("1234567890") } // Defaults admin for easy access
    var password by remember { mutableStateOf("admin") }
    var otpCode by remember { mutableStateOf("1234") }
    
    var isActivationMode by remember { mutableStateOf(false) }
    var activationPhone by remember { mutableStateOf("") }
    var activationAadhaar by remember { mutableStateOf("") }
    var activationPan by remember { mutableStateOf("") }
    var activationNewPassword by remember { mutableStateOf("") }
    var activationError by remember { mutableStateOf<String?>(null) }

    var isForgotPasswordMode by remember { mutableStateOf(false) }
    var forgotPhone by remember { mutableStateOf("") }
    var forgotOtpCode by remember { mutableStateOf("") }
    var forgotGeneratedOtp by remember { mutableStateOf("") }
    var isForgotOtpSent by remember { mutableStateOf(false) }
    var isForgotOtpVerified by remember { mutableStateOf(false) }
    var forgotNewPassword by remember { mutableStateOf("") }
    var forgotConfirmPassword by remember { mutableStateOf("") }
    var forgotError by remember { mutableStateOf<String?>(null) }
    
    val otpRequired by viewModel.otpRequired.collectAsState()
    val loginError by viewModel.loginError.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 400.dp)
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BrandLogoHeader()
                Spacer(modifier = Modifier.height(24.dp))

                if (isActivationMode) {
                    Text(
                        text = "Verify Onboarded Profile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Text(
                        text = "Activate your staff login account using details submitted during your onboarding registration.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp).align(Alignment.Start)
                    )

                    OutlinedTextField(
                        value = activationPhone,
                        onValueChange = { 
                            activationPhone = it
                            activationError = null 
                        },
                        label = { Text("Registered Mobile Phone") },
                        leadingIcon = { Icon(Icons.Default.Phone, "phone") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = activationAadhaar,
                        onValueChange = { 
                            activationAadhaar = it
                            activationError = null 
                        },
                        label = { Text("Aadhaar Card Number") },
                        placeholder = { Text("XXXX-XXXX-XXXX") },
                        leadingIcon = { Icon(Icons.Default.Assignment, "aadhaar") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = activationPan,
                        onValueChange = { 
                            activationPan = it
                            activationError = null 
                        },
                        label = { Text("PAN Card Number") },
                        placeholder = { Text("ABCDE1234F") },
                        leadingIcon = { Icon(Icons.Default.Person, "pan") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = activationNewPassword,
                        onValueChange = { 
                            activationNewPassword = it
                            activationError = null 
                        },
                        label = { Text("Set New Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, "password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (activationError != null) {
                        Text(
                            text = activationError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (activationPhone.isEmpty() || activationAadhaar.isEmpty() || activationPan.isEmpty() || activationNewPassword.isEmpty()) {
                                activationError = "Please fill in all onboarding verification fields."
                            } else {
                                viewModel.verifyOnboardingAndSetPassword(
                                    phone = activationPhone,
                                    aadhaar = activationAadhaar,
                                    pan = activationPan,
                                    newPassword = activationNewPassword,
                                    onSuccess = {
                                        Toast.makeText(context, "Account activated successfully!", Toast.LENGTH_LONG).show()
                                        isActivationMode = false
                                    },
                                    onFailure = { error ->
                                        activationError = error
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Verify & Activate Login", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = { isActivationMode = false }
                    ) {
                        Text("Back to Secure Sign In", color = MaterialTheme.colorScheme.secondary)
                    }
                } else if (isForgotPasswordMode) {
                    Text(
                        text = "Recover Password",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Text(
                        text = "Follow the OTP-based verification steps to safely recover and reset your account password.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp).align(Alignment.Start)
                    )

                    if (!isForgotOtpSent) {
                        // Step 1: Input Phone Number to request OTP
                        OutlinedTextField(
                            value = forgotPhone,
                            onValueChange = { 
                                forgotPhone = it
                                forgotError = null
                            },
                            label = { Text("Registered Mobile Phone") },
                            leadingIcon = { Icon(Icons.Default.Phone, "phone") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (forgotError != null) {
                            Text(
                                text = forgotError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                             )
                         }

                         Button(
                             onClick = {
                                 if (forgotPhone.isEmpty()) {
                                     forgotError = "Please enter your registered phone number."
                                 } else {
                                     viewModel.sendPasswordRecoveryOtp(
                                         phone = forgotPhone,
                                         onSuccess = { generatedOtp ->
                                             forgotGeneratedOtp = generatedOtp
                                             isForgotOtpSent = true
                                             Toast.makeText(context, "OTP Sent successfully!", Toast.LENGTH_SHORT).show()
                                         },
                                         onFailure = { error ->
                                             forgotError = error
                                         }
                                     )
                                 }
                             },
                             modifier = Modifier
                                 .fillMaxWidth()
                                 .height(50.dp),
                             colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                             shape = RoundedCornerShape(12.dp)
                         ) {
                             Text("Send Reset OTP", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                         }
                     } else if (!isForgotOtpVerified) {
                         // Step 2: Verification of Sent OTP
                         OutlinedTextField(
                             value = forgotPhone,
                             onValueChange = {},
                             label = { Text("Registered Phone") },
                             leadingIcon = { Icon(Icons.Default.Phone, "phone") },
                             modifier = Modifier.fillMaxWidth(),
                             enabled = false,
                             singleLine = true
                         )
                         Spacer(modifier = Modifier.height(12.dp))

                         // Beautiful M3 card showing mock received SMS OTP securely
                         Card(
                             modifier = Modifier.fillMaxWidth(),
                             colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)),
                             shape = RoundedCornerShape(12.dp)
                         ) {
                             Row(
                                 modifier = Modifier.padding(12.dp),
                                 verticalAlignment = Alignment.CenterVertically
                             ) {
                                 Text(
                                     text = "🔑 Verification SMS Received:\nYour password reset verification code is: ",
                                     fontSize = 12.sp,
                                     color = MaterialTheme.colorScheme.onSecondaryContainer,
                                     modifier = Modifier.weight(1f)
                                 )
                                 Text(
                                     text = forgotGeneratedOtp,
                                     fontSize = 18.sp,
                                     fontWeight = FontWeight.ExtraBold,
                                     color = MaterialTheme.colorScheme.primary
                                 )
                             }
                         }
                         Spacer(modifier = Modifier.height(12.dp))

                         OutlinedTextField(
                             value = forgotOtpCode,
                             onValueChange = { 
                                 forgotOtpCode = it
                                 forgotError = null
                             },
                             label = { Text("Enter 4-digit OTP Code") },
                             leadingIcon = { Icon(Icons.Default.Lock, "otp") },
                             modifier = Modifier.fillMaxWidth(),
                             keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                             singleLine = true
                         )
                         Spacer(modifier = Modifier.height(16.dp))

                         if (forgotError != null) {
                             Text(
                                 text = forgotError ?: "",
                                 color = MaterialTheme.colorScheme.error,
                                 fontSize = 12.sp,
                                 modifier = Modifier.padding(bottom = 12.dp)
                             )
                         }

                         Button(
                             onClick = {
                                 if (forgotOtpCode == forgotGeneratedOtp || forgotOtpCode == "1234") {
                                     isForgotOtpVerified = true
                                     forgotError = null
                                 } else {
                                     forgotError = "Invalid verification code. Please check and try again."
                                 }
                             },
                             modifier = Modifier
                                 .fillMaxWidth()
                                 .height(50.dp),
                             colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                             shape = RoundedCornerShape(12.dp)
                         ) {
                             Text("Verify Security OTP", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                         }
                     } else {
                         // Step 3: Enter and Confirm New Password
                         OutlinedTextField(
                             value = forgotNewPassword,
                             onValueChange = { 
                                 forgotNewPassword = it
                                 forgotError = null
                             },
                             label = { Text("Enter New Password") },
                             leadingIcon = { Icon(Icons.Default.Lock, "password") },
                             visualTransformation = PasswordVisualTransformation(),
                             modifier = Modifier.fillMaxWidth(),
                             keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                             singleLine = true
                         )
                         Spacer(modifier = Modifier.height(8.dp))

                         OutlinedTextField(
                             value = forgotConfirmPassword,
                             onValueChange = { 
                                 forgotConfirmPassword = it
                                 forgotError = null
                             },
                             label = { Text("Confirm New Password") },
                             leadingIcon = { Icon(Icons.Default.Lock, "password") },
                             visualTransformation = PasswordVisualTransformation(),
                             modifier = Modifier.fillMaxWidth(),
                             keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                             singleLine = true
                         )
                         Spacer(modifier = Modifier.height(16.dp))

                         if (forgotError != null) {
                             Text(
                                 text = forgotError ?: "",
                                 color = MaterialTheme.colorScheme.error,
                                 fontSize = 12.sp,
                                 modifier = Modifier.padding(bottom = 12.dp)
                             )
                         }

                         Button(
                             onClick = {
                                 if (forgotNewPassword.isEmpty() || forgotConfirmPassword.isEmpty()) {
                                     forgotError = "Please fill in all password fields."
                                 } else if (forgotNewPassword != forgotConfirmPassword) {
                                     forgotError = "Passwords do not match."
                                 } else {
                                     viewModel.resetPassword(
                                         phone = forgotPhone,
                                         newPassword = forgotNewPassword,
                                         onSuccess = {
                                             Toast.makeText(context, "Password reset successfully! Logged in.", Toast.LENGTH_LONG).show()
                                             isForgotPasswordMode = false
                                             isForgotOtpSent = false
                                             isForgotOtpVerified = false
                                             forgotPhone = ""
                                             forgotOtpCode = ""
                                             forgotNewPassword = ""
                                             forgotConfirmPassword = ""
                                         },
                                         onFailure = { error ->
                                             forgotError = error
                                         }
                                     )
                                 }
                             },
                             modifier = Modifier
                                 .fillMaxWidth()
                                 .height(50.dp),
                             colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                             shape = RoundedCornerShape(12.dp)
                         ) {
                             Text("Reset Password & Login", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                         }
                     }

                     Spacer(modifier = Modifier.height(12.dp))

                     TextButton(
                         onClick = {
                             isForgotPasswordMode = false
                             isForgotOtpSent = false
                             isForgotOtpVerified = false
                             forgotError = null
                         }
                     ) {
                         Text("Back to Sign In", color = MaterialTheme.colorScheme.secondary)
                     }
                 } else {
                     if (!otpRequired) {
                        Text(
                            text = "Secure Sign In",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, "phone") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, "password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { 
                                    isForgotPasswordMode = true
                                    forgotPhone = phone
                                    forgotError = null
                                }
                            ) {
                                Text(
                                    text = "Forgot Password?",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        if (loginError != null) {
                            Text(
                                text = loginError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        Button(
                            onClick = { viewModel.requestOtp(phone, password) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Send OTP & Verification", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = "OTP Authentication Required",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Text(
                            text = "Admin/Manager verification code sent",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { otpCode = it },
                            label = { Text("One-Time PIN (OTP)") },
                            leadingIcon = { Icon(Icons.Default.Lock, "otp") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        if (loginError != null) {
                            Text(
                                text = loginError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        Button(
                            onClick = { viewModel.verifyOtpAndLogin(phone, otpCode) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Verify & Access Portal", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        onClick = { isActivationMode = true }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "First-Time Staff Login?",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Verify your Onboarding Aadhaar/PAN to activate your account and choose password.",
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Demo Credentials:\nAdmin: 1234567890 / admin (OTP: 1234)\nStaff: 9876543210 / staff",
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// --- Upcoming Policy Renewals highlight system ---
fun getDaysRemaining(renewalDateStr: String?): Long? {
    if (renewalDateStr.isNullOrBlank()) return null
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsed = format.parse(renewalDateStr) ?: return null
        
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val target = Calendar.getInstance().apply {
            time = parsed
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val diffMs = target.timeInMillis - today.timeInMillis
        diffMs / (1000 * 60 * 60 * 24)
    } catch (e: Exception) {
        null
    }
}

@Composable
fun UpcomingPolicyRenewalsBanner(
    leads: List<Lead>,
    viewModel: CRMViewModel,
    modifier: Modifier = Modifier
) {
    val upcomingRenewals = remember(leads) {
        leads.mapNotNull { lead ->
            getDaysRemaining(lead.renewalDate)?.let { days ->
                if (days in 0..30) lead to days else null
            }
        }.sortedBy { it.second }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Renewals Icon",
                    tint = if (upcomingRenewals.isNotEmpty()) Color(0xFFC2185B) else Color(0xFF388E3C),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Upcoming Policy Renewals (30 Days)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            if (upcomingRenewals.isNotEmpty()) {
                Surface(
                    color = Color(0xFFC2185B),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${upcomingRenewals.size} Pending",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        if (upcomingRenewals.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9).copy(alpha = 0.6f)
                ),
                border = BorderStroke(1.dp, Color(0xFFC8E6C9))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "All up to date",
                        tint = Color(0xFF388E3C),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "All active policies are secure",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                        Text(
                            text = "No upcoming customer renewals within the next 30 days.",
                            fontSize = 11.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                upcomingRenewals.forEach { (lead, daysLeft) ->
                    val isUrgent = daysLeft <= 7
                    val accentColor = if (isUrgent) Color(0xFFD32F2F) else Color(0xFFF57C00)
                    val bgGradient = Brush.horizontalGradient(
                        colors = listOf(accentColor, accentColor.copy(alpha = 0.8f))
                    )

                    Card(
                        modifier = Modifier
                            .width(260.dp)
                            .height(145.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(6.dp)
                                    .background(bgGradient)
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = lead.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        
                                        Surface(
                                            color = accentColor.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = if (daysLeft == 0L) "Today" else if (daysLeft == 1L) "1 Day Left" else "$daysLeft Days Left",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = accentColor,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = lead.phone,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier.height(18.dp)
                                    ) {
                                        Text(
                                            text = lead.insuranceCategory ?: "General Insurance",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Due: ${lead.renewalDate}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )

                                    TextButton(
                                        onClick = {
                                            viewModel.selectLead(lead)
                                            viewModel.navigateTo(CrmScreen.LeadsList)
                                        },
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(
                                            text = "Act Now",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = "Act Now",
                                            tint = accentColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class MonthStat(
    val monthName: String,
    val count: Int,
    val year: Int,
    val monthIndex: Int
)

@Composable
fun RenewalsByMonthDistributionChart(
    leads: List<Lead>,
    modifier: Modifier = Modifier
) {
    val monthStats = remember(leads) {
        val calendar = Calendar.getInstance()
        val statsList = mutableListOf<MonthStat>()
        
        // Generate next 6 months starting from the current system time
        val sdfLabel = SimpleDateFormat("MMM ''yy", Locale.getDefault())
        for (i in 0 until 6) {
            val tempCal = calendar.clone() as Calendar
            tempCal.add(Calendar.MONTH, i)
            val monthName = sdfLabel.format(tempCal.time)
            val yr = tempCal.get(Calendar.YEAR)
            val mIdx = tempCal.get(Calendar.MONTH)
            statsList.add(MonthStat(monthName, 0, yr, mIdx))
        }

        // Count leads in each month
        val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        leads.forEach { lead ->
            if (!lead.renewalDate.isNullOrBlank() && lead.status != "Rejected") {
                try {
                    val date = sdfInput.parse(lead.renewalDate)
                    if (date != null) {
                        val leadCal = Calendar.getInstance().apply { time = date }
                        val leadYear = leadCal.get(Calendar.YEAR)
                        val leadMonth = leadCal.get(Calendar.MONTH)
                        
                        // Find match in our statsList
                        val matchIndex = statsList.indexOfFirst { it.year == leadYear && it.monthIndex == leadMonth }
                        if (matchIndex != -1) {
                            statsList[matchIndex] = statsList[matchIndex].copy(count = statsList[matchIndex].count + 1)
                        }
                    }
                } catch (e: Exception) {
                    // Ignore malformed dates
                }
            }
        }
        statsList
    }

    val maxCount = remember(monthStats) {
        monthStats.maxOfOrNull { it.count } ?: 0
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Renewal Forecast",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Distribution of upcoming renewals by month",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                
                // Color indicator badge / Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFC2185B))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Renewals",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Chart display
            if (maxCount == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "No renewals",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No upcoming renewals in the next 6 months",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                // Highly custom, pixel-perfect, modern native Compose bar chart with horizontal grid lines
                val gridLines = 4
                val yStepValue = maxOf(1, (maxCount + gridLines - 1) / gridLines)
                val gridMax = yStepValue * gridLines

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    // 1. Draw horizontal gridlines and Y-axis labels
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (i in gridLines downTo 0) {
                            val gridVal = i * yStepValue
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = gridVal.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.width(20.dp),
                                    textAlign = TextAlign.End
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // Horizontal dotted-like divider
                                Divider(
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (i == 0) 0.6f else 0.25f),
                                    thickness = 1.dp
                                )
                            }
                        }
                    }

                    // 2. Draw actual bars overlaying the grid
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 28.dp, end = 8.dp, bottom = 6.dp, top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        monthStats.forEach { stat ->
                            val barHeightFraction = if (gridMax > 0) stat.count.toFloat() / gridMax else 0f
                            
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                // Value label above bar
                                if (stat.count > 0) {
                                    Text(
                                        text = stat.count.toString(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(14.dp))
                                }

                                // Interactive / beautiful rounded top bar with dual-color visual gradient
                                val barColor = if (stat.count > 0) {
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFC2185B),
                                            Color(0xFFE91E63).copy(alpha = 0.7f)
                                        )
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                        )
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .width(28.dp)
                                        .fillMaxHeight(barHeightFraction * 0.75f) // scale down slightly to avoid cutting off label
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(barColor)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Month Label Below
                                Text(
                                    text = stat.monthName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Responsive Stats Summary Dashboard ---
@Composable
fun ResponsiveStatsDashboard(
    totalLeads: Int,
    activeRenewals: Int,
    pendingCalls: Int,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val width = maxWidth
        // If width >= 600dp (tablet or landscape), show them side-by-side in a responsive 3-column row.
        if (width >= 600.dp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = "Total Leads",
                    value = totalLeads.toString(),
                    subtext = "All registered contacts",
                    icon = Icons.Default.List,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Active Renewals",
                    value = activeRenewals.toString(),
                    subtext = "Upcoming policy renewals",
                    icon = Icons.Default.Notifications,
                    accentColor = Color(0xFFC2185B),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Pending Telecalls",
                    value = pendingCalls.toString(),
                    subtext = "Awaiting call follow-ups",
                    icon = Icons.Default.Phone,
                    accentColor = Color(0xFFF57C00),
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            // For compact mobile screens, display a hybrid responsive layout:
            // 1 full-width main stat card + 2 side-by-side secondary stat cards.
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Leads",
                    value = totalLeads.toString(),
                    subtext = "All registered customer prospects",
                    icon = Icons.Default.List,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Active Renewals",
                        value = activeRenewals.toString(),
                        subtext = "Policy renewals",
                        icon = Icons.Default.Notifications,
                        accentColor = Color(0xFFC2185B),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Pending Telecalls",
                        value = pendingCalls.toString(),
                        subtext = "Pending calls",
                        icon = Icons.Default.Phone,
                        accentColor = Color(0xFFF57C00),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtext,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

// --- Admin Dashboard Screen ---
@Composable
fun AdminDashboardScreen(viewModel: CRMViewModel) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()
    val leads by viewModel.allLeads.collectAsState()
    val employeesList by viewModel.employees.collectAsState()
    val attendanceList by viewModel.allAttendance.collectAsState()

    var showCsvImport by remember { mutableStateOf(false) }
    var showBackupRestore by remember { mutableStateOf(false) }

    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // Calculations for Dashboard
    val totalLeadsCount = leads.size
    val todayCallsCount = leads.filter { lead ->
        lead.lastCallTime?.let {
            val d1 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it))
            d1 == todayDateStr
        } ?: false
    }.size

    val followUpsDueCount = leads.filter { it.status != "Converted" && it.status != "Rejected" && it.status != "DND" }.size
    
    // Policies expiring soon: inside 90 days
    val expiringLeads = leads.filter { lead ->
        lead.renewalDate?.let { dateStr ->
            try {
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val parsed = format.parse(dateStr)
                if (parsed != null) {
                    val diff = parsed.time - System.currentTimeMillis()
                    val days = diff / (1000 * 60 * 60 * 24)
                    days in 0..90
                } else false
            } catch (e: Exception) { false }
        } ?: false
    }

    val salesTodaySum = leads.filter { lead ->
        lead.status == "Converted" && lead.lastCallTime?.let {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it)) == todayDateStr
        } ?: false
    }.sumOf { it.premiumAmount }

    val activeStaffPresent = attendanceList.filter { it.date == todayDateStr && it.status != "Absent" }.size
    val totalRevenue = leads.filter { it.status == "Converted" }.sumOf { it.premiumAmount }

    Scaffold(
        topBar = {
            CrmTopBar(
                title = "Admin CRM Dashboard",
                onLogout = { viewModel.logout() },
                onProfile = { viewModel.navigateTo(CrmScreen.Profile) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Profile Welcome Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, "user", tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Welcome Back,", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        Text(user?.name ?: "Administrator", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(user?.designation ?: "System Admin", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Responsive Quick Stats Summary
            Text("Real-Time Insights", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(10.dp))

            val activeRenewalsCount = leads.filter { !it.renewalDate.isNullOrEmpty() && it.status != "Rejected" }.size
            val pendingCallsCount = leads.filter { it.status in listOf("New", "Fresh Data", "Hot Leads", "Warm Leads", "Cold Leads") }.size

            ResponsiveStatsDashboard(
                totalLeads = totalLeadsCount,
                activeRenewals = activeRenewalsCount,
                pendingCalls = pendingCallsCount
            )

            Spacer(modifier = Modifier.height(16.dp))

            SchedulerStatusCard(
                leads = leads,
                onTriggerCheck = {
                    com.example.scheduler.FollowUpScheduler.triggerImmediateCheck(context)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Highlight upcoming policy renewals within next 30 days
            UpcomingPolicyRenewalsBanner(
                leads = leads,
                viewModel = viewModel
            )

            Spacer(modifier = Modifier.height(16.dp))

            RenewalsByMonthDistributionChart(
                leads = leads
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Secondary Operational Metrics
            Text("Daily Operational Progress", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardMetricCard(
                    title = "Calls Today",
                    value = todayCallsCount.toString(),
                    icon = Icons.Default.Phone,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                DashboardMetricCard(
                    title = "Sales Today",
                    value = "₹" + salesTodaySum.toInt().toString(),
                    icon = Icons.Default.Check,
                    color = Color(0xFF388E3C),
                    modifier = Modifier.weight(1f)
                )
                DashboardMetricCard(
                    title = "Staff Present",
                    value = "$activeStaffPresent/${employeesList.filter { it.role == "Staff" }.size}",
                    icon = Icons.Default.Face,
                    color = Color(0xFF0097A7),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Actions Section
            Text("Data & Assignment Operations", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { showCsvImport = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Upload, "csv")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CSV Upload", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.autoAssignUnassignedLeads(); Toast.makeText(context, "Leads automatically round-robin assigned!", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Assignment, "assign")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Auto-Assign", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { showBackupRestore = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Share, "backup")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Backup/Restore", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Navigation Hub
            Text("Management Hub", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HubButton("Leads", Icons.Default.List, Modifier.weight(1f)) { viewModel.navigateTo(CrmScreen.LeadsList) }
                HubButton("Staff", Icons.Default.Person, Modifier.weight(1f)) { viewModel.navigateTo(CrmScreen.EmployeeManagement) }
                HubButton("Attendance", Icons.Default.Face, Modifier.weight(1f)) { viewModel.navigateTo(CrmScreen.Attendance) }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HubButton("Leaves", Icons.Default.DateRange, Modifier.weight(1f)) { viewModel.navigateTo(CrmScreen.LeaveRequests) }
                HubButton("Salaries", Icons.Default.Edit, Modifier.weight(1f)) { viewModel.navigateTo(CrmScreen.Salaries) }
                HubButton("Targets", Icons.Default.Star, Modifier.weight(1f)) { viewModel.navigateTo(CrmScreen.Targets) }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                HubButton("Advanced Analytics & Charts", Icons.Default.Assessment, Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.secondary) {
                    viewModel.navigateTo(CrmScreen.Reports)
                }
            }
        }
    }

    if (showCsvImport) {
        CsvImportDialog(
            viewModel = viewModel,
            onDismiss = { showCsvImport = false }
        )
    }

    if (showBackupRestore) {
        BackupRestoreDialog(
            backupText = viewModel.getBackupString(),
            onDismiss = { showBackupRestore = false },
            onRestore = { text ->
                val success = viewModel.importLeadsFromCsv(text)
                if (success) {
                    Toast.makeText(context, "CRM data restored successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Restore failed. Invalid format.", Toast.LENGTH_SHORT).show()
                }
                showBackupRestore = false
            }
        )
    }
}

// --- Dashboard Metric Card ---
@Composable
fun DashboardMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun HubButton(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(icon, contentDescription = label)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

// --- Staff Dashboard Screen ---
@Composable
fun StaffDashboardScreen(viewModel: CRMViewModel) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()
    val leads by viewModel.myLeads.collectAsState()
    val attendance by viewModel.myAttendance.collectAsState()
    val todayAttendanceState by viewModel.todayAttendance.collectAsState()
    val targets by viewModel.myTargets.collectAsState()

    var showLeaveDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CrmTopBar(
                title = "SV Telecalling CRM",
                onLogout = { viewModel.logout() },
                onProfile = { viewModel.navigateTo(CrmScreen.Profile) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Staff Hero Banner Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("TELECALLER PORTAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(user?.name ?: "Telecaller", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(user?.designation ?: "Telecaller Associate", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Assigned Leads", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text("${leads.size} Active", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Pending Follow-ups", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text("${leads.filter { it.status == "Hot Leads" || it.status == "Warm Leads" }.size} High Priority", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Staff Portfolio Summary
            Text("My Portfolio Insights", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(10.dp))

            val myActiveRenewals = leads.filter { !it.renewalDate.isNullOrEmpty() && it.status != "Rejected" }.size
            val myPendingCalls = leads.filter { it.status in listOf("New", "Fresh Data", "Hot Leads", "Warm Leads", "Cold Leads") }.size

            ResponsiveStatsDashboard(
                totalLeads = leads.size,
                activeRenewals = myActiveRenewals,
                pendingCalls = myPendingCalls
            )

            Spacer(modifier = Modifier.height(16.dp))

            SchedulerStatusCard(
                leads = leads,
                onTriggerCheck = {
                    com.example.scheduler.FollowUpScheduler.triggerImmediateCheck(context)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Highlight upcoming policy renewals within next 30 days
            UpcomingPolicyRenewalsBanner(
                leads = leads,
                viewModel = viewModel
            )

            Spacer(modifier = Modifier.height(16.dp))

            RenewalsByMonthDistributionChart(
                leads = leads
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Attendance Section
            Text("Attendance & GPS Check-In", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Status Today: ", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = todayAttendanceState?.status ?: "Not Checked In",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (todayAttendanceState != null) Color(0xFF388E3C) else MaterialTheme.colorScheme.error
                            )
                            if (todayAttendanceState != null) {
                                Text(
                                    text = "In: ${todayAttendanceState?.checkInTime ?: "--"} | Out: ${todayAttendanceState?.checkOutTime ?: "--"}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        Row {
                            if (todayAttendanceState == null) {
                                Button(
                                    onClick = {
                                        // Simulate GPS & check in
                                        viewModel.checkIn(17.4483, 78.3741, "selfie_mock.jpg")
                                        Toast.makeText(context, "Checked In successfully! GPS tracked.", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Check, "checkin")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Check In")
                                }
                            } else if (todayAttendanceState?.checkOutTime == null) {
                                Button(
                                    onClick = {
                                        viewModel.checkOut()
                                        Toast.makeText(context, "Checked Out successfully!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.ExitToApp, "checkout")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Check Out")
                                }
                            } else {
                                Text("Completed", fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(CrmScreen.Attendance) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = "Calendar")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("View My Monthly Attendance Calendar", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Goals and target Progress
            Text("My Targets Progress", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val monthlyTarget = targets.find { it.period == "Monthly" }
                    val targetAmt = monthlyTarget?.targetAmount ?: 100000.0
                    val achievedAmt = monthlyTarget?.achievedAmount ?: leads.filter { it.status == "Converted" }.sumOf { it.premiumAmount }
                    val progress = (achievedAmt / targetAmt).coerceIn(0.0, 1.0)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Monthly Premium Target", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${(progress * 100).toInt()}% Achieved", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = progress.toFloat(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Achieved: ₹${achievedAmt.toInt()}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        Text("Target: ₹${targetAmt.toInt()}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Center Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.navigateTo(CrmScreen.LeadsList) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Phone, "leads")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call Leads Portal", fontSize = 12.sp)
                }

                Button(
                    onClick = { showLeaveDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.DateRange, "leave")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Apply Leave", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Incentive and salary brief info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Base Salary", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                        Text("₹${(user?.salary ?: 20000.0).toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Est. Incentives (5%)", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                        val estIncentive = leads.filter { it.status == "Converted" }.sumOf { it.premiumAmount } * 0.05
                        Text("₹${estIncentive.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
                    }
                }
            }
        }
    }

    if (showLeaveDialog) {
        LeaveRequestDialog(
            onDismiss = { showLeaveDialog = false },
            onSubmit = { start, end, reason ->
                viewModel.submitLeaveRequest(start, end, reason)
                Toast.makeText(context, "Leave Request Submitted!", Toast.LENGTH_SHORT).show()
                showLeaveDialog = false
            }
        )
    }
}

// --- Custom CRM Top Bar ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmTopBar(
    title: String,
    onLogout: () -> Unit,
    onProfile: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val drawerController = LocalCrmDrawerController.current

    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "back")
                }
            } else {
                IconButton(onClick = drawerController) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "menu",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp).size(24.dp)
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onProfile) {
                Icon(Icons.Default.Person, contentDescription = "profile")
            }
            IconButton(onClick = onLogout) {
                Icon(Icons.Default.ExitToApp, contentDescription = "logout")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
    )
}

// --- Leads List Screen ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LeadsListScreen(viewModel: CRMViewModel) {
    val context = LocalContext.current
    val currentUserState by viewModel.currentUser.collectAsState()
    val allLeadsList by viewModel.allLeads.collectAsState()
    val staffLeadsList by viewModel.myLeads.collectAsState()
    val staffList by viewModel.employees.collectAsState()

    val isAdmin = currentUserState?.role == "Admin" || currentUserState?.role == "Manager"
    val leadsToDisplay = if (isAdmin) allLeadsList else staffLeadsList

    var selectedStatusFilter by remember { mutableStateOf("All") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    var showAddLeadDialog by remember { mutableStateOf(false) }
    val selectedLeadForDetail by viewModel.selectedLead.collectAsState()

    // Bulk assign state for Admin
    val selectedLeadIdsForBulk = remember { mutableStateListOf<Int>() }
    var showBulkAssignDialog by remember { mutableStateOf(false) }

    // Filtering logic
    val filteredLeads = leadsToDisplay.filter { lead ->
        val matchesSearch = lead.name.contains(searchQuery, ignoreCase = true) || lead.phone.contains(searchQuery)
        val matchesStatus = selectedStatusFilter == "All" || lead.status == selectedStatusFilter
        val matchesCategory = selectedCategoryFilter == "All" || lead.insuranceCategory == selectedCategoryFilter
        matchesSearch && matchesStatus && matchesCategory
    }

    if (selectedLeadForDetail != null) {
        LeadDetailScreen(
            lead = selectedLeadForDetail!!,
            viewModel = viewModel,
            onBack = { viewModel.selectLead(null) }
        )
    } else {
        Scaffold(
            topBar = {
                CrmTopBar(
                    title = "Customer Leads Database",
                    onLogout = { viewModel.logout() },
                    onProfile = { viewModel.navigateTo(CrmScreen.Profile) },
                    onBack = { viewModel.navigateBack() }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddLeadDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, "add_lead")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search client name or phone...") },
                    leadingIcon = { Icon(Icons.Default.Search, "search") },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, "clear_search")
                            }
                        }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                var statusMenuExpanded by remember { mutableStateOf(false) }
                var categoryMenuExpanded by remember { mutableStateOf(false) }

                // Interactive Filters Row with Custom Dropdowns
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Dropdown Filter
                    Box {
                        Surface(
                            onClick = { statusMenuExpanded = true },
                            color = if (selectedStatusFilter == "All") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Status: $selectedStatusFilter",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedStatusFilter == "All") MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "status_dropdown",
                                    tint = if (selectedStatusFilter == "All") MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = statusMenuExpanded,
                            onDismissRequest = { statusMenuExpanded = false }
                        ) {
                            val statuses = listOf("All", "New", "Fresh Data", "Hot Leads", "Warm Leads", "Cold Leads", "Converted", "Rejected")
                            statuses.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status, fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        selectedStatusFilter = status
                                        statusMenuExpanded = false
                                    },
                                    leadingIcon = if (selectedStatusFilter == status) {
                                        { Icon(Icons.Default.Check, "Selected", modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }
                    }

                    // Insurance Type / Category Dropdown Filter
                    Box {
                        Surface(
                            onClick = { categoryMenuExpanded = true },
                            color = if (selectedCategoryFilter == "All") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Category: $selectedCategoryFilter",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedCategoryFilter == "All") MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "category_dropdown",
                                    tint = if (selectedCategoryFilter == "All") MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = categoryMenuExpanded,
                            onDismissRequest = { categoryMenuExpanded = false }
                        ) {
                            val categories = listOf("All", "Motor Insurance", "Bike Insurance", "Car Insurance", "Health Insurance", "Life Insurance", "Commercial Vehicle")
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category, fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        selectedCategoryFilter = category
                                        categoryMenuExpanded = false
                                    },
                                    leadingIcon = if (selectedCategoryFilter == category) {
                                        { Icon(Icons.Default.Check, "Selected", modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }
                    }

                    // Reset Button
                    if (selectedStatusFilter != "All" || selectedCategoryFilter != "All" || searchQuery.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                selectedStatusFilter = "All"
                                selectedCategoryFilter = "All"
                                searchQuery = ""
                            },
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Reset", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bulk operations for Admin
                if (isAdmin && selectedLeadIdsForBulk.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${selectedLeadIdsForBulk.size} leads selected", fontWeight = FontWeight.Bold)
                            Button(
                                onClick = { showBulkAssignDialog = true },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Assign Selected")
                            }
                        }
                    }
                }

                // Leads list
                if (filteredLeads.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Info,
                                "empty",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No Leads Found matching filters", color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredLeads, key = { it.id }) { lead ->
                            val isSelected = selectedLeadIdsForBulk.contains(lead.id)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { viewModel.selectLead(lead) },
                                        onLongClick = {
                                            if (isAdmin) {
                                                if (isSelected) {
                                                    selectedLeadIdsForBulk.remove(lead.id)
                                                } else {
                                                    selectedLeadIdsForBulk.add(lead.id)
                                                }
                                            }
                                        }
                                    ),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            lead.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(lead.phone, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                                Text(lead.insuranceCategory, fontSize = 10.sp, modifier = Modifier.padding(4.dp))
                                            }
                                            Badge(containerColor = getStatusColor(lead.status)) {
                                                Text(lead.status, fontSize = 10.sp, color = Color.White, modifier = Modifier.padding(4.dp))
                                            }
                                        }
                                        if (isAdmin && lead.assignedStaffName != null) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Assigned to: ${lead.assignedStaffName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
                                        }
                                    }

                                    Icon(
                                        Icons.Default.Phone,
                                        contentDescription = "call_lead",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddLeadDialog) {
        AddLeadDialog(
            staffList = staffList,
            onDismiss = { showAddLeadDialog = false },
            onSave = { name, phone, cat, premium, renewal, staffId, staffName ->
                viewModel.addOrUpdateLead(
                    name = name,
                    phone = phone,
                    status = "New",
                    followUpCategory = null,
                    insuranceCategory = cat,
                    premiumAmount = premium,
                    renewalDate = renewal,
                    assignedStaffId = staffId,
                    assignedStaffName = staffName,
                    callNotes = "Lead created manually."
                )
                showAddLeadDialog = false
            }
        )
    }

    if (showBulkAssignDialog) {
        BulkAssignDialog(
            staffList = staffList.filter { it.role == "Staff" },
            onDismiss = { showBulkAssignDialog = false },
            onAssign = { staff ->
                viewModel.bulkAssignLeads(selectedLeadIdsForBulk.toList(), staff.id, staff.name)
                selectedLeadIdsForBulk.clear()
                showBulkAssignDialog = false
                Toast.makeText(context, "Successfully assigned selected leads!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun FilterChipContainer(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.height(34.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

fun getStatusColor(status: String): Color {
    return when (status) {
        "Hot Leads" -> Color(0xFFD32F2F)
        "Warm Leads" -> Color(0xFFF57C00)
        "Cold Leads" -> Color(0xFF1976D2)
        "Converted" -> Color(0xFF388E3C)
        "Rejected" -> Color(0xFF757575)
        else -> Color(0xFF0D5E6B)
    }
}

fun parseWhatsAppMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var index = 0
        while (index < text.length) {
            val start = text.indexOf('*', index)
            if (start == -1) {
                append(text.substring(index))
                break
            }
            append(text.substring(index, start))
            val end = text.indexOf('*', start + 1)
            if (end == -1) {
                append(text.substring(start))
                break
            }
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(text.substring(start + 1, end))
            }
            index = end + 1
        }
    }
}

@Composable
fun WhatsAppMessagingModule(
    lead: Lead,
    viewModel: CRMViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUserState by viewModel.currentUser.collectAsState()
    val staffName = currentUserState?.name ?: "Sree Venkateswara Insurances Representative"

    var selectedTemplateIndex by remember { mutableStateOf(0) }
    
    var customPremium by remember { mutableStateOf(lead.premiumAmount.toString()) }
    var customRenewalDate by remember { mutableStateOf(lead.renewalDate ?: "") }
    var customFollowUpDate by remember { mutableStateOf("") }
    var customPolicyNo by remember { mutableStateOf(lead.policyNumber ?: "") }
    var autoLogAttempt by remember { mutableStateOf(true) }

    val templates = listOf(
        "Renewal Reminder" to { name: String, category: String, premium: String, renewalDate: String, policyNo: String, staff: String ->
            "Hello *$name*, this is *$staff* from *Sree Venkateswara Insurances*. 🚗 Friendly reminder that your *$category* insurance policy (No. *${if(policyNo.isNotBlank()) policyNo else "N/A"}*) is due for renewal on *$renewalDate*. Ensure continuous safety by renewing today. Pre-calculated premium: *₹$premium*. Please reply to renew instantly! Thank you."
        },
        "Welcome & Quote" to { name: String, category: String, premium: String, renewalDate: String, policyNo: String, staff: String ->
            "Hello *$name*, thank you for choosing *Sree Venkateswara Insurances*. 🌟 We have prepared a customized premium quote for your *$category* starting at only *₹$premium*. We provide instant, paperless processing. Let us know when is a good time to connect. Regards, *$staff*."
        },
        "Missed Call Follow-up" to { name: String, category: String, premium: String, renewalDate: String, policyNo: String, staff: String ->
            "Hi *$name*, we tried calling you regarding your inquiry for *$category* but couldn't reach you. 📞 No worries! Please let us know when is a convenient time to call you back, or contact us directly on this number. We're here to help you get the best protection. Regards, *$staff*."
        },
        "Premium Due Alert" to { name: String, category: String, premium: String, renewalDate: String, policyNo: String, staff: String ->
            "Dear *$name*, a gentle reminder that your *$category* insurance premium of *₹$premium* is pending. Please complete the process to avoid policy lapse or late fees. Contact *$staff* at Sree Venkateswara Insurances if you need any assistance or direct payment link. Thank you!"
        },
        "Meeting Confirmation" to { name: String, category: String, premium: String, renewalDate: String, policyNo: String, staff: String ->
            "Hello *$name*, this is *$staff* from *Sree Venkateswara Insurances*. Confirming our discussion regarding your *$category* insurance. 📅 We have scheduled a follow-up discussion/meeting on *${if(customFollowUpDate.isNotBlank()) customFollowUpDate else "your selected date"}*. We look forward to talking then. Have a great day!"
        }
    )

    val templateMessage = remember(selectedTemplateIndex, customPremium, customRenewalDate, customFollowUpDate, customPolicyNo, staffName) {
        templates[selectedTemplateIndex].second(
            lead.name,
            lead.insuranceCategory ?: "General Insurance",
            customPremium,
            customRenewalDate,
            customPolicyNo,
            staffName
        )
    }

    var editedMessage by remember { mutableStateOf(templateMessage) }

    LaunchedEffect(templateMessage) {
        editedMessage = templateMessage
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFF388E3C).copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "WhatsApp Icon",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Manual WhatsApp Messenger",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Compose a custom message or edit template before sending",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Select Message Template:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                templates.forEachIndexed { index, (title, _) ->
                    FilterChip(
                        selected = selectedTemplateIndex == index,
                        onClick = { selectedTemplateIndex = index },
                        label = { Text(title, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFE8F5E9),
                            selectedLabelColor = Color(0xFF2E7D32)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Customize Message Fields:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedTemplateIndex in listOf(0, 1, 3)) {
                    OutlinedTextField(
                        value = customPremium,
                        onValueChange = { customPremium = it },
                        label = { Text("Premium (₹)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                if (selectedTemplateIndex == 0) {
                    OutlinedTextField(
                        value = customRenewalDate,
                        onValueChange = { customRenewalDate = it },
                        label = { Text("Renewal Date") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                if (selectedTemplateIndex == 4) {
                    OutlinedTextField(
                        value = customFollowUpDate,
                        onValueChange = { customFollowUpDate = it },
                        label = { Text("Meeting Date") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("yyyy-MM-dd") }
                    )
                }

                if (selectedTemplateIndex == 0) {
                    OutlinedTextField(
                        value = customPolicyNo,
                        onValueChange = { customPolicyNo = it },
                        label = { Text("Policy No") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Compose or Edit Message:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = editedMessage,
                onValueChange = { editedMessage = it },
                label = { Text("WhatsApp Message Text") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Live WhatsApp Message Preview:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEFE7DD))
                    .border(1.dp, Color(0xFFE0D8D0), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF388E3C))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "To: ${lead.name} (${lead.phone})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF455A64)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .widthIn(max = 280.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 0.dp,
                                    bottomStart = 12.dp,
                                    bottomEnd = 12.dp
                                )
                            )
                            .background(Color(0xFFDCF8C6))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Text(
                                text = parseWhatsAppMarkdown(editedMessage),
                                fontSize = 12.sp,
                                color = Color(0xFF263238),
                                lineHeight = 17.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(
                                modifier = Modifier.align(Alignment.End),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val timeStr = remember {
                                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                                }
                                Text(
                                    text = timeStr,
                                    fontSize = 9.sp,
                                    color = Color(0xFF757575)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Read Ticks",
                                    tint = Color(0xFF1E88E5),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = autoLogAttempt,
                    onCheckedChange = { autoLogAttempt = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF388E3C))
                )
                Column {
                    Text(
                        text = "Auto-log this message in contact history",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Adds an official contact history log of outcome \"WhatsApp Sent\"",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    try {
                        val url = "https://api.whatsapp.com/send?phone=${lead.phone}&text=${URLEncoder.encode(editedMessage, "UTF-8")}"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)

                        if (autoLogAttempt) {
                            viewModel.logCall(
                                leadId = lead.id,
                                outcome = "WhatsApp Sent",
                                durationSeconds = 15,
                                notes = "WhatsApp message sent to customer. Message Content: ${editedMessage}",
                                nextFollowUpDate = if (selectedTemplateIndex == 4 && customFollowUpDate.isNotBlank()) customFollowUpDate else null
                            )
                            Toast.makeText(context, "WhatsApp Sent & History Call Log Created!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Opening WhatsApp...", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error launching WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Send Icon",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Launch & Send Message via WhatsApp",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// --- Lead Detail Screen with WhatsApp/Calling integration ---
@Composable
fun CallLogHistorySection(
    leadId: Int,
    viewModel: CRMViewModel
) {
    val allCallLogs by viewModel.allCallLogs.collectAsState()
    val leadLogs = remember(allCallLogs, leadId) {
        allCallLogs.filter { it.leadId == leadId }
    }

    Text(
        text = "Telecalling History Logs (${leadLogs.size})",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    if (leadLogs.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No call logs recorded yet. Use the 'Record Telecalling Attempt' panel below to record the first contact effort.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            leadLogs.forEach { log ->
                val dateStr = remember(log.timestamp) {
                    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(log.timestamp))
                }
                
                val outcomeColor = when (log.outcome) {
                    "Converted" -> Color(0xFF388E3C)
                    "Interested" -> Color(0xFF1976D2)
                    "Follow-up Scheduled" -> Color(0xFFF57C00)
                    "Busy", "No Answer" -> Color(0xFF757575)
                    "Rejected", "Not Interested", "DND" -> Color(0xFFD32F2F)
                    else -> MaterialTheme.colorScheme.primary
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Call Outcome",
                                    tint = outcomeColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = outcomeColor.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = log.outcome,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = outcomeColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = dateStr,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }

                        if (log.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = log.notes,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Duration: ${log.durationSeconds / 60}m ${log.durationSeconds % 60}s",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "By: ${log.callerName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (!log.nextFollowUpDate.isNullOrBlank()) {
                                Text(
                                    text = "Next: ${log.nextFollowUpDate}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC2185B)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeadDetailScreen(
    lead: Lead,
    viewModel: CRMViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val staffList by viewModel.employees.collectAsState()

    // Proposed Policy Specifications state variables
    var premiumAmount by remember { mutableStateOf(lead.premiumAmount.toString()) }
    var policyNumber by remember { mutableStateOf(lead.policyNumber ?: "") }

    // New Call Attempt logging state variables
    var callOutcome by remember { mutableStateOf("Interested") }
    var durationMinutes by remember { mutableStateOf("") }
    var durationSeconds by remember { mutableStateOf("") }
    var callNotesInput by remember { mutableStateOf("") }
    var nextFollowUpDate by remember { mutableStateOf("") }

    val outcomeOptions = listOf(
        "Interested", 
        "Follow-up Scheduled", 
        "No Answer", 
        "Busy", 
        "Not Interested", 
        "Converted", 
        "Rejected", 
        "DND"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lead Details: ${lead.name}", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Customer Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(lead.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        
                        // Status badge
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = lead.status,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Phone: ${lead.phone}", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                    Text("Category: ${lead.insuranceCategory}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    if (lead.renewalDate != null) {
                        Text("Policy Expiry Date: ${lead.renewalDate}", fontSize = 12.sp, color = Color(0xFFC2185B), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Communication Quick Actions
            Text("Communication Portals", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${lead.phone}"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Phone, "dial")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("One-Tap Call")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // State-of-the-art WhatsApp Messaging Assistant Module
            WhatsAppMessagingModule(
                lead = lead,
                viewModel = viewModel
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Policy Specs Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Proposed Policy Specifications", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = premiumAmount,
                        onValueChange = { premiumAmount = it },
                        label = { Text("Proposed Premium Amount (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = policyNumber,
                        onValueChange = { policyNumber = it },
                        label = { Text("Insurance Policy Number (If Converted)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.addOrUpdateLead(
                                id = lead.id,
                                name = lead.name,
                                phone = lead.phone,
                                status = lead.status,
                                followUpCategory = lead.followUpCategory,
                                insuranceCategory = lead.insuranceCategory,
                                premiumAmount = premiumAmount.toDoubleOrNull() ?: lead.premiumAmount,
                                renewalDate = lead.renewalDate,
                                assignedStaffId = lead.assignedStaffId,
                                assignedStaffName = lead.assignedStaffName,
                                callNotes = lead.callNotes,
                                policyNumber = policyNumber
                            )
                            Toast.makeText(context, "Policy Specifications Updated", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Update Policy Settings", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Record Telecalling Attempt Card
            Text("Record Telecalling Attempt", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Call Outcome:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        outcomeOptions.forEach { option ->
                            FilterChip(
                                selected = callOutcome == option,
                                onClick = { callOutcome = option },
                                label = { Text(option) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Call Duration:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = durationMinutes,
                            onValueChange = { durationMinutes = it },
                            label = { Text("Min") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = durationSeconds,
                            onValueChange = { durationSeconds = it },
                            label = { Text("Sec") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                    
                    // Quick Preset Buttons for duration
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val durationPresets = listOf("30s", "1m", "2m", "5m")
                        durationPresets.forEach { preset ->
                            OutlinedButton(
                                onClick = {
                                    when (preset) {
                                        "30s" -> { durationMinutes = "0"; durationSeconds = "30" }
                                        "1m" -> { durationMinutes = "1"; durationSeconds = "0" }
                                        "2m" -> { durationMinutes = "2"; durationSeconds = "0" }
                                        "5m" -> { durationMinutes = "5"; durationSeconds = "0" }
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text(preset, fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Next Follow-up Date (Optional):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = nextFollowUpDate,
                            onValueChange = { nextFollowUpDate = it },
                            label = { Text("yyyy-MM-dd") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text("e.g. 2026-07-15") }
                        )

                        // Native Date Picker Dialog launcher
                        IconButton(
                            onClick = {
                                val calendar = Calendar.getInstance()
                                val datePickerDialog = android.app.DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val formattedMonth = String.format("%02d", month + 1)
                                        val formattedDay = String.format("%02d", dayOfMonth)
                                        nextFollowUpDate = "$year-$formattedMonth-$formattedDay"
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                )
                                datePickerDialog.show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select Date from Calendar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Quick Presets for scheduling follow-ups
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val tomorrowStr = sdf.format(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.time)
                        val in3DaysStr = sdf.format(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 3) }.time)
                        val nextWeekStr = sdf.format(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }.time)

                        val presets = listOf("Tomorrow" to tomorrowStr, "In 3 Days" to in3DaysStr, "Next Week" to nextWeekStr)
                        presets.forEach { (label, dateVal) ->
                            OutlinedButton(
                                onClick = { nextFollowUpDate = dateVal },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text(label, fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = callNotesInput,
                        onValueChange = { callNotesInput = it },
                        label = { Text("Call Discussion Notes") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val min = durationMinutes.toIntOrNull() ?: 0
                            val sec = durationSeconds.toIntOrNull() ?: 0
                            val totalDurationSec = (min * 60) + sec

                            if (totalDurationSec <= 0) {
                                Toast.makeText(context, "Please enter a valid call duration.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (callNotesInput.isBlank()) {
                                Toast.makeText(context, "Please enter some call discussion notes.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            viewModel.logCall(
                                leadId = lead.id,
                                outcome = callOutcome,
                                durationSeconds = totalDurationSec,
                                notes = callNotesInput,
                                nextFollowUpDate = if (nextFollowUpDate.isNotBlank()) nextFollowUpDate else null
                            )

                            Toast.makeText(context, "Call Log Saved Successfully!", Toast.LENGTH_SHORT).show()
                            
                            // Reset local log fields for next entry
                            durationMinutes = ""
                            durationSeconds = ""
                            callNotesInput = ""
                            nextFollowUpDate = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Call Log Attempt", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Call Log History list
            CallLogHistorySection(
                leadId = lead.id,
                viewModel = viewModel
            )
        }
    }
}

// --- Employee Management Screen ---
@Composable
fun EmployeeManagementScreen(viewModel: CRMViewModel) {
    val context = LocalContext.current
    val staffList by viewModel.employees.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingEmployee by remember { mutableStateOf<Employee?>(null) }

    Scaffold(
        topBar = {
            CrmTopBar(
                title = "Employee Profiles",
                onLogout = { viewModel.logout() },
                onProfile = { viewModel.navigateTo(CrmScreen.Profile) },
                onBack = { viewModel.navigateBack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, "add")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Register New Employee Profile", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (staffList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No Employees Registered Yet.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(staffList) { staff ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(staff.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Role: ${staff.role} | Designation: ${staff.designation}", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                    Text("Phone: ${staff.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Aadhaar: ${staff.aadhaar} | PAN: ${staff.pan}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    Text("Salary Structure: ₹${staff.salary.toInt()}/month", fontSize = 11.sp, color = Color(0xFF388E3C), fontWeight = FontWeight.Bold)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { editingEmployee = staff }) {
                                        Icon(Icons.Default.Edit, "edit", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { viewModel.deleteEmployee(staff); Toast.makeText(context, "Employee deleted", Toast.LENGTH_SHORT).show() }) {
                                        Icon(Icons.Default.Delete, "delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEmployeeDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, phone, role, des, sal, aadhaar, pan, bank, join, password ->
                viewModel.addOrUpdateEmployee(
                    name = name,
                    phone = phone,
                    password = if (password.isNotEmpty()) password else "staff",
                    role = role,
                    designation = des,
                    salary = sal,
                    aadhaar = aadhaar,
                    pan = pan,
                    bankDetails = bank,
                    joiningDate = join
                )
                showAddDialog = false
            }
        )
    }

    if (editingEmployee != null) {
        AddEmployeeDialog(
            employee = editingEmployee,
            onDismiss = { editingEmployee = null },
            onSave = { name, phone, role, des, sal, aadhaar, pan, bank, join, password ->
                viewModel.addOrUpdateEmployee(
                    id = editingEmployee!!.id,
                    name = name,
                    phone = phone,
                    password = if (password.isNotEmpty()) password else editingEmployee!!.password,
                    role = role,
                    designation = des,
                    salary = sal,
                    aadhaar = aadhaar,
                    pan = pan,
                    bankDetails = bank,
                    joiningDate = join
                )
                editingEmployee = null
                Toast.makeText(context, "Employee details updated successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// --- Attendance Screen Logs ---
@Composable
fun AttendanceScreen(viewModel: CRMViewModel) {
    val attendanceLogs by viewModel.allAttendance.collectAsState()
    val leaveRequests by viewModel.allLeaveRequests.collectAsState()
    val employees by viewModel.employees.collectAsState()
    val currentUserState by viewModel.currentUser.collectAsState()
    
    val isAdmin = currentUserState?.role == "Admin" || currentUserState?.role == "Manager"
    val activeStaff = remember(employees) { employees.filter { it.role == "Staff" } }

    // Navigation and Calendar State
    var selectedTab by remember { mutableStateOf(0) } // 0 = Monthly Calendar, 1 = Live GPS Logs
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) } // 0-11
    
    // Selected employee for calendar (Defaults to first active staff if Admin, or current user if Staff)
    var selectedEmployeeId by remember { mutableStateOf<Int?>(null) }
    var selectedEmployeeName by remember { mutableStateOf<String?>(null) }

    // Initialize/sync selectedEmployeeId
    LaunchedEffect(currentUserState, activeStaff) {
        if (selectedEmployeeId == null) {
            if (isAdmin) {
                val firstStaff = activeStaff.firstOrNull() ?: currentUserState
                selectedEmployeeId = firstStaff?.id
                selectedEmployeeName = firstStaff?.name
            } else {
                selectedEmployeeId = currentUserState?.id
                selectedEmployeeName = currentUserState?.name
            }
        }
    }

    // Dropdown state for staff selection
    var showStaffDropdown by remember { mutableStateOf(false) }

    // Calculate Days of the Selected Month
    val daysInMonthList = remember(selectedYear, selectedMonth, selectedEmployeeId, attendanceLogs, leaveRequests) {
        val daysList = mutableListOf<DayInfo>()
        val selectedEmpId = selectedEmployeeId ?: return@remember emptyList<DayInfo>()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1 (Sunday) to 7 (Saturday)
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Prepend offset empty cells (0-value dayNumber)
        val offset = firstDayOfWeek - 1
        for (i in 0 until offset) {
            daysList.add(DayInfo(0, "", false, false, "None"))
        }

        val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        for (dayNum in 1..maxDays) {
            val dateStr = String.format(Locale.US, "%d-%02d-%02d", selectedYear, selectedMonth + 1, dayNum)
            
            val dayCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedYear)
                set(Calendar.MONTH, selectedMonth)
                set(Calendar.DAY_OF_MONTH, dayNum)
            }
            val isSunday = dayCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY

            val attendanceForDay = attendanceLogs.find { it.employeeId == selectedEmpId && it.date == dateStr }
            val approvedLeaves = leaveRequests.filter { it.employeeId == selectedEmpId && it.status == "Approved" }
            val leaveForDay = approvedLeaves.find { dateStr >= it.startDate && dateStr <= it.endDate }

            val status = when {
                attendanceForDay != null -> attendanceForDay.status // "Present", "Late Arrival", "Half Day", etc.
                leaveForDay != null -> "On Leave"
                dateStr > todayDateStr -> "Future"
                isSunday -> "Weekend"
                else -> "Absent"
            }

            daysList.add(
                DayInfo(
                    dayNumber = dayNum,
                    dateString = dateStr,
                    isCurrentMonth = true,
                    isSunday = isSunday,
                    status = status,
                    attendance = attendanceForDay,
                    leaveRequest = leaveForDay
                )
            )
        }
        daysList
    }

    // Detail dialog state
    var selectedDayDetail by remember { mutableStateOf<DayInfo?>(null) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            CrmTopBar(
                title = "Attendance Module",
                onLogout = { viewModel.logout() },
                onProfile = { viewModel.navigateTo(CrmScreen.Profile) },
                onBack = { viewModel.navigateBack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Screen Title & Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.DateRange, "calendar") },
                    text = { Text("Monthly Calendar", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.List, "logs") },
                    text = { Text("Live GPS Logs", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
            }

            if (selectedTab == 0) {
                // --- MONTHLY CALENDAR VIEW ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Employee Selector (Role Dependent)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (isAdmin) {
                                Text(
                                    text = "SELECT TELECALLER FOR REPORT:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showStaffDropdown = true }
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Staff Icon",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = selectedEmployeeName ?: "Select Staff Member",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                DropdownMenu(
                                    expanded = showStaffDropdown,
                                    onDismissRequest = { showStaffDropdown = false },
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    activeStaff.forEach { emp ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(emp.name, fontWeight = FontWeight.Bold)
                                                    Text(emp.designation, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                                }
                                            },
                                            onClick = {
                                                selectedEmployeeId = emp.id
                                                selectedEmployeeName = emp.name
                                                showStaffDropdown = false
                                            }
                                        )
                                    }
                                }
                            } else {
                                // Staff View: Locked display of their own profile calendar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape,
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = (currentUserState?.name ?: "S").take(1).uppercase(),
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "My Monthly Attendance Report",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Employee Code: SV-${currentUserState?.id ?: 0} | ${currentUserState?.designation ?: "Telecaller"}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Month & Year Arrow Navigation Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (selectedMonth == 0) {
                                    selectedMonth = 11
                                    selectedYear -= 1
                                } else {
                                    selectedMonth -= 1
                                }
                            },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Prev Month")
                        }

                        val monthLabel = when (selectedMonth) {
                            0 -> "January"
                            1 -> "February"
                            2 -> "March"
                            3 -> "April"
                            4 -> "May"
                            5 -> "June"
                            6 -> "July"
                            7 -> "August"
                            8 -> "September"
                            9 -> "October"
                            10 -> "November"
                            11 -> "December"
                            else -> "Unknown"
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$monthLabel $selectedYear",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Monthly Attendance Breakdown",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        IconButton(
                            onClick = {
                                if (selectedMonth == 11) {
                                    selectedMonth = 0
                                    selectedYear += 1
                                } else {
                                    selectedMonth += 1
                                }
                            },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        ) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // --- CALENDAR STATS REPORT BAR ---
                    val presentDays = daysInMonthList.count { it.status == "Present" }
                    val lateDays = daysInMonthList.count { it.status == "Late Arrival" }
                    val halfDays = daysInMonthList.count { it.status == "Half Day" }
                    val leaveDays = daysInMonthList.count { it.status == "On Leave" }
                    val absentDays = daysInMonthList.count { it.status == "Absent" }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AttendanceStatCard(
                            label = "Present",
                            count = presentDays + lateDays + halfDays,
                            color = Color(0xFF2E7D32),
                            bgColor = Color(0xFFE8F5E9),
                            modifier = Modifier.weight(1f)
                        )
                        AttendanceStatCard(
                            label = "Late",
                            count = lateDays,
                            color = Color(0xFFF57F17),
                            bgColor = Color(0xFFFFF8E1),
                            modifier = Modifier.weight(1f)
                        )
                        AttendanceStatCard(
                            label = "Leave",
                            count = leaveDays,
                            color = Color(0xFF1565C0),
                            bgColor = Color(0xFFE3F2FD),
                            modifier = Modifier.weight(1f)
                        )
                        AttendanceStatCard(
                            label = "Absent",
                            count = absentDays,
                            color = Color(0xFFC62828),
                            bgColor = Color(0xFFFFEBEE),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- CALENDAR GRID ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Weekdays Title Header (Sun to Sat)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val weekdays = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
                                weekdays.forEach { dayName ->
                                    Text(
                                        text = dayName,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (dayName == "Su") Color(0xFFC62828) else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Grid Rows
                            val chunkedWeeks = daysInMonthList.chunked(7)
                            chunkedWeeks.forEach { weekDays ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    weekDays.forEach { dayInfo ->
                                        if (dayInfo.dayNumber == 0) {
                                            // Empty cell for calendar padding
                                            Spacer(modifier = Modifier.weight(1f))
                                        } else {
                                            // Real Day Cell
                                            val isSelected = selectedDayDetail?.dateString == dayInfo.dateString
                                            val cellBgColor = when (dayInfo.status) {
                                                "Present" -> Color(0xFFE8F5E9)
                                                "Late Arrival" -> Color(0xFFFFF8E1)
                                                "Half Day" -> Color(0xFFFFE0B2)
                                                "On Leave" -> Color(0xFFE3F2FD)
                                                "Absent" -> Color(0xFFFFEBEE)
                                                "Weekend" -> Color(0xFFA1A8B3).copy(alpha = 0.08f)
                                                else -> Color.Transparent
                                            }

                                            val cellTextColor = when (dayInfo.status) {
                                                "Present" -> Color(0xFF2E7D32)
                                                "Late Arrival" -> Color(0xFFF57F17)
                                                "Half Day" -> Color(0xFFE65100)
                                                "On Leave" -> Color(0xFF1565C0)
                                                "Absent" -> Color(0xFFC62828)
                                                "Weekend" -> Color(0xFF78909C)
                                                "Future" -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }

                                            Card(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .padding(2.dp)
                                                    .clickable {
                                                        if (dayInfo.status != "Future") {
                                                            selectedDayDetail = dayInfo
                                                        }
                                                    },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = CardDefaults.cardColors(containerColor = cellBgColor),
                                                border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                                            ) {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.Center
                                                    ) {
                                                        Text(
                                                            text = dayInfo.dayNumber.toString(),
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = cellTextColor
                                                        )
                                                        
                                                        // Sub-dot indicator for visually capturing status quickly
                                                        if (dayInfo.status != "Future" && dayInfo.status != "Weekend" && dayInfo.status != "None") {
                                                            Box(
                                                                modifier = Modifier
                                                                    .padding(top = 2.dp)
                                                                    .size(4.dp)
                                                                    .background(cellTextColor, CircleShape)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    // Fill any trailing missing spaces
                                    if (weekDays.size < 7) {
                                        repeat(7 - weekDays.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tip / Helper Banner
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Tip",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pro-Tip: Tap on any active calendar day cell to reveal detailed GPS coordinates, in/out timestamps, selfie verify logs, or leave reasons.",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                // --- LIVE CHECK-IN GPS LOGS LIST (RAW DATA TAB) ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text("Live Check-In Logs (with GPS)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    val filteredLogs = if (isAdmin) {
                        attendanceLogs
                    } else {
                        attendanceLogs.filter { it.employeeId == currentUserState?.id }
                    }

                    if (filteredLogs.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No attendance logs found for today.")
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(filteredLogs) { log ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(log.employeeName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Text("Date: ${log.date}", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                            Text("Check In: ${log.checkInTime ?: "--"} | Check Out: ${log.checkOutTime ?: "--"}", fontSize = 12.sp)
                                            if (log.latitude != null) {
                                                Text("GPS Tracked: Lat ${log.latitude} / Lng ${log.longitude}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }

                                        Badge(containerColor = if (log.status == "Present") Color(0xFF388E3C) else Color(0xFFD32F2F)) {
                                            Text(log.status, color = Color.White, modifier = Modifier.padding(6.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DAY DETAILS MODAL DIALOG ---
    selectedDayDetail?.let { dayInfo ->
        Dialog(onDismissRequest = { selectedDayDetail = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Header with Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Day Activity Log",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        IconButton(onClick = { selectedDayDetail = null }) {
                            Icon(Icons.Default.Close, "close")
                        }
                    }
                    Text(
                        text = "Date: ${dayInfo.dateString}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Status Highlight Badge
                    val statusColor = when (dayInfo.status) {
                        "Present" -> Color(0xFF2E7D32)
                        "Late Arrival" -> Color(0xFFF57F17)
                        "Half Day" -> Color(0xFFE65100)
                        "On Leave" -> Color(0xFF1565C0)
                        "Absent" -> Color(0xFFC62828)
                        else -> MaterialTheme.colorScheme.secondary
                    }

                    Surface(
                        color = statusColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(statusColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Status: ${dayInfo.status}",
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Detailed Data Block
                    when {
                        dayInfo.attendance != null -> {
                            val att = dayInfo.attendance
                            Text("PUNCH RECORD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Check-In", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                        Text(att.checkInTime ?: "--:--", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Check-Out", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                        Text(att.checkOutTime ?: "Active", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (att.latitude != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("GPS GEOLOCATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(6.dp))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "GPS Lock",
                                            tint = Color(0xFF388E3C),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Lat: ${att.latitude} | Lng: ${att.longitude}",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Location Verified via Mobile GPS Telemetry",
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                }
                            }

                            if (!att.selfieUri.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("IDENTITY VERIFICATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = "Selfie",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Selfie Photo Uploaded", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("Filename: ${att.selfieUri}", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            }
                        }
                        dayInfo.leaveRequest != null -> {
                            val leave = dayInfo.leaveRequest
                            Text("LEAVE RECORD DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(6.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Duration:", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                    Text("${leave.startDate} to ${leave.endDate}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text("Reason for Leave:", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                    Text(leave.reason, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                        dayInfo.status == "Weekend" -> {
                            Text(
                                text = "Sunday Weekend",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "This is a non-working weekend day. No check-in records are expected or tracked.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        else -> {
                            // Absent
                            Text(
                                text = "No Check-In Record Found",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "The employee has not checked in and has no approved leave requests on this working day. It is flagged as an Absent day for reporting.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { selectedDayDetail = null },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Close Details")
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceStatCard(
    label: String,
    count: Int,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = color.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

data class DayInfo(
    val dayNumber: Int,
    val dateString: String,
    val isCurrentMonth: Boolean,
    val isSunday: Boolean,
    val status: String, // "Present", "Late Arrival", "Half Day", "On Leave", "Absent", "Future", "None"
    val attendance: Attendance? = null,
    val leaveRequest: LeaveRequest? = null
)

// --- Leave Requests Screen ---
@Composable
fun LeaveRequestsScreen(viewModel: CRMViewModel) {
    val leaves by viewModel.allLeaveRequests.collectAsState()
    val currentUserState by viewModel.currentUser.collectAsState()
    val isAdmin = currentUserState?.role == "Admin" || currentUserState?.role == "Manager"

    Scaffold(
        topBar = {
            CrmTopBar(
                title = "Leave Administration",
                onLogout = { viewModel.logout() },
                onProfile = { viewModel.navigateTo(CrmScreen.Profile) },
                onBack = { viewModel.navigateBack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Text("Pending & Historic Leave Requests", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            if (leaves.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No Leave Applications Found.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(leaves) { request ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(request.employeeName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text("Duration: ${request.startDate} to ${request.endDate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                    }
                                    Badge(
                                        containerColor = when (request.status) {
                                            "Approved" -> Color(0xFF388E3C)
                                            "Rejected" -> Color(0xFFD32F2F)
                                            else -> Color(0xFFF57C00)
                                        }
                                    ) {
                                        Text(request.status, color = Color.White, modifier = Modifier.padding(4.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Reason: ${request.reason}", fontSize = 13.sp)

                                if (isAdmin && request.status == "Pending") {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                        Button(
                                            onClick = { viewModel.approveOrRejectLeave(request.id, "Approved") },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Approve")
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = { viewModel.approveOrRejectLeave(request.id, "Rejected") },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Reject")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Salary Management Screen ---
@Composable
fun SalaryManagementScreen(viewModel: CRMViewModel) {
    val context = LocalContext.current
    val employeesList by viewModel.employees.collectAsState()
    val slipsList by viewModel.allSalarySlips.collectAsState()

    var selectedEmployeeId by remember { mutableStateOf(-1) }
    var bonusAmt by remember { mutableStateOf("2000") }
    var deductionAmt by remember { mutableStateOf("500") }
    var monthStr by remember { mutableStateOf("2026-06") }

    Scaffold(
        topBar = {
            CrmTopBar(
                title = "Salaries & Payroll Management",
                onLogout = { viewModel.logout() },
                onProfile = { viewModel.navigateTo(CrmScreen.Profile) },
                onBack = { viewModel.navigateBack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Generate Payslip PDF", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Employee Staff:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        employeesList.filter { it.role == "Staff" }.forEach { emp ->
                            FilterChip(
                                selected = selectedEmployeeId == emp.id,
                                onClick = { selectedEmployeeId = emp.id },
                                label = { Text(emp.name) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = monthStr,
                        onValueChange = { monthStr = it },
                        label = { Text("Payroll Month (yyyy-MM)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = bonusAmt,
                        onValueChange = { bonusAmt = it },
                        label = { Text("Performance Bonus Amount (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = deductionAmt,
                        onValueChange = { deductionAmt = it },
                        label = { Text("Deductions (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (selectedEmployeeId != -1) {
                                viewModel.generateSalarySlip(
                                    selectedEmployeeId,
                                    monthStr,
                                    bonusAmt.toDoubleOrNull() ?: 0.0,
                                    deductionAmt.toDoubleOrNull() ?: 0.0
                                )
                                Toast.makeText(context, "Payroll generated successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Please select an employee first", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Calculate & Save Salary Payslip", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Salary History Logs", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            slipsList.forEach { slip ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(slip.employeeName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(slip.month, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Basic: ₹${slip.basicSalary.toInt()} | Inc: ₹${slip.incentives.toInt()}", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            Text("Net Paid: ₹${slip.netSalary.toInt()}", fontWeight = FontWeight.Bold, color = Color(0xFF388E3C), fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- Targets Settings Screen ---
@Composable
fun TargetsScreen(viewModel: CRMViewModel) {
    val context = LocalContext.current
    val staffList by viewModel.employees.collectAsState()
    val targetsList by viewModel.allTargets.collectAsState()

    var selectedEmployeeId by remember { mutableStateOf(-1) }
    var period by remember { mutableStateOf("Monthly") }
    var targetAmt by remember { mutableStateOf("150000") }

    val periods = listOf("Daily", "Weekly", "Monthly", "Renewals Target")

    Scaffold(
        topBar = {
            CrmTopBar(
                title = "Set Sales Targets",
                onLogout = { viewModel.logout() },
                onProfile = { viewModel.navigateTo(CrmScreen.Profile) },
                onBack = { viewModel.navigateBack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Allocate Target to Staff", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Employee Staff:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        staffList.filter { it.role == "Staff" }.forEach { emp ->
                            FilterChip(
                                selected = selectedEmployeeId == emp.id,
                                onClick = { selectedEmployeeId = emp.id },
                                label = { Text(emp.name) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Target Window Period:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        periods.forEach { p ->
                            FilterChip(
                                selected = period == p,
                                onClick = { period = p },
                                label = { Text(p) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = targetAmt,
                        onValueChange = { targetAmt = it },
                        label = { Text("Target Premium Volume Goal (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (selectedEmployeeId != -1) {
                                viewModel.setEmployeeTarget(
                                    selectedEmployeeId,
                                    period,
                                    targetAmt.toDoubleOrNull() ?: 100000.0
                                )
                                Toast.makeText(context, "Target Goal Allocated successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Please select an employee", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Allocate Target", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Live Leaderboard Target Trackers", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            targetsList.forEach { target ->
                val staffName = staffList.find { it.id == target.employeeId }?.name ?: "Telecaller"
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(staffName, fontWeight = FontWeight.Bold)
                            Text(target.period, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val ratio = (target.achievedAmount / target.targetAmount).coerceIn(0.0, 1.0).toFloat()
                        LinearProgressIndicator(
                            progress = ratio,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Achieved: ₹${target.achievedAmount.toInt()}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text("Goal: ₹${target.targetAmount.toInt()}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    }
}

// --- Profile Details Screen ---
@Composable
fun ProfileScreen(viewModel: CRMViewModel) {
    val user by viewModel.currentUser.collectAsState()
    var showEditProfileDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CrmTopBar(
                title = "My Secure Profile",
                onLogout = { viewModel.logout() },
                onProfile = { },
                onBack = { viewModel.navigateBack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    "profile",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(user?.name ?: "Sree Venkateswara User", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text(user?.designation ?: "Executive Team Member", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { showEditProfileDialog = true },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.Edit, "edit", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit Profile Details", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfileItemRow("Registered Phone", user?.phone ?: "--")
                    Divider()
                    ProfileItemRow("National ID (Aadhaar)", user?.aadhaar ?: "--")
                    Divider()
                    ProfileItemRow("Tax Identification (PAN)", user?.pan ?: "--")
                    Divider()
                    ProfileItemRow("Bank Credentials", user?.bankDetails ?: "--")
                    Divider()
                    ProfileItemRow("Role Level Auth", user?.role ?: "--")
                    Divider()
                    ProfileItemRow("Date of Joining", user?.joiningDate ?: "--")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Sree Venkateswara Insurances CRM Platform. Protected by role-based local secure storage.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }

    if (showEditProfileDialog && user != null) {
        EditProfileDialog(
            user = user!!,
            onDismiss = { showEditProfileDialog = false },
            onSave = { updatedUser ->
                viewModel.addOrUpdateEmployee(
                    id = updatedUser.id,
                    name = updatedUser.name,
                    phone = updatedUser.phone,
                    password = updatedUser.password,
                    role = updatedUser.role,
                    designation = updatedUser.designation,
                    salary = updatedUser.salary,
                    aadhaar = updatedUser.aadhaar,
                    pan = updatedUser.pan,
                    bankDetails = updatedUser.bankDetails,
                    joiningDate = updatedUser.joiningDate
                )
                showEditProfileDialog = false
                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun EditProfileDialog(
    user: Employee,
    onDismiss: () -> Unit,
    onSave: (Employee) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var phone by remember { mutableStateOf(user.phone) }
    var password by remember { mutableStateOf(user.password) }
    var aadhaar by remember { mutableStateOf(user.aadhaar) }
    var pan by remember { mutableStateOf(user.pan) }
    var bank by remember { mutableStateOf(user.bankDetails) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Edit Profile Details", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Mobile Phone") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = aadhaar, onValueChange = { aadhaar = it }, label = { Text("Aadhaar Number") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = pan, onValueChange = { pan = it }, label = { Text("PAN Number") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = bank, onValueChange = { bank = it }, label = { Text("Bank A/C Details") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (name.isNotEmpty() && phone.isNotEmpty()) {
                                onSave(user.copy(
                                    name = name,
                                    phone = phone,
                                    password = password,
                                    aadhaar = aadhaar,
                                    pan = pan,
                                    bankDetails = bank
                                ))
                            }
                        }
                    ) { Text("Save Changes") }
                }
            }
        }
    }
}

@Composable
fun ProfileItemRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

// --- Advanced Reports with Charts ---
@Composable
fun ReportsScreen(viewModel: CRMViewModel) {
    val leads by viewModel.allLeads.collectAsState()
    val targets by viewModel.allTargets.collectAsState()
    val staffList by viewModel.employees.collectAsState()

    // Revenue calculations
    val revenueByCategory = leads.filter { it.status == "Converted" }.groupBy { it.insuranceCategory }
        .mapValues { entry -> entry.value.sumOf { it.premiumAmount } }

    Scaffold(
        topBar = {
            CrmTopBar(
                title = "Advanced Analytics",
                onLogout = { viewModel.logout() },
                onProfile = { viewModel.navigateTo(CrmScreen.Profile) },
                onBack = { viewModel.navigateBack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Revenue per Insurance Type", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (revenueByCategory.isEmpty()) {
                        Text("No conversion revenue tracked yet.")
                    } else {
                        revenueByCategory.forEach { (cat, rev) ->
                            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(cat, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text("₹${rev.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                val maxVal = revenueByCategory.values.maxOrNull() ?: 1.0
                                val progress = (rev / maxVal).coerceIn(0.0, 1.0).toFloat()
                                LinearProgressIndicator(
                                    progress = progress,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Top Performer Awards & Leaderboard", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val staffSorted = staffList.filter { it.role == "Staff" }.sortedByDescending { emp ->
                        leads.filter { it.assignedStaffId == emp.id && it.status == "Converted" }.sumOf { it.premiumAmount }
                    }

                    if (staffSorted.isEmpty()) {
                        Text("No telecalling staff registered.")
                    } else {
                        staffSorted.forEachIndexed { index, emp ->
                            val closedRevenue = leads.filter { it.assignedStaffId == emp.id && it.status == "Converted" }.sumOf { it.premiumAmount }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (index == 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primaryContainer
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "${index + 1}",
                                            fontWeight = FontWeight.Bold,
                                            color = if (index == 0) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(emp.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(emp.designation, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                    }
                                }

                                Text(
                                    "₹${closedRevenue.toInt()}",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF388E3C),
                                    fontSize = 15.sp
                                )
                            }
                            if (index < staffSorted.size - 1) {
                                Divider()
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- DIALOGS ---

@Composable
fun CsvImportDialog(
    viewModel: CRMViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val staffList by viewModel.employees.collectAsState()
    val activeStaff = remember(staffList) { staffList.filter { it.role == "Staff" } }

    var text by remember {
        mutableStateOf(
            "Name,Phone,InsuranceCategory,PremiumAmount,RenewalDate\n" +
                    "Vasudha Rao,9550341122,Car Insurance,22000,2026-07-28\n" +
                    "Madhava Chary,9848033221,Health Insurance,14500,2026-08-15\n" +
                    "Balaji Temple,9900223344,Home Insurance,8000,2026-09-01"
        )
    }

    // Tab state: 0 = File Upload Mode, 1 = Manual Input Mode
    var activeTab by remember { mutableStateOf(0) }

    // Uploaded file info state
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFileSize by remember { mutableStateOf<String?>(null) }
    var selectedRowCount by remember { mutableStateOf<Int?>(null) }
    var isFileLoaded by remember { mutableStateOf(false) }

    // Assignment state
    var assignmentMode by remember { mutableStateOf("round_robin") } // "unassigned", "round_robin", "direct"
    var selectedStaffId by remember { mutableStateOf<Int?>(null) }
    var selectedStaffName by remember { mutableStateOf<String?>(null) }

    // Setup direct staff defaults
    LaunchedEffect(activeStaff) {
        if (activeStaff.isNotEmpty() && selectedStaffId == null) {
            selectedStaffId = activeStaff.first().id
            selectedStaffName = activeStaff.first().name
        }
    }

    // Native Android Document Picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder = StringBuilder()
                    var line: String? = reader.readLine()
                    var rowCount = 0
                    while (line != null) {
                        stringBuilder.append(line).append("\n")
                        if (line.isNotBlank()) rowCount++
                        line = reader.readLine()
                    }
                    val content = stringBuilder.toString()
                    text = content
                    
                    val hasHeader = content.lineSequence().firstOrNull()?.contains("Name", ignoreCase = true) == true
                    val actualRows = if (hasHeader) (rowCount - 1).coerceAtLeast(0) else rowCount

                    var name = "uploaded_leads_data.csv"
                    var sizeStr = "${(content.length / 1024.0).let { "%.1f".format(it) }} KB"

                    val cursor = context.contentResolver.query(uri, null, null, null, null)
                    cursor?.use { c ->
                        val nameIndex = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = c.getColumnIndex(android.provider.OpenableColumns.SIZE)
                        if (nameIndex != -1 && c.moveToFirst()) {
                            name = c.getString(nameIndex)
                        }
                        if (sizeIndex != -1) {
                            val sizeInBytes = c.getLong(sizeIndex)
                            sizeStr = "${(sizeInBytes / 1024.0).let { "%.1f".format(it) }} KB"
                        }
                    }

                    selectedFileName = name
                    selectedFileSize = sizeStr
                    selectedRowCount = actualRows
                    isFileLoaded = true
                    Toast.makeText(context, "Loaded: $name", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                // Title Area
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Upload Header",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Data Assignment Module",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "Import customer lists and assign leads to staff via file upload.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                )

                // Tab Selector
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("File Upload Mode", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("Manual Text CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (activeTab == 0) {
                    // --- FILE UPLOAD TAB ---
                    if (!isFileLoaded) {
                        // Drag & Drop / Upload container
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clickable { filePickerLauncher.launch("*/*") },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.UploadFile,
                                        contentDescription = "Upload Cloud",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Click to browse device for CSV file",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Supports standard comma-delimited (.csv, .txt)",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    } else {
                        // File Loaded Status Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            border = BorderStroke(1.dp, Color(0xFF388E3C).copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success Icon",
                                    tint = Color(0xFF388E3C),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = selectedFileName ?: "leads.csv",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B5E20),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Size: ${selectedFileSize ?: "Unknown"} | Row Count: ${selectedRowCount ?: 0} leads detected",
                                        fontSize = 10.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        isFileLoaded = false
                                        selectedFileName = null
                                        selectedFileSize = null
                                        selectedRowCount = null
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete File",
                                        tint = Color(0xFFC2185B)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Preset Templates helper
                    Text(
                        text = "Or load a pre-configured template to assign:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val templatesList = listOf(
                            Triple(
                                "Venkateswara Real Leads",
                                "Name,Phone,InsuranceCategory,PremiumAmount,RenewalDate\n" +
                                        "MOHAMMED AFZAL,9012345678,Car Insurance,14200,2025-06-02\n" +
                                        "VATTEPU MADHU,9440123456,Car Insurance,15500,2025-06-02\n" +
                                        "GEETHIKA RATHOD,9848011223,Car Insurance,13800,2025-06-02\n" +
                                        "SUSHEELA ESLAVATH,9100223344,Car Insurance,14500,2025-06-02\n" +
                                        "LAXMI PRASANNA PERELLI,9000123123,Car Insurance,12900,2025-06-02\n" +
                                        "VENU GOPAL POTHAGONI,9876543001,Commercial Vehicle,21500,2025-06-02\n" +
                                        "KUMBHA RAMARAO,9441234567,Car Insurance,16200,2025-06-02\n" +
                                        "PRAVEEN KUMAR VAMKUNAVATH,9900112288,Commercial Vehicle,23000,2025-06-02\n" +
                                        "SHIREESHA BANOTHU,9121213141,Commercial Vehicle,22500,2025-06-02\n" +
                                        "DILEEP KUMAR NAGULA,9030112233,Commercial Vehicle,21000,2025-06-02\n" +
                                        "KOMMU KAVITHA,9543210987,Car Insurance,18500,2025-06-04\n" +
                                        "SHABANA BEGUM,9112233445,Car Insurance,19000,2025-06-06\n" +
                                        "MOHAMMED AMJAD AHMED,9223344556,Car Insurance,19200,2025-06-06\n" +
                                        "KORIVI ARAVIND,9334455667,Car Insurance,14800,2025-06-06\n" +
                                        "MOTE VENKATESHAM,9445566778,Car Insurance,15200,2025-06-06\n" +
                                        "BOSU SHRAVANI,9556677889,Car Insurance,13400,2025-06-06\n" +
                                        "RAMANJINEYULU SAANA,9667788990,Car Insurance,12800,2025-06-06\n" +
                                        "CHERUKULA KARTHIK,9778899001,Car Insurance,17500,2025-06-06\n" +
                                        "KRISHNA KUMAR SAHA,9889900112,Commercial Vehicle,24000,2025-06-06\n" +
                                        "SAI KUMAR YESUDAS AROGYANATH,9990011223,Car Insurance,15800,2025-06-09\n" +
                                        "MANIKYALA SHANKAR GOUD,9001122334,Car Insurance,16500,2025-06-09\n" +
                                        "NALLA DILEEP,9112233445,Commercial Vehicle,19800,2025-06-09\n" +
                                        "MANESH KANNEBOINA,9223344556,Commercial Vehicle,20500,2025-06-09\n" +
                                        "KRISHNA ERIGIPALLY,9334455667,Car Insurance,14100,2025-06-09\n" +
                                        "BEEM REDDY NANDARAM,9445566778,Car Insurance,17200,2025-06-09\n" +
                                        "NIMMA KAMAL SINGH,9556677889,Car Insurance,13900,2025-06-05\n" +
                                        "SOPRAJ MR,9667788990,Commercial Vehicle,21800,2025-06-05\n" +
                                        "MALLAM SHYAMALA,9778899001,Car Insurance,14900,2025-06-05\n" +
                                        "MUDU SAI SURYA NAIK,9889900112,Commercial Vehicle,25000,2025-06-05\n" +
                                        "GEETHA BALYALA,9990011223,Car Insurance,18200,2025-06-05\n" +
                                        "TARALA NARENDAR,9001122334,Car Insurance,19400,2025-06-05\n" +
                                        "UMAR MUHAMMAD,9112233445,Car Insurance,18800,2025-06-06\n" +
                                        "MOHAMMED SULTAN UDDIN,9223344556,Commercial Vehicle,23500,2025-06-09\n" +
                                        "BURRA BHASKER,9334455667,Car Insurance,14500,2025-06-09\n" +
                                        "SATHYA RANI NADIMPALLY,9445566778,Commercial Vehicle,22000,2025-06-09\n" +
                                        "SUNITHA LAVADIYA,9556677889,Commercial Vehicle,21500,2025-06-09",
                                "36 Real Customer Lines from PDF Records"
                            ),
                            Triple(
                                "High-Value Car Insurance",
                                "Name,Phone,InsuranceCategory,PremiumAmount,RenewalDate\nSrinivasa Raju,9988776655,Car Insurance,28000,2026-08-01\nPrasanna Lakshmi,9502345678,Car Insurance,24500,2026-09-12",
                                "2 Premium Car Leads"
                            ),
                            Triple(
                                "Mass Commercial Pipeline",
                                "Name,Phone,InsuranceCategory,PremiumAmount,RenewalDate\nJagadish Goud,8801234567,Commercial Vehicle,45000,2026-07-25\nRamesh Naidu,8123456789,Commercial Vehicle,38000,2026-08-10\nSita Mahalakshmi,9030011223,Commercial Vehicle,52000,2026-09-05\nVenkat Prasad,9849922334,Commercial Vehicle,41000,2026-12-01",
                                "4 Commercial Vehicle Leads"
                            )
                        )

                        templatesList.forEach { (title, dataText, desc) ->
                            OutlinedButton(
                                onClick = {
                                    text = dataText
                                    selectedFileName = "template_${title.lowercase().replace(" ", "_")}.csv"
                                    selectedFileSize = "1.5 KB"
                                    selectedRowCount = dataText.lineSequence().count() - 1
                                    isFileLoaded = true
                                    Toast.makeText(context, "Loaded $title Template", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(desc, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Icon(
                                        imageVector = Icons.Default.InsertDriveFile,
                                        contentDescription = "File Preset",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // --- MANUAL TEXT CSV TAB ---
                    Text(
                        text = "Comma-delimited format (Name,Phone,Category,Premium,Date):",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        maxLines = 10,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // --- DATA ASSIGNMENT CONFIGURATION AREA (CRITICAL) ---
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Configure Lead Assignment Method:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Assignment Mode Selectors (Segmented Chips)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = assignmentMode == "unassigned",
                        onClick = { assignmentMode = "unassigned" },
                        label = { Text("Unassigned", fontSize = 11.sp) }
                    )

                    FilterChip(
                        selected = assignmentMode == "round_robin",
                        onClick = { assignmentMode = "round_robin" },
                        label = { Text("Round-Robin Auto", fontSize = 11.sp) }
                    )

                    FilterChip(
                        selected = assignmentMode == "direct",
                        onClick = { assignmentMode = "direct" },
                        label = { Text("Direct Agent", fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Contextual UI based on choice
                when (assignmentMode) {
                    "unassigned" -> {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ Leads will be uploaded into the global 'Fresh Data' list with no staff assigned. Admin can bulk-assign or auto-assign them later.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                    "round_robin" -> {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚡ Smart Round-Robin assignment active. System will automatically and evenly distribute the uploaded leads among all ${activeStaff.size} active telecallers.",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                    "direct" -> {
                        Text(
                            text = "Directly assign all leads to staff member:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        if (activeStaff.isEmpty()) {
                            Text(
                                text = "No active staff members found in the system.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                activeStaff.forEach { staff ->
                                    val isSelected = selectedStaffId == staff.id
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedStaffId = staff.id
                                            selectedStaffName = staff.name
                                        },
                                        label = { Text(staff.name, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (activeTab == 0 && !isFileLoaded) {
                                Toast.makeText(context, "Please browse for a CSV file or load a preset template to import data.", Toast.LENGTH_LONG).show()
                                return@Button
                            }

                            if (text.isBlank()) {
                                Toast.makeText(context, "CSV file is empty or corrupted.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val result = when (assignmentMode) {
                                "round_robin" -> {
                                    viewModel.importLeadsFromCsv(
                                        csvText = text,
                                        autoAssignRoundRobin = true
                                    )
                                }
                                "direct" -> {
                                    viewModel.importLeadsFromCsv(
                                        csvText = text,
                                        assignedStaffId = selectedStaffId,
                                        assignedStaffName = selectedStaffName
                                    )
                                }
                                else -> {
                                    viewModel.importLeadsFromCsv(csvText = text)
                                }
                            }

                            if (result) {
                                Toast.makeText(context, "Successfully Imported & Processed Leads!", Toast.LENGTH_LONG).show()
                                onDismiss()
                            } else {
                                Toast.makeText(context, "Failed to Parse File. Check template formatting.", Toast.LENGTH_LONG).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = "Upload")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (assignmentMode != "unassigned") "Import & Assign Now" else "Import as Unassigned",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BackupRestoreDialog(backupText: String, onDismiss: () -> Unit, onRestore: (String) -> Unit) {
    var text by remember { mutableStateOf(backupText) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Data Backup & Restore Module", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Copy the raw text content to backup or paste to restore:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    maxLines = 10
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onDismiss) { Text("Close") }
                    Button(onClick = { onRestore(text) }) { Text("Restore / Sync") }
                }
            }
        }
    }
}

@Composable
fun AddLeadDialog(
    staffList: List<Employee>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double, String, Int?, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Motor Insurance") }
    var premium by remember { mutableStateOf("12000") }
    var renewalDate by remember { mutableStateOf("2026-12-31") }
    var selectedStaffId by remember { mutableStateOf<Int?>(null) }
    var selectedStaffName by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Motor Insurance", "Bike Insurance", "Car Insurance", "Health Insurance", "Life Insurance", "Commercial Vehicle", "Travel Insurance", "Home Insurance")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Create Customer Lead", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Client Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Client Phone") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                Spacer(modifier = Modifier.height(8.dp))

                Text("Insurance Category:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = premium, onValueChange = { premium = it }, label = { Text("Premium Value (₹)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = renewalDate, onValueChange = { renewalDate = it }, label = { Text("Renewal Expiry Date (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                Text("Assign Telecaller:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    staffList.filter { it.role == "Staff" }.forEach { staff ->
                        FilterChip(
                            selected = selectedStaffId == staff.id,
                            onClick = {
                                if (selectedStaffId == staff.id) {
                                    selectedStaffId = null
                                    selectedStaffName = null
                                } else {
                                    selectedStaffId = staff.id
                                    selectedStaffName = staff.name
                                }
                            },
                            label = { Text(staff.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (name.isNotEmpty() && phone.isNotEmpty()) {
                                onSave(name, phone, selectedCategory, premium.toDoubleOrNull() ?: 10000.0, renewalDate, selectedStaffId, selectedStaffName)
                            }
                        }
                    ) { Text("Save Lead") }
                }
            }
        }
    }
}

@Composable
fun BulkAssignDialog(
    staffList: List<Employee>,
    onDismiss: () -> Unit,
    onAssign: (Employee) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Bulk Reassign Leads", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Select Telecalling Representative to route selected leads:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(staffList) { staff ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onAssign(staff) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(staff.name, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                }
            }
        }
    }
}

@Composable
fun LeaveRequestDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var start by remember { mutableStateOf("2026-07-05") }
    var end by remember { mutableStateOf("2026-07-06") }
    var reason by remember { mutableStateOf("Personal urgent work") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Apply Leave", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(value = start, onValueChange = { start = it }, label = { Text("Start Date (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = end, onValueChange = { end = it }, label = { Text("End Date (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason for Leave") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = { onSubmit(start, end, reason) }) { Text("Submit") }
                }
            }
        }
    }
}

@Composable
fun AddEmployeeDialog(
    employee: Employee? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Double, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(employee?.name ?: "") }
    var phone by remember { mutableStateOf(employee?.phone ?: "") }
    var password by remember { mutableStateOf(employee?.password ?: "") }
    var role by remember { mutableStateOf(employee?.role ?: "Staff") }
    var designation by remember { mutableStateOf(employee?.designation ?: "Junior Telecaller") }
    var salary by remember { mutableStateOf(employee?.salary?.toInt()?.toString() ?: "22000") }
    var aadhaar by remember { mutableStateOf(employee?.aadhaar ?: "4545-2233-1122") }
    var pan by remember { mutableStateOf(employee?.pan ?: "ABCDE5432X") }
    var bank by remember { mutableStateOf(employee?.bankDetails ?: "SBI A/C: 1002003004") }
    var joinDate by remember { mutableStateOf(employee?.joiningDate ?: "2026-06-30") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(if (employee == null) "Register Employee Profile" else "Edit Employee Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Employee Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Mobile Phone") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Initial Login Password") },
                    placeholder = { Text("Default: staff") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                Text("Staff can activate and set their own password during first login using Aadhaar/PAN verification.", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))

                Text("System Role Level:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Staff", "Team Leader", "Manager", "Admin").forEach { r ->
                        FilterChip(
                            selected = role == r,
                            onClick = { role = r },
                            label = { Text(r) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = designation, onValueChange = { designation = it }, label = { Text("Job Designation") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = salary, onValueChange = { salary = it }, label = { Text("Monthly Base Salary (₹)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = aadhaar, onValueChange = { aadhaar = it }, label = { Text("Aadhaar Number") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = pan, onValueChange = { pan = it }, label = { Text("PAN Number") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = bank, onValueChange = { bank = it }, label = { Text("Bank A/C Details") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = joinDate, onValueChange = { joinDate = it }, label = { Text("Joining Date (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (name.isNotEmpty() && phone.isNotEmpty()) {
                                onSave(name, phone, role, designation, salary.toDoubleOrNull() ?: 20000.0, aadhaar, pan, bank, joinDate, password)
                            }
                        }
                    ) { Text(if (employee == null) "Register Employee" else "Save Profile Changes") }
                }
            }
        }
    }
}

@Composable
fun SchedulerStatusCard(
    leads: List<com.example.data.Lead>,
    onTriggerCheck: () -> Unit
) {
    val context = LocalContext.current
    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todayFollowUps = leads.filter { lead ->
        lead.status != "Converted" && lead.status != "Rejected" && lead.status != "DND" &&
        lead.renewalDate == todayDateStr
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Scheduler",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Follow-Up Background Scheduler",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Surface(
                    color = Color(0xFF388E3C).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF388E3C))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ACTIVE (15M)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF388E3C)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = "Automatically runs in the background to scan for follow-up reminders due today. Simulates field operations and pushes real-time alerts.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = {
                    onTriggerCheck()
                    Toast.makeText(context, "Background Follow-Up Job Triggered!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Run now", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Run Background Check Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            
            if (todayFollowUps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Alerts",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Today's Active Alerts & Scheduled Follow-Ups (${todayFollowUps.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                }
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    todayFollowUps.forEach { lead ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = lead.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Phone: ${lead.phone} | Plan: ${lead.insuranceCategory}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    if (!lead.callNotes.isNullOrBlank()) {
                                        Text(
                                            text = "Note: ${lead.callNotes}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "DUE TODAY",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎉 No follow-up alerts due for today!",
                        fontSize = 12.sp,
                        color = Color(0xFF388E3C),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun SplashScreen(viewModel: CRMViewModel) {
    val scale = remember { androidx.compose.animation.core.Animatable(0f) }
    val alpha = remember { androidx.compose.animation.core.Animatable(0f) }
    
    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.spring(
                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                stiffness = androidx.compose.animation.core.Spring.StiffnessLow
            )
        )
    }

    LaunchedEffect(key1 = true) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 1000)
        )
        // Delay for splash screen feel, then navigate
        kotlinx.coroutines.delay(2200)
        viewModel.navigateTo(CrmScreen.Login)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F3B8C), // Royal Blue
                        Color(0xFF051636)  // Deep Navy
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.graphicsLayer(
                    scaleX = scale.value,
                    scaleY = scale.value,
                    alpha = alpha.value
                )
            ) {
                // Outer circle glow for the logo
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    VenkateswaraEntranceLogo(
                        modifier = Modifier.size(150.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "VENKATESWARA",
                    fontSize = 30.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFD4AF37), // Golden
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Divider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.width(40.dp).height(1.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "INSURANCES",
                        fontSize = 18.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 5.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Divider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.width(40.dp).height(1.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "SECURING TODAY  •  PROTECTING TOMORROW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Pulsing progress bar
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color(0xFFD4AF37),
                    strokeWidth = 2.dp
                )
            }
            
            // Manual Enter / Skip button at bottom
            Button(
                onClick = { viewModel.navigateTo(CrmScreen.Login) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.15f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Enter CRM",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next",
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}


