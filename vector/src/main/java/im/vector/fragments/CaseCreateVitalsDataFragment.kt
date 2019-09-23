package im.vector.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import im.vector.R
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_BLOOD_PRESSURE
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_BODY_TEMPERATURE
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_BODY_WEIGHT
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_GLUCOSE
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_OXYGEN
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_PULSE
import im.vector.activity.CaseCreateActivity.Companion.caseDataMap
import im.vector.settings.VectorLocale
import im.vector.view.CaseDataSectionHeaderView
import kotlinx.android.synthetic.main.fragment_case_create_vitals.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.roundToInt

class CaseCreateVitalsDataFragment : CaseCreateDataFragment()  {
    /*
    * *********************************************************************************************
    * Fragment lifecycle
    * *********************************************************************************************
    */

    val layoutResId: Int
        get() = R.layout.fragment_case_create_vitals

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutResId, null)
    }

    private fun initViews() {
        Log.e(LOG_TAG,"InitViews")

        weight_time.setOnClickListener {
            showDateTimePicker(CASE_DATA_BODY_WEIGHT)
        }

        save_data_button.setOnClickListener {
            detailActivity.saveData(this)
        }

        updateValueTextFields()
        updateDateLabels()

        case_data_heart_section_header.show(CaseDataSectionHeaderView.CASE_DATA_SECTION_HEART)
    }

    private fun showDateTimePicker(type: Int) {
        var date: Date? = null
        times[type]?.let {
            date = it
        }
        val listener = object : CustomDateTimePicker.ICustomDateTimeListener {
            override fun onSet(dialog: Dialog, calendarSelected: Calendar,
                               dateSelected: Date, year: Int,
                               monthFullName: String,
                               monthShortName: String,
                               monthNumber: Int, date: Int,
                               weekDayFullName: String,
                               weekDayShortName: String, hour24: Int,
                               hour12: Int,
                               min: Int, sec: Int, AM_PM: String) {
                times[type] = dateSelected

                updateDateLabels()
            }

            override fun onCancel() {

            }
        }

        CustomDateTimePicker(context, listener)
                .set24HourFormat(true)
                .setDate(date)
                .showDialog()
    }

    private fun getRoundedValue(value: Float, decimals: Int = 0): String {
        if (decimals == 0) return value.roundToInt().toString()

        return String.format("%.${decimals}f", value)
    }

    private fun updateValueTextFields() {
        Log.e(LOG_TAG, values.toString())

        (values[CASE_DATA_BODY_WEIGHT] as? Float)?.let { body_weight.setText(getRoundedValue(it, 1)) }
        (values[CASE_DATA_BODY_TEMPERATURE] as? Float)?.let { body_temperature.setText(getRoundedValue(it, 1)) }
        (values[CASE_DATA_GLUCOSE] as? Float)?.let { glucose.setText(getRoundedValue(it)) }
        (values[CASE_DATA_BLOOD_PRESSURE] as? Pair<Float, Float>)?.let {
            blood_pressure_systolic.setText(getRoundedValue(it.first))
            blood_pressure_diastolic.setText(getRoundedValue(it.second))
        }
        (values[CASE_DATA_PULSE] as? Float)?.let { pulse.setText(getRoundedValue(it)) }
        (values[CASE_DATA_OXYGEN] as? Float)?.let { oxygen.setText(getRoundedValue(it)) }
    }

    private fun updateDateLabels() {
        Log.e(LOG_TAG, times.toString())

        times[CASE_DATA_BODY_WEIGHT]?.let { updateTimeButton(it, weight_time) }
        times[CASE_DATA_BODY_TEMPERATURE]?.let { updateTimeButton(it, temperature_time) }
        times[CASE_DATA_GLUCOSE]?.let { updateTimeButton(it, glucose_time) }
        times[CASE_DATA_BLOOD_PRESSURE]?.let { updateTimeButton(it, blood_pressure_time) }
        times[CASE_DATA_PULSE]?.let { updateTimeButton(it, pulse_time) }
        times[CASE_DATA_OXYGEN]?.let { updateTimeButton(it, oxygen_time) }
    }

    @SuppressLint("SetTextI18n")
    fun updateTimeButton(dateSelected: Date, button: Button) {
        val dateFormat = DateFormat.getDateFormat(context)
        val timeFormat = DateFormat.getTimeFormat(context)
        val cal = Calendar.getInstance(VectorLocale.applicationLocale)
        val smsTime = Calendar.getInstance(VectorLocale.applicationLocale)
        smsTime.timeInMillis = dateSelected.time
        if (cal.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            button.text = timeFormat.format(dateSelected)
        } else {
            button.text = "${dateFormat.format(dateSelected)} ${timeFormat.format(dateSelected)}"
        }
    }

    override fun getResultIntent(): Intent {
        val intent = Intent()

        body_weight.text.toString().toFloatOrNull()?.let {value ->
            val date = times[CASE_DATA_BODY_WEIGHT]
            intent.putExtra(BODY_WEIGHT, Pair(value, date))
        }
        body_temperature.text.toString().toFloatOrNull()?.let {value ->
            val date = times[CASE_DATA_BODY_TEMPERATURE]
            intent.putExtra(BODY_TEMPERATURE, Pair(value, date))
        }
        glucose.text.toString().toFloatOrNull()?.let {value ->
            val date = times[CASE_DATA_GLUCOSE]
            intent.putExtra(GLUCOSE, Pair(value, date))
        }
        var systolic = 0f
        var diastolic = 0f
        blood_pressure_systolic.text.toString().toFloatOrNull()?.let {value ->
            systolic = value
        }
        blood_pressure_diastolic.text.toString().toFloatOrNull()?.let {value ->
            diastolic = value
        }
        if (systolic > 0 && diastolic > 0) {
            intent.putExtra(BLOOD_PRESSURE, Triple(systolic, diastolic, times[CASE_DATA_BLOOD_PRESSURE]))
        }
        pulse.text.toString().toFloatOrNull()?.let {value ->
            val date = times[CASE_DATA_PULSE]
            intent.putExtra(PULSE, Pair(value, date))
        }
        oxygen.text.toString().toFloatOrNull()?.let {value ->
            val date = times[CASE_DATA_OXYGEN]
            intent.putExtra(OXYGEN, Pair(value, date))
        }

        return intent
    }

    companion object {
        const val BODY_WEIGHT = "BODY_WEIGHT"
        const val BODY_TEMPERATURE = "BODY_TEMPERATURE"
        const val GLUCOSE = "GLUCOSE"
        const val BLOOD_PRESSURE = "BLOOD_PRESSURE"
        const val PULSE = "PULSE"
        const val OXYGEN = "OXYGEN"

        private val LOG_TAG = CaseCreateVitalsDataFragment::class.java.simpleName
        var times = HashMap<Int, Date>()
        var values = HashMap<Int, Any>()

        fun newInstance(): CaseCreateVitalsDataFragment {
            times.clear()
            values.clear()

            val fragment = CaseCreateVitalsDataFragment()
            val types = listOf(CASE_DATA_BODY_WEIGHT, CASE_DATA_BODY_TEMPERATURE, CASE_DATA_GLUCOSE, CASE_DATA_PULSE, CASE_DATA_OXYGEN)
            types.forEach { type ->
                (caseDataMap[type] as? Pair<Float, Date?>)?.let { pair ->
                    pair.second?.let {
                        times[type] = it
                    }
                    values[type] = pair.first
                }
            }
            (caseDataMap[CASE_DATA_BLOOD_PRESSURE] as? Triple<Float, Float, Date?>)?.let { triple ->
                triple.third?.let {
                    times[CASE_DATA_BLOOD_PRESSURE] = it
                }
                values[CASE_DATA_BLOOD_PRESSURE] = Pair(triple.first, triple.second)
            }

            return fragment
        }
    }
}
