package com.denzygames.mobilepos

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import  androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCameraScanBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        startCameraScan()

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
                    viewBinding.textView.text = codeResult[0].rawValue.toString()
                }
            }
        )
        cameraController.bindToLifecycle(this)
        preview.controller = cameraController
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        barcodeScanner.close()
    }
}
