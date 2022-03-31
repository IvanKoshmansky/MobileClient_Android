package com.example.android.mobileclient.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.LinearLayout

//здесь если делать по книге (первичный конструктор - вторичный конструктор - делегирование),
//то можно реализовать только через второй параметр = null во вторичном конструкторе
//что-то похожее можно сделать через фабрику и объект компаньон
//class LinearLayoutEx (context: Context?, attrs: AttributeSet?) : LinearLayout (context, attrs) {
//    constructor (context: Context?) : this (context, null) {}
//однако разрешается напрямую вызывать нужный конструктор супер класса столько раз
//сколько требуется и наследовать каждый из них
//при этом первичный конструктор остается без изменений (параметров)

class LinearLayoutEx : LinearLayout  {

    constructor (context: Context?) : super (context) {}
    constructor (context: Context?, attrs: AttributeSet?) : super (context, attrs) {}

    private var linePaint: Paint? = null
    private var layoutRect: Rect = Rect()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val strokeWidth = this.context.resources.displayMetrics.scaledDensity * 1
        linePaint = Paint(0)
        linePaint!!.color = -0xaaaaab  //TODO: убрать хардкод
        linePaint!!.strokeWidth = strokeWidth
        linePaint!!.style = Paint.Style.STROKE
        val rect = Rect()
        val paddingTop = paddingTop
        getDrawingRect(rect)
        layoutRect = Rect(rect.left, rect.top + paddingTop, rect.right, rect.bottom)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        val rect = Rect()
        linePaint?.let {
            canvas.drawRect(layoutRect, it)
            var x = layoutRect.left.toFloat()
            for (ii in 0 until childCount - 1) {
                val view = getChildAt(ii)
                if (view != null) {
                    view.getDrawingRect(rect)
                    x += rect.width().toFloat()
                    canvas.drawLine(x, layoutRect.top.toFloat(), x, layoutRect.bottom.toFloat(), it)
                }
            }
        }
    }
}
