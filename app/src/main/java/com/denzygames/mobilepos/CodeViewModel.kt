package com.denzygames.mobilepos

import android.graphics.Rect
import com.google.mlkit.vision.barcode.common.Barcode

class CodeViewModel(barcode: Barcode) {
    var boundingRect: Rect = barcode.boundingBox!!
    var content: String = ""

    init {
        when(barcode.valueType)
        {
            Barcode.TYPE_UNKNOWN->
            {
                content = "Unknown Code"
            }else ->{
                content = barcode.rawValue.toString()
            }
        }
    }
}