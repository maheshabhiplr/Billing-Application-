package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.example.data.InvoiceWithItems
import com.example.model.CompanyDetails
import com.example.model.Customer
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    fun generateInvoicePdf(
        context: Context,
        invoiceWithItems: InvoiceWithItems,
        companyDetails: CompanyDetails?
    ): File? {
        val invoice = invoiceWithItems.invoice
        val items = invoiceWithItems.items

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size (595x842 pt)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        val titlePaint = Paint()
        val headerPaint = Paint()
        val boldPaint = Paint()

        // Page Background
        canvas.drawColor(Color.WHITE)

        var y = 40f

        // Company Header Banner
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 18f
        titlePaint.color = Color.rgb(0, 108, 76) // Emerald Green

        val compName = companyDetails?.companyNameMalayalam ?: "എസ്.കെ. ട്രേഡേഴ്സ്"
        val compEng = companyDetails?.companyNameEnglish ?: "SK Traders & Provisions"
        canvas.drawText(compName, 30f, y, titlePaint)
        y += 20f

        paint.textSize = 11f
        paint.color = Color.DKGRAY
        canvas.drawText(compEng, 30f, y, paint)
        y += 16f

        val tagline = companyDetails?.tagline ?: "മൊത്ത-ചില്ലറ വ്യാപാരി (Wholesale & Retail)"
        canvas.drawText(tagline, 30f, y, paint)
        y += 16f

        val address = companyDetails?.address ?: "മെയിൻ റോഡ്, കേരളം"
        val phone = "Phone: " + (companyDetails?.phone ?: "")
        val gstin = if (!companyDetails?.gstin.isNull_or_empty()) "GSTIN: " + companyDetails?.gstin else ""
        canvas.drawText("$address | $phone", 30f, y, paint)
        y += 16f
        if (gstin.isNotEmpty()) {
            canvas.drawText(gstin, 30f, y, paint)
            y += 16f
        }

        // Header Line
        paint.strokeWidth = 2f
        paint.color = Color.rgb(0, 108, 76)
        canvas.drawLine(30f, y, 565f, y, paint)
        y += 20f

        // Invoice Title & Date Box
        headerPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        headerPaint.textSize = 14f
        headerPaint.color = Color.BLACK
        canvas.drawText("ക്യാഷ് ബിൽ / INVOICE", 30f, y, headerPaint)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(invoice.dateTimestamp))
        paint.textSize = 10f
        canvas.drawText("ബിൽ നം: ${invoice.invoiceNumber}", 380f, y - 4f, paint)
        canvas.drawText("തീയതി: $dateStr", 380f, y + 12f, paint)
        y += 30f

        // Customer Details Box
        paint.color = Color.rgb(240, 245, 242)
        canvas.drawRect(30f, y, 565f, y + 50f, paint)

        boldPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        boldPaint.textSize = 11f
        boldPaint.color = Color.BLACK
        canvas.drawText("ഉപഭോക്താവ് (Customer): ${invoice.customerName}", 40f, y + 20f, boldPaint)

        paint.color = Color.BLACK
        if (invoice.customerPhone.isNotEmpty()) {
            canvas.drawText("ഫോൺ: ${invoice.customerPhone}", 40f, y + 38f, paint)
        }
        if (invoice.previousBalanceAdded > 0) {
            canvas.drawText("മുൻ കുടിശ്ശിക (Prev. Balance): ₹ ${invoice.previousBalanceAdded}", 350f, y + 20f, boldPaint)
        }
        y += 65f

        // Items Table Header
        paint.color = Color.rgb(0, 108, 76)
        canvas.drawRect(30f, y, 565f, y + 24f, paint)

        headerPaint.color = Color.WHITE
        headerPaint.textSize = 10f
        canvas.drawText("S.No", 35f, y + 16f, headerPaint)
        canvas.drawText("ഉൽപ്പന്നം (Item)", 75f, y + 16f, headerPaint)
        canvas.drawText("അളവ് (Qty)", 280f, y + 16f, headerPaint)
        canvas.drawText("നിരക്ക് (Rate/Kg)", 390f, y + 16f, headerPaint)
        canvas.drawText("തുക (Total ₹)", 490f, y + 16f, headerPaint)
        y += 24f

        // Table Rows
        paint.color = Color.BLACK
        paint.textSize = 10f

        var slNo = 1
        for (item in items) {
            // Alternating row background
            if (slNo % 2 == 0) {
                val bgPaint = Paint().apply { color = Color.rgb(248, 249, 250) }
                canvas.drawRect(30f, y, 565f, y + 20f, bgPaint)
            }

            val qtyText = if (item.unit == "kg") {
                val kg = item.quantityKg.toInt()
                val grm = item.quantityGrm.toInt()
                if (grm > 0) "$kg kg $grm grm" else "$kg kg"
            } else {
                "${item.quantityKg.toInt()} ${item.unit}"
            }

            canvas.drawText("$slNo", 38f, y + 14f, paint)
            canvas.drawText(item.productName, 75f, y + 14f, paint)
            canvas.drawText(qtyText, 280f, y + 14f, paint)
            canvas.drawText("₹ ${item.unitPrice}", 390f, y + 14f, paint)
            canvas.drawText("₹ ${String.format(Locale.US, "%.2f", item.totalPrice)}", 490f, y + 14f, paint)

            y += 20f
            slNo++
        }

        // Table Bottom Line
        paint.color = Color.GRAY
        paint.strokeWidth = 1f
        canvas.drawLine(30f, y, 565f, y, paint)
        y += 15f

        // Item Count & Total Weight Summary
        boldPaint.textSize = 10f
        canvas.drawText("ആകെ ഇനങ്ങൾ (Total Items): ${invoice.totalItemCount}", 30f, y, boldPaint)
        if (invoice.totalWeightKg > 0) {
            canvas.drawText("ആകെ ഭാരം (Total Weight): ${String.format(Locale.US, "%.3f", invoice.totalWeightKg)} kg", 200f, y, boldPaint)
        }
        y += 20f

        // Summary Totals Calculation Box (Right Aligned)
        val boxTop = y
        paint.color = Color.rgb(245, 247, 246)
        canvas.drawRect(300f, boxTop, 565f, boxTop + 100f, paint)

        paint.color = Color.BLACK
        var boxY = boxTop + 20f
        canvas.drawText("സബ്‌ടോട്ടൽ (Sub Total):", 310f, boxY, paint)
        canvas.drawText("₹ ${String.format(Locale.US, "%.2f", invoice.subTotal)}", 480f, boxY, paint)
        boxY += 18f

        if (invoice.previousBalanceAdded > 0) {
            canvas.drawText("മുൻ കുടിശ്ശിക (Prev. Balance):", 310f, boxY, paint)
            canvas.drawText("₹ ${String.format(Locale.US, "%.2f", invoice.previousBalanceAdded)}", 480f, boxY, paint)
            boxY += 18f
        }

        if (invoice.discount > 0) {
            canvas.drawText("ഡിസ്കൗണ്ട് (Discount):", 310f, boxY, paint)
            canvas.drawText("- ₹ ${String.format(Locale.US, "%.2f", invoice.discount)}", 480f, boxY, paint)
            boxY += 18f
        }

        // Grand Total Row
        boldPaint.textSize = 12f
        boldPaint.color = Color.rgb(0, 108, 76)
        canvas.drawText("ആകെ തുക (Grand Total):", 310f, boxY + 5f, boldPaint)
        canvas.drawText("₹ ${String.format(Locale.US, "%.2f", invoice.grandTotal)}", 480f, boxY + 5f, boldPaint)

        y = boxTop + 115f

        // Total in Words Box
        paint.color = Color.rgb(235, 245, 240)
        canvas.drawRect(30f, y, 565f, y + 45f, paint)

        val wordsEng = IndianCurrencyUtils.convertToIndianRupeesInWords(invoice.grandTotal)
        val wordsMl = IndianCurrencyUtils.convertToMalayalamRupeesInWords(invoice.grandTotal)

        boldPaint.textSize = 10f
        boldPaint.color = Color.rgb(0, 80, 50)
        canvas.drawText("തുക വാക്കുകളിൽ (Words): $wordsMl", 40f, y + 18f, boldPaint)

        paint.textSize = 9f
        paint.color = Color.DKGRAY
        canvas.drawText("In English: $wordsEng", 40f, y + 34f, paint)
        y += 65f

        // Footer / Terms
        val upi = companyDetails?.upiId
        if (!upi.isNull_or_empty()) {
            boldPaint.textSize = 10f
            boldPaint.color = Color.BLACK
            canvas.drawText("GPay / UPI വഴി പണം അടയ്ക്കാം: $upi", 30f, y, boldPaint)
            y += 20f
        }

        paint.textSize = 11f
        paint.color = Color.rgb(0, 108, 76)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
        canvas.drawText("നന്ദി! വീണ്ടും വരിക! (Thank You! Visit Again!)", 180f, y + 10f, paint)

        pdfDocument.finishPage(page)

        // Save PDF to file
        return try {
            val fileDir = File(context.cacheDir, "invoices")
            if (!fileDir.exists()) fileDir.mkdirs()
            val pdfFile = File(fileDir, "Invoice_${invoice.invoiceNumber}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()

    fun openOrSharePdf(context: Context, file: File, share: Boolean = false) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(if (share) Intent.ACTION_SEND else Intent.ACTION_VIEW).apply {
            if (share) {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "കട ബിൽ (Invoice)")
            } else {
                setDataAndType(uri, "application/pdf")
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, if (share) "ബിൽ ഷെയർ ചെയ്യുക" else "ബിൽ കാണുക")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
