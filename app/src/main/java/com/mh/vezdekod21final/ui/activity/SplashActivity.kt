package com.mh.vezdekod21final.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.give.vezdekodmh.utils.L

class SplashActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        L.d("splash screen onCreate")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }


}