package com.example.android.bootservice


import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.IBinder
import android.view.*
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.view.WindowManager.LayoutParams
import android.widget.Button
import android.widget.Toast


class OverlayShowingService : Service(), OnTouchListener, OnClickListener {

    private var topLeftView: View? = null

    private lateinit var button: Button
    private var offsetX: Float = 0.toFloat()
    private var offsetY: Float = 0.toFloat()
    private var originalXPos: Int = 0
    private var originalYPos: Int = 0
    private var moving: Boolean = false
    private lateinit var windowManager: WindowManager

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager


        val layoutFlag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        createOverlay(layoutFlag)

        createRefPoint(layoutFlag)

    }

    private fun createOverlay(layoutFlag: Int) {
        button = Button(this)
        button.text = "Danger"
        button.textSize = 20.toFloat()
        button.setOnTouchListener(this)
        button.alpha = 0.5.toFloat()
        button.setBackgroundColor(Color.BLACK)
        button.setTextColor(Color.GREEN)
        button.setOnClickListener(this)

        val params = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                layoutFlag,
                LayoutParams.FLAG_NOT_TOUCH_MODAL or LayoutParams.FLAG_LAYOUT_IN_SCREEN or LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)

        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)

        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(button.width, View.MeasureSpec.UNSPECIFIED)
        val height = View.MeasureSpec.makeMeasureSpec(button.height, View.MeasureSpec.UNSPECIFIED)

        button.measure(widthMeasureSpec, height)

        params.x = size.x / 2 - button.measuredWidth / 2
        params.y = 0
        params.gravity = Gravity.START or Gravity.TOP
        windowManager.addView(button, params)
    }

    private fun createRefPoint(layoutFlag: Int) {
        topLeftView = View(this)
        val topLeftParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                layoutFlag,
                LayoutParams.FLAG_NOT_FOCUSABLE or LayoutParams.FLAG_LAYOUT_IN_SCREEN or LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT)

        topLeftParams.gravity = Gravity.START or Gravity.TOP
        topLeftParams.x = 0
        topLeftParams.y = 0
        topLeftParams.width = 0
        topLeftParams.height = 0
        windowManager.addView(topLeftView, topLeftParams)
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(button)
        windowManager.removeView(topLeftView)
        topLeftView = null
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {

        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.rawX
            val y = event.rawY

            moving = false

            val location = IntArray(2)
            button.getLocationOnScreen(location)

            originalXPos = location[0]
            originalYPos = location[1]

            offsetX = originalXPos - x
            offsetY = originalYPos - y

        } else if (event.action == MotionEvent.ACTION_MOVE) {
            val topLeftLocationOnScreen = IntArray(2)
            topLeftView!!.getLocationOnScreen(topLeftLocationOnScreen)

            println("topLeftY=" + topLeftLocationOnScreen[1])
            println("originalY=$originalYPos")

            val x = event.rawX
            val y = event.rawY

            val params = button.layoutParams as LayoutParams

            val newX = (offsetX + x).toInt()
            val newY = (offsetY + y).toInt()

            if (Math.abs(newX - originalXPos) < 1 && Math.abs(newY - originalYPos) < 1 && !moving) {
                return false
            }

            params.x = newX - topLeftLocationOnScreen[0]
            params.y = newY - topLeftLocationOnScreen[1]

            windowManager.updateViewLayout(button, params)
            moving = true
        } else if (event.action == MotionEvent.ACTION_UP) {
            if (moving) {
                return true
            }
        }

        return false
    }

    override fun onClick(v: View) {
        Toast.makeText(this, "Danger! Danger! Will Roberson", Toast.LENGTH_SHORT).show()
    }
}