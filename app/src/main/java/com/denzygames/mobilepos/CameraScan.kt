package com.denzygames.mobilepos

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AlertDialog
import  androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.mlkit.vision.MlKitAnalyzer
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

class ScanContract : ActivityResultContract<Unit, String?>()
{
    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(context, CameraScan::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        val data = intent?.getStringExtra("ScanResult")
        return if (resultCode == Activity.RESULT_OK && data != null) data
        else null
    }
}

class CameraScan : AppCompatActivity() {

    private lateinit var viewBinding: ActivityCameraScanBinding
    private lateinit var barcodeScanner: BarcodeScanner
    private lateinit var cameraExecutor: ExecutorService
    private var retCode: String = ""
    private var caminit = false

    companion object
    {
        private val NEEDED_PERMISSIONS =
            mutableListOf(
                android.Manifest.permission.CAMERA
            ).toTypedArray()
        private  const val REQUEST_CODE = 0x07
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCameraScanBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        if(permissionCameraGranted())
            startCameraScan()
        else
            needCameraPermissionDialog()


        viewBinding.button.setOnClickListener {
            if(retCode != "")
            {
                setResult(Activity.RESULT_OK, Intent().apply {
                    putExtra("ScanResult", retCode)
                })
                finish()
            }
        }

        viewBinding.textView.setOnClickListener{
            if(retCode != "")
            {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("", retCode))
                //toast for <= A12
                if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                    Toast.makeText(this, "Copied \"${retCode}\" to clipboard", Toast.LENGTH_SHORT).show()
            }
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    fun startCameraScan()
    {
        var cameraController = LifecycleCameraController(baseContext)
        val preview: PreviewView = viewBinding.previewView
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()

        barcodeScanner = BarcodeScanning.getClient(options)
        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(this),
            MlKitAnalyzer(
                listOf(barcodeScanner),
                COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(this))
            { result: MlKitAnalyzer.Result? ->
                val codeResult = result?.getValue(barcodeScanner)
                if(codeResult == null || codeResult.size == 0 || codeResult.first() == null)
                {
                    preview.overlay.clear()
                    return@MlKitAnalyzer
                }

                val model = CodeViewModel(codeResult[0])
                val drawable = CodeDrawable(model)
                preview.overlay.clear()
                preview.overlay.add(drawable)

                runOnUiThread {
                    val res = codeResult[0].rawValue.toString()
                    val p: Product? = MobilePOSDb.getDb(applicationContext).productDao().getProductByCode(res)
                    val str: String
                    if(p == null)
                        str = "Unsaved: ${res}"
                    else
                        str = "${p.productName}: $res"

                    viewBinding.textView.text = str
                    retCode = res
                }
            }
        )
        cameraController.bindToLifecycle(this)
        preview.controller = cameraController
        caminit = true
    }

    fun needCameraPermissionDialog()
    {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.activity_camera_scan_camera_permdialog_title)
        builder.setMessage(R.string.activity_camera_scan_camera_permdialog_message)
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes"){ _, _ ->
            ActivityCompat.requestPermissions(this,
                CameraScan.NEEDED_PERMISSIONS, CameraScan.REQUEST_CODE
            )
        }
        builder.setNegativeButton("No"){_, _ ->
            finish()
        }
        builder.setNeutralButton("Cancel"){_, _ ->
            finish()
        }

        val dialog = builder.create()
        dialog.setCancelable(true)
        dialog.show()
    }

    fun permissionCameraGranted(): Boolean {
        return ContextCompat.checkSelfPermission(baseContext, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
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
                    Toast.makeText(this, "Camera permission has been explicitly denied! Manually grant it in app info",Toast.LENGTH_LONG).show()
                    finish()
                }
                return
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(caminit)
        {
            cameraExecutor.shutdown()
            barcodeScanner.close()
        }
    }
}
