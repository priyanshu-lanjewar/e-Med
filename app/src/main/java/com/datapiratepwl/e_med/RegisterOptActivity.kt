package com.datapiratepwl.e_med

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.ActionBar
import kotlinx.android.synthetic.main.activity_register_opt.*

class RegisterOptActivity : AppCompatActivity() {

    lateinit var dialogProgress:ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register_opt)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide()

        register_hospital.setOnClickListener{
            dialogProgress = ProgressDialog.show(this@RegisterOptActivity,"e - Med","Loading..",true)
            startActivity(Intent(this@RegisterOptActivity,RegisterHospitalActivity::class.java))
            finish()
        }
    }


    override fun onBackPressed() {
        startActivity(Intent(this@RegisterOptActivity,LoginActivity::class.java))
        finish()
    }
}
