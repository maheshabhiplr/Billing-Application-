package com.example.util

import java.text.DecimalFormat

object IndianCurrencyUtils {

    private val units = arrayOf("", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
        "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen")
    private val tens = arrayOf("", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety")

    /**
     * Formats double to Indian Currency String in Figures. E.g. ₹ 1,25,450.00
     */
    fun formatToIndianRupees(amount: Double): String {
        val formatter = DecimalFormat("#,##,##0.00")
        return "₹ " + formatter.format(amount)
    }

    /**
     * Converts amount to Words in Indian English format.
     * E.g. 1250.50 -> "Rupees One Thousand Two Hundred Fifty and Fifty Paise Only"
     */
    fun convertToIndianRupeesInWords(amount: Double): String {
        if (amount <= 0.0) return "Rupees Zero Only"

        val rupees = amount.toLong()
        val paise = Math.round((amount - rupees) * 100)

        val rupeesInWords = convertNumberToWords(rupees)
        val paiseInWords = if (paise > 0) convertNumberToWords(paise) else ""

        val sb = StringBuilder("Rupees ")
        if (rupeesInWords.isNotEmpty()) {
            sb.append(rupeesInWords)
        } else {
            sb.append("Zero")
        }

        if (paise > 0) {
            sb.append(" and ").append(paiseInWords).append(" Paise")
        }
        sb.append(" Only")

        return sb.toString()
    }

    /**
     * Malayalam currency in words representation.
     * E.g., 1250 -> "രൂപ ആയിരത്തി ഇരുന്നൂറ്റി അൻപത് മാത്രം"
     */
    fun convertToMalayalamRupeesInWords(amount: Double): String {
        val rupees = amount.toLong()
        if (rupees <= 0) return "രൂപ പൂജ്യം മാത്രം"

        val mlWords = convertNumberToMalayalamWords(rupees)
        return "രൂപ $mlWords മാത്രം"
    }

    private fun convertNumberToWords(n: Long): String {
        if (n == 0L) return ""
        if (n < 20) return units[n.toInt()]
        if (n < 100) return tens[(n / 10).toInt()] + (if (n % 10 != 0L) " " + units[(n % 10).toInt()] else "")
        if (n < 1000) return units[(n / 100).toInt()] + " Hundred" + (if (n % 100 != 0L) " " + convertNumberToWords(n % 100) else "")
        if (n < 100000) return convertNumberToWords(n / 1000) + " Thousand" + (if (n % 1000 != 0L) " " + convertNumberToWords(n % 1000) else "")
        if (n < 10000000) return convertNumberToWords(n / 100000) + " Lakh" + (if (n % 100000 != 0L) " " + convertNumberToWords(n % 100000) else "")
        return convertNumberToWords(n / 10000000) + " Crore" + (if (n % 10000000 != 0L) " " + convertNumberToWords(n % 10000000) else "")
    }

    private fun convertNumberToMalayalamWords(n: Long): String {
        if (n == 0L) return ""
        val unitsMl = arrayOf("", "ഒന്ന്", "രണ്ട്", "മൂന്ന്", "നാല്", "അഞ്ച്", "ആറ്", "ഏഴ്", "എട്ട്", "ഒൻപത്", "പത്ത്",
            "പതിനൊന്ന്", "പന്ത്രണ്ട്", "പതിമൂന്ന്", "പതിനാല്", "പതിനഞ്ച്", "പതിനാറ്", "പതിനേഴ്", "പതിനെട്ട്", "പത്തൊൻപത്")
        val tensMl = arrayOf("", "", "ഇരുപത്", "മുപ്പത്", "നാൽപ്പത്", "അൻപത്", "അറുപത്", "എഴുപത്", "എൺപത്", "തൊണ്ണൂറ്")

        if (n < 20) return unitsMl[n.toInt()]
        if (n < 100) return tensMl[(n / 10).toInt()] + (if (n % 10 != 0L) " " + unitsMl[(n % 10).toInt()] else "")
        if (n < 1000) {
            val h = (n / 100).toInt()
            val rem = n % 100
            val hText = when (h) {
                1 -> if (rem > 0) "നൂറ്റി" else "നൂറ്"
                2 -> if (rem > 0) "ഇരുന്നൂറ്റി" else "ഇരുന്നൂറ്"
                3 -> if (rem > 0) "മൂന്നൂറ്റി" else "മൂന്നൂറ്"
                4 -> if (rem > 0) "നാനൂറ്റി" else "നാനൂറ്"
                5 -> if (rem > 0) "അഞ്ഞൂറ്റി" else "അഞ്ഞൂറ്"
                6 -> if (rem > 0) "അറന്നൂറ്റി" else "അറന്നൂറ്"
                7 -> if (rem > 0) "എഴുന്നൂറ്റി" else "എഴുന്നൂറ്"
                8 -> if (rem > 0) "എണ്ണൂറ്റി" else "എണ്ണൂറ്"
                9 -> if (rem > 0) "തൊള്ളായിരത്തി" else "തൊള്ളായിരം"
                else -> ""
            }
            return hText + (if (rem > 0) " " + convertNumberToMalayalamWords(rem) else "")
        }
        if (n < 100000) {
            val th = n / 1000
            val rem = n % 1000
            val thText = if (rem > 0) "${convertNumberToMalayalamWords(th)} ആയിരത്തി" else "${convertNumberToMalayalamWords(th)} ആയിരം"
            return thText + (if (rem > 0) " " + convertNumberToMalayalamWords(rem) else "")
        }
        if (n < 10000000) {
            val lk = n / 100000
            val rem = n % 100000
            val lkText = if (rem > 0) "${convertNumberToMalayalamWords(lk)} ലക്ഷത്തി" else "${convertNumberToMalayalamWords(lk)} ലക്ഷം"
            return lkText + (if (rem > 0) " " + convertNumberToMalayalamWords(rem) else "")
        }
        val cr = n / 10000000
        val rem = n % 10000000
        val crText = if (rem > 0) "${convertNumberToMalayalamWords(cr)} കോടി" else "${convertNumberToMalayalamWords(cr)} കോടി"
        return crText + (if (rem > 0) " " + convertNumberToMalayalamWords(rem) else "")
    }
}
