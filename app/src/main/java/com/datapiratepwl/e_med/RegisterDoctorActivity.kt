package com.datapiratepwl.e_med

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.ActionBar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class RegisterDoctorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register_doctor)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide()
        var pb = ProgressDialog.show(this,"e - Med","Loading..",true)
        var list = mutableListOf<String>()

        val dataref = FirebaseDatabase.getInstance().reference.child("data").child("hospitals")

        dataref.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                for(dataSnapshot:DataSnapshot in p0.children)
                {
                    if(dataSnapshot.toString() == "huic")
                    {
                        continue
                    }
                    list.add(dataSnapshot.child("hiuc").value.toString())
                }
                Toast.makeText(this@RegisterDoctorActivity,"$list",Toast.LENGTH_SHORT).show()
                pb.dismiss()
            }

        })

        var huic = findViewById<AutoCompleteTextView>(R.id.huic)
        var ArrayAdpt:ArrayAdapter<String> = ArrayAdapter(this,android.R.layout.simple_list_item_1,list)
        huic.setAdapter(ArrayAdpt)


    }
}
