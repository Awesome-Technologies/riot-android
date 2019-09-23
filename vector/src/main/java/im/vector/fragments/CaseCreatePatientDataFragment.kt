package im.vector.fragments

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import im.vector.R
import im.vector.activity.CaseCreateActivity
import kotlinx.android.synthetic.main.fragment_case_create_patient_data.*
import java.text.SimpleDateFormat
import java.util.*

class CaseCreatePatientDataFragment : CaseCreateDataFragment()  {
    var birthDate: Calendar? = null

    /*
    * *********************************************************************************************
    * Fragment lifecycle
    * *********************************************************************************************
    */

    val layoutResId: Int
        get() = R.layout.fragment_case_create_patient_data

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutResId, null)
    }

    private fun initViews() {
        Log.e(LOG_TAG,"## InitViews() : Initializing patient fragment")

        set_date_button.setOnClickListener {
            showDatePicker()
        }

        save_data_button.setOnClickListener {
            detailActivity.saveData(this)
        }

        var data = HashMap<String, String>()
        if (CaseCreateActivity.caseDataMap.containsKey(CaseCreateActivity.CASE_DATA_PATIENT)) {
            data = CaseCreateActivity.caseDataMap[CaseCreateActivity.CASE_DATA_PATIENT] as HashMap<String, String>
        }

        val name = data[PATIENT_NAME]
        case_create_patient_name_value.setText(name)

        val gender = data[PATIENT_GENDER]
        if (gender != null) gender.let {
            when (gender) {
                "male" -> case_data_gender_radio_group.check(R.id.case_data_gender_male)
                "female" -> case_data_gender_radio_group.check(R.id.case_data_gender_female)
                "other" -> case_data_gender_radio_group.check(R.id.case_data_gender_other)
                "unknown" -> case_data_gender_radio_group.check(R.id.case_data_gender_unknown)
            }
        } else {
            case_data_gender_radio_group.check(R.id.case_data_gender_unknown)
        }

        val birthDateString = data[PATIENT_BIRTHDATE]
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd.SSSZ", Locale.US)
        birthDateString?.let { dateString ->
            val date = dateFormatter.parse(dateString)
            birthDate = Calendar.getInstance()
            birthDate?.time = date
        }

        updateDateButton()
    }

    private fun showDatePicker() {
        val listener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            if (birthDate == null) {
                birthDate = Calendar.getInstance()
            }
            birthDate?.set(year, monthOfYear, dayOfMonth)
            updateDateButton()
        }
        var date = birthDate
        if (date == null) {
            date = Calendar.getInstance()
        }
        val dialog = DatePickerDialog(context!!,
                listener,
                date!!.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH))
        dialog.datePicker.maxDate = Date().time
        dialog.show()
    }

    private fun updateDateButton() {
        val dateFormatter = SimpleDateFormat("dd.MM.yyy", Locale.US)
        birthDate?.let {calendar ->
            set_date_button.text = dateFormatter.format(calendar.time)
        }
    }

    override fun getResultIntent(): Intent {
        val intent = Intent()

        intent.putExtra(PATIENT_NAME, case_create_patient_name_value.text.toString())

        var gender = "other"
        when (case_data_gender_radio_group.checkedRadioButtonId) {
            R.id.case_data_gender_male -> gender = "male"
            R.id.case_data_gender_female -> gender = "female"
            R.id.case_data_gender_unknown -> gender = "unknown"
        }
        intent.putExtra(PATIENT_GENDER, gender)
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd.SSSZ", Locale.US)
        birthDate?.let {date ->
            intent.putExtra(PATIENT_BIRTHDATE, dateFormatter.format(date.time)) }

        return intent
    }

    companion object {
        private val LOG_TAG = CaseCreatePatientDataFragment::class.java.simpleName

        const val PATIENT_NAME = "PATIENT_NAME"
        const val PATIENT_GENDER = "PATIENT_GENDER"
        const val PATIENT_BIRTHDATE = "PATIENT_BIRTHDATE"
    }
}
