package com.datapiratepwl.e_med

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.ActionBar
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_register_hospital.*
import kotlinx.android.synthetic.main.activity_register_opt.*
import java.util.concurrent.TimeUnit

class RegisterHospitalActivity : AppCompatActivity() {

    lateinit var verificationId:String
    lateinit var auth:FirebaseAuth
    var isEmailVerified = false
    var isMobileVerified = false
    lateinit var Email:String
    lateinit var Mobile:String
    lateinit var pb:ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register_hospital)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide()
        auth = FirebaseAuth.getInstance()

        verify_email.setOnClickListener{


            if(hospital_email.text.isEmpty())
            {
                hospital_email.error = "Required"
            }
            else {
                pb = ProgressDialog.show(this,"e - Med","Verifying..",true)

                Email = hospital_email.text.toString()
                hospital_email.isEnabled = false
                verify_email.isEnabled = false
                auth.createUserWithEmailAndPassword(Email, "00000000").addOnFailureListener { task ->
                    val dialogBuilder = android.app.AlertDialog.Builder(this)

                    dialogBuilder.setMessage("Error : ${task.message}")
                        .setCancelable(false)
                        .setPositiveButton("Ok", DialogInterface.OnClickListener {
                                dialog, id -> dialog.dismiss()
                        })

                    val alert = dialogBuilder.create()
                    alert.setTitle("e - Med")
                    pb.dismiss()
                    alert.show()
                    hospital_email.isEnabled = true
                    verify_email.isEnabled = true


                }
                    .addOnSuccessListener {
                        val dialogBuilder = android.app.AlertDialog.Builder(this)

                        dialogBuilder.setMessage("An E-mail Verification link is mailed to your registered E-mail account.Please Verify your E-mail using link to complete registration.")
                            .setCancelable(false)
                            .setPositiveButton("Proceed", DialogInterface.OnClickListener {
                                    dialog, id -> dialog.dismiss()
                            })

                        val alert = dialogBuilder.create()
                        alert.setTitle("e - Med")
                        pb.dismiss()
                        alert.show()
                    }
                auth.currentUser?.sendEmailVerification()

            }
        }

        verify_mobile.setOnClickListener{
            if(hospital_mobile.text.length!=13)
            {
                hospital_mobile.error = "Invalid Number"
            }
            else

            {
                hospital_mobile.visibility = View.INVISIBLE
                hospital_mobile_otp.visibility=View.VISIBLE
                verify_mobile.visibility = View.INVISIBLE
                verify_mobile_otp.visibility = View.VISIBLE
                Mobile = hospital_mobile.text.toString()
                verify(Mobile)
            }
        }

        verify_mobile_otp.setOnClickListener {

            val code = hospital_mobile_otp.text.toString().trim()
            if(code.isEmpty() || code.length<6)
            {
                hospital_mobile_otp.error = "Enter Valid Code"
                hospital_mobile_otp.requestFocus()
            }
            else{

                verifyCode(code)
            }

        }
        register_hosp.setOnClickListener{

            if(EmailVerified())
            {
                if(MobileVerified())
                {
                    if(checkALl())
                    {
                        Submit()
                    }
                }
            }

        }


    }

    private fun verify(mobile: String) {

        pro.visibility = View.VISIBLE
        sendVerificationCode(mobile)
    }
    private fun sendVerificationCode(mobile:String)
    {
        pb = ProgressDialog.show(this,"e - Med","Sending..",true)
        PhoneAuthProvider.getInstance().verifyPhoneNumber(mobile,35,TimeUnit.SECONDS,TaskExecutors.MAIN_THREAD,mCallbacks)
        pb.dismiss()
    }

    private var mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks()
    {
        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            val code = p0.smsCode
            if(code!=null)
            {
                hospital_mobile_otp.setText("$code",TextView.BufferType.EDITABLE)
                verifyCode(code)

            }


        }

        override fun onVerificationFailed(p0: FirebaseException) {
            pro.visibility = View.INVISIBLE

            val dialogBuilder = android.app.AlertDialog.Builder(this@RegisterHospitalActivity)

            dialogBuilder.setMessage("Error : ${p0.message}")
                .setCancelable(false)
                .setPositiveButton("Proceed", DialogInterface.OnClickListener {
                        dialog, id -> dialog.dismiss()
                })

            val alert = dialogBuilder.create()
            alert.setTitle("e - Med")
            hospital_mobile.visibility = View.VISIBLE
            hospital_mobile_otp.visibility=View.INVISIBLE
            verify_mobile.visibility = View.VISIBLE
            verify_mobile_otp.visibility = View.INVISIBLE
            alert.show()
        }

        override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(p0, p1)
            verificationId = p0
        }

        override fun onCodeAutoRetrievalTimeOut(p0: String) {
            super.onCodeAutoRetrievalTimeOut(p0)
            pro.visibility = View.INVISIBLE
            val dialogBuilder = android.app.AlertDialog.Builder(this@RegisterHospitalActivity)

            dialogBuilder.setMessage("Request Timeout for Verification of mobile number ${Mobile}")
                .setNegativeButton("Resend Verification Code",DialogInterface.OnClickListener {
                        dialog, id -> sendVerificationCode(mobile = Mobile)
                })
                .setPositiveButton("Enter Code Manually", DialogInterface.OnClickListener {
                        dialog, id -> dialog.dismiss()
                })
                .setNeutralButton("Change Mobile Number",DialogInterface.OnClickListener {
                        dialog, id -> changeNo()
                })
            val alert = dialogBuilder.create()
            alert.setTitle("e - Med")
            alert.show()
        }
    }

    private fun changeNo()
    {
        hospital_mobile.visibility = View.VISIBLE
        hospital_mobile_otp.visibility=View.INVISIBLE
        verify_mobile.visibility = View.VISIBLE
        verify_mobile_otp.visibility = View.INVISIBLE
    }
    private fun verifyCode(code:String)
    {
        var credential = PhoneAuthProvider.getCredential(verificationId,code)
        auth.signInWithCredential(credential).addOnSuccessListener {
            Toast.makeText(this,"Done",Toast.LENGTH_LONG).show()
            isMobileVerified = true
           auth.currentUser?.delete()
            verify_mobile_otp.text = "Verified"
            hospital_mobile.visibility = View.VISIBLE
            hospital_mobile.isEnabled  = false
            hospital_mobile_otp.visibility = View.INVISIBLE
            verify_mobile_otp.isEnabled = false
            pro.visibility = View.INVISIBLE

            val dialogBuilder = android.app.AlertDialog.Builder(this)

            dialogBuilder.setMessage("Mobile Number Verified Successfully..")
                .setCancelable(false)
                .setPositiveButton("Proceed", DialogInterface.OnClickListener {
                        dialog, id -> dialog.dismiss()
                })

            val alert = dialogBuilder.create()
            alert.setTitle("e - Med")
            alert.show()

        }
            .addOnFailureListener {task ->
                val dialogBuilder = android.app.AlertDialog.Builder(this)

                dialogBuilder.setMessage("Error ${task.message}")
                    .setCancelable(false)
                    .setPositiveButton("Proceed", DialogInterface.OnClickListener {
                            dialog, id -> dialog.dismiss()
                    })

                val alert = dialogBuilder.create()
                alert.setTitle("e - Med")
                alert.show()
            }
    }

    private fun Submit() {
        auth.currentUser?.updatePassword(hospital_password_confirm.text.toString())
    }

    private fun checkALl(): Boolean {
        return false
    }

    private fun MobileVerified(): Boolean = isMobileVerified

    private fun EmailVerified(): Boolean
    {
       if(auth.currentUser?.isEmailVerified!!)
           isEmailVerified = true
        return isEmailVerified
    }



    override fun onBackPressed() {
        auth.currentUser?.delete()
        startActivity(Intent(this@RegisterHospitalActivity,RegisterOptActivity::class.java))
        finish()
    }
}
