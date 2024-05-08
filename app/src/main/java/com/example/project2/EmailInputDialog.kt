package com.example.project2

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText

class EmailInputDialog(private val context: Context, private val onEmailEntered: (String) -> Unit) {

    fun showInputDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Enter Email Address")

        val input = EditText(context)
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val emailAddress = input.text.toString().trim()
            onEmailEntered(emailAddress)
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }
}
