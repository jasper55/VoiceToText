package com.example.app.jasper.voicetotext.ui

import android.R
import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar




//@file:JvmName("KeyboardUtils")

fun Activity.hideSoftKeyboard() {
    currentFocus?.let {
        val inputMethodManager = ContextCompat.getSystemService(this, InputMethodManager::class.java)!!
        inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
    }
}

fun Activity.showSnackBarWithText(view: View,text: String) {
    Snackbar.make(view, text, Snackbar.LENGTH_LONG)
            .show()
}

fun Activity.showSnackBarWithCancel(view: View,text: String) {
    Snackbar.make(view, text, Snackbar.LENGTH_LONG)
            .setAction("CLOSE", object : View.OnClickListener {
                override fun onClick(v: View) { v.visibility = View.GONE }
            })
            .setActionTextColor(resources.getColor(R.color.holo_red_light))
            .show()
}