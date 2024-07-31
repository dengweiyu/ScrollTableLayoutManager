package com.weipai.scrolltablelayoutmanager.layoutmanager

import android.graphics.Rect
import android.util.SparseArray
import android.view.View

interface TableLayoutScrollControl {
    fun setCanScrollHorizontal(canScroll: Boolean)
    fun setCanScrollVertical(canScroll: Boolean)

}
interface TableLayoutFixRowColumnControl : TableLayoutScrollControl{
    fun findVisibleRowsWithBuffer(withBuffer: Boolean = true): Pair<Int, Int>
    fun findVisibleColumnsWithBuffer(withBuffer: Boolean = true): Pair<Int, Int>
    fun rowColumn2Position(row: Int, column: Int): Int
    fun position2RowColumn(columnCount: Int, position: Int): Pair<Int, Int>
    fun findViewByPosition(position: Int): View?
    fun getViewsRects(): SparseArray<Rect>
    fun getScrollHorizontalInt(): Int
    fun getScrollVerticalInt(): Int
    fun itemCount (): Int
    fun spanCount (): Int
}