package com.datapiratepwl.e_med


import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.login.*


open class LoginActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    lateinit var progDil:ProgressDialog
    lateinit var googleApiClient:Any
    val key:String ="6LdIUewUAAAAAMmunYC8mV1muhmHoTqJL1OlYeFv"

    override fun onCreate(savedInstanceState: Bundle?) {
        progDil = ProgressDialog(this@LoginActivity)
        progDil.setTitle("Loading...")
        progDil.show()
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide()
        var mail="aa"
        var rr="11"
        progDil.dismiss()
        var mGoogleApiClient = GoogleApiClient.Builder(this)
            .addApi(SafetyNet.API)
            .addConnectionCallbacks(this@LoginActivity)
            .build()

        mGoogleApiClient.connect()
        login.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                SafetyNet.getClient(this@LoginActivity).verifyWithRecaptcha(key)
                    .addOnSuccessListener(this@LoginActivity, OnSuccessListener { response ->
                        Toast.makeText(this@LoginActivity,"Begin",Toast.LENGTH_SHORT).show()
                        val userResponseToken = response.tokenResult
                        if (response.tokenResult?.isNotEmpty() == true) {
                            Toast.makeText(this@LoginActivity,"Done",Toast.LENGTH_SHORT).show()

                        }
                    })
                    .addOnFailureListener(this@LoginActivity, OnFailureListener { e ->
                        if (e is ApiException) {

                            Toast.makeText(this@LoginActivity,"Error: ${CommonStatusCodes.getStatusCodeString(e.statusCode)}",Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@LoginActivity,"Error: ${e.message}",Toast.LENGTH_SHORT).show()
                        }
                    })
            }

        })


        reg.setOnClickListener(object :View.OnClickListener{
            override fun onClick(v: View?) {
                progDil = ProgressDialog.show(this@LoginActivity,"e - Med","Loading..",true)
                var intent:Intent = Intent(this@LoginActivity,RegisterOptActivity::class.java)
                startActivity(intent)
                progDil.dismiss()
                finish()
            }

        })

    }

    override fun onConnected(p0: Bundle?) {
       //done

    }

    override fun onConnectionSuspended(p0: Int) {
//ugj
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
      Toast.makeText(this@LoginActivity,"${p0.errorMessage}",Toast.LENGTH_LONG).show()
    }
}

