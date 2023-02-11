package com.denzygames.mobilepos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.*
import com.denzygames.mobilepos.databinding.ActivityProductsBinding

/* product entity */
@Entity
data class Product(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "ProductName") var productName: String?,
    @ColumnInfo(name = "ProductPrice") var productPrice: Int?,
    @ColumnInfo(name = "ProductStock") var productStock: Int?,
    @ColumnInfo(name = "ProductCode") var productCode: String?,
)

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

/* DB access object that abstacts low level queries */
@Dao
interface ProductDao{
    @Query("SELECT * FROM Product")
    fun getProducts(): List<Product>

    @Query("SELECT * FROM Product WHERE id= (:id)")
    fun getProductByID(id: Int): Product

    @Insert
    fun insertProduct(product: Product)

    @Update
    fun updateProduct(product: Product)

    @Delete
    fun delele(product: Product)
}

class Products : AppCompatActivity() {

    companion object
    {
        private val NEEDED_PERMISSIONS =
            mutableListOf(
                android.Manifest.permission.CAMERA
            ).toTypedArray()
        private  const val REQUEST_CODE = 0x07
    }

    private lateinit var viewBinding: ActivityProductsBinding
    private var currentID: Int = 1

    fun permissionCameraGranted(): Boolean {
        return ContextCompat.checkSelfPermission(baseContext, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
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
        builder.setNegativeButton("No"){_, _ -> }
        builder.setNeutralButton("Cancel"){_, _ ->}

        val dialog = builder.create()
        dialog.setCancelable(true)
        dialog.show()
    }

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
            db.productDao().insertProduct(product)
            Toast.makeText(this, "productName:${product.productName}, " +
                    "productPrice:${product.productPrice}, " +
                    "productStock:${product.productStock}, " +
                    "productCode:${product.productCode}", Toast.LENGTH_LONG).show()
            currentID = db.productDao().getProducts().size
            updateUIWithCurrentProduct()
        }

        viewBinding.btSave.setOnClickListener{
            val product = db.productDao().getProductByID(currentID)
            product.productName = viewBinding.etName.text.toString()
            product.productStock = viewBinding.etStock.text.toString().toInt()
            product.productPrice = viewBinding.etPrice.text.toString().toInt()
            product.productCode = viewBinding.tvCodeStr.text.toString()
            db.productDao().updateProduct(product)
            Toast.makeText(this, "SAVE SUCCESS!",Toast.LENGTH_SHORT).show()
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

        viewBinding.btScan.setOnClickListener{
            if(permissionCameraGranted())
            {
                startScanContract.launch(Unit)
            }else
            {
                needCameraPermissionDialog()
            }

        }
    }

    val startScanContract = registerForActivityResult(ScanContract()) { res ->
        if(res != null){
            runOnUiThread{
                viewBinding.tvCodeStr.text = res
            }
        }
        else
            Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show()
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
                    startScanContract.launch(Unit)
                }else
                {
                    /* not granted 😢 */
                    Toast.makeText(this, "Camera permission has been explicitly denied! Manually grant it in app info",Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }
}
