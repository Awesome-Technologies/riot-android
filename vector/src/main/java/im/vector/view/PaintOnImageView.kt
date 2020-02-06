/*
 * Copyright 2020 Awesome Technologies Innovationslabor GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.view

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.provider.MediaStore
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import androidx.annotation.ColorInt
import org.matrix.androidsdk.core.Log
import java.io.IOException
import kotlin.math.abs

class PaintOnImageView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var mOriginalBitmap: Bitmap? = null

    private var mOriginalUri: Uri? = null
    private var mBitmapPaint = Paint(Paint.FILTER_BITMAP_FLAG)

    private var mColorPaths = mutableMapOf<@ColorInt Int, Path>()
    private var mCachedPaints = mutableMapOf<@ColorInt Int, Paint>()
    private var mCurrentColor = -1

    private var mCanvasSize = RectF()

    private var lastX = 0f
    private var lastY = 0f

    private val tolerance = 3

    fun setStrokeColor(@ColorInt newColor: Int) {
        mCurrentColor = newColor
        cacheColor(newColor)

        invalidate()
    }

    private fun getPath(@ColorInt color: Int): Path {
        if (!mColorPaths.containsKey(color)) {
            mColorPaths[color] = Path()
        }

        return mColorPaths[color]!!
    }

    @ColorInt
    fun strokeColor(): Int {
        return mCurrentColor
    }

    private fun cacheColor(@ColorInt color: Int) {
        if (!mCachedPaints.containsKey(color)) {
            val paint = Paint()
            paint.color = color
            paint.isAntiAlias = true
            paint.style = Paint.Style.STROKE
            paint.strokeJoin = Paint.Join.ROUND
            paint.strokeWidth = 14f

            mCachedPaints[color] = paint
        }
    }

    fun clear() {
        mColorPaths.clear()

        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mOriginalBitmap?.let { originalBitmap ->
            val bitmapRatio = originalBitmap.width / originalBitmap.height.toFloat()
            val params = layoutParams as RelativeLayout.LayoutParams
            params.height = (w / bitmapRatio).toInt()
            layoutParams = params

            mCanvasSize = RectF(0f, 0f, w.toFloat(), params.height.toFloat())
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        mOriginalBitmap?.let {
            canvas?.drawBitmap(it, null, mCanvasSize, null)
        }
        mColorPaths.forEach {
            val paint = mCachedPaints[it.key]!!
            canvas?.drawPath(it.value, paint)
        }
    }

    private fun touchStarted(x: Float, y: Float)  {
        getPath(mCurrentColor).let { path ->
            path.moveTo(x, y)
            lastX = x
            lastY = y
            invalidate()
        }
    }

    private fun touchMoved(x: Float, y: Float) {
        val dx = abs(x - lastX)
        val dy = abs(y - lastY)
        if (dx >= tolerance || dy >= tolerance) {
            getPath(mCurrentColor).let { path ->
                path.quadTo(lastX, lastY, (x + lastX) / 2f, (y + lastY) / 2f)
                lastX = x
                lastY = y
                invalidate()
            }
        }
    }

    private fun touchEnded(x: Float, y: Float) {
        mColorPaths[mCurrentColor]?.let { path ->
            path.lineTo(x, y)
            invalidate()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return super.onTouchEvent(event)
        }
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStarted(x, y)
            MotionEvent.ACTION_MOVE -> touchMoved(x, y)
            MotionEvent.ACTION_UP -> touchEnded(x, y)
            else -> {
                return false
            }
        }

        return true
    }

    fun setBitmap(uri: Uri) {
        mOriginalUri = uri
        mOriginalBitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        mOriginalBitmap?.let {
            invalidate()
        }
        clear()
    }

    fun combinedBitmapUri(): Uri? {
        if (mColorPaths.isEmpty()) {
            return mOriginalUri
        }
        try {
            mOriginalBitmap?.let {originalBitmap ->
                val resultingBitmap = Bitmap.createBitmap(originalBitmap.width, originalBitmap.height, Bitmap.Config.ARGB_8888)

                val matrix = Matrix()
                matrix.setScale(originalBitmap.width / width.toFloat(), originalBitmap.height / height.toFloat())
                val scaledPaths = mColorPaths

                val canvas = Canvas(resultingBitmap)
                canvas.drawBitmap(originalBitmap, 0f, 0f, mBitmapPaint)
                scaledPaths.forEach { pair ->
                    pair.value.transform(matrix)
                    mCachedPaints[pair.key]?.let { paint ->
                        canvas.drawPath(pair.value, paint)
                    }
                }

                mOriginalUri?.let { originalUri ->
                    context.contentResolver.openOutputStream(originalUri)?.let { out ->
                        resultingBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    }
                }
            }
            return mOriginalUri
        } catch (e: IOException) {
            Log.e(LOG_TAG, "## combinedBitmapUri(): IOException trying to write bitmap to file: " + e.localizedMessage)
            e.printStackTrace()
        }
        return null
    }

    companion object {
        private val LOG_TAG = PaintOnImageView::class.java.simpleName
    }
}