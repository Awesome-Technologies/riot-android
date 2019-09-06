package im.vector.view

import android.content.Context
import android.util.AttributeSet
import im.vector.R
import im.vector.fragments.CaseDataListFragment
import kotlinx.android.synthetic.main.case_data_patient_view.view.*
import org.matrix.androidsdk.rest.model.Event
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class CaseDataPatientView : CaseDataLabelValueView {
    override fun displayEvent(event: Event?, dataType: Int) {
        super.displayEvent(event, dataType)
        if (event != null && dataType == CaseDataListFragment.CASE_DATA_PATIENT) {
            val obj = event.contentAsJsonObject
            patient_name.text = obj["name"].asString
            val years = getAge(obj["birthDate"].asString)
            if (years == 0) {
                patient_info.text = "${obj["gender"].asString.capitalize()} | ${context.getString(R.string.case_data_age_unknown)}"
            } else {
                patient_info.text = "${obj["gender"].asString.capitalize()} | $years"
            }
        }
    }

    override fun getLayoutRes(): Int {
        return R.layout.case_data_patient_view
    }

    private fun getAge(dobString: String): Int {

        var date: Date? = null
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd.SSSZ", Locale.GERMANY)
        try {
            date = dateFormatter.parse(dobString)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        if (date == null) return 0

        val dob = Calendar.getInstance()
        val today = Calendar.getInstance()

        dob.time = date

        val year = dob.get(Calendar.YEAR)
        val month = dob.get(Calendar.MONTH)
        val day = dob.get(Calendar.DAY_OF_MONTH)

        dob.set(year, month + 1, day)

        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        return age
    }

    /**
     * constructors
     */
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)
}