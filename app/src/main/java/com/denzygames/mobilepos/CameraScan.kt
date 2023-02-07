package com.denzygames.mobilepos

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.camera.camera2.internal.annotation.CameraExecutor
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.denzygames.mobilepos.databinding.ActivityCameraScanBinding
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraScan : AppCompatActivity() {

    private lateinit var viewBinding: ActivityCameraScanBinding
    private lateinit var barcodeScanner: BarcodeScanner
    private lateinit var cameraExecutor: ExecutorService
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
        viewBinding = ActivityCameraScanBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        if(permissionCameraGranted())
        {
            startCameraScan()
        }else
        {
            needCameraPermissionDialog()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
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
        var cameraController = LifecycleCameraController(baseContext)
        val preview: PreviewView = viewBinding.previewView
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()

        barcodeScanner = BarcodeScanning.getClient(options)
//        cameraController.setImageAnalysisAnalyzer(
//            ContextCompat.getMainExecutor(this),
//            ...
//        )
        cameraController.bindToLifecycle(this)
        preview.controller = cameraController
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        barcodeScanner.close()
    }
}
