package com.mouraviev.inventory

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var httpClient: OkHttpClient
    private lateinit var settings: SharedPreferences

    fun showToast(msg: String) {
        runOnUiThread(
                object : Runnable {
                    override fun run() {
                        var toast = Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT)
                        toast.setGravity(Gravity.TOP, 0, 160)
                        toast.show()
                    }
                }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        settings = PreferenceManager.getDefaultSharedPreferences(this)
        val site = settings.getString("site", "")
        val siteEditText: EditText = findViewById(R.id.siteTextView)

        if (!site.isEmpty())
            siteEditText.setText(site)

        httpClient = OkHttpClient
                .Builder()
                .callTimeout(2, TimeUnit.SECONDS)
                .build()

        findViewById<Button>(R.id.loginBtn).setOnClickListener {

            val useridEditText: EditText = findViewById(R.id.userIdtextView)

            if (siteEditText.text.isEmpty() || useridEditText.text.isEmpty()) {
                showToast("Введите данные логина")
                return@setOnClickListener
            }

            val edit: SharedPreferences.Editor = settings.edit()
            edit.putString("site", siteEditText.text.toString())
            edit.apply()

            val request: Request

            try {
                request = Request.Builder()
                        .url(siteEditText.text.toString())
                        .build()

                httpClient.newCall(request).enqueue(
                        object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                showToast("Ошибка. Проверьте соединение и данные")
                            }

                            override fun onResponse(call: Call, response: Response) {
                                if (response.isSuccessful) {
                                    showToast(response.toString())

                                    //val intent = Intent(applicationContext, BarcodeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                    ////startActivityForResult(intent, BARCODE_READER_REQUEST_CODE)
                                    //startActivity(intent)
                                    ////this.overridePendingTransition(0, 0);
                                } else
                                    showToast("Ошибка. Проверьте соединение и данные")
                            }
                        }
                )

            } catch (e: Exception) {
                showToast("Ошибка. Проверьте соединение и данные")
                return@setOnClickListener
            }
        }
    }
}