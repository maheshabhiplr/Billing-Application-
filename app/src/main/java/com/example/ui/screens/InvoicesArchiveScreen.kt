package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Invoice
import com.example.ui.MainViewModel
import com.example.util.PdfGenerator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicesArchiveScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val invoicesList by viewModel.invoices.collectAsState()
    val companyDetails by viewModel.companyDetails.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val selectedInvoiceDetails by viewModel.selectedInvoiceDetails.collectAsState()
    var showInvoiceDetailModal by remember { mutableStateOf(false) }

    val filtered = invoicesList.filter {
        it.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                it.customerName.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ബില്ലുകളുടെ ചരിത്രം (Invoices Archive)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "നേരത്തെ ഉണ്ടാക്കിയ എല്ലാ ബില്ലുകളും കാണാം, PDF പ്രിന്റ് ചെയ്യാം",
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
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ബിൽ നം / ഉപഭോക്താവിന്റെ പേര് തിരയുക...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("ബില്ലുകൾ ഒന്നും കണ്ടെത്തിയില്ല", fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.id }) { invoice ->
                        InvoiceCardItem(
                            invoice = invoice,
                            onClick = {
                                viewModel.loadInvoiceDetails(invoice.id)
                                showInvoiceDetailModal = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showInvoiceDetailModal && selectedInvoiceDetails != null) {
        val details = selectedInvoiceDetails!!
        AlertDialog(
            onDismissRequest = { showInvoiceDetailModal = false },
            title = {
                Text(
                    text = "ബിൽ വിവരം: ${details.invoice.invoiceNumber}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                ) {
                    Text("ഉപഭോക്താവ്: ${details.invoice.customerName}", fontWeight = FontWeight.Bold)
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
                    Text("തീയതി: ${dateFormat.format(Date(details.invoice.dateTimestamp))}", fontSize = 11.sp, color = Color.Gray)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Text("ഇനങ്ങൾ (${details.items.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(details.items.size) { i ->
                            val item = details.items[i]
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.productName, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                Text("₹ ${item.unitPrice} x ${item.quantityKg} ${item.unit}", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("₹ ${item.totalPrice}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ആകെ തുക:", fontWeight = FontWeight.Bold)
                        Text("₹ ${details.invoice.grandTotal}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pdf = PdfGenerator.generateInvoicePdf(context, details, companyDetails)
                        if (pdf != null) {
                            PdfGenerator.openOrSharePdf(context, pdf, share = false)
                        } else {
                            Toast.makeText(context, "PDF തയ്യാറാക്കാൻ കഴിഞ്ഞില്ല", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PDF പ്രിന്റ് / കാണുക")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInvoiceDetailModal = false }) {
                    Text("അടയ്ക്കുക")
                }
            }
        )
    }
}

@Composable
fun InvoiceCardItem(invoice: Invoice, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = invoice.invoiceNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = dateFormat.format(Date(invoice.dateTimestamp)),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = invoice.customerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "ആകെ ഇനങ്ങൾ: ${invoice.totalItemCount}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹ ${String.format(Locale.US, "%.2f", invoice.grandTotal)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (invoice.dueBalance > 0) {
                        Text(
                            text = "ബാക്കി: ₹ ${invoice.dueBalance}",
                            fontSize = 10.sp,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
