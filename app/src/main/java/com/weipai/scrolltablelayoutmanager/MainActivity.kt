package com.weipai.scrolltablelayoutmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.recyclerview.widget.RecyclerView
import com.weipai.scrolltablelayoutmanager.layoutmanager.ScrollTableLayoutManager
import com.weipai.scrolltablelayoutmanager.layoutmanager.TableTouchHelper
import com.weipai.scrolltablelayoutmanager.ui.theme.ScrollTableLayoutManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val rv = findViewById<TableRecyclerView>(R.id.rv)
        val list = mutableListOf<String>()
        val spanCount = 8
        for (i in 0 ..< 300){
            list.add("这是第${i/8}行第${i%8}个")
        }
        rv.layoutManager = ScrollTableLayoutManager().apply {
            columnCount = spanCount
        }
        val adapter= Adapter(list)
        rv.adapter = adapter
        TableTouchHelper().bindRecyclerView(rv)
    }
}

class Adapter(val list : MutableList<String>) : RecyclerView.Adapter<Adapter.ViewHolder>(){
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_item,parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.tv).text = list[position]
    }
}