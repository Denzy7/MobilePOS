package com.denzygames.mobilepos

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import androidx.room.*
import com.denzygames.mobilepos.databinding.ActivityProductsBinding

/* product entity */
@Entity
data class Product(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "ProductName") val productName: String?,
    @ColumnInfo(name = "ProductPrice") val productPrice: Int?,
    @ColumnInfo(name = "ProductStock") val productStock: Int?,
    @ColumnInfo(name = "ProductCode") val productCode: String?,
)

/* DB access object that abstacts low level queries */
@Dao
interface ProductDao{
    @Query("SELECT * FROM Product")
    fun getProducts(): List<Product>

    @Query("SELECT * FROM Product WHERE id= (:id)")
    fun getProductByID(id: Int): Product

    @Insert
    fun inserProduct(product: Product)

    @Delete
    fun delele(product: Product)
}

class Products : AppCompatActivity() {
    private lateinit var viewBinding: ActivityProductsBinding
    private var currentID: Int = 1

    fun updateUIWithCurrentProduct(){
        val products = MobilePOSDb.getDb(applicationContext).productDao()
        val product = products.getProductByID(currentID)
        viewBinding.etName.text = Editable.Factory.getInstance().newEditable(product.productName)
        viewBinding.etPrice.text = Editable.Factory.getInstance().newEditable(product.productPrice.toString())
        viewBinding.etStock.text = Editable.Factory.getInstance().newEditable(product.productStock.toString())
        viewBinding.tvCodeStr.text = product.productCode
        viewBinding.tvCount.text = "${currentID} of ${products.getProducts().size}"

        viewBinding.btPrevious.isEnabled = currentID != 1
        viewBinding.btNext.isEnabled = currentID != products.getProducts().size
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val db = MobilePOSDb.getDb(applicationContext)

        if(db.productDao().getProducts().size.toInt() > 0)
            updateUIWithCurrentProduct()

        viewBinding.btAdd.setOnClickListener{
            val product = Product(
                db.productDao().getProducts().size.toInt() + 1,
                viewBinding.etName.text.toString(),
                viewBinding.etPrice.text.toString().toInt(),
                viewBinding.etStock.text.toString().toInt(),
                viewBinding.tvCodeStr.text.toString())
            db.productDao().inserProduct(product)
            Toast.makeText(this, "productName:${product.productName}, " +
                    "productPrice:${product.productPrice}, " +
                    "productStock:${product.productStock}, " +
                    "productCode:${product.productCode}", Toast.LENGTH_LONG).show()
            currentID = db.productDao().getProducts().size
            updateUIWithCurrentProduct()
        }

        viewBinding.btNext.setOnClickListener{
            if(db.productDao().getProducts().size.toInt() > 0){
                currentID++
                currentID = currentID.coerceIn(1, db.productDao().getProducts().size.toInt())
                updateUIWithCurrentProduct()
            }else
            {
                Toast.makeText(this,"No products saved!", Toast.LENGTH_SHORT).show()
            }

        }

        viewBinding.btPrevious.setOnClickListener{
            if(db.productDao().getProducts().size.toInt() > 0){
                currentID--
                currentID = currentID.coerceIn(1, db.productDao().getProducts().size.toInt())
                updateUIWithCurrentProduct()
            }else
            {
                Toast.makeText(this,"No products saved!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}