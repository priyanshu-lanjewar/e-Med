package com.datapiratepwl.e_med


import com.google.firebase.database.*

open class FirebaseDatabaseHelper
{

    private var mDatabase:FirebaseDatabase = FirebaseDatabase.getInstance()
    private var mRef:DatabaseReference
    private var list = mutableListOf<hospitals>()

    interface DataStatus{
        fun DataIsLoaded(rate:MutableList<hospitals>, keys:MutableList<String>)
    }
    init {
        mRef=mDatabase.reference.child("data").child("hospitals")
    }


    fun readHospital(dataStatus: DataStatus)
    {

        mRef.addValueEventListener(object : ValueEventListener
        {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                list.clear()
                val keys= mutableListOf<String>()
                for(keynode:DataSnapshot in p0.children )
                {
                    keys.add(keynode.key!!)
                    val huic:hospitals? = keynode.getValue(hospitals::class.java)
                    list.add(huic!!)
                }
                dataStatus.DataIsLoaded(list,keys)
            }

        })
    }
}