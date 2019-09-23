package im.vector.view

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.util.Log
import im.vector.R
import kotlinx.android.synthetic.main.case_data_label_value_view.view.*
import kotlinx.android.synthetic.main.case_data_patient_view.view.*
import kotlinx.android.synthetic.main.case_data_patient_view.view.label
import org.matrix.androidsdk.rest.model.Event

class CaseCreateDataPicturesView : CaseCreateDataLabelValueView {

    override fun getLayoutRes(): Int {
        return R.layout.case_data_label_value_view
    }

    /**
     * constructors
     */
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    companion object {
        var pictures: ArrayList<Bitmap> = ArrayList()
    }
}