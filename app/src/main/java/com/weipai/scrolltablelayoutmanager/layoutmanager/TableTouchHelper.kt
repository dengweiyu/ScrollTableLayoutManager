package com.weipai.scrolltablelayoutmanager.layoutmanager

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.SparseArray
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.drawToBitmap
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.weipai.scrolltablelayoutmanager.TableRecyclerView
import kotlin.math.abs

class TableTouchHelper {

    companion object {
        const val SCROLL_HORIZONTAL_AND_VERTICALLY = 1
        const val DEFAULT_SCROLL_MODE = 0
    }

    var distanceX = 0f
    var distanceY = 0f

    private var fixedRow = 0
    private var fixedColumn = 0
    private var dividerResId: Int = 0
    private var dividerResId2: Int = 0
    private var showDivider = false

    fun bindRecyclerView(recyclerView: RecyclerView, mode: Int = DEFAULT_SCROLL_MODE) : TableTouchHelper {
        val manager = recyclerView.layoutManager
        if (manager == null || manager !is TableLayoutScrollControl) throw IllegalStateException("invalid layoutManager")
        this.rv = recyclerView
        this.mode = mode
        this.rv?.addItemDecoration(itemDecoration)
        this.rv?.addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                showDivider =  abs(dx)>0
            }

        })
        setLayoutManager(manager)
        setOnFixClickListener(recyclerView)
        return this
    }

    fun setDividerRes(@DrawableRes dividerResId: Int,@DrawableRes dividerResId2: Int = 0){
        this.dividerResId = dividerResId
        this.dividerResId2 = dividerResId2
    }

    private fun setOnFixClickListener(recyclerView: RecyclerView) {
        if (recyclerView is TableRecyclerView){
            recyclerView.initOnClickListener = {view,e->
                var isHandled = false
                val manager = (recyclerView.layoutManager as? TableLayoutFixRowColumnControl?)
                if (manager != null){
                    val rects = manager.getViewsRects()
                    for (i in 0 until  rects.size()){
                        val firstRect = manager.getViewsRects().valueAt(0)
                        if (e.y <= firstRect.bottom && e.x >firstRect.left){
                            if(i > manager.spanCount() * fixedRow) break
                            val rect = Rect(rects.valueAt(i))
                            rect.offset(-manager.getScrollHorizontalInt(),0)
                            if (rect.contains(e.x.toInt() , e.y.toInt())){
                                manager.findViewByPosition(i)?.apply {
                                    performClick()
                                    Log.d(this@TableTouchHelper.javaClass.simpleName,"$i")
                                }
                                isHandled = true
                                break
                            }
                        }else if (e.y >  firstRect.bottom && e.x <= firstRect.right){
                            val columnPos = i % (manager.spanCount())
                            if(columnPos < fixedColumn) {
                                val rect = Rect(rects.valueAt(i))
                                rect.offset(0, -manager.getScrollVerticalInt())
                                if (rect.contains(e.x.toInt(), e.y.toInt())) {
                                    manager.findViewByPosition(i)?.apply {
                                        performClick()
                                        Log.d(this@TableTouchHelper.javaClass.simpleName,"$i")
                                    }
                                    isHandled = true
                                    break
                                }
                            }
                        }else{//其他项
                            val rect = Rect(rects.valueAt(i))
                            rect.offset(-manager.getScrollHorizontalInt(), -manager.getScrollVerticalInt())
                            if (rect.contains(e.x.toInt(), e.y.toInt())) {
                                manager.findViewByPosition(i)?.apply {
                                    performClick()
                                    Log.d(this@TableTouchHelper.javaClass.simpleName,"$i")
                                }
                                isHandled = true
                                break
                            }
                        }

                    }

                }
                isHandled
            }
            recyclerView.initOnLongPress = {view,e->
                val manager = (recyclerView.layoutManager as? TableLayoutFixRowColumnControl?)
                if (manager != null){
                    val rects = manager.getViewsRects()
                    for (i in 0 until  rects.size()){
                        val firstRect = manager.getViewsRects().valueAt(0)
                        if (e.y <= firstRect.bottom && e.x >firstRect.left){
                            if(i > manager.spanCount() * fixedRow) break
                            val rect = Rect(rects.valueAt(i))
                            rect.offset(-manager.getScrollHorizontalInt(),0)
                            if (rect.contains(e.x.toInt() , e.y.toInt())){
                                manager.findViewByPosition(i)?.apply {
                                    performLongClick()
                                    Log.d(this@TableTouchHelper.javaClass.simpleName,"$i")
                                }
                                break
                            }
                        }else if (e.y >  firstRect.bottom && e.x <= firstRect.right){
                            val columnPos = i % (manager.spanCount())
                            if(columnPos < fixedColumn) {
                                val rect = Rect(rects.valueAt(i))
                                rect.offset(0, -manager.getScrollVerticalInt())
                                if (rect.contains(e.x.toInt(), e.y.toInt())) {
                                    manager.findViewByPosition(i)?.apply {
                                        performLongClick()
                                        Log.d(this@TableTouchHelper.javaClass.simpleName,"$i")
                                    }
                                    break
                                }
                            }
                        }else{//其他项
                            val rect = Rect(rects.valueAt(i))
                            rect.offset(-manager.getScrollHorizontalInt(), -manager.getScrollVerticalInt())
                            if (rect.contains(e.x.toInt(), e.y.toInt())) {
                                manager.findViewByPosition(i)?.apply {
                                    performLongClick()
                                    Log.d(this@TableTouchHelper.javaClass.simpleName,"$i")
                                }
                                break
                            }
                        }

                    }

                }
            }
        }
    }


    private var layoutManager: LayoutManager? = null
    private var rv: RecyclerView? = null

    var mode: Int = DEFAULT_SCROLL_MODE
        set(value) {
            field = value
            val manager = layoutManager
            if (manager is TableLayoutScrollControl) {
                initGestureListener(manager)
            }
        }

    private fun setLayoutManager(layout: LayoutManager?) {
        if (layout is TableLayoutScrollControl) {
            layoutManager = layout
            initGestureListener(layout)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initGestureListener(layoutManager: TableLayoutScrollControl) {
        rv?.let { rv ->
            if (mode == DEFAULT_SCROLL_MODE) {
                val simpleOnGestureListener = object : GestureDetector.SimpleOnGestureListener() {
                    override fun onScroll(
                        e1: MotionEvent?,
                        e2: MotionEvent,
                        distanceX: Float,
                        distanceY: Float
                    ): Boolean {
                        this@TableTouchHelper.distanceX = distanceX
                        this@TableTouchHelper.distanceY = distanceY
                        checkScroll(layoutManager, distanceX, distanceY)
                        return false
                    }
                }
                val mGestureDetector = GestureDetector(rv.context, simpleOnGestureListener)
                rv.overScrollMode = View.OVER_SCROLL_NEVER
                rv.setOnTouchListener { v, event ->
                    mGestureDetector.onTouchEvent(event)
                }
            } else {
                rv.setOnTouchListener { v, event ->
                    false
                }
                rv.overScrollMode = View.OVER_SCROLL_ALWAYS
            }
        }
    }

    private fun checkScroll(layoutManager: TableLayoutScrollControl, x: Float, y: Float) {
        if (abs(x) > abs(y)) {
            // 横向滑动
            layoutManager.setCanScrollHorizontal(true)
            layoutManager.setCanScrollVertical(false)
        } else if (abs(x) < abs(y)) {
            // 纵向滑动
            layoutManager.setCanScrollHorizontal(false)
            layoutManager.setCanScrollVertical(true)
        }
    }

    var onFixRowClick : ((View,Int)->Unit) ?= null

    private val rowDrawables by lazy { SparseArray<Drawable>() }
    private val columnDrawables by lazy { SparseArray<Drawable>() }
    private val rowClickListener by lazy { SparseArray<OnClickListener>() }
    private val columnClickListener by lazy { SparseArray<OnClickListener>() }
    private lateinit var shadow :Drawable
    private lateinit var shadowRight :Drawable
    private val itemDecoration= object : ItemDecoration(){
        override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            super.onDrawOver(c, parent, state)
            Log.d(this@TableTouchHelper.javaClass.simpleName,"onDraw")

            if (parent.childCount > 0) {
                (parent.getChildAt(0).layoutParams as? RecyclerView.LayoutParams)?.let {params->
                    (parent.layoutManager as? TableLayoutFixRowColumnControl?)?.let{manager->
                        if (fixedRow > 0) {
                            val pairRow = manager.findVisibleRowsWithBuffer(false)
                            for (row in pairRow.first .. pairRow.second){
                                kotlin.runCatching {
                                    val position = manager.rowColumn2Position(row,0)
                                    val view = manager.findViewByPosition(position)
                                    val drawable = kotlin.runCatching { columnDrawables.valueAt(row) }.getOrNull() ?: (view?.drawToBitmap(Bitmap.Config.ARGB_8888)?.toDrawable(parent.context.resources).apply {
                                        columnDrawables.put(row,this)
                                        val onClickListener = OnClickListener{view->
                                            onFixRowClick?.invoke(view,position)
                                        }
                                        view?.setOnClickListener(onClickListener)
                                        columnClickListener.put(row,onClickListener)
                                    })
                                    drawable?.bounds = manager.getViewsRects().get(position)
                                    drawable?.bounds?.offset(0,-manager.getScrollVerticalInt())
                                    drawable?.draw(c)
                                }.onFailure {
                                    it.printStackTrace()
                                }
                            }
                        }
                        if (fixedColumn>0){
                            val pairColumn = manager.findVisibleColumnsWithBuffer(false)
                            for (column in pairColumn.first .. pairColumn.second){
                                kotlin.runCatching {
                                    val position = manager.rowColumn2Position(0,column)
                                    val view = manager.findViewByPosition(position)
                                    val drawable = kotlin.runCatching { rowDrawables.valueAt(column) }.getOrNull() ?: (view?.drawToBitmap(Bitmap.Config.ARGB_8888)?.toDrawable(parent.context.resources).apply {
                                        rowDrawables.put(column,this)
                                        val onClickListener = OnClickListener{view->
                                            onFixRowClick?.invoke(view,position)
                                        }
                                        view?.setOnClickListener(onClickListener)
                                        rowClickListener.put(column,onClickListener)
                                    })
                                    drawable?.bounds = manager.getViewsRects().get(column)
                                    drawable?.bounds?.offset(-manager.getScrollHorizontalInt(),0)
                                    drawable?.draw(c)
                                }.onFailure {
                                    it.printStackTrace()
                                }
                            }
                            val position = manager.rowColumn2Position(0,0)
                            val view = manager.findViewByPosition(position)
                            val drawable = kotlin.runCatching { columnDrawables.valueAt(0) }.getOrNull() ?: (view?.drawToBitmap(Bitmap.Config.ARGB_8888)?.toDrawable(parent.context.resources).apply {
                                columnDrawables.put(0,this)
                                val onClickListener = OnClickListener{view->
                                    onFixRowClick?.invoke(view,position)
                                }
                                view?.setOnClickListener(onClickListener)
                                columnClickListener.put(0,onClickListener)
                            })
                            drawable?.bounds = manager.getViewsRects().get(position)
                            drawable?.bounds?.offset(0,0)
                            drawable?.draw(c)
                        }
                        if (!::shadow.isInitialized) {
                            kotlin.runCatching {  shadow = ContextCompat.getDrawable(parent.context, dividerResId)!! }
                            kotlin.runCatching {  shadowRight = ContextCompat.getDrawable(parent.context, dividerResId2)!! }
                            kotlin.runCatching {
                                var left = kotlin.runCatching { manager.getViewsRects().get(0).right }.getOrNull()?:0
                                val top = kotlin.runCatching { manager.getViewsRects().get(0).bottom }.getOrNull()?:0
                                val bottom = parent.measuredHeight
                                var right = left + shadow.intrinsicWidth
                                shadow.setBounds(left, top, right, bottom)
                                right = parent.measuredWidth
                                left = right - shadow.intrinsicWidth
                                shadowRight.setBounds(left ,top,right,bottom)
                            }
                        }
                        if (::shadow.isInitialized && showDivider && parent.scrollState != SCROLL_STATE_IDLE){
                            shadow.draw(c)
                            shadowRight.draw(c)
                        }
                    }
                }
            }

        }
    }

    /**
     * 锁定第几行 从1开始算 >=1
     */
    fun setFixedLeftRow(boolean: Boolean){
        if (this.rv !is TableRecyclerView) throw IllegalStateException("wrong rv!")
        this.fixedRow = if (boolean) 1 else 0
    }

    /**
     * 锁定第几列 从1开始算 >=1
     */
    fun setFixedTopColumn(boolean : Boolean){
        if (this.rv !is TableRecyclerView) throw IllegalStateException("wrong rv!")
        this.fixedColumn = if (boolean) 1 else 0
    }
}