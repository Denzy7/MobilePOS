package com.denzygames.mobilepos

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CameraScan : AppCompatActivity() {

    companion object
    {
        private val NEEDED_PERMISSIONS =
            mutableListOf(
                android.Manifest.permission.CAMERA
            ).toTypedArray()
        private  const val REQUEST_CODE = 0x07
    }

    fun needCameraPermissionDialog()
    {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.activity_camera_scan_camera_permdialog_title)
        builder.setMessage(R.string.activity_camera_scan_camera_permdialog_message)
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes"){ _, _ ->
            ActivityCompat.requestPermissions(this,
                NEEDED_PERMISSIONS, REQUEST_CODE)
        }
        builder.setNegativeButton("No"){_, _ -> finish()}
        builder.setNeutralButton("Cancel"){_, _ -> finish()}

        val dialog = builder.create()
        dialog.setCancelable(true)
        dialog.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_scan)

        if(permissionCameraGranted())
        {
            startCameraScan()
        }else
        {
            needCameraPermissionDialog()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode)
        {
            REQUEST_CODE ->
            {
                if((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                {
                    /* granted */
                    startCameraScan()
                }else
                {
                    /* not granted 😢 */
                    needCameraPermissionDialog()
                    finish()
                }
                return
            }
        }
    }

    fun permissionCameraGranted(): Boolean {
        return ContextCompat.checkSelfPermission(baseContext, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    fun startCameraScan()
    {

    }
}
