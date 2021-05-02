package org.zhx.commom.widgets

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import java.util.*


class AnimatedTabView : View, ValueAnimator.AnimatorUpdateListener {

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet, arg: Int) : super(
        context,
        attributeSet,
        arg
    )

    /**
     * bitmap paint
     */
    private val mBitmapPaint: Paint by lazy {
        Paint().also {
            it.color = Color.WHITE
            it.style = Paint.Style.FILL
        }
    }

    /**
     * cicle paint
     */
    private val mCiclePaint: Paint by lazy {
        Paint().also {
            it.color = resources.getColor(R.color.white)
            it.style = Paint.Style.STROKE
        }

    }
    private val mTextPaint: Paint by lazy {
        var paint = Paint()
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        paint.textSize = 30f
        paint
    }
    private val mBackgroundPaint: Paint by lazy {
        var paint = Paint()
        paint.color = resources.getColor(R.color.black_30)
        paint.style = Paint.Style.FILL
        paint
    }
    private val TAG = AnimatedTabView::class.java.simpleName

    //item  count
    private var itemCount = 0

    // radius
    private var mRadius = 0

    // item height
    private var mHeight = mRadius * 2

    //view width
    private var mWidth = mHeight * itemCount

    //background Rect
    private var mCicleRectF: RectF? = null

    // animation process
    private var mProcess = 1f

    // translation valus
    private var mProcessValus = 0f
    private var currentX = mRadius
    private var targetX = 0
    private var currentPosition = 1
    private var mLastPosition = currentPosition
    private val MAX_ALPHA = 255
    private var currentAlpha = MAX_ALPHA
    private val valueAnimator: ValueAnimator by lazy {
        var valueAnimator = ValueAnimator()
        valueAnimator?.addUpdateListener(this)
        valueAnimator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mProcess = 1f
                mProcessValus = 0f
                currentX = targetX
            }

            override fun onAnimationStart(animation: Animator) {
                mLastPosition = currentPosition
                currentPosition = getCurrentPositionByX(targetX)
                Log.e(TAG, "$mLastPosition   current : $currentPosition")
            }
        })
        valueAnimator
    }
    private val textRect = Rect()
    private val ANIMATION_DURATION: Long = 400
    private var mBuilder: Builder? = null
    private val sparseArray = SparseArray<Bitmap>()
    private fun getTargetX(x: Float): Int {
        return if (x <= mHeight) {
            mHeight - mRadius
        } else {
            (x / mHeight).toInt() * mHeight + mRadius
        }
    }

    private fun isMoving(): Boolean {
        return valueAnimator != null && valueAnimator?.isRunning!!
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = WidgetUtil.measureWidth(widthMeasureSpec, mWidth)
        mHeight = WidgetUtil.measureHeight(heightMeasureSpec, mHeight)
        setMeasuredDimension(mWidth, mHeight)
        mRadius = mHeight / 2
        mCicleRectF = RectF(0f, 0f, mWidth.toFloat(), mHeight.toFloat())
        currentX = mRadius
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (itemCount == 0) {
            return
        }
        canvas.drawRoundRect(
            mCicleRectF!!, mRadius.toFloat(), mRadius.toFloat(),
            mBackgroundPaint!!
        ) // background
        canvas.drawCircle(
            currentX + mProcessValus,
            mRadius.toFloat(),
            (mRadius - 1).toFloat(),
            mCiclePaint
        ) //item cicle


        for (i in 1 until itemCount + 1) {
            val bitmap = sparseArray[i]
            val text = mBuilder?.arrays!![i - 1]
            mTextPaint?.getTextBounds(text, 0, text.length, textRect)
            if (currentPosition != i && mLastPosition != i) {
                mBitmapPaint.alpha = MAX_ALPHA
                canvas.drawBitmap(
                    bitmap, getXByPosition(i, bitmap.width / 2),
                    (mRadius - bitmap.height / 2).toFloat(),
                    mBitmapPaint
                )
            } else if (currentPosition != i && mLastPosition == i) {
                mTextPaint.alpha = MAX_ALPHA - currentAlpha
                canvas.drawText(
                    text,
                    getXByPosition(i, textRect.width() / 2),
                    (mRadius + textRect.height() / 2) * (1 - mProcess), mTextPaint
                )
                mBitmapPaint.alpha = currentAlpha
                canvas.drawBitmap(
                    bitmap,
                    getXByPosition(i, bitmap.width / 2),
                    mRadius - bitmap.height / 2 + bitmap.height * (1 - mProcess),
                    mBitmapPaint
                )
            } else if (currentPosition == i) {
                mTextPaint.alpha = currentAlpha
                canvas.drawText(
                    text, getXByPosition(i, textRect.width() / 2),
                    (mRadius + textRect.height() / 2) * mProcess,
                    mTextPaint
                )
                if (mProcess != 1f) {
                    mBitmapPaint.alpha = MAX_ALPHA - currentAlpha
                    canvas.drawBitmap(
                        bitmap, getXByPosition(i, bitmap.width / 2),
                        mRadius - bitmap.height / 2 + mProcess * mHeight,
                        mBitmapPaint
                    )
                }
            }
        }
    }

    /**
     *get current position by x
     */
    private fun getXByPosition(i: Int, offSet: Int): Float {
        return (mRadius * 2 * i - mRadius - offSet).toFloat()
    }

    /**
     * get current position by x
     */
    private fun getCurrentPositionByX(targetX: Int): Int {
        return targetX / mHeight + 1
    }

    /**
     * anmate view
     */
    private fun moveAnimation(toTargetX: Int) {
        targetX = toTargetX
        val totalValus = toTargetX - currentX
        valueAnimator.setFloatValues(0f, totalValus.toFloat())
        valueAnimator?.duration = ANIMATION_DURATION
        valueAnimator?.start()
    }


    override fun onAnimationUpdate(animation: ValueAnimator) {
        mProcessValus = animation.animatedValue as Float
        if (mProcessValus != 0f) {
            mProcess = mProcessValus / (targetX - currentX)
            currentAlpha = (255 * mProcess).toInt();
            invalidate()
        }
    }

    private var mGestureDetector: GestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                val clickX: Int = getTargetX(e!!.x)
                Log.e(TAG, "$currentX  click x $clickX")
                if (!isMoving() && currentX != clickX) {
                    moveAnimation(clickX)
                }
                return true
            }
        });

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.e(TAG, "  event " + event.x)
        mGestureDetector.onTouchEvent(event)
        return true
    }


    class Builder(private val context: Context) {
        var height = 120
        var width = 0
        var click: OnItemClick? = null
        lateinit var arrays: Array<String>
        lateinit var images: Array<Int>
        fun setWidth(width: Int): Builder {
            this.width = width
            return this
        }

        fun setHeight(height: Int): Builder {
            this.height = height
            return this
        }

        fun setClick(click: OnItemClick?): Builder {
            this.click = click
            return this
        }

        fun setArrays(arrays: Array<String>): Builder {
            this.arrays = arrays
            return this
        }

        fun setImages(images: Array<Int>): Builder {
            this.images = images
            return this
        }

        fun build(): AnimatedTabView {
            val tabView = AnimatedTabView(context)
            tabView.setBuilder(this@Builder)
            return tabView
        }
    }

    private fun setBuilder(builder: Builder?) {
        mBuilder = builder
        if (builder != null) notifyParamchanage()
    }

    private fun notifyParamchanage() {
        if (mBuilder!!.arrays == null || mBuilder!!.images == null) {
            return
        }
        itemCount = Math.min(mBuilder!!.arrays.size, mBuilder!!.images.size)
        mHeight = mBuilder!!.height
        mWidth = if (mBuilder!!.width == 0) {
            mHeight * itemCount
        } else {
            mBuilder!!.width
        }
        mRadius = mHeight / 2
        for (i in 0 until itemCount) {
            val bitmap = sparseArray[i + 1]
            if (bitmap == null) {
                sparseArray.put(
                    i + 1,
                    BitmapFactory.decodeResource(resources, mBuilder!!.images[i])
                )
            }
        }
        postInvalidate()
    }

    interface OnItemClick {
        fun onItemClick(position: Int)
    }


}