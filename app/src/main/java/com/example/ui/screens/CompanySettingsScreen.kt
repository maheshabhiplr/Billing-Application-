package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CompanyDetails
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanySettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val companyDetails by viewModel.companyDetails.collectAsState()

    var compMl by remember(companyDetails) { mutableStateOf(companyDetails?.companyNameMalayalam ?: "") }
    var compEng by remember(companyDetails) { mutableStateOf(companyDetails?.companyNameEnglish ?: "") }
    var tagline by remember(companyDetails) { mutableStateOf(companyDetails?.tagline ?: "") }
    var address by remember(companyDetails) { mutableStateOf(companyDetails?.address ?: "") }
    var phone by remember(companyDetails) { mutableStateOf(companyDetails?.phone ?: "") }
    var gstin by remember(companyDetails) { mutableStateOf(companyDetails?.gstin ?: "") }
    var upiId by remember(companyDetails) { mutableStateOf(companyDetails?.upiId ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "സ്ഥാപന വിവരങ്ങൾ (Company Details)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "ബില്ലുകളിലും റിപ്പോർട്ടുകളിലും പ്രിന്റ് ചെയ്യേണ്ട സ്ഥാപന വിവരങ്ങൾ",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Business,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = compMl.ifBlank { "കടയുടെ പേര് നൽകുക" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = compEng.ifBlank { "Shop Name in English" },
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            OutlinedTextField(
                value = compMl,
                onValueChange = { compMl = it },
                label = { Text("സ്ഥാപനത്തിന്റെ പേര് (മലയാളത്തിൽ)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = compEng,
                onValueChange = { compEng = it },
                label = { Text("കടയുടെ പേര് (ഇംഗ്ലീഷിൽ / Invoice Header)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = tagline,
                onValueChange = { tagline = it },
                label = { Text("ടാഗ് ലൈൻ / കച്ചവട തരം (e.g. മൊത്ത-ചില്ലറ വ്യാപാരി)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("വിലാസം & സ്ഥലം (Address)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("ഫോൺ നമ്പർ (Contact Phone)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = gstin,
                onValueChange = { gstin = it },
                label = { Text("GSTIN (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = upiId,
                onValueChange = { upiId = it },
                label = { Text("GPay / PhonePe / UPI ID (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val updated = CompanyDetails(
                        id = 1,
                        companyNameMalayalam = compMl.trim(),
                        companyNameEnglish = compEng.trim(),
                        tagline = tagline.trim(),
                        address = address.trim(),
                        phone = phone.trim(),
                        gstin = gstin.trim(),
                        upiId = upiId.trim()
                    )
                    viewModel.updateCompanyDetails(updated)
                    Toast.makeText(context, "സ്ഥാപന വിവരങ്ങൾ സേവ് ചെയ്തു!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("വിവരങ്ങൾ സേവ് ചെയ്യുക", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
