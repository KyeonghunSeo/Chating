package com.ayaan.twelvepages.ui.view.base

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.ayaan.twelvepages.AppTheme
import com.ayaan.twelvepages.dpToPx
import com.ayaan.twelvepages.l
import java.util.ArrayList


class CalendarBackground @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {
    private val paint = Paint()
    private val strokeWidth = dpToPx(0f)
    private val dashEffect = DashPathEffect(floatArrayOf(dpToPx(6.0f), dpToPx(3.0f)), 2f)

    init {
        paint.color = Color.parseColor("#50000000")
        setWillNotDraw(false)
    }
    
    val pointList = ArrayList<Point>()
    val secondPointList = ArrayList<Point>()
    var headerHeight = 0
    var calendarPadding = 0

    fun setDragPoint(startCellNum: Int, endCellNum: Int, weekLys: Array<FrameLayout>,
                     minWidth: Float, headerHeight: Int, calendarPadding: Int) {
        pointList.clear()
        secondPointList.clear()
        this.headerHeight = headerHeight
        this.calendarPadding = calendarPadding

        val sColumn = startCellNum % 7
        val sRow = startCellNum / 7
        val eColumn = endCellNum % 7
        val eRows = endCellNum / 7
        val cellWidth = minWidth.toInt()
        
        var p: Point
        p = Point() // 시작 : 왼쪽 상단
        for (i in 0 until sColumn) {
            p.x += cellWidth
        }
        p.x += strokeWidth.toInt()
        for (i in 0 until sRow) {
            p.y += weekLys[i].height
        }
        p.y += strokeWidth.toInt()
        pointList.add(p)

        if (sRow == eRows) {
            p = Point() // 오른쪽 상단
            for (i in 0..eColumn) {
                p.x += cellWidth
            }
            p.x -= strokeWidth.toInt()
            for (i in 0 until sRow) {
                p.y += weekLys[i].height
            }
            p.y += strokeWidth.toInt()
            pointList.add(p)

            p = Point() // 오른쪽 하단
            for (i in 0..eColumn) {
                p.x += cellWidth
            }
            p.x -= strokeWidth.toInt()
            for (i in 0..eRows) {
                p.y += weekLys[i].height
            }
            p.y -= strokeWidth.toInt()
            pointList.add(p)

            p = Point() // 왼쪽 하단
            for (i in 0 until sColumn) {
                p.x += cellWidth
            }
            p.x += strokeWidth.toInt()
            for (i in 0..eRows) {
                p.y += weekLys[i].height
            }
            p.y -= strokeWidth.toInt()
            pointList.add(p)

            p = Point() // 끝 : 왼쪽 상단
            for (i in 0 until sColumn) {
                p.x += cellWidth
            }
            p.x += strokeWidth.toInt()
            for (i in 0 until sRow) {
                p.y += weekLys[i].height
            }
            p.y += strokeWidth.toInt()
            pointList.add(p)
        } else if (sRow + 1 == eRows && eColumn % 7 < sColumn % 7) { // 2개로 나눠질때
            p = Point() // 오른쪽 상단
            for (i in 0 until 7) {
                p.x += cellWidth
            }
            p.x -= strokeWidth.toInt()
            for (i in 0 until sRow) {
                p.y += weekLys[i].height
            }
            p.y += strokeWidth.toInt()
            pointList.add(p)

            p = Point() // 오른쪽 하단
            for (i in 0 until 7) {
                p.x += cellWidth
            }
            p.x -= strokeWidth.toInt()
            for (i in 0..sRow) {
                p.y += weekLys[i].height
            }
            p.y -= strokeWidth.toInt()
            pointList.add(p)

            p = Point() // 왼쪽 하단
            for (i in 0 until sColumn) {
                p.x += cellWidth
            }
            p.x += strokeWidth.toInt()
            for (i in 0..sRow) {
                p.y += weekLys[i].height
            }
            p.y -= strokeWidth.toInt()
            pointList.add(p)

            p = Point() // 끝 : 왼쪽 상단
            for (i in 0 until sColumn) {
                p.x += cellWidth
            }
            p.x += strokeWidth.toInt()
            for (i in 0 until sRow) {
                p.y += weekLys[i].height
            }
            p.y += strokeWidth.toInt()
            pointList.add(p)

            p = Point() // 2번 왼쪽 상단
            p.x = 0
            p.x += strokeWidth.toInt()
            for (i in 0 until eRows) {
                p.y += weekLys[i].height
            }
            p.y += strokeWidth.toInt()
            secondPointList.add(p)

            p = Point() // 2번 오른쪽 상단
            for (i in 0..eColumn) {
                p.x += cellWidth
            }
            p.x -= strokeWidth.toInt()
            for (i in 0 until eRows) {
                p.y += weekLys[i].height
            }
            p.y += strokeWidth.toInt()
            secondPointList.add(p)

            p = Point() // 2번 오른쪽 하단
            for (i in 0..eColumn) {
                p.x += cellWidth
            }
            p.x -= strokeWidth.toInt()
            for (i in 0..eRows) {
                p.y += weekLys[i].height
            }
            p.y -= strokeWidth.toInt()
            secondPointList.add(p)

            p = Point() // 2번 왼쪽 하단
            p.x = 0
            p.x += strokeWidth.toInt()
            for (i in 0..eRows) {
                p.y += weekLys[i].height
            }
            p.y -= strokeWidth.toInt()
            secondPointList.add(p)

            p = Point() // 2번 끝 : 왼쪽 상단
            p.x = 0
            p.x += strokeWidth.toInt()
            for (i in 0 until eRows) {
                p.y += weekLys[i].height
            }
            p.y += strokeWidth.toInt()
            secondPointList.add(p)
        } else {
            p = Point() // 오른쪽 상단
            for (i in 0 until 7) {
                p.x += cellWidth
            }
            p.x -= strokeWidth.toInt()
            for (i in 0 until sRow) {
                p.y += weekLys[i].height
            }
            p.y += strokeWidth.toInt()
            pointList.add(p)

            if (eColumn % 7 == 6) { // 마지막 셀에서 끝날때
                p = Point() // 오른쪽 하단
                for (i in 0..eColumn) {
                    p.x += cellWidth
                }
                p.x -= strokeWidth.toInt()
                for (i in 0..eRows) {
                    p.y += weekLys[i].height
                }
                p.y -= strokeWidth.toInt()
                pointList.add(p)
            } else { // 중간 꺾이는 지점이 생길때
                p = Point() // 오른쪽 중단
                for (i in 0 until 7) {
                    p.x += cellWidth
                }
                p.x -= strokeWidth.toInt()
                for (i in 0 until eRows) {
                    p.y += weekLys[i].height
                }
                p.y -= strokeWidth.toInt()
                pointList.add(p)

                p = Point() // 오른쪽 중단
                for (i in 0..eColumn) {
                    p.x += cellWidth
                }
                p.x -= strokeWidth.toInt()
                for (i in 0 until eRows) {
                    p.y += weekLys[i].height
                }
                p.y -= strokeWidth.toInt()
                pointList.add(p)

                p = Point() // 오른쪽 하단
                for (i in 0..eColumn) {
                    p.x += cellWidth
                }
                p.x -= strokeWidth.toInt()
                for (i in 0..eRows) {
                    p.y += weekLys[i].height
                }
                p.y -= strokeWidth.toInt()
                pointList.add(p)
            }

            p = Point() // 왼쪽 하단
            p.x = 0
            p.x += strokeWidth.toInt()
            for (i in 0..eRows) {
                p.y += weekLys[i].height
            }
            p.y -= strokeWidth.toInt()
            pointList.add(p)

            if (sColumn % 7 == 0) {
                p = Point() // 끝 : 왼쪽 상단
                for (i in 0 until sColumn) {
                    p.x += cellWidth
                }
                p.x += strokeWidth.toInt()
                for (i in 0 until sRow) {
                    p.y += weekLys[i].height
                }
                p.y += strokeWidth.toInt()
                pointList.add(p)
            } else {
                p = Point() // 왼쪽 중단
                p.x = 0
                p.x += strokeWidth.toInt()
                for (i in 0..sRow) {
                    p.y += weekLys[i].height
                }
                p.y += strokeWidth.toInt()
                pointList.add(p)

                p = Point() // 왼쪽 중단
                for (i in 0 until sColumn) {
                    p.x += cellWidth
                }
                p.x += strokeWidth.toInt()
                for (i in 0..sRow) {
                    p.y += weekLys[i].height
                }
                p.y += strokeWidth.toInt()
                pointList.add(p)

                p = Point() // 끝 : 왼쪽 상단
                for (i in 0 until sColumn) {
                    p.x += cellWidth
                }
                p.x += strokeWidth.toInt()
                for (i in 0 until sRow) {
                    p.y += weekLys[i].height
                }
                p.y += strokeWidth.toInt()
                pointList.add(p)
            }
        }
        invalidate()
    }

    fun clearDragPoint() {
        pointList.clear()
        secondPointList.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        if(pointList.isNotEmpty()) {
            drawPoints(pointList, canvas)
        }
        if(secondPointList.isNotEmpty()) {
            drawPoints(secondPointList, canvas)
        }
        super.onDraw(canvas)
    }

    private fun drawPoints(list: ArrayList<Point>, canvas: Canvas?) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dpToPx(3f)
        paint.pathEffect = dashEffect
        val path = Path()
        list.forEachIndexed { index, p ->
            if(index == 0) {
                path.moveTo(p.x.toFloat() + calendarPadding, p.y.toFloat() + headerHeight)
            }else {
                path.lineTo(p.x.toFloat() + calendarPadding, p.y.toFloat() + headerHeight)
            }
        }
        path.close()
        canvas?.drawPath(path, paint)
    }
}