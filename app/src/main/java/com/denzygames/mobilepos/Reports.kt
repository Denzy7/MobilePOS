package com.denzygames.mobilepos

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.denzygames.mobilepos.databinding.ActivityReportsBinding
import java.text.SimpleDateFormat
import java.util.*

class Reports : AppCompatActivity() {
    private lateinit var viewBinding: ActivityReportsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val db = MobilePOSDb.getDb(applicationContext)
        val deets = db.saleDao().getSalesWithDetails()
        var outstr = "REPORT (${deets.size} items)\n"
        for(deet in deets)
        {
            //format a date str
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            sdf.timeZone = TimeZone.getDefault()
            val datestr = sdf.format(Date(deet.sale.saleDate.toLong() * 1000))

            outstr += "=*=*=*=*=*=\nSale ID: ${deet.sale.saleID}, Sale Date: ${datestr}\n"
            outstr += "Products:\n"
            var i = 1

            for(sale in deet.sales)
            {
                val prod = db.productDao().getProductByID(sale.productID)
                val subtot = prod.productPrice * sale.saleCount
                outstr += "$i: ${prod.productName}, Count: ${sale.saleCount}, Price: ${prod.productPrice}, Sub: $subtot\n"
                i++
            }
            outstr += "TOTAL: ${deet.sale.saleTotal}\n=*=*=*=*=*=\n\n"
        }
        viewBinding.tvReport.text = outstr
    }
}