package com.weipai.scrolltablelayoutmanager.layoutmanager

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class ScrollTableLayoutManager : RecyclerView.LayoutManager(),TableLayoutFixRowColumnControl{

    var canScrollY = true
    var canScrollX = true
    var columnCount: Int = 0
    private var rv: RecyclerView? = null
    private var mFirstVisiPos = 0 //屏幕可见的第一个View的Position
    private var mLastVisiPos = 0 //屏幕可见的最后一个View的Position
    private var mLastCount = 0
    private var mVerticalOffset = 0 //竖直偏移量 每次换行时，要根据这个offset判断
    private var mHorizontalOffset = 0 //竖直偏移量 每次换行时，要根据这个offset判断
    private var mSparseArrayRect = SparseArray<Rect>()
    private var mSparseArrayColumnRect = SparseArray<Int>()
    private var mSparseArrayRowRect = SparseArray<Int>()
    private val visRect = Rect()
    private val mainRect = Rect()
    private var rowCount = 0
    var maxShowColumnCount = 0
    var maxShowRowCount = 0
    private val bufferCount = 1
    private var fixRow = 1
    private var fixColumn= 1
    var onLayoutComplete :(()->Unit )?= null
    var recycler: RecyclerView.Recycler?=null
    var state: RecyclerView.State?=null

    override fun onAttachedToWindow(view: RecyclerView?) {
        super.onAttachedToWindow(view)
        this.rv = view
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun generateLayoutParams(c: Context, attrs: AttributeSet?): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(c, attrs)
    }

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams): RecyclerView.LayoutParams {
        return if (lp is MarginLayoutParams) {
            RecyclerView.LayoutParams(lp)
        } else {
            RecyclerView.LayoutParams(lp)
        }
    }

    override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
        return lp is  RecyclerView.LayoutParams
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (itemCount == 0) { //没有Item，界面空着吧
            detachAndScrapAttachedViews(recycler)
            return
        }
        if (childCount == 0 && state.isPreLayout) { //state.isPreLayout()是支持动画的
            return
        }
        this.recycler = recycler
        this.state = state
        mLastCount = itemCount - 1
        if (visRect.isEmpty){
            visRect.set(visRect.left,visRect.top,visRect.left + width,visRect.top + height)
        }
        if (mainRect.isEmpty){
            fill(recycler, state)
        }
    }

    override fun onLayoutCompleted(state: RecyclerView.State?) {
        super.onLayoutCompleted(state)
        onLayoutComplete?.invoke()
    }
    override fun canScrollHorizontally(): Boolean {
        return canScrollX
    }

    override fun canScrollVertically(): Boolean {
        return canScrollY
    }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        //位移0、没有子View 当然不移动
        if (dx == 0 || childCount == 0) {
            return 0
        }
        if (mainRect.width() < visRect.width()) return 0
        var realOffset = dx //实际滑动的距离， 可能会在边界处被修复
        //边界修复代码
        if (mHorizontalOffset + realOffset < 0) { //左边界
            val firstChild = getChildAt(0)
            if (firstChild != null && getPosition(firstChild) == 0) {
                val gap = paddingLeft - getDecoratedLeft(firstChild)
                realOffset = if (gap > 0) {
                    - gap
                } else if (gap == 0) {
                    0
                } else {
                    min(realOffset.toDouble(), -gap.toDouble()).toInt()
                }
            }
        } else if (realOffset > 0) { //右边界
            //利用最后一个子View比较修正
//            val lastChild = getChildAt(childCount - 1)
            if (visRect.right + realOffset >= mainRect.right) {
//                val gap = width - paddingRight - (kotlin.runCatching { getDecoratedRight(lastChild!!) }.getOrNull()?:0)

//            if (getPosition(lastChild!!) == itemCount - 1) {
//                val gap = width - paddingRight - getDecoratedRight(lastChild)
//                realOffset = if (gap > 0) {
//                    -gap
//                } else if (gap == 0) {
//                    0
//                } else {
//                    min(realOffset.toDouble(), -gap.toDouble()).toInt()
//                }
                realOffset = mainRect.right - visRect.right
            }
        }
        mHorizontalOffset += realOffset //累加实际滑动距离
        visRect.offset(realOffset,0)
        fill(recycler, state, dx = dx)
        offsetChildrenHorizontal(-realOffset) //滑动
        Log.e("mainRect","$mainRect  visRect ${visRect}")
        return realOffset
    }

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        //位移0、没有子View 当然不移动
        if (dy == 0 || childCount == 0) {
            return 0
        }
        if (mainRect.height() < visRect.height()) return 0
        var realOffset = dy //实际滑动的距离， 可能会在边界处被修复
        //边界修复代码
        if (mVerticalOffset + realOffset < 0) { //上边界
            val firstChild = getChildAt(0)
            if (firstChild != null && getPosition(firstChild) == 0) {
                val gap = paddingTop - getDecoratedTop(firstChild)
                realOffset = if (gap > 0) {
                    - gap
                } else if (gap == 0) {
                    0
                } else {
                    min(realOffset.toDouble(), -gap.toDouble()).toInt()
                }
            }
        } else if (realOffset > 0) { //下边界
            //利用最后一个子View比较修正
//            val lastChild = getChildAt(childCount - 1)
            if (visRect.bottom + realOffset >= mainRect.bottom) {
//                val gap = height - paddingBottom - (kotlin.runCatching { getDecoratedBottom(lastChild!!) }.getOrNull()?:0)

//            if (getPosition(lastChild!!) == itemCount - 1) {
//                val gap = height - paddingBottom - getDecoratedBottom(lastChild)
//                realOffset = if (gap > 0) {
//                    -gap
//                } else if (gap == 0) {
//                    0
//                } else {
//                    min(realOffset.toDouble(), -gap.toDouble()).toInt()
//                }
                realOffset = mainRect.bottom - visRect.bottom
            }
        }
        mVerticalOffset += realOffset //累加实际滑动距离
        visRect.offset(0,realOffset)
        fill(recycler, state, dy = dy)
        offsetChildrenVertical(-realOffset) //滑动
        Log.e("mainRect","$mainRect  visRect ${visRect}")
        return realOffset
    }

    private fun fill(recycler: RecyclerView.Recycler, state: RecyclerView.State, dx : Int = 0, dy: Int = 0) {
        var topOffset = paddingTop
        var leftOffset = paddingLeft
        rowCount = ceil(itemCount.toDouble() / columnCount.toDouble()).toInt()
        var lineMaxHeight = 0
        var lineMaxWidth = 0
        var pairColumn = findVisibleColumnsWithBuffer()
        var pairRow = findVisibleRowsWithBuffer()
        if (mainRect.isEmpty) {
            for (i in 0..itemCount - 1) {
                val child = recycler.getViewForPosition(i)
                measureChildWithMargins(child, 0, 0)
                val xy = position2RowColumn(columnCount,position = i)
                val width = max(kotlin.runCatching {
                    mSparseArrayColumnRect.get(i % columnCount)
                }.getOrNull() ?: 0, getDecoratedMeasurementHorizontal(child))
                val height = max(kotlin.runCatching {
                    mSparseArrayRowRect.get(i / columnCount)
                }.getOrNull() ?: 0, getDecoratedMeasurementVertical(child))
                mSparseArrayColumnRect.put(
                    xy.second,
                    max(
                        width,
                        kotlin.runCatching { mSparseArrayColumnRect.get(xy.first) }.getOrNull() ?: 0
                    )
                )
                mSparseArrayRowRect.put(
                    xy.first,
                    max(
                        height,
                        kotlin.runCatching { mSparseArrayRowRect.get(xy.first) }.getOrNull() ?: 0
                    )
                )
            }
            if (mainRect.width() == 0) {
                var calculateShowItemWidth = 0
                var getMaxColumnShowCount = false
                for (i in 0 until mSparseArrayColumnRect.size()) {
                    calculateShowItemWidth += mSparseArrayColumnRect.get(i)
                    if (!getMaxColumnShowCount && calculateShowItemWidth > width) {
                        maxShowColumnCount = i + 3
                        mFirstVisiPos = 0
                        getMaxColumnShowCount = true
                    }
                }
                mainRect.set(
                    mainRect.left,
                    mainRect.top,
                    max(mainRect.right, calculateShowItemWidth),
                    mainRect.bottom
                )
            }
            if (mainRect.height() == 0) {
                var calculateShowItemHeight = 0
                var getMaxRowShowCount = false
                for (i in 0 until mSparseArrayRowRect.size()) {
                    calculateShowItemHeight += mSparseArrayRowRect.get(i)
                    if (!getMaxRowShowCount && calculateShowItemHeight > height) {
                        maxShowRowCount = i + 3
                        mLastVisiPos = maxShowColumnCount * maxShowRowCount
                        getMaxRowShowCount = true
                    }
                }
                mainRect.set(
                    mainRect.left,
                    mainRect.top,
                    mainRect.right,
                    max(mainRect.bottom, calculateShowItemHeight)
                )
            }
            pairColumn = Pair(0,columnCount -1)
            pairRow = Pair(0,rowCount-1)
        }
        var minPos = 0
        var maxPos = itemCount - 1
        if (dy > 0 || dx > 0){
            if (childCount > 0) {
                val lastView = getChildAt(childCount - 1)
                minPos = getPosition(lastView!!) + 1
            }
        }else if (dy <0 || dx < 0) {
            if (childCount > 0) {
                val firstView = getChildAt(0)
                kotlin.runCatching {
                    maxPos = getPosition(firstView!!) - 1
                }
            }
        }
        if (dx==0 && dy == 0){
            detachAndScrapAttachedViews(recycler)
        }else{
            recyclerView(recycler,pairColumn,pairRow)
        }
        for (b in pairRow.first .. pairRow.second) {
            for (a in pairColumn.first .. pairColumn.second ) {
                val i = rowColumn2Position(b,a)
                if (dy > 0 && minPos > i ) continue
                if (dx > 0 && minPos > i ) continue
                if ((dy < 0 || dx < 0 )&& maxPos < i ) continue
                val child = kotlin.runCatching { recycler.getViewForPosition(i) }.getOrNull()?:continue
                addView(child)
                measureChildWithMargins(child, 0, 0)
                val width = max(kotlin.runCatching {
                    mSparseArrayColumnRect.get(i % columnCount)
                }.getOrNull() ?: 0, getDecoratedMeasurementHorizontal(child))
                val height = max(kotlin.runCatching {
                    mSparseArrayRowRect.get(i / columnCount)
                }.getOrNull() ?: 0, getDecoratedMeasurementVertical(child))

                val rect = kotlin.runCatching { mSparseArrayRect.get(i) }.getOrNull() ?: Rect(
                    leftOffset , topOffset ,
                    leftOffset + width,
                    topOffset + height
                ).apply {
                    mSparseArrayRect.put(i, this)
                }
                layoutDecoratedWithMargins(
                    child,
                    rect.left,
                    rect.top,
                    rect.right,
                    rect.bottom
                )
                if (a == pairColumn.second){
                    Log.d(this.javaClass.simpleName,"onLayoutChildren ${a} rect ${rect} ${rv?.measuredWidth} ${mainRect} vis ${visRect}")
                }
                leftOffset += width
                lineMaxHeight = max(lineMaxHeight.toDouble(), height.toDouble()).toInt()
                lineMaxWidth = max(lineMaxWidth.toDouble(), width.toDouble()).toInt()
            }
            leftOffset = paddingLeft
            topOffset += lineMaxHeight
        }
    }


    fun getVerticalSpace(): Int {
        return height - paddingTop - paddingBottom
    }

    fun getHorizontalSpace(): Int {
        return width - paddingLeft - paddingRight
    }

    /**
     * 获取某个childView在水平方向所占的空间
     *
     * @param view
     * @return
     */
    fun getDecoratedMeasurementHorizontal(view: View): Int {
        val params = view.layoutParams as RecyclerView.LayoutParams
        return (getDecoratedMeasuredWidth(view) + params.leftMargin
                + params.rightMargin)
    }

    /**
     * 获取某个childView在竖直方向所占的空间
     *
     * @param view
     * @return
     */
    fun getDecoratedMeasurementVertical(view: View?): Int {
        val params = view!!.layoutParams as RecyclerView.LayoutParams
        return (getDecoratedMeasuredHeight(view) + params.topMargin
                + params.bottomMargin)
    }


    override fun rowColumn2Position(row:Int, column :Int) : Int{
        return row * columnCount + column
    }
    override fun position2RowColumn(columnCount : Int, position : Int) : Pair<Int,Int>{
        return Pair(position/columnCount,position%columnCount)
    }


    override fun findVisibleColumnsWithBuffer(withBuffer : Boolean): Pair<Int, Int> {
        var startColumn = 0
        var leftOffset = 0
        for (i in 0 until mSparseArrayColumnRect.size()) {
            leftOffset += mSparseArrayColumnRect.valueAt(i)
            if (leftOffset >= visRect.left) {
                startColumn = mSparseArrayColumnRect.keyAt(i)
                break
            }
        }

        var endColumn = startColumn
        leftOffset = 0
        for (i in 0 until mSparseArrayColumnRect.size()) {
            leftOffset += mSparseArrayColumnRect.valueAt(i)
            if (leftOffset >= visRect.right) {
                endColumn = mSparseArrayColumnRect.keyAt(i)
                break
            }
        }
        if (!withBuffer) return Pair(startColumn,endColumn)
        val bufferColumns = bufferCount
        val bufferedStartColumn = max(0, startColumn - bufferColumns)
        val bufferedEndColumn = min(columnCount - 1, endColumn + bufferColumns)

        return Pair(bufferedStartColumn, bufferedEndColumn)
    }

    override fun findVisibleRowsWithBuffer(withBuffer: Boolean): Pair<Int, Int> {
        var startRow = 0
        var topOffset = 0
        for (i in 0 until mSparseArrayRowRect.size()) {
            topOffset += mSparseArrayRowRect.valueAt(i)
            if (topOffset >= visRect.top) {
                startRow = mSparseArrayRowRect.keyAt(i)
                break
            }
        }

        var endRow = startRow
        topOffset = 0
        for (i in 0 until mSparseArrayRowRect.size()) {
            topOffset += mSparseArrayRowRect.valueAt(i)
            if (topOffset >= visRect.bottom) {
                endRow = mSparseArrayRowRect.keyAt(i)
                break
            }
            if (i == mSparseArrayRowRect.size()-1&& topOffset <= visRect.height()){
                endRow = mSparseArrayRowRect.size()-1
            }
        }
        if (!withBuffer) return Pair(startRow,endRow)
        val bufferRows = bufferCount
        val bufferedStartRow = max(0, startRow - bufferRows)
        val bufferedEndRow = min(mSparseArrayRowRect.size(), endRow + bufferRows)

        return Pair(bufferedStartRow, bufferedEndRow)
    }


    private fun recyclerView(
        recycler: RecyclerView.Recycler,
        pairColumn: Pair<Int, Int>,
        pairRow: Pair<Int, Int>
    ){
        var recyclerViewCount = 0
        for (row in 0 ..< mSparseArrayRowRect.size()) {
            if (row < pairRow.first || row > pairRow.second) {
                for (column in 0..<mSparseArrayColumnRect.size()) {
                    if (column < pairColumn.first || column > pairColumn.second) {
                        val position = rowColumn2Position(row,column)
                        if (position < itemCount){
                            val child = recycler.getViewForPosition(position)
                            removeAndRecycleView(child,recycler)
                            recyclerViewCount ++
                        }
                    }
                }
            }
        }

        Log.d(this.javaClass.simpleName,"recyclerViewCount $recyclerViewCount")
    }


    override fun setCanScrollHorizontal(canScroll: Boolean) {
        this.canScrollX = canScroll
    }

    override fun setCanScrollVertical(canScroll: Boolean) {
        this.canScrollY = canScroll
    }


    override fun getViewsRects() = mSparseArrayRect

    override fun getScrollHorizontalInt() = visRect.left
    override fun getScrollVerticalInt() = visRect.top

    override fun findViewByPosition(position: Int): View? {
        return super.findViewByPosition(position)
    }

    override fun itemCount(): Int {
        return itemCount
    }

    override fun spanCount(): Int {
        return columnCount
    }

}