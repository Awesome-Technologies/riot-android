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
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_LAST_DEFECATION
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_MISC
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_PAIN
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_RESPONSIVENESS
import im.vector.activity.CaseCreateActivity.Companion.caseDataMap
import im.vector.fragments.CustomDateTimePicker.ICustomDateTimeListener
import im.vector.settings.VectorLocale
import kotlinx.android.synthetic.main.fragment_case_create_anamnesis.*
import java.util.*
import kotlin.collections.HashMap

class CaseCreateAnamnesisDataFragment : CaseCreateDataFragment()  {
    /*
    * *********************************************************************************************
    * Fragment lifecycle
    * *********************************************************************************************
    */

    val layoutResId: Int
        get() = R.layout.fragment_case_create_anamnesis

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutResId, null)
    }

    private fun initViews() {
        Log.e(LOG_TAG,"InitView")

        responsiveness_time.setOnClickListener {
            showDateTimePicker(CASE_DATA_RESPONSIVENESS)
        }

        pain_time.setOnClickListener {
            showDateTimePicker(CASE_DATA_PAIN)
        }

        misc_time.setOnClickListener {
            showDateTimePicker(CASE_DATA_MISC)
        }

        last_defecation_time.setOnClickListener {
            showDateTimePicker(CASE_DATA_LAST_DEFECATION)
        }

        save_data_button.setOnClickListener {
            detailActivity.saveData(this)
        }

        updateDateLabels()
        updateValueTextFields()
    }

    private fun showDateTimePicker(type: Int) {
        var date: Date? = null
        times[type]?.let {
            date = it
        }
        val listener = object : ICustomDateTimeListener {
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

    private fun updateValueTextFields() {
        values[CASE_DATA_RESPONSIVENESS]?.let {
            responsiveness.setText(it)
        }
        values[CASE_DATA_PAIN]?.let {
            pain.setText(it)
        }
        values[CASE_DATA_MISC]?.let {
            misc.setText(it)
        }
    }

    private fun updateDateLabels() {
        times[CASE_DATA_RESPONSIVENESS]?.let { updateTimeButton(it, responsiveness_time) }
        times[CASE_DATA_PAIN]?.let { updateTimeButton(it, pain_time) }
        times[CASE_DATA_MISC]?.let { updateTimeButton(it, misc_time) }
        times[CASE_DATA_LAST_DEFECATION]?.let { updateTimeButton(it, last_defecation_time) }
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

        if (!responsiveness.text.isNullOrBlank()) {
            responsiveness.text.toString().let {
                intent.putExtra(RESPONSIVENESS, Pair(it, times[CASE_DATA_RESPONSIVENESS]))
            }
        }
        if (!pain.text.isNullOrBlank()) {
            pain.text.toString().let {
                intent.putExtra(PAIN, Pair(it, times[CASE_DATA_PAIN]))
            }
        }
        if (!misc.text.isNullOrBlank()) {
            misc.text.toString().let {
                intent.putExtra(MISC, Pair(it, times[CASE_DATA_MISC]))
            }
        }
        if (times.containsKey(CASE_DATA_LAST_DEFECATION)) {
            intent.putExtra(LAST_DEFECATION, times[CASE_DATA_LAST_DEFECATION])
        }

        return intent
    }

    companion object {
        const val RESPONSIVENESS = "RESPONSIVENESS"
        const val PAIN = "PAIN"
        const val MISC = "MISC"
        const val LAST_DEFECATION = "LAST_DEFECATION"

        private val LOG_TAG = CaseCreateAnamnesisDataFragment::class.java.simpleName
        private var times = HashMap<Int, Date>()
        private var values = HashMap<Int, String>()

        fun newInstance(): CaseCreateAnamnesisDataFragment {
            times.clear()
            values.clear()

            val fragment = CaseCreateAnamnesisDataFragment()
            val types = listOf(CASE_DATA_RESPONSIVENESS, CASE_DATA_PAIN, CASE_DATA_MISC, CASE_DATA_LAST_DEFECATION)
            types.forEach { type ->
                (caseDataMap[type] as? Pair<String, Date?>)?.let { pair ->
                    pair.second?.let { date ->
                        times[type] = date
                    }
                    values[type] = pair.first
                } ?: kotlin.run {
                    (caseDataMap[type] as? Date)?.let { date ->
                        times[type] = date
                    }
                }
            }

            return fragment
        }
    }
}
