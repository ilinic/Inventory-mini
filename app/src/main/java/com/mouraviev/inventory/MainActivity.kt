package com.mouraviev.inventory

import android.content.Intent
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
        val userId = settings.getString("user", "")

        if (!userId.equals("")) {
            val intent = Intent(applicationContext, BarcodeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
            finish()
        }

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
            edit.putString("site", siteEditText.text.toString().replace(Regex("/$"), ""))
            edit.apply()

            val request: Request

            try {
                request = Request.Builder()
                        .url(siteEditText.text.toString().replace(Regex("/$"), "") + "/check_login?uid=" + useridEditText.text)
                        .build()

                httpClient.newCall(request).enqueue(
                        object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                showToast("Ошибка. Проверьте соединение и данные")
                            }

                            override fun onResponse(call: Call, response: Response) {
                                if (response.isSuccessful) {
                                    var responseBody: ResponseBody? = response.body

                                    if (responseBody == null)
                                        return

                                    if (responseBody.equals("{\"err\": 0}")) {

                                        edit.putString("user", useridEditText.text.toString())
                                        edit.apply()

                                        val intent = Intent(applicationContext, BarcodeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                        startActivity(intent)
                                        //showToast(responseBody.string())
                                    }

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