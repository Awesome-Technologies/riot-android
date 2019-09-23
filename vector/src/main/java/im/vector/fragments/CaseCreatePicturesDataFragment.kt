package im.vector.fragments

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import com.bumptech.glide.Glide
import im.vector.R
import im.vector.util.PERMISSIONS_FOR_TAKING_PHOTO
import im.vector.util.PERMISSION_REQUEST_CODE_LAUNCH_NATIVE_CAMERA
import im.vector.util.checkPermissions
import im.vector.util.openCamera
import kotlinx.android.synthetic.main.activity.*
import kotlinx.android.synthetic.main.fragment_case_create_pictures.*

class CaseCreatePicturesDataFragment : CaseCreateDataFragment() {
    var mLatestTakePictureCameraUri: String? = null // has to be String not Uri because of Serializable

    /*
    * *********************************************************************************************
    * Fragment lifecycle
    * *********************************************************************************************
    */

    val layoutResId: Int
        get() = R.layout.fragment_case_create_pictures

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutResId, null)
    }

    private fun initViews() {
        Log.e(LOG_TAG,"InitViews")

        save_data_button.setOnClickListener {
            detailActivity.saveData(this)
        }

        val imageButton = ImageButton(context)
        imageButton.setImageResource(R.drawable.ic_material_camera)
        imageButton.setColorFilter(Color.WHITE)
        imageButton.setOnClickListener {
            startCameraIntent()
        }
        imageButton.setBackgroundColor(Color.TRANSPARENT)
        detailActivity.toolbar.addView(imageButton)
        val param = imageButton.layoutParams as androidx.appcompat.widget.Toolbar.LayoutParams
        param.gravity = Gravity.END
        param.setMargins(0, 0, 24, 0)
        imageButton.layoutParams = param

        for (bitmap in pictures) {
            addImageView(bitmap)
        }
    }

    override fun getResultIntent(): Intent {
        val intent = Intent()

        intent.putExtra(PICTURES, pictures)

        return intent
    }

    private fun startCameraIntent() {
        if (checkPermissions(PERMISSIONS_FOR_TAKING_PHOTO, this, PERMISSION_REQUEST_CODE_LAUNCH_NATIVE_CAMERA)) {
            mLatestTakePictureCameraUri = openCamera(detailActivity, "Care_Image", REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun addImageView(uri: Uri) {
        val imageView = ImageView(context)
        val params = GridLayout.LayoutParams()
        params.width = picture_layout.width / 3
        params.height = GridLayout.LayoutParams.WRAP_CONTENT
        imageView.layoutParams = params
        imageView.setPadding(12, 12, 12, 12)

        imageView.run {
            Glide.with(imageView)
                    .load(uri)
                    .into(imageView)
        }
        picture_layout.addView(imageView)
    }

    fun addPicture(uri: Uri) {
        pictures.add(uri)
        addImageView(uri)
    }

    companion object {
        private val LOG_TAG = CaseCreatePicturesDataFragment::class.java.simpleName
        val PICTURES = "PICTURES"
        val REQUEST_IMAGE_CAPTURE = 11109
        private var pictures: ArrayList<Uri> = ArrayList()


        fun newInstance(pictures: ArrayList<Uri>): CaseCreatePicturesDataFragment {
            val fragment = CaseCreatePicturesDataFragment()
            Companion.pictures = pictures

            return fragment
        }
    }
}