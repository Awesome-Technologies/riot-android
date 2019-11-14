package im.vector.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import im.vector.R
import im.vector.adapters.AdapterUtils
import im.vector.fragments.CaseDataListFragment
import kotlinx.android.synthetic.main.case_data_label_value_date_view.view.*
import org.matrix.androidsdk.rest.model.Event
import java.text.SimpleDateFormat
import java.util.*

open class CaseDataLabelValueDateView : CaseDataLabelValueView {

    override fun displayEvent(event: Event?, dataType: Int) {
        super.displayEvent(event, dataType)

        activity_indicator.visibility = View.VISIBLE

        if (event != null && dataType != CaseDataListFragment.CASE_DATA_NONE) {
            val obj = event.contentAsJsonObject
            if (obj.has("effectiveDateTime")) {
                val dateString = obj["effectiveDateTime"].asString
                if (dateString.isNotEmpty()) {
                    val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
                    val date = dateFormatter.parse(dateString)
                    date_label.text = AdapterUtils.tsToString(context, date.time, false)
                } else {
                    date_label.text = "-"
                }
            } else {
                date_label.text = "-"
            }
        }

        activity_indicator.visibility = View.GONE
    }

    override fun getLayoutRes(): Int {
        return R.layout.case_data_label_value_date_view
    }

    /**
     * constructors
     */
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)
}