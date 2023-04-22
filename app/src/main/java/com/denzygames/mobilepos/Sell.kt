package com.denzygames.mobilepos

import androidx.appcompat.app.AppCompatActivity
import com.denzygames.mobilepos.databinding.ActivitySellBinding
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.room.*

@Entity
data class Sale (
    @PrimaryKey var saleID: Int?,
    var saleTotal: Int,
    var salePaid: Int?,
    var saleChange: Int?,
    var saleDate: Int?, /* UNIX timestamp */
)

@Entity
data class SaleDetails(
    @PrimaryKey(autoGenerate = true) val SaleDetailID: Int,
    var saleIDParent: Int?, /* foreign key to saleID */
    var productID: Int,
    var saleCount: Int
)

// defines a one-to-many r/ship. a sale can have many details (products)
data class SaleWithDetails(
    @Embedded var sale: Sale,
    @Relation(
        parentColumn = "saleID",
        entityColumn = "saleIDParent"
    )
    var sales: MutableList<SaleDetails>
)

@Dao
interface SaleDao
{
    @Transaction
    @Query("SELECT * FROM Sale")
    fun getSalesWithDetails(): List<SaleWithDetails>

    @Transaction
    @Query("SELECT * FROM Sale WHERE saleID= (:id)")
    fun getSaleWithDetailsByID(id: Int?): SaleWithDetails

    @Insert
    fun insertSale(sale: Sale)

    @Insert
    fun insertDetails(details: List<SaleDetails>)

    @Transaction
    fun insertSaleWithDetails(sale: Sale, details: List<SaleDetails>)
    {
        insertSale(sale)
        insertDetails(details)
    }
}

class Sell : AppCompatActivity() {
    private lateinit var viewBinding: ActivitySellBinding
    private lateinit var currentSale: SaleWithDetails

    fun initCurrentSaleAndClearUI()
    {
        val db = MobilePOSDb.getDb(applicationContext)
        currentSale = SaleWithDetails(
            Sale(db.saleDao().getSalesWithDetails().size + 1, 0, 0, 0, 0),
            mutableListOf()
        )
        viewBinding.etCash.text.clear()
        viewBinding.tvItems.text = "Products:\n"
    }

    fun updateCurrentSaleAndFillUI()
    {
        var outstr = "Products:\n"
        val db = MobilePOSDb.getDb(applicationContext)
        var tot = 0
        var i = 1
        for (sale in currentSale.sales)
        {
            val product = db.productDao().getProductByID(sale.productID)
            val subtot = product.productPrice * sale.saleCount
            tot += subtot
            //str = "<Product>, Items: <n>, Price: <n>\nTotal: <n>"
            outstr += "$i: ${product.productName}, Items: ${sale.saleCount}, Price: ${product.productPrice}, Sub-Total: ${subtot}\n"
            i++
        }
        outstr += "\nTotal: ${tot}"
        viewBinding.tvItems.text = outstr
        currentSale.sale.saleTotal = tot
    }

    val startScanContract = registerForActivityResult(ScanContract()) { res ->
        if(res != null){
            runOnUiThread{
                val db = MobilePOSDb.getDb(applicationContext)
                val product = db.productDao().getProductByCode(res)
                if(product != null)
                {
                    var found = false
                    var insuf = false
                    for (sale in currentSale.sales)
                    {
                        if(sale.productID == product.id)
                        {
                            if(product.productStock >= sale.saleCount + 1)
                            {
                                sale.saleCount++
                                found = true
                                break
                            }else
                            {
                                insuf = true
                                Toast.makeText(this, "Insufficient stock to sell more ${product.productName}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    if(product.productStock <= 0 )
                    {
                        Toast.makeText(this, "Insufficient stock to sell ${product.productName}", Toast.LENGTH_LONG).show()
                    }
                    if(!found && !insuf && product.productStock > 0)
                    {
                        currentSale.sales.add(SaleDetails(0, currentSale.sale.saleID, product.id, 1, ))
                    }

                    updateCurrentSaleAndFillUI()
                }else
                {
                    Toast.makeText(this, "$res has not been saved!", Toast.LENGTH_LONG).show()
                }
            }
        }
        else
            Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivitySellBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        val db = MobilePOSDb.getDb(applicationContext)

        initCurrentSaleAndClearUI()

        viewBinding.btAdd.setOnClickListener {
            startScanContract.launch(Unit)
        }

        viewBinding.btDone.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Finishing Sale...")
            builder.setIcon(android.R.drawable.ic_dialog_alert)
            builder.setPositiveButton("OK"){_, _ -> }

            var cash = 0
            try {
                 cash =  viewBinding.etCash.text.toString().toInt()
            }catch (e: Exception)
            {
                builder.setMessage("Invalid cash entered!")
            }

            if(cash < currentSale.sale.saleTotal)
                builder.setMessage("Cash is LESS than TOTAL!!!")
            else
            {
                currentSale.sale.saleChange = cash - currentSale.sale.saleTotal
                currentSale.sale.salePaid = cash
                currentSale.sale.saleDate = (System.currentTimeMillis() / 1000).toInt()

                db.saleDao().insertSaleWithDetails(currentSale.sale, currentSale.sales)

                //subtract stocks
                for (sale in currentSale.sales)
                {
                    val product = db.productDao().getProductByID(sale.productID)
                    product.productStock -= sale.saleCount
                    db.productDao().updateProduct(product)
                }

                builder.setIcon(android.R.drawable.ic_menu_save)
                builder.setMessage("Sale complete! Change is ${currentSale.sale.saleChange}")
                initCurrentSaleAndClearUI()
            }

            builder.create().show()
        }
    }
}