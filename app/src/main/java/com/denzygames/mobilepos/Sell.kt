package com.denzygames.mobilepos

import androidx.appcompat.app.AppCompatActivity
import com.denzygames.mobilepos.databinding.ActivitySellBinding
import android.os.Bundle
import androidx.room.*

@Entity
data class Sale (
    @PrimaryKey var saleID: Int?,
    var saleTotal: Int?,
    var salePaid: Int?,
    var saleChange: Int?,
    var saleDate: Int?, /* UNIX timestamp */
)

@Entity
data class SaleDetails(
    @PrimaryKey var SaleDetailID: Int?,
    var saleIDParent: Int?, /* foreign key to saleID */
    var productName: Int?, /* look up field: Product table */
    var saleCount: Int?
)

// defines a one-to-many r/ship. a sale can have many details (products)
data class SaleWithDetails(
    @Embedded var sale: Sale,
    @Relation(
        parentColumn = "saleID",
        entityColumn = "saleIDParent"
    )
    var sales: List<SaleDetails>
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivitySellBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val db = MobilePOSDb.getDb(applicationContext)

        //parse str = "<Product>, Items: <n>, Price: <n>\nTotal: <n>"
    }
}