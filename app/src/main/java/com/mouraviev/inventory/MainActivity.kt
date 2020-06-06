/*
    Author: Artem M br_in_arms@mail.ru
 */
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var httpClient: OkHttpClient
    private lateinit var settings: SharedPreferences

    companion object {
        lateinit var userId: String
        lateinit var site: String
    }

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
        site = settings.getString("site", "")
        userId = settings.getString("user", "")

        if (!userId.equals("")) {
            val intent = Intent(applicationContext, BarcodeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

            startActivity(intent)
            finish()
            overridePendingTransition(0, 0)
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
                showToast(getString(R.string.enter_login))
                return@setOnClickListener
            }

            val edit: SharedPreferences.Editor = settings.edit()
            site = siteEditText.text.toString().replace(Regex("/$"), "")
            edit.putString("site", site)
            edit.apply()

            val request: Request

            try {
                request = Request.Builder()
                        .url(siteEditText.text.toString().replace(Regex("/$"), "") + "/check_login?uid=" + useridEditText.text)
                        .build()

                httpClient.newCall(request).enqueue(
                        object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                showToast(getString(R.string.html_error))
                            }

                            override fun onResponse(call: Call, response: Response) {
                                if (!response.isSuccessful) {
                                    showToast(getString(R.string.html_error))
                                    return
                                }

                                var responseBody: ResponseBody? = response.body

                                if (responseBody == null) {
                                    showToast(getString(R.string.html_error))
                                    return
                                }

                                val gson = Gson()
                                var responseMap: Map<String, Int> = gson.fromJson(responseBody.string(), object : TypeToken<Map<String, Int>>() {}.type)

                                if (responseMap["err"] != 0) {
                                    showToast(getString(R.string.html_error))
                                    return
                                }

                                userId = useridEditText.text.toString()
                                edit.putString("user", userId)
                                edit.apply()

                                val intent = Intent(applicationContext, BarcodeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                startActivity(intent)
                                finish()
                                overridePendingTransition(0, 0)
                            }
                        })

            } catch (e: Exception) {
                showToast(getString(R.string.html_error))
                return@setOnClickListener
            }
        }
    }
}