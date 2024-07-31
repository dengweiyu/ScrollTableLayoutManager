package com.weipai.scrolltablelayoutmanager

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView


class TableRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var startX = 0f
    private var startY = 0f
    private val simpleOnGestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            return super.onSingleTapUp(e)

        }

        override fun onContextClick(e: MotionEvent): Boolean {
            return super.onContextClick(e)
        }

        override fun onDown(e: MotionEvent): Boolean {
            return super.onDown(e)
        }
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            return initOnClickListener.invoke(this@TableRecyclerView,e)
        }

        override fun onLongPress(e: MotionEvent) {
            initOnLongPress.invoke(this@TableRecyclerView,e)
        }
    }

    private val mGestureDetector = GestureDetector(context, simpleOnGestureListener)

    var initOnClickListener :(View,MotionEvent)->Boolean = {view,event->
        false
    }
    var initOnLongPress :(View,MotionEvent)->Unit = {view,event->
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {

        when(ev.action){
            MotionEvent.ACTION_DOWN->{

            }
            MotionEvent.ACTION_MOVE->{

            }
            MotionEvent.ACTION_CANCEL->{

            }
        }
        mGestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

}