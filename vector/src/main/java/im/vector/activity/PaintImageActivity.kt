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

package im.vector.activity

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.core.content.ContextCompat
import im.vector.R
import kotlinx.android.synthetic.main.activity_paint_image.*
import org.matrix.androidsdk.core.Log
import kotlin.math.roundToLong

/**
 * Class to enable the user to paint on images after they have been taken.
 */
class PaintImageActivity : VectorAppCompatActivity() {
    private var colorButtonsVisible = false

    override fun getLayoutRes(): Int {
        return R.layout.activity_paint_image
    }

    override fun getTitleRes(): Int {
        return R.string.paint_image_title
    }

    @CallSuper
    override fun initUiAndData() {
        super.initUiAndData()
        Log.d(LOG_TAG, "## init(): Show paint on image activity")

        configureToolbar()

        color_buttons.layoutParams.width = 0
        paintOnImageView.setStrokeColor(ContextCompat.getColor(this, R.color.paint_image_stroke_white))
        updateChangeColorButtonTint()

        intent.data?.let { uri ->
            paintOnImageView.setBitmap(uri)
        }

        send_button.setOnClickListener {
            val intent = Intent()
            intent.data = paintOnImageView.combinedBitmapUri()
            if (intent.data == null) {
                Toast.makeText(this, R.string.paint_image_write_to_file_error, Toast.LENGTH_LONG).show()
            } else {
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }

        remove_button.setOnClickListener {
            paintOnImageView.clear()
        }

        change_color_button.setOnClickListener {
            toggleColorButtonVisibility()
        }

        red_color_button.setOnClickListener {
            paintOnImageView.setStrokeColor(ContextCompat.getColor(this, R.color.paint_image_stroke_red))
            hideColorButtons()
        }

        blue_color_button.setOnClickListener {
            paintOnImageView.setStrokeColor(ContextCompat.getColor(this, R.color.paint_image_stroke_blue))
            hideColorButtons()
        }

        black_color_button.setOnClickListener {
            paintOnImageView.setStrokeColor(ContextCompat.getColor(this, R.color.paint_image_stroke_black))
            hideColorButtons()
        }

        white_color_button.setOnClickListener {
            paintOnImageView.setStrokeColor(ContextCompat.getColor(this, R.color.paint_image_stroke_white))
            hideColorButtons()
        }
    }

    private fun toggleColorButtonVisibility() {
        if (colorButtonsVisible) {
            hideColorButtons()
        } else {
            showColorButtons()
        }
    }

    private fun showColorButtons() {
        val newWidth = (color_buttons.parent as View).measuredWidth
        val duration = (100 * ((newWidth - color_buttons.measuredWidth) / newWidth.toFloat())).roundToLong()
        colorButtonsVisible = true

        animateColorButtons(newWidth, duration)
    }

    private fun hideColorButtons() {
        val parentWidth = (color_buttons.parent as View).measuredWidth
        val duration = (100 * (color_buttons.measuredWidth / parentWidth.toFloat())).roundToLong()
        colorButtonsVisible = false

        animateColorButtons(0, duration)

        updateChangeColorButtonTint()
    }

    private fun updateChangeColorButtonTint() {
        change_color_button.setColorFilter(paintOnImageView.strokeColor())
    }

    private fun animateColorButtons(newWidth: Int, duration: Long) {
        val valueAnimator = ValueAnimator.ofInt(color_buttons.measuredWidth, newWidth)
        valueAnimator.addUpdateListener { animation ->
            val newValue = animation?.animatedValue as Int
            val params = color_buttons.layoutParams as RelativeLayout.LayoutParams
            params.width = newValue
            color_buttons.layoutParams = params
        }
        valueAnimator.duration = duration
        valueAnimator.start()
    }

    companion object {
        private val LOG_TAG = PaintImageActivity::class.java.simpleName
    }
}