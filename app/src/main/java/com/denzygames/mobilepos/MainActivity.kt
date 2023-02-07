package com.denzygames.mobilepos

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.denzygames.mobilepos.databinding.ActivityMainBinding
import androidx.room.*;

@Database(entities = [Product::class], version = 1)
abstract class MobilePOSDb: RoomDatabase()
{
    abstract fun productDao(): ProductDao

    /*
    we want to initalize the db once
    and access it from other activities
    ATTRIB: https://stackoverflow.com/questions/54651749/access-room-database-from-service-and-activity
     */
    companion object{
        private var db: MobilePOSDb? = null
        private val appDbName = "MobilePOSDb"
        fun getDb(context: Context): MobilePOSDb{
            if(db == null)
            {
                db = Room.databaseBuilder(
                    context,
                    MobilePOSDb::class.java,
                    appDbName
                ).allowMainThreadQueries().build()
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
            Toast.makeText(this,"Launch Products", Toast.LENGTH_SHORT).show()
            val intent = Intent(it.context,Products::class.java)
            startActivity(intent)
        }
    }
}