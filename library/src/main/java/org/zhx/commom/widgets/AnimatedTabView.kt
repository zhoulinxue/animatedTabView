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

    // item height
    private var mItemHeight = mRadius * 2

    //view width
    private var mWidth = mItemHeight * itemCount

    //view width
    private var mItemWidth = mItemHeight * itemCount

    //background Rect
    private var mBackgroundRectF: RectF? = null

    // animation process
    private var mProcess = 1f

    // translation valus
    private var mProcessValus = 0f
    private var currentX = mRadius
    private var targetX = 0
    private var targetPosition = 1
    private var mShowPosition = targetPosition
    private var mLastPosition = mShowPosition
    private val MAX_ALPHA = 255
    private var currentAlpha = MAX_ALPHA
    public var state: State = State.NORMAL
    private val valueAnimator: ValueAnimator by lazy {
        var valueAnimator = ValueAnimator()
        valueAnimator?.addUpdateListener(this)
        valueAnimator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mProcess = 1f
                mProcessValus = 0f
                currentX = getXByPosition(mShowPosition, 0).toInt()
                if (State.OPEN == state) {
                    state = State.NORMAL
                }
            }

            override fun onAnimationStart(animation: Animator) {
                mLastPosition = mShowPosition
                targetPosition = getCurrentPositionByX(targetX)
                if (State.NORMAL == state) {
                    mShowPosition = targetPosition
                    Log.e(TAG, "$mLastPosition   current : $mShowPosition")
                    if (mLastPosition != mShowPosition) {
                        mBuilder?.onItemClick?.onItemSelected(mShowPosition - 1)
                    }
                }
            }
        })
        valueAnimator
    }
    private val textRect = Rect()
    private var mBuilder: Builder? = null
    private val sparseArray = SparseArray<Bitmap>()

    private fun getTargetX(x: Float): Int {
        return (x / mItemWidth).toInt() * mItemWidth + mItemWidth / 2
    }

    private fun isMoving(): Boolean {
        return valueAnimator != null && valueAnimator?.isRunning!!
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = WidgetUtil.measureWidth(widthMeasureSpec, mWidth)
        mHeight = WidgetUtil.measureHeight(heightMeasureSpec, mHeight)
        setMeasuredDimension(mWidth, mHeight)
        currentX = getXByPosition(mShowPosition, 0).toInt()
        mItemWidth = mWidth / itemCount
    }

    public fun tocenter() {
        if (!isMoving()) {
            when {
                State.NORMAL == state -> {
                    state = State.CLOSE
                    if (currentX == mWidth / 2) {
                        currentX = getXByPosition(1, 0).toInt()
                    }
                    moveAnimation(mWidth / 2, currentX)
                }
                State.CLOSE == state -> {
                    state = State.OPEN
                    if (currentX == mWidth / 2) {
                        currentX = getXByPosition(1, 0).toInt()
                    }
                    moveAnimation(mWidth / 2, currentX)
                }
            }
        }
    }

    public enum class State {
        OPEN, CLOSE, NORMAL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (itemCount == 0) {
            return
        }
        if (State.NORMAL == state) {
            drawNomal(canvas)
        } else {
            val text = mBuilder?.arrays!![mShowPosition - 1]
            mTextPaint?.getTextBounds(text, 0, text.length, textRect)
            mTextPaint.alpha = MAX_ALPHA
            mTextPaint.color = mBuilder?.selectedTextColor!!
            var process = mProcess
            if (State.OPEN == state) {
                process = 1 - mProcess
            }

            var offset = (mWidth / 2 - mRadius) * process
            var right = mWidth - offset

            mBackgroundRectF = RectF(offset, 0f, right, mHeight.toFloat())
            var itemX = getXByPosition(mShowPosition, 0).toInt()

            var textStart = mRadius - textRect.width() / 2 + offset
            var cicleStart = offset + mRadius

            if (itemX > mWidth / 2) {
                textStart = right - mRadius - textRect.width() / 2
                cicleStart = right - mRadius
            } else if (itemX == mWidth / 2) {
                textStart = (mWidth / 2).toFloat() - textRect.width() / 2
                cicleStart = (mWidth / 2).toFloat()
            }
            drawbackGround(canvas)
            drawSelctedTag(
                canvas,
                cicleStart
            )
            canvas.drawText(
                text,
                textStart,
                (mRadius + textRect.height() / 2).toFloat(),
                mTextPaint)

            if (State.CLOSE == state) {
                mBitmapPaint.alpha = MAX_ALPHA - currentAlpha
            } else {
                mBitmapPaint.alpha = currentAlpha
            }
            for (i in 1 until itemCount + 1) {
                var positionX = getXByPosition(i, 0).toInt()
                val bitmap = sparseArray[i]
                // drawbitmap  except text
                if (positionX != itemX) {
                    var bitmapX = getXByPosition(i, bitmap.width / 2)
                    if (positionX > mWidth / 2) {
                        var rightX = right - mRadius - bitmap.width / 2

                        bitmapX = rightX
                    } else if (positionX < mWidth / 2) {
                        var leftX = offset + mRadius - bitmap.width / 2

                        bitmapX = leftX
                    }
                    canvas.drawBitmap(
                        bitmap, bitmapX,
                        (mHeight / 2 - bitmap.height / 2).toFloat(),
                        mBitmapPaint
                    )
                }
            }

        }
    }

    private fun drawNomal(canvas: Canvas) {
        mBackgroundRectF = RectF(0f, 0f, mWidth.toFloat(), mHeight.toFloat())
        drawbackGround(canvas)
        drawSelctedTag(canvas, currentX + mProcessValus)
        for (i in 1 until itemCount + 1) {
            val bitmap = sparseArray[i]
            val text = mBuilder?.arrays!![i - 1]
            mTextPaint?.getTextBounds(text, 0, text.length, textRect)
            if (mShowPosition != i && mLastPosition != i) {
                mBitmapPaint.alpha = MAX_ALPHA
                canvas.drawBitmap(
                    bitmap, getXByPosition(i, bitmap.width / 2),
                    (mHeight / 2 - bitmap.height / 2).toFloat(),
                    mBitmapPaint
                )
            } else if (mShowPosition != i && mLastPosition == i) {
                if (mBuilder?.unSelectedTextColor != 0) {
                    mTextPaint.color = mBuilder?.unSelectedTextColor!!
                } else {
                    mTextPaint.color = Color.WHITE
                }
                mTextPaint.alpha = MAX_ALPHA - currentAlpha
                canvas.drawText(
                    text,
                    getXByPosition(i, textRect.width() / 2),
                    (mHeight / 2 + textRect.height() / 2) * (1 - mProcess), mTextPaint
                )
                mBitmapPaint.alpha = currentAlpha
                canvas.drawBitmap(
                    bitmap,
                    getXByPosition(i, bitmap.width / 2),
                    mHeight / 2 - bitmap.height / 2 + bitmap.height * (1 - mProcess),
                    mBitmapPaint
                )
            } else if (mShowPosition == i) {
                if (mBuilder?.selectedTextColor != 0) {
                    mTextPaint.color = mBuilder?.selectedTextColor!!
                }
                mTextPaint.alpha = currentAlpha
                canvas.drawText(
                    text, getXByPosition(i, textRect.width() / 2),
                    (mHeight / 2 + textRect.height() / 2) * mProcess,
                    mTextPaint
                )
                if (mProcess != 1f) {
                    mBitmapPaint.alpha = MAX_ALPHA - currentAlpha
                    canvas.drawBitmap(
                        bitmap, getXByPosition(i, bitmap.width / 2),
                        mHeight / 2 - bitmap.height / 2 + mProcess * mHeight,
                        mBitmapPaint
                    )
                }
            }
        }

    }

    private fun drawSelctedTag(canvas: Canvas, start: Float) {
        canvas.drawCircle(
            start,
            mHeight / 2.toFloat(),
            (mRadius - 1).toFloat(),
            mCiclePaint
        ) //item cicle
    }

    private fun drawbackGround(canvas: Canvas) {
        if (mBuilder?.backgroundColor != 0) {
            mBuilder?.backgroundColor?.let {
                mBackgroundPaint.color = it
                canvas.drawRoundRect(
                    mBackgroundRectF!!, mRadius.toFloat(), mRadius.toFloat(),
                    mBackgroundPaint
                ) // background
            }
        }
    }

    /**
     *get current position by x
     */
    private fun getXByPosition(i: Int, offSet: Int): Float {
        return ((mWidth / itemCount) * i - (mWidth / itemCount) / 2 - offSet).toFloat()
    }

    /**
     * get current position by x
     */
    private fun getCurrentPositionByX(targetX: Int): Int {
        return targetX / (mWidth / itemCount) + 1
    }

    /**
     * anmate view
     */
    private fun moveAnimation(toTargetX: Int, currentX: Int) {
        targetX = toTargetX
        var totalValus = toTargetX - currentX
        valueAnimator.setFloatValues(0f, totalValus.toFloat())
        valueAnimator?.duration =mBuilder?.duration!!
        valueAnimator?.start()
    }


    override fun onAnimationUpdate(animation: ValueAnimator) {
        mProcessValus = animation.animatedValue as Float
        if (mProcessValus != 0f) {
            mProcess = mProcessValus / (targetX - currentX)
            currentAlpha = (MAX_ALPHA * mProcess).toInt()
            invalidate()
        }
    }

    private var mGestureDetector: GestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                val clickX: Int = getTargetX(e!!.x)
                Log.e(TAG, "$currentX  click x $clickX")
                if (!isMoving() && currentX != clickX) {
                    state = State.NORMAL
                    moveAnimation(clickX, currentX)
                }
                return true
            }
        });

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.e(TAG, "  event " + event.x)
        mGestureDetector.onTouchEvent(event)
        return true
    }

    fun setSelection(position: Int) {
        var realPosition = position + 1
        Log.e(TAG, realPosition.toString())
        if (realPosition in 1 until itemCount + 1 && realPosition != mShowPosition && !isMoving()) {
            moveAnimation(getXByPosition(realPosition, 0).toInt(), currentX)
        }
    }

    class Builder(private val context: Context) {
        var height = 120
        var width = 0
        var onItemClick: OnItemChangeLisenter? = null
        var backgroundColor: Int = 0
        var selectedTextColor: Int = 0
        var unSelectedTextColor: Int = 0
        lateinit var arrays: Array<String>
        lateinit var images: Array<Int>
        var duration: Long = 400

        fun setDuration(duration: Long): Builder {
            this.duration = duration
            return this
        }

        fun setOnItemClick(itemClick: OnItemChangeLisenter): Builder {
            this.onItemClick = itemClick
            return this
        }

        fun setBackgroundColor(color: Int): Builder {
            backgroundColor = color
            return this
        }

        fun setSelectedTextColor(color: Int): Builder {
            selectedTextColor = color
            return this
        }

        fun setUnSelectedTextColor(color: Int): Builder {
            unSelectedTextColor = color
            return this
        }

        fun setWidth(width: Int): Builder {
            this.width = width
            return this
        }

        fun setHeight(height: Int): Builder {
            this.height = height
            return this
        }

        fun setClick(click: OnItemChangeLisenter?): Builder {
            this.onItemClick = click
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

    public fun setBuilder(builder: Builder?) {
        mBuilder = builder
        if (builder != null) notifyParamchanage()
    }

    private fun notifyParamchanage() {
        if (mBuilder!!.arrays == null || mBuilder!!.images == null) {
            return
        }
        itemCount = mBuilder!!.arrays.size.coerceAtMost(mBuilder!!.images.size)
        mItemHeight = mBuilder!!.height
        mHeight = mItemHeight + paddingTop + paddingBottom
        mWidth = if (mBuilder!!.width == 0) {
            mItemHeight * itemCount
        } else {
            mBuilder!!.width
        }
        mRadius = mItemHeight / 2
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

    interface OnItemChangeLisenter {
        fun onItemSelected(position: Int)
    }


}