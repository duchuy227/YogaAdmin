import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yogaappadmin.R
import com.example.yogaappadmin.components.SyncConfirmationDialog
import com.example.yogaappadmin.components.isInternetAvailable
import com.example.yogaappadmin.data.YogaClass
import com.example.yogaappadmin.viewmodel.YogaViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YogaClassListScreen(viewModel: YogaViewModel, modifier: Modifier = Modifier) {
    val yogaClasses by viewModel.allYogaClasses.collectAsState(initial = emptyList())
    var showForm by remember { mutableStateOf(false) }
    var selectedYogaClass by remember { mutableStateOf<YogaClass?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var expanded by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val filterOptions = listOf("All", "Time", "Day of Week", "Teacher")

    Scaffold(
        floatingActionButton = {
            if (!showForm) {
                FloatingActionButton(
                    onClick = {
                        showForm = true
                        selectedYogaClass = null
                    },
                    containerColor = Color(0xFF026B07),
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add class", tint = Color.White)
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Universal Yoga",
                    style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFF6200EA)),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
                IconButton(onClick = { showSyncDialog = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_sync_24),
                        contentDescription = "Sync"
                    )
                }
            }

            if (!showForm) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        placeholder = { Text("Search class...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Box {
                        Button(
                            onClick = { expanded = true },
                            shape = RoundedCornerShape(15.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF9800)
                            )
                        ) {
                            Text(selectedFilter)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.height(IntrinsicSize.Min)
                        ) {
                            filterOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        selectedFilter = option
                                        expanded = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface) // Thêm background ở đây
                                        .padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (showForm) {
                ClassForm(
                    yogaClass = selectedYogaClass,
                    onClose = {
                        showForm = false
                        selectedYogaClass = null
                    },
                    viewModel = viewModel,
                )
            } else {
                YogaClassList(
                    classes = yogaClasses.filter {
                        it.matchesSearchQuery(searchQuery, selectedFilter)
                    },
                    onItemClick = { yogaClass ->
                        selectedYogaClass = yogaClass
                        showForm = true
                    }
                )
            }

            if (showSyncDialog) {
                SyncConfirmationDialog(
                    onConfirm = {
                        if (isInternetAvailable(context)) {
                            viewModel.syncWithFirestore()
                        } else {
                            // Show no internet message
                        }
                        showSyncDialog = false
                    },
                    onDismiss = { showSyncDialog = false },
                    hasInternet = isInternetAvailable(context)
                )
            }
        }
    }
}

@Composable
fun YogaClassList(classes: List<YogaClass>, onItemClick: (YogaClass) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp) // Padding cho toàn bộ LazyColumn
    ) {
        items(classes) { yogaClass ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable {onItemClick(yogaClass) },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE0B2)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Yoga class title
                    Text(
                        text = "Yoga class: ${yogaClass.id}", // Đảm bảo className tồn tại
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B4513)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row for Day of week and Teacher
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Day of week: ${yogaClass.dayOfWeek}",
                            style = TextStyle(fontSize = 14.sp, color = Color(0xFF0060A0))
                        )
                        Text(
                            text = "Teacher: ${yogaClass.teacher}",
                            style = TextStyle(fontSize = 14.sp, color = Color(0xFF6A1B9A))
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Row for Type and Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Type: ${yogaClass.type}",
                            style = TextStyle(fontSize = 14.sp, color = Color(0xFF0404B1))
                        )
                        Text(
                            text = "Time: ${yogaClass.time}",
                            style = TextStyle(fontSize = 14.sp, color = Color(0xFFD32F2F))
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Row for Duration and Capacity
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Duration: ${yogaClass.duration} hours",
                            style = TextStyle(fontSize = 14.sp, color = Color(0xFF02C9CF))
                        )
                        Text(
                            text = "Capacity: ${yogaClass.capacity}",
                            style = TextStyle(fontSize = 14.sp, color = Color(0xFFFF5722))
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Price
                    Text(
                        text = "Price: $${yogaClass.price}",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4CAF50))
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Description
                    Text(
                        text = "Description: ${yogaClass.description}",
                        style = TextStyle(fontSize = 14.sp, color = Color(0xFF424242))
                    )
                }
            }
        }
    }
}


// Extension function to filter YogaClass based on search query and selected filter
fun YogaClass.matchesSearchQuery(query: String, filter: String): Boolean {
    if (query.isEmpty()) return true
    return when (filter) {
        "All" -> id.toString().contains(query, ignoreCase = true) ||
                dayOfWeek.contains(query, ignoreCase = true) ||
                teacher.contains(query, ignoreCase = true) ||
                type.contains(query, ignoreCase = true) ||
                time.contains(query, ignoreCase = true)
        "Time" -> time.contains(query, ignoreCase = true)
        "Day of Week" -> dayOfWeek.contains(query, ignoreCase = true)
        "Teacher" -> teacher.contains(query, ignoreCase = true)
        else -> false
    }
}

