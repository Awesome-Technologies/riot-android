package im.vector.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import im.vector.R
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_ANAMNESIS
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_BLOOD_PRESSURE
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_BODY_TEMPERATURE
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_BODY_WEIGHT
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_GLUCOSE
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_LAST_DEFECATION
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_MISC
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_OXYGEN
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_PAIN
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_PICTURES
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_PULSE
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_RECIPIENT
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_RESPONSIVENESS
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_SEVERITY
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_TITLE
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_VITALS
import im.vector.activity.CaseCreateActivity.Companion.caseDataMap
import im.vector.adapters.ParticipantAdapterItem
import kotlinx.android.synthetic.main.case_data_label_value_view.view.*

open class CaseCreateDataLabelValueView : RelativeLayout {

    open fun fillView(type: Int) {
        var labelText = ""
        var valueText = ""

        activity_indicator.visibility = View.GONE
        when (type) {
            CASE_DATA_TITLE -> {
                labelText = context.getString(R.string.case_data_title)
                val title = caseDataMap[CASE_DATA_TITLE] as? String
                title?.let {
                    valueText = it
                } ?: run {
                    valueText = ""
                }
            }
            CASE_DATA_RECIPIENT -> {
                labelText = context.getString(R.string.case_data_recipient)
                val recipient = caseDataMap[CASE_DATA_RECIPIENT] as? ParticipantAdapterItem
                recipient?.let {
                    valueText = it.mDisplayName
                } ?: run {
                    valueText = ""
                }
            }
            CASE_DATA_SEVERITY -> {
                labelText = context.getString(R.string.case_data_severity)
                label.setTextColor(Color.parseColor("#FFFFFF"))
                val severity = caseDataMap[CASE_DATA_SEVERITY] as String

                value.setTextColor(Color.WHITE)

                when (severity) {
                    "info" -> {
                        valueText = context.getString(R.string.case_data_severity_info)
                        setBackgroundColor(Color.parseColor("#45abf2"))
                    }
                    "request" -> {
                        valueText = context.getString(R.string.case_data_severity_request)
                        setBackgroundColor(Color.parseColor("#26de82"))
                    }
                    "urgent" -> {
                        valueText = context.getString(R.string.case_data_severity_urgent)
                        setBackgroundColor(Color.parseColor("#f7c930"))
                    }
                    "critical" -> {
                        valueText = context.getString(R.string.case_data_severity_critical)
                        setBackgroundColor(Color.parseColor("#eb3b59"))
                    }
                }
            }
            CASE_DATA_ANAMNESIS -> {
                labelText = context.getString(R.string.case_data_anamnesis)
                val types = listOf(CASE_DATA_RESPONSIVENESS,
                        CASE_DATA_MISC,
                        CASE_DATA_LAST_DEFECATION,
                        CASE_DATA_PAIN)
                valueText = getCountOfTypes(types)
            }
            CASE_DATA_VITALS -> {
                labelText = context.getString(R.string.case_data_vitals)
                val types = listOf(CASE_DATA_BODY_WEIGHT,
                        CASE_DATA_BLOOD_PRESSURE,
                        CASE_DATA_OXYGEN,
                        CASE_DATA_PULSE,
                        CASE_DATA_GLUCOSE,
                        CASE_DATA_BODY_TEMPERATURE)
                valueText = getCountOfTypes(types)
            }
            CASE_DATA_PICTURES -> {
                labelText = context.getString(R.string.case_data_pictures)

                val picArray = caseDataMap[CASE_DATA_PICTURES] as? ArrayList<*>
                var pictureCount = 0
                picArray?.let {
                    pictureCount = it.size
                }

                valueText =  resources.getQuantityString(R.plurals.case_create_pictures, pictureCount, pictureCount)
            }
        }
        label.text = labelText
        value.text = valueText
    }

    private fun getCountOfTypes(types: List<Int>): String {
        var count = caseDataMap.count { types.contains(it.key) }
        return resources.getQuantityString(R.plurals.case_create_entries, count, count)
    }


    open fun getLayoutRes() : Int {
        return R.layout.case_data_label_value_view
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