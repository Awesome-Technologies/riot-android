package im.vector.view

import android.annotation.SuppressLint
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
    @SuppressLint("SetTextI18n")
    override fun displayEvent(event: Event?, dataType: Int) {
        super.displayEvent(event, dataType)
        if (event != null && dataType == CaseDataListFragment.CASE_DATA_PATIENT) {
            val obj = event.contentAsJsonObject
            patient_name.text = obj["name"]?.asString
            var years = 0
            var gender: String? = null
            if (obj.has("birthDate")) {
                years = getAge(obj["birthDate"].asString)
            }
            if (obj.has("gender")) {
                gender = getLocalizedGender(obj["gender"].asString)
            }
            if (years <= 0) {
                if (gender != null) {
                    patient_info.text = gender
                } else {
                    patient_info.text = ""
                }
            } else {
                val yearsString = "$years ${context.getString(R.string.case_data_age_years)}"
                if (gender != null) {
                    patient_info.text = "$gender | $yearsString"
                } else {
                    patient_info.text = yearsString
                }
            }
        }
    }

    private fun getLocalizedGender(gender: String): String {
        when (gender) {
            "male" -> return context.getString(R.string.case_data_gender_male)
            "female" -> return context.getString(R.string.case_data_gender_female)
            "other" -> return context.getString(R.string.case_data_gender_other)
        }
        return context.getString(R.string.case_data_gender_unknown)
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