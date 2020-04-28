package com.datapiratepwl.e_med

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_register_hospital.*
import kotlinx.android.synthetic.main.login.*
import java.lang.Exception
import java.util.concurrent.TimeUnit
import kotlin.math.E

class RegisterHospitalActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks {

    lateinit var verificationId:String
    lateinit var auth:FirebaseAuth
    var isMobileVerified = false
    lateinit var Email:String
    lateinit var Mobile:String
    lateinit var pb1:ProgressDialog
    lateinit var pb2:ProgressDialog
    lateinit var pb3:ProgressDialog
    lateinit var pb4:ProgressDialog
    lateinit var refUsers: DatabaseReference
    var hospitalIdentificationUniqueCode:Int = 0
    var isHospitalIdentificationUniqueCodeGenerated:Boolean = false
    val key:String ="6LdIUewUAAAAAMmunYC8mV1muhmHoTqJL1OlYeFv"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register_hospital)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide()
        Email = ""


        refUsers = FirebaseDatabase.getInstance().reference.child("data").child("hospitals").child("huic")
        refUsers.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                val dialogBuilder = android.app.AlertDialog.Builder(this@RegisterHospitalActivity)

                dialogBuilder.setMessage("There is some Network Error, Please try again later.")
                    .setPositiveButton("ok",DialogInterface.OnClickListener {
                            dialog, id -> exit()
                    })

                val alert = dialogBuilder.create()
                alert.setTitle("e - Med")
                alert.show()

            }
            override fun onDataChange(p0: DataSnapshot) {
                hospitalIdentificationUniqueCode = p0.value.toString().toInt()
                isHospitalIdentificationUniqueCodeGenerated =true


            }
        })
        auth = FirebaseAuth.getInstance()
        var mGoogleApiClient = GoogleApiClient.Builder(this)
            .addApi(SafetyNet.API)
            .addConnectionCallbacks(this@RegisterHospitalActivity)
            .build()

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
                pb3 = ProgressDialog.show(this,"e - Med","Verifying..",true)
                verifyCode(code)
                pb3.dismiss()
            }

        }
        register_hosp.setOnClickListener{

                if (checkALl()) {
                    pb1 = ProgressDialog.show(this, "e - Med", "Registration under process", true)
                    mGoogleApiClient.connect()
                    SafetyNet.getClient(this).verifyWithRecaptcha(key)
                        .addOnSuccessListener(this, OnSuccessListener { response ->
                            val userResponseToken = response.tokenResult
                            if (response.tokenResult?.isNotEmpty() == true) {
                                Submit()
                            }
                        })
                        .addOnFailureListener(this, OnFailureListener { e ->
                            if (e is ApiException) {

                                val dialogBuilder = android.app.AlertDialog.Builder(this)

                                dialogBuilder.setMessage(
                                    "Error : ${CommonStatusCodes.getStatusCodeString(
                                        e.statusCode
                                    )}"
                                )
                                    .setPositiveButton(
                                        "ok",
                                        DialogInterface.OnClickListener { dialog, id ->
                                            dialog.dismiss()
                                        })
                                val alert = dialogBuilder.create()
                                alert.setTitle("e - Med")
                                pb1.dismiss()
                                alert.show()
                            } else {
                                val dialogBuilder = android.app.AlertDialog.Builder(this)

                                dialogBuilder.setMessage("Error : ${e.message}")
                                    .setPositiveButton(
                                        "ok",
                                        DialogInterface.OnClickListener { dialog, id ->
                                            dialog.dismiss()
                                        })
                                val alert = dialogBuilder.create()
                                alert.setTitle("e - Med")
                                pb1.dismiss()
                                alert.show()
                            }
                        })
                }
        }

        reset_hosp.setOnClickListener{

            val dialogBuilder = android.app.AlertDialog.Builder(this)

            dialogBuilder.setMessage("Want to Reset ?")
                .setPositiveButton("No",DialogInterface.OnClickListener {
                        dialog, id -> dialog.dismiss()
                })
                .setNegativeButton("Proceed", DialogInterface.OnClickListener {
                        dialog, id -> Reset()
                })

            val alert = dialogBuilder.create()
            alert.setTitle("e - Med")
            alert.show()
        }


    }

    private fun Reset() {

        hospital_email.setText("",TextView.BufferType.EDITABLE)
        hospital_email.isEnabled = false
        hospital_mobile.isEnabled = true
        hospital_mobile.setText("+91",TextView.BufferType.EDITABLE)
        hospital_mobile_otp.setText("",TextView.BufferType.EDITABLE)
        hospital_mobile.visibility = View.VISIBLE
        hospital_mobile_otp.visibility=View.INVISIBLE
        verify_mobile.visibility = View.VISIBLE
        verify_mobile_otp.visibility = View.INVISIBLE
        verify_mobile_otp.text = "Verify"
        isMobileVerified = false
        hospital_name.setText("",TextView.BufferType.EDITABLE)
        hospital_name.isEnabled=false
        hospital_address.setText("",TextView.BufferType.EDITABLE)
        hospital_address.isEnabled =false
        hosp_add_pin.setText("",TextView.BufferType.EDITABLE)
        hosp_add_pin.isEnabled = false
        hosp_add_city.setText("",TextView.BufferType.EDITABLE)
        hosp_add_city.isEnabled=false
        hosp_add_District.setText("",TextView.BufferType.EDITABLE)
        hosp_add_District.isEnabled=false
        hosp_add_State.setText("",TextView.BufferType.EDITABLE)
        hosp_add_State.isEnabled = false
        hospital_password.setText("",TextView.BufferType.EDITABLE)
        hospital_password.isEnabled = false
        hospital_password_confirm.setText("",TextView.BufferType.EDITABLE)
        hospital_password_confirm.isEnabled = false
    }


    private fun verify(mobile: String) {

        sendVerificationCode(mobile)
    }
    private fun sendVerificationCode(mobile:String)
    {

        pb2 = ProgressDialog.show(this,"e - Med","Verifying...",true)
        PhoneAuthProvider.getInstance().verifyPhoneNumber(mobile,35,TimeUnit.SECONDS,TaskExecutors.MAIN_THREAD,mCallbacks)
    }

    private var mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks()
    {
        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            val code = p0.smsCode
            if(code!=null)
            {
                hospital_mobile_otp.setText("$code",TextView.BufferType.EDITABLE)
                verifyCode(code)
                pb2.hide()
            }


        }

        override fun onVerificationFailed(p0: FirebaseException) {

            val dialogBuilder = android.app.AlertDialog.Builder(this@RegisterHospitalActivity)

            dialogBuilder.setMessage("Error : ${p0.message}")
                .setCancelable(false)
                .setPositiveButton("Proceed", DialogInterface.OnClickListener {
                        dialog, id -> dialog.dismiss()
                })
            pb2.hide()

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
            pb2.hide()
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
            hospital_name.isEnabled = true
            hospital_address.isEnabled=true
            hosp_add_pin.isEnabled=true
            hosp_add_city.isEnabled=true
            hosp_add_District.isEnabled=true
            hosp_add_State.isEnabled=true
            hospital_email.isEnabled=true
            hospital_password.isEnabled=true
            hospital_password_confirm.isEnabled=true

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
        try {
            Email = hospital_email.text.toString()
            refUsers = FirebaseDatabase.getInstance().reference.child("data").child("hospitals").child("huic")
            refUsers.setValue((hospitalIdentificationUniqueCode+1).toString())

            val Password = hospital_password.text.toString()
            auth.createUserWithEmailAndPassword(Email, Password).addOnSuccessListener {
            var firebaseUserID = auth.currentUser!!.uid
            refUsers = FirebaseDatabase.getInstance().reference.child("data").child("hospitals").child(firebaseUserID)

            val hospHashMap = HashMap<String, Any>()

            hospHashMap["uid"] = firebaseUserID
            hospHashMap["email"] = Email
            hospHashMap["mobile"] = Mobile
            hospHashMap["name"] = hospital_name.text.toString()
            hospHashMap["address"] = hospital_address.text.toString()
            hospHashMap["pin"] = hosp_add_pin.text.toString()
            hospHashMap["city"] = hosp_add_city.text.toString()
            hospHashMap["district"] = hosp_add_District.text.toString()
            hospHashMap["state"] = hosp_add_State.text.toString()
            hospHashMap["hiuc"] = "HOSP"+Integer.toHexString(hospitalIdentificationUniqueCode).toUpperCase()
                    refUsers.updateChildren(hospHashMap).addOnSuccessListener {
                        auth.currentUser!!.sendEmailVerification()
                val dialogBuilder = android.app.AlertDialog.Builder(this)

                dialogBuilder.setMessage("Registration Successful\nAn E-mail Verification link is sent to your registered e-mail Account. Please verify account.")
                    .setCancelable(false)
                    .setPositiveButton(
                        "Proceed",
                        DialogInterface.OnClickListener { dialog, id -> exit() })

                val alert = dialogBuilder.create()
                alert.setTitle("e - Med")
                pb1.dismiss()
                alert.show()
            }
                .addOnFailureListener { task ->
                    val dialogBuilder = android.app.AlertDialog.Builder(this)

                    dialogBuilder.setMessage("Error : ${task.message}")
                        .setCancelable(false)
                        .setPositiveButton(
                            "Proceed",
                            DialogInterface.OnClickListener { dialog, id ->
                                dialog.dismiss()
                            })

                    val alert = dialogBuilder.create()
                    alert.setTitle("e - Med")
                    pb1.dismiss()
                    alert.show()
                }
        }.addOnFailureListener { task ->
                val dialogBuilder = android.app.AlertDialog.Builder(this)

                dialogBuilder.setMessage("Error : ${task.message}")
                    .setCancelable(false)
                    .setPositiveButton(
                        "Proceed",
                        DialogInterface.OnClickListener { dialog, id ->
                            dialog.dismiss()
                        })

                val alert = dialogBuilder.create()
                alert.setTitle("e - Med")
                pb1.dismiss()
                alert.show()
            }
            }
        catch (e:Exception)
        {

        }
    }

    private fun hospitalIdentificationUniqueCode(): Int {
       lateinit var temp1:String
        refUsers = FirebaseDatabase.getInstance().reference.child("data").child("hospitals").child("huic")
        refUsers.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                isHospitalIdentificationUniqueCodeGenerated =false

            }
            override fun onDataChange(p0: DataSnapshot) {
                temp1 = p0.value.toString()
                Toast.makeText(this@RegisterHospitalActivity,"Uni $temp1",Toast.LENGTH_SHORT)
                    .show()
                isHospitalIdentificationUniqueCodeGenerated =true


            }
        })
        return temp1.toInt()
    }

    private fun checkALl(): Boolean {
        if(!isMobileVerified)
        {
            hospital_mobile.error = "Not Verified"
            return false
        }
        else if(hospital_name.text.toString().isEmpty())
        {
            hospital_name.error = "Required"
            return false
        }
        else if(hospital_address.text.toString().isEmpty())
        {
            hospital_address.error = "Required"
            return false
        }
        else if(hosp_add_pin.text.toString().isEmpty())
        {
            hosp_add_pin.error = "Required"
            return false
        }
        else if(hosp_add_city.text.toString().isEmpty())
        {
            hosp_add_city.error = "Required"
            return false
        }
        else if(hosp_add_District.text.toString().isEmpty())
        {
            hosp_add_District.error = "Required"
            return false
        }
        else if(hosp_add_State.text.toString().isEmpty())
        {
            hosp_add_State.error = "Required"
            return false
        }
        else if(hospital_email.text.toString().isEmpty())
        {
            hospital_email.error = "Required"
            return false
        }
        else if(hospital_password.text.toString().isEmpty())
        {
            hospital_password.error = "Required"
            return false
        }
        else if(passwordNotStrong(hospital_password.text.toString()))
        {
            return false
        }
        else if(hospital_password_confirm.text.toString().isEmpty())
        {
            hospital_password_confirm.error = "Required"
            return false
        }
        else if(!hospital_password.text.toString().equals(hospital_password_confirm.text.toString()))
        {
            hospital_password.error = "Password Not Matched"
            return false
        }
        else
        {
            return true
        }
    }

    private fun passwordNotStrong(password: String): Boolean {
        if(password.length<9)
        {
            hospital_password.error = "Password Length must be atleast 9"
            return true
        }
        else {

            var hasChar = false
            var hasNumber = false
            var hasUppercaseAlphabet = false
            var hasLowercaseAlphabet = false

            for (i in password.indices) {
                if (password[i] == '@' || password[i] == '$' || password[i] == '_') {
                    hasChar = true
                } else if (password[i] in 'A'..'Z') {
                    hasUppercaseAlphabet = true
                } else if (password[i] in 'a'..'z') {
                    hasLowercaseAlphabet = true
                } else if (password[i] in '0'..'9') {
                    hasNumber = true
                }
                else
                {
                    hospital_password.error = " ' ${password[i]} ' is Not Allowed"
                    return true
                }
            }
            if (!hasChar) {
                hospital_password.error =
                    "Atleast 1 Character Required ( only @ , $ and _ are allowed"
                return true
            } else if (!hasNumber) {
                hospital_password.error = "Atleast 1 numeric character is required"
                return true
            } else if (!hasUppercaseAlphabet) {
                hospital_password.error = "Atleast 1 UpperCase Alphabet is must"
                return true
            } else if (!hasLowercaseAlphabet) {
                hospital_password.error = "Atleast 1 LowerCase Alphabet is must"
                return true
            } else {
                return false
            }
        }
    }

    private fun MobileVerified(): Boolean = isMobileVerified
    override fun onBackPressed() {
        val dialogBuilder = android.app.AlertDialog.Builder(this)

        dialogBuilder.setMessage("Are You sure to stop registration process here ?")
            .setPositiveButton("Proceed",DialogInterface.OnClickListener {
                    dialog, id -> exit()
            })
            .setNegativeButton("No", DialogInterface.OnClickListener {
                    dialog, id -> dialog.dismiss()
            })

        val alert = dialogBuilder.create()
        alert.setTitle("e - Med")
        alert.show()
    }

    private fun exit() {
        auth.signOut()
            startActivity(Intent(this@RegisterHospitalActivity, RegisterOptActivity::class.java))
            finish()

    }

    override fun onConnected(p0: Bundle?) {
        //
    }

    override fun onConnectionSuspended(p0: Int) {
        //
    }


}
