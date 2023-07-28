package com.unero.pinar.utils

import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.unero.pinar.R
import com.unero.pinar.data.model.Building

object UIHelper {

    fun buildingName(building: Building, context: Context): String {
        return if (building.type != "academy") {
            building.name
        } else {
            context.getString(R.string.building_name, building.id)
        }
    }

    fun goToFragment(view: View, action: NavDirections) {
        view.findNavController().navigate(action)
    }

    fun popBackFragment(view: View) {
        view.findNavController().popBackStack()
    }

    fun showSnackbar(view: View, msg: String) {
        Snackbar.make(
            view,
            msg,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    fun showToast(context: Context, msg: String) {
        Toast.makeText(
            context,
            msg,
            Toast.LENGTH_SHORT
        ).show()
    }

    fun alertBuilder(context: Context, title: String, msg: String): MaterialAlertDialogBuilder =
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(msg)

    fun arrayAdapterBuilder(context: Context, resource: Array<String>): ArrayAdapter<String> =
        ArrayAdapter(context, R.layout.item_dropdown, resource)
}