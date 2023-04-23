package com.denzygames.mobilepos

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.denzygames.mobilepos.databinding.ActivityMainBinding
import androidx.room.*

@Database(entities = [Product::class, Sale::class, SaleDetails::class], version = 1)
abstract class MobilePOSDb: RoomDatabase()
{
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao

    /*
    we want to initalize the db once
    and access it from other activities
    ATTRIB: https://stackoverflow.com/questions/54651749/access-room-database-from-service-and-activity
     */
    companion object{
        private var db: MobilePOSDb? = null
        private const val appDbName = "MobilePOSDb"
        fun getDb(context: Context): MobilePOSDb{
            if(db == null)
            {
                db = Room.databaseBuilder(
                    context,
                    MobilePOSDb::class.java,
                    appDbName
                )
                    .allowMainThreadQueries()
                    .build()
            }
            return db!!
        }
    }
}

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.btProducts.setOnClickListener{
            //Toast.makeText(this,"Launch Products", Toast.LENGTH_SHORT).show()
            startActivity(Intent(it.context,Products::class.java))
        }

        viewBinding.btSell.setOnClickListener{
            //Toast.makeText(this,"Launch Sell", Toast.LENGTH_SHORT).show()
            startActivity(Intent(it.context, Sell::class.java))
        }

        viewBinding.btReports.setOnClickListener {
            //Toast.makeText(this,"Launch Reports", Toast.LENGTH_SHORT).show()
            startActivity(Intent(it.context, Reports::class.java))
        }

        viewBinding.btExit.setOnClickListener {
            Toast.makeText(this, System.currentTimeMillis().toString(), Toast.LENGTH_LONG).show()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Exit?");
            builder.setMessage("Are you sure?")
            builder.setPositiveButton("Yes"){_, _ -> finish()}
            builder.setNegativeButton("No") {_, _ -> }
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.create().show()
        }
    }
}