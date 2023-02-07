package com.denzygames.mobilepos

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.denzygames.mobilepos.databinding.ActivityMainBinding
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