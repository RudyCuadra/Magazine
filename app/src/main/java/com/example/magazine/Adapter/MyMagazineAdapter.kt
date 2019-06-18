package com.example.magazine.Adapter

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.magazine.Common.Common
import com.example.magazine.Interface.IRecyclerClick
import com.example.magazine.Objects.Magazine
import com.example.magazine.R
import com.example.magazine.ViewerPDFActivity
import com.squareup.picasso.Picasso

class MyMagazineAdapter(internal var context: Context,
                        internal var magazineList: List<Magazine>
):RecyclerView.Adapter<MyMagazineAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.magazine_item,p0,false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return magazineList.size
    }

    override fun onBindViewHolder(p0: MyViewHolder, p1: Int) {
        Picasso.get().load(magazineList[p1].uriImage).into(p0.imageView)
        p0.textView.text = magazineList[p1].nombre

        p0.setClick(object:IRecyclerClick{
            override fun onClick(view: View, position: Int) {
                //context.startActivity(Intent(context,ViewerPDFActivity::class.java))
                //Common.select_magazine=magazineList[position]
                val string:String = magazineList[position].pdf.toString()
                val intent = Intent(context,ViewerPDFActivity::class.java)
                intent.putExtra("pdf",string.toString())
                context.startActivity(intent)

            }
        })
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        override fun onClick(p0: View?) {
            iRecyclerClick.onClick(p0!!,adapterPosition)

        }

        var imageView:ImageView
        var textView:TextView
        lateinit var iRecyclerClick: IRecyclerClick

        fun setClick(iRecyclerClick: IRecyclerClick){
            this.iRecyclerClick = iRecyclerClick
        }

        init {
            imageView = itemView.findViewById(R.id.magazine_image) as ImageView
            textView = itemView.findViewById(R.id.magazine_name) as TextView

            itemView.setOnClickListener(this)
        }

    }

}