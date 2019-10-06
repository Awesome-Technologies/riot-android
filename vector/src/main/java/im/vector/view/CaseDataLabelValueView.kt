package im.vector.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import im.vector.R
import im.vector.adapters.AdapterUtils
import im.vector.fragments.CaseDataListFragment
import kotlinx.android.synthetic.main.case_data_label_value_view.view.*
import org.matrix.androidsdk.rest.model.Event
import java.text.SimpleDateFormat
import java.util.*

open class CaseDataLabelValueView : RelativeLayout {

    open fun displayEvent(event: Event?, dataType: Int = -1) {
        if (event == null || dataType == CaseDataListFragment.CASE_DATA_NONE) {
            label.text = ""
            value.text = ""
        } else {
            visibility = View.VISIBLE
            activity_indicator.visibility = View.VISIBLE
            val obj = event.contentAsJsonObject

            when (dataType) {
                CaseDataListFragment.CASE_DATA_CREATED -> {
                    label.text = context.getString(R.string.case_data_created)

                    val creationDate = AdapterUtils.tsToString(context, event.getOriginServerTs(), false)
                    value.text = creationDate
                }
                CaseDataListFragment.CASE_DATA_PATIENT -> label.text = context.getString(R.string.case_data_patient)
                CaseDataListFragment.CASE_DATA_TITLE -> {
                    label.text = context.getString(R.string.case_data_title)
                    value.text = obj["title"].asString
                }
                CaseDataListFragment.CASE_DATA_SEVERITY -> {
                    label.text = context.getString(R.string.case_data_severity)
                    label.setTextColor(Color.WHITE)

                    val severity = obj["severity"].asString
                    value.setTextColor(Color.WHITE)

                    when (severity) {
                        "info" -> {
                            value.text = context.getString(R.string.case_data_severity_info)
                            setBackgroundColor(Color.parseColor("#45abf2"))
                        }
                        "request" -> {
                            value.text = context.getString(R.string.case_data_severity_request)
                            setBackgroundColor(Color.parseColor("#26de82"))
                        }
                        "urgent" -> {
                            value.text = context.getString(R.string.case_data_severity_urgent)
                            setBackgroundColor(Color.parseColor("#f7c930"))
                        }
                        "critical" -> {
                            value.text = context.getString(R.string.case_data_severity_critical)
                            setBackgroundColor(Color.parseColor("#eb3b59"))
                        }
                    }
                }
                CaseDataListFragment.CASE_DATA_REQUESTER -> {
                    label.text = context.getString(R.string.case_data_requester)
                    value.text = obj["requester"].asJsonObject["reference"].asString
                }
                CaseDataListFragment.CASE_DATA_RESPONSIVENESS -> {
                    label.text = context.getString(R.string.case_data_responsiveness)
                    value.text = obj["valueString"].asString
                }
                CaseDataListFragment.CASE_DATA_PAIN -> {
                    label.text = context.getString(R.string.case_data_pain)
                    value.text = obj["valueString"].asString
                }
                CaseDataListFragment.CASE_DATA_MISC -> {
                    label.text = context.getString(R.string.case_data_misc)
                    value.text = obj["valueString"].asString
                }
                CaseDataListFragment.CASE_DATA_LAST_DEFECATION -> {
                    label.text = context.getString(R.string.case_data_last_defecation)
                    val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.GERMANY)
                    val dateString = obj["effectiveDateTime"].asString
                    val date = dateFormatter.parse(dateString)
                    value.text = AdapterUtils.tsToString(context, date.time, false)
                }
                CaseDataListFragment.CASE_DATA_BODY_WEIGHT -> {
                    label.text = context.getString(R.string.case_data_body_weight)
                    value.text = obj["valueQuantity"].asJsonObject["value"].asString
                }
                CaseDataListFragment.CASE_DATA_BODY_TEMPERATURE -> {
                    label.text = context.getString(R.string.case_data_body_temperature)
                    value.text = obj["valueQuantity"].asJsonObject["value"].asString
                }
                CaseDataListFragment.CASE_DATA_GLUCOSE -> {
                    label.text = context.getString(R.string.case_data_glucose)
                    value.text = obj["valueQuantity"].asJsonObject["value"].asString
                }
                CaseDataListFragment.CASE_DATA_BLOOD_PRESSURE -> {
                    label.text = context.getString(R.string.case_data_blood_pressure)

                    val array = obj["component"].asJsonArray

                    var bloodPressure = ""
                    for (obj in array) {
                        val code = obj.asJsonObject["code"]
                        when (code.asJsonObject["coding"].asJsonArray[0].asJsonObject["code"].asString) {
                            "8480-6" -> {
                                val valueQuantity = obj.asJsonObject["valueQuantity"]
                                val value = valueQuantity.asJsonObject["value"].asString
                                val unit = valueQuantity.asJsonObject["unit"].asString
                                val combined = "$value $unit"
                                bloodPressure = if (bloodPressure.isEmpty()) combined else "$combined / $bloodPressure"
                            }
                            "8462-4" -> {
                                val valueQuantity = obj.asJsonObject["valueQuantity"]
                                val value = valueQuantity.asJsonObject["value"].asString
                                val unit = valueQuantity.asJsonObject["unit"].asString
                                val combined = "$value $unit"
                                bloodPressure = if (bloodPressure.isEmpty()) combined else "$bloodPressure / $combined"
                            }
                        }
                    }

                    value.text = bloodPressure
                }
                CaseDataListFragment.CASE_DATA_PULSE -> {
                    label.text = context.getString(R.string.case_data_pulse)
                    value.text = obj["valueQuantity"].asJsonObject["value"].asString
                }
                CaseDataListFragment.CASE_DATA_OXYGEN -> {
                    label.text = context.getString(R.string.case_data_oxygen)
                    value.text = obj["valueQuantity"].asJsonObject["value"].asString
                }
            }

            activity_indicator.visibility = View.GONE
        }
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