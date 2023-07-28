package com.unero.pinar.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import com.vmadalin.easypermissions.EasyPermissions

object PermissionHelper {

    private const val LOCATION_CODE_REQUEST = 13
    private const val CAMERA_CODE_REQUEST = 14

    fun requestLocationPermission(activity: Activity) {
        EasyPermissions.requestPermissions(
            activity,
            "Application need to use this permission to known user location",
            LOCATION_CODE_REQUEST,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    fun hasLocationPermission(context: Context) =
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    fun requestCameraPermission(activity: Activity) {
        EasyPermissions.requestPermissions(
            activity,
            "Application need to use this permission to scan text",
            CAMERA_CODE_REQUEST,
            Manifest.permission.CAMERA
        )
    }

    fun hasCameraPermission(context: Context) =
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.CAMERA
        )
}