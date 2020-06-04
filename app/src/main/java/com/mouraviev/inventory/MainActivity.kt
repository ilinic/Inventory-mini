package com.mouraviev.inventory

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var httpClient: OkHttpClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        httpClient = OkHttpClient()

        findViewById<Button>(R.id.loginBtn).setOnClickListener {

            val siteEditText: EditText = findViewById(R.id.siteTextView)
            val useridEditText: EditText = findViewById(R.id.userIdtextView)

            if (siteEditText.text.isEmpty() || useridEditText.text.isEmpty()) {
                Toast.makeText(this, "Введите данные логина", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request: Request

            try {
                request = Request.Builder()
                        .url(siteEditText.text.toString())
                        .build()

                httpClient.newCall(request).enqueue(
                        object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                runOnUiThread {
                                    fun run() {
                                        Toast.makeText(this@MainActivity, "Ошибка. Проверьте соединение и данные логина", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                            override fun onResponse(call: Call, response: Response) {
                                if (response.isSuccessful)
                                    Toast.makeText(this@MainActivity, "OK!", Toast.LENGTH_SHORT).show()
                                else
                                    Toast.makeText(this@MainActivity, "Ошибка. Проверьте соединение и данные логина", Toast.LENGTH_SHORT).show()
                            }
                        }
                )

            } catch (e: Exception) {
                Toast.makeText(this, "Ошибка. Проверьте соединение и данные логина", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val intent = Intent(applicationContext, BarcodeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            //startActivityForResult(intent, BARCODE_READER_REQUEST_CODE)
            startActivity(intent)
            //this.overridePendingTransition(0, 0);
        }
    }
}
