package im.vector.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import im.vector.R
import im.vector.activity.CaseCreateActivity
import im.vector.fragments.CaseCreatePatientDataFragment
import kotlinx.android.synthetic.main.case_data_patient_view.view.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class CaseCreateDataPatientView : CaseCreateDataLabelValueView {

    @SuppressLint("SetTextI18n")
    override fun fillView(type: Int) {
        activity_indicator.visibility = View.GONE

        label.text = context.getString(R.string.case_data_patient)

        var data = HashMap<String, String>()
        if (CaseCreateActivity.caseDataMap.containsKey(CaseCreateActivity.CASE_DATA_PATIENT)) {
            data = CaseCreateActivity.caseDataMap[CaseCreateActivity.CASE_DATA_PATIENT] as HashMap<String, String>
        }

        val name = data[CaseCreatePatientDataFragment.PATIENT_NAME]
        if (name != null) name.let {
            patient_name.text = name
        } else {
            patient_name.text = ""
        }

        val gender = data[CaseCreatePatientDataFragment.PATIENT_GENDER]
        val birthDate = data[CaseCreatePatientDataFragment.PATIENT_BIRTHDATE]
        if (gender != null) gender.let {
            val locGender = getLocalizedGender(gender)
            if (birthDate != null) birthDate.let {age ->
                patient_info.text = "$locGender | ${getAge(age)} ${context.getString(R.string.case_data_age_years)}"
            } else {
                patient_info.text = locGender
            }
        } else {
            patient_info.text = ""
        }
    }

    private fun getLocalizedGender(gender: String?): String {
        when (gender) {
            "male" -> return context.getString(R.string.case_data_gender_male)
            "female" -> return context.getString(R.string.case_data_gender_female)
            "unknown" -> return context.getString(R.string.case_data_gender_unknown)
            "other" -> return context.getString(R.string.case_data_gender_other)
        }

        return context.getString(R.string.case_data_gender_unknown)
    }

    private fun getAge(dobString: String): Int {

        var date: Date? = null
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd.SSSZ", Locale.US)
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

    override fun getLayoutRes(): Int {
        return R.layout.case_data_patient_view
    }

    /**
     * constructors
     */
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)
}