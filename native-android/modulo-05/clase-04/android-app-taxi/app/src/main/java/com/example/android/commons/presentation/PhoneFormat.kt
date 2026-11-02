package com.example.android.commons.presentation

fun formatPeruPhone(digits: String): String =
    if (digits.length == 9) "+51 ${digits.substring(0, 3)} ${digits.substring(3, 6)} ${digits.substring(6)}"
    else "+51 $digits"
