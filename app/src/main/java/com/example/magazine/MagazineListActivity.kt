package com.example.magazine

import android.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.widget.Toast
import com.example.magazine.Adapter.MyMagazineAdapter
import com.example.magazine.Common.Common
import com.example.magazine.Interface.IMagazineLoadDoneListener
import com.example.magazine.Objects.Magazine
import com.example.magazine.Service.PicassoImageLoandingService
import com.google.firebase.database.*
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_magazine_list.*
import ss.com.bannerslider.Slider
import java.lang.StringBuilder

class MagazineListActivity : AppCompatActivity(), IMagazineLoadDoneListener {
    override fun OnMagazineLoadDoneListener(MagazineList: List<Magazine>) {
        alertDialog.dismiss()

        Common.magazineList = MagazineList
        recycler_magazine.adapter = MyMagazineAdapter(baseContext,MagazineList)
        txt_magazine.text = StringBuilder("NEW MAGAZINE (")
            .append(MagazineList.size)
            .append(")")

        if(swipe_to_refresh.isRefreshing)
            swipe_to_refresh.isRefreshing=false


    }

    lateinit var alertDialog: AlertDialog
    lateinit var magazine_ref:DatabaseReference
    lateinit var iMagazineLoadDoneListener: IMagazineLoadDoneListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_magazine_list)

        iMagazineLoadDoneListener = this

        alertDialog = SpotsDialog.Builder().setContext(this@MagazineListActivity)
            .setCancelable(false)
            .setMessage("Please wait...")
            .build()

        magazine_ref = FirebaseDatabase.getInstance().getReference("magazines")

        //loadMagazine()
        swipe_to_refresh.setColorSchemeResources(R.color.colorPrimary,R.color.colorPrimaryDark)
        swipe_to_refresh.setOnRefreshListener {
            loadMagazine()
        }
        swipe_to_refresh.post {
            loadMagazine()
        }

        Slider.init(PicassoImageLoandingService())

        recycler_magazine.setHasFixedSize(true)
recycler_magazine.layoutManager = GridLayoutManager(this@MagazineListActivity,2)

    }

    private fun loadMagazine() {
        alertDialog.show()

        magazine_ref.addListenerForSingleValueEvent(object:ValueEventListener{
            var magazine_load:MutableList<Magazine> = ArrayList<Magazine>() as MutableList<Magazine>
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(this@MagazineListActivity, ""+p0.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                for(magazineSnapshot in p0.children){
                    val magazine = magazineSnapshot.getValue(Magazine::class.java)
                    magazine_load.add(magazine!!)
                }
                    iMagazineLoadDoneListener.OnMagazineLoadDoneListener(magazine_load)

            }

        })
    }
}
