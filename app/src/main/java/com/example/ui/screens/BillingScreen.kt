package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Customer
import com.example.model.Product
import com.example.ui.DraftItem
import com.example.ui.MainViewModel
import com.example.util.IndianCurrencyUtils
import com.example.util.PdfGenerator
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    viewModel: MainViewModel,
    onViewPdf: () -> Unit
) {
    val context = LocalContext.current
    val draftState by viewModel.draftInvoice.collectAsState()
    val customersList by viewModel.customers.collectAsState()
    val productsList by viewModel.products.collectAsState()

    var showProductSearchDialog by remember { mutableStateOf(false) }
    var showCustomerSelectDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "പുതിയ ബിൽ (New Bill)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "Malayalam Offline Store Billing",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearDraftBill() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Clear Bill",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showProductSearchDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Item") },
                text = { Text("ഉൽപ്പന്നം ചേർക്കുക", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Section 1: Customer Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ഉപഭോക്താവ് (Customer):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            OutlinedButton(
                                onClick = { showCustomerSelectDialog = true },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (draftState.customer != null) "മാറ്റുക" else "തിരഞ്ഞെടുക്കുക", fontSize = 12.sp)
                            }
                        }

                        if (draftState.customer != null) {
                            val cust = draftState.customer!!
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(cust.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            if (cust.phone.isNotEmpty()) {
                                Text("ഫോൺ: ${cust.phone}", fontSize = 12.sp, color = Color.Gray)
                            }
                            if (cust.previousBalance > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFFF3E0))
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("മുൻ കുടിശ്ശിക (Prev Balance):", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text("₹ ${cust.previousBalance}", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = draftState.customCustomerName,
                                onValueChange = { name -> viewModel.setCustomCustomerDetails(name, draftState.customCustomerPhone) },
                                label = { Text("ഉപഭോക്താവിന്റെ പേര് (Optional)", fontSize = 12.sp) },
                                placeholder = { Text("e.g. ക്യാഷ് കസ്റ്റമർ") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // Section 2: Items Table Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ബിൽ ഇനങ്ങൾ (Items List - ${draftState.totalItemCount})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (draftState.totalWeightKg > 0) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "ആകെ ഭാരം: ${String.format(Locale.US, "%.3f", draftState.totalWeightKg)} kg",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Table Header Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ഉൽപ്പന്നം (Item)", modifier = Modifier.weight(2.2f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("അളവ് (Qty)", modifier = Modifier.weight(1.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text("നിരക്ക്", modifier = Modifier.weight(1.3f), fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                    Text("തുക (₹)", modifier = Modifier.weight(1.4f), fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                    Spacer(modifier = Modifier.width(36.dp))
                }
            }

            // Empty state
            if (draftState.items.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "ബില്ലിൽ ഉൽപ്പന്നങ്ങൾ ഒന്നും ചേർത്തിട്ടില്ല.",
                                fontSize = 13.sp,
                                color = Color.DarkGray,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "ഉൽപ്പന്നങ്ങൾ ചേർക്കാൻ താഴെയുള്ള '+' ബട്ടൺ അമർത്തുക.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Line items list
            itemsIndexed(draftState.items) { index, item ->
                BillItemRow(
                    index = index,
                    item = item,
                    onUpdate = { kg, grm, price -> viewModel.updateBillItem(index, kg, grm, price) },
                    onDelete = { viewModel.removeItemFromBill(index) }
                )
            }

            // Section 3: Billing Summary & Figures / Words Calculation Card
            if (draftState.items.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("ബിൽ സമ്മറി (Bill Summary)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            SummaryRow("സബ്‌ടോട്ടൽ (Sub Total)", "₹ ${String.format(Locale.US, "%.2f", draftState.subTotal)}")

                            // Previous Balance Included Field
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("മുൻ കുടിശ്ശിക (Prev. Balance):", fontSize = 12.sp)
                                OutlinedTextField(
                                    value = if (draftState.previousBalanceAdded == 0.0) "" else draftState.previousBalanceAdded.toString(),
                                    onValueChange = { str -> viewModel.setPreviousBalance(str.toDoubleOrNull() ?: 0.0) },
                                    modifier = Modifier.width(110.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    placeholder = { Text("0") },
                                    prefix = { Text("₹ ") }
                                )
                            }

                            // Discount
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("ഡിസ്കൗണ്ട് (Discount):", fontSize = 12.sp)
                                OutlinedTextField(
                                    value = if (draftState.discount == 0.0) "" else draftState.discount.toString(),
                                    onValueChange = { str -> viewModel.setDiscount(str.toDoubleOrNull() ?: 0.0) },
                                    modifier = Modifier.width(110.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    placeholder = { Text("0") },
                                    prefix = { Text("₹ ") }
                                )
                            }

                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            // GRAND TOTAL IN FIGURES
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "ആകെ തുക (Grand Total):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    draftState.grandTotalInFigures,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // GRAND TOTAL IN WORDS (Malayalam & English)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        "തുക വാക്കുകളിൽ (Total in Words):",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        draftState.grandTotalInWordsMl,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        draftState.grandTotalInWordsEng,
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Payment received input
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("ഇപ്പോൾ നൽകിയ തുക (Paid):", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                OutlinedTextField(
                                    value = if (draftState.amountPaid == 0.0) "" else draftState.amountPaid.toString(),
                                    onValueChange = { str -> viewModel.setPaidAmount(str.toDoubleOrNull() ?: 0.0) },
                                    modifier = Modifier.width(130.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    placeholder = { Text("0") },
                                    prefix = { Text("₹ ") }
                                )
                            }

                            if (draftState.dueBalance > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("ബാക്കി നിൽക്കുന്ന കുടിശ്ശിക (Due):", fontSize = 12.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                                    Text("₹ ${String.format(Locale.US, "%.2f", draftState.dueBalance)}", fontSize = 12.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // SAVE & EXPORT PDF BUTTON
                            Button(
                                onClick = {
                                    isSaving = true
                                    viewModel.saveInvoiceAndExportPdf(context) { file ->
                                        isSaving = false
                                        if (file != null) {
                                            Toast.makeText(context, "ബിൽ സേവ് ചെയ്തു! PDF തയ്യാറാണ്.", Toast.LENGTH_SHORT).show()
                                            PdfGenerator.openOrSharePdf(context, file, share = false)
                                        } else {
                                            Toast.makeText(context, "ബിൽ സേവ് ചെയ്യാൻ കഴിഞ്ഞില്ല", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isSaving
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                } else {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ബിൽ സേവ് ചെയ്യുക & PDF രൂപപ്പെടുത്തുക", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }

    // Product Picker Dialog with Kg/Grm Inputs
    if (showProductSearchDialog) {
        ProductPickerWithKgGrmDialog(
            products = productsList,
            onDismiss = { showProductSearchDialog = false },
            onAdd = { product, kg, grm, price ->
                viewModel.addItemToBill(product, kg, grm, price)
                showProductSearchDialog = false
            }
        )
    }

    // Customer Selection Dialog
    if (showCustomerSelectDialog) {
        CustomerSelectDialog(
            customers = customersList,
            onDismiss = { showCustomerSelectDialog = false },
            onSelect = { cust ->
                viewModel.selectCustomerForBill(cust)
                showCustomerSelectDialog = false
            }
        )
    }
}

@Composable
fun BillItemRow(
    index: Int,
    item: DraftItem,
    onUpdate: (Double, Double, Double) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(2.2f)) {
                Text(item.product.nameMalayalam, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                if (item.product.nameEnglish.isNotEmpty()) {
                    Text(item.product.nameEnglish, fontSize = 10.sp, color = Color.Gray)
                }
            }

            val qtyDesc = if (item.product.unit == "kg") {
                val kg = item.quantityKg.toInt()
                val grm = item.quantityGrm.toInt()
                if (grm > 0) "$kg kg $grm g" else "$kg kg"
            } else {
                "${item.quantityKg.toInt()} ${item.product.unit}"
            }

            Text(qtyDesc, modifier = Modifier.weight(1.8f), fontSize = 12.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
            Text("₹ ${item.unitPrice}", modifier = Modifier.weight(1.3f), fontSize = 12.sp, textAlign = TextAlign.End)
            Text("₹ ${String.format(Locale.US, "%.2f", item.totalPrice)}", modifier = Modifier.weight(1.4f), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun SummaryRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 12.sp, color = Color.DarkGray)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ProductPickerWithKgGrmDialog(
    products: List<Product>,
    onDismiss: () -> Unit,
    onAdd: (Product, Double, Double, Double) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var kgText by remember { mutableStateOf("1") }
    var grmText by remember { mutableStateOf("0") }
    var priceText by remember { mutableStateOf("") }

    val filtered = products.filter {
        it.nameMalayalam.contains(searchQuery, ignoreCase = true) ||
                it.nameEnglish.contains(searchQuery, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ഉൽപ്പന്നം തിരഞ്ഞെടുക്കുക (Select Product)", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (selectedProduct == null) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("ഉൽപ്പന്നത്തിന്റെ പേര് ടൈപ്പ് ചെയ്യുക (Search)") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn(modifier = Modifier.height(260.dp)) {
                        items(filtered.size) { i ->
                            val prod = filtered[i]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable {
                                        selectedProduct = prod
                                        priceText = prod.pricePerUnit.toString()
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(prod.nameMalayalam, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        if (prod.nameEnglish.isNotEmpty()) {
                                            Text(prod.nameEnglish, fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                    Text("₹ ${prod.pricePerUnit} / ${prod.unit}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                } else {
                    val prod = selectedProduct!!
                    Text("തിരഞ്ഞെടുത്തത്: ${prod.nameMalayalam}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(10.dp))

                    if (prod.unit == "kg") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = kgText,
                                onValueChange = { kgText = it },
                                label = { Text("കിലോഗ്രാം (Kg)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = grmText,
                                onValueChange = { grmText = it },
                                label = { Text("ഗ്രാം (Grams)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = kgText,
                            onValueChange = { kgText = it },
                            label = { Text("എണ്ണം (Quantity)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("വില (Rate/Unit ₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        prefix = { Text("₹ ") }
                    )

                    val kgVal = kgText.toDoubleOrNull() ?: 0.0
                    val grmVal = grmText.toDoubleOrNull() ?: 0.0
                    val priceVal = priceText.toDoubleOrNull() ?: 0.0
                    val totalW = if (prod.unit == "kg") kgVal + (grmVal / 1000.0) else kgVal
                    val totalCalculated = totalW * priceVal

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("ആകെ തുക: ₹ ${String.format(Locale.US, "%.2f", totalCalculated)}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(onClick = { selectedProduct = null }) {
                        Text("വേറൊരു ഉൽപ്പന്നം മാറ്റുക")
                    }
                }
            }
        },
        confirmButton = {
            if (selectedProduct != null) {
                Button(onClick = {
                    val prod = selectedProduct!!
                    val kgVal = kgText.toDoubleOrNull() ?: 1.0
                    val grmVal = grmText.toDoubleOrNull() ?: 0.0
                    val priceVal = priceText.toDoubleOrNull() ?: prod.pricePerUnit
                    onAdd(prod, kgVal, grmVal, priceVal)
                }) {
                    Text("ചേർക്കുക (Add)")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("റദ്ദാക്കുക") }
        }
    )
}

@Composable
fun CustomerSelectDialog(
    customers: List<Customer>,
    onDismiss: () -> Unit,
    onSelect: (Customer?) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = customers.filter { it.name.contains(query, ignoreCase = true) || it.phone.contains(query) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ഉപഭോക്താവിനെ തിരഞ്ഞെടുക്കുക", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("പേര് / ഫോൺ തിരയുക") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(modifier = Modifier.height(240.dp)) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { onSelect(null) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        ) {
                            Text("റോക്കറ്റ് / ക്യാഷ് കസ്റ്റമർ (Cash Customer)", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                    items(filtered.size) { i ->
                        val cust = filtered[i]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { onSelect(cust) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(cust.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (cust.phone.isNotEmpty()) {
                                    Text("Ph: ${cust.phone}", fontSize = 11.sp, color = Color.Gray)
                                }
                                if (cust.previousBalance > 0) {
                                    Text("കുടിശ്ശിക: ₹ ${cust.previousBalance}", fontSize = 11.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("റദ്ദാക്കുക") }
        }
    )
}
