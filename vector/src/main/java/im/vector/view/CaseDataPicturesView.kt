package im.vector.view

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.gson.JsonElement
import im.vector.Matrix
import im.vector.R
import im.vector.fragments.CaseDataListFragment
import kotlinx.android.synthetic.main.case_data_label_value_view.view.*
import kotlinx.android.synthetic.main.case_data_pictures_view.view.*
import org.matrix.androidsdk.MXSession
import org.matrix.androidsdk.core.JsonUtils
import org.matrix.androidsdk.core.callback.ApiCallback
import org.matrix.androidsdk.core.model.MatrixError
import org.matrix.androidsdk.listeners.MXMediaDownloadListener
import org.matrix.androidsdk.rest.model.Event
import org.matrix.androidsdk.rest.model.message.ImageMessage
import java.io.File

class CaseDataPicturesView : GridLayout {
    var mSession: MXSession? = null

    fun displayEvent(event: Event?, dataType: Int = -1) {
        if (event == null || dataType == CaseDataListFragment.CASE_DATA_NONE) {
            label.text = ""
            value.text = ""
        } else {
            visibility = View.VISIBLE
            val message = JsonUtils.toMessage(event.content)

            if (null != message && message is ImageMessage) {

                val mediasCache = Matrix.getInstance(context).mediaCache
                if (mediasCache.isMediaCached(message.getUrl(), message.mimeType)) {

                    createTmpFile(message)
                } else {
                    val downloadId = mediasCache.downloadMedia(context,
                            mSession?.homeServerConfig,
                            message.getUrl(),
                            message.mimeType,
                            message.file)

                    if (null != downloadId) {
                        mediasCache.addDownloadListener(downloadId, object : MXMediaDownloadListener() {
                            override fun onDownloadError(downloadId: String, jsonElement: JsonElement) {
                                val error = JsonUtils.toMatrixError(jsonElement)

                                if (null != error && error.isSupportedErrorCode) {
                                    Toast.makeText(context, error.localizedMessage, Toast.LENGTH_LONG).show()
                                }
                            }

                            override fun onDownloadComplete(aDownloadId: String) {
                                if (aDownloadId == downloadId) {
                                    createTmpFile(message)
                                }
                            }
                        })
                    }
                }
            }
        }
    }

    fun createTmpFile(message: ImageMessage) {
        val mediasCache = Matrix.getInstance(context).mediaCache

        mediasCache.createTmpDecryptedMediaFile(message.getUrl(), message.mimeType, message.file, object : ApiCallback<File> {
            override fun onSuccess(mediaFile: File?) {

                val uri = Uri.fromFile(mediaFile)

                addImageView(uri)

            }

            private fun onError(errorMessage: String) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }

            override fun onNetworkError(e: Exception) {
                onError(e.localizedMessage)
            }

            override fun onMatrixError(e: MatrixError) {
                onError(e.localizedMessage)
            }

            override fun onUnexpectedError(e: Exception) {
                onError(e.localizedMessage)
            }

        })
    }

    fun addImageView(uri: Uri) {
        val imageView = ImageView(context)
        val params = LayoutParams()
        params.width = width / 3
        params.height = LayoutParams.WRAP_CONTENT
        params.setMargins(12, 12, 12, 12)
        imageView.layoutParams = params

        imageView.run {
            Glide.with(imageView)
                    .load(uri)
                    .into(imageView)
        }
        grid.addView(imageView)
    }

    fun getLayoutRes() : Int {
        return R.layout.case_data_pictures_view
    }

    /**
     * constructors
     */
    constructor(context: Context) : super(context) {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        setup()
    }

    /*
     * *********************************************************************************************
     * Private methods
     * *********************************************************************************************
     */

    /**
     * Setup the view
     */
    private fun setup() {
        LayoutInflater.from(context).inflate(getLayoutRes(), this)
    }
}