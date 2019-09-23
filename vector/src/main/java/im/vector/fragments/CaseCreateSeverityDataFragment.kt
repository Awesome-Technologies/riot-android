package im.vector.fragments


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import im.vector.R
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_SEVERITY
import im.vector.activity.CaseCreateActivity.Companion.caseDataMap
import kotlinx.android.synthetic.main.fragment_case_create_severity_data.*

class CaseCreateSeverityDataFragment : CaseCreateDataFragment() {
    private var severity: String? = null

    /*
    * *********************************************************************************************
    * Fragment lifecycle
    * *********************************************************************************************
    */

    val layoutResId: Int
        get() = R.layout.fragment_case_create_severity_data

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutResId, null)
    }

    private fun initViews() {
        updateColor()

        fun onClick(newSeverity: String) {
            severity = newSeverity
            updateColor()
            Log.e(LOG_TAG, "## onClick : Clicked on " + severity!!.capitalize())
            detailActivity.saveData(this)
        }

        infoView.setOnClickListener {
            onClick("info")
        }

        requestView.setOnClickListener {
            onClick("request")
        }

        urgentView.setOnClickListener {
            onClick("urgent")
        }

        criticalView.setOnClickListener {
            onClick("critical")
        }
    }

    override fun getResultIntent(): Intent {
        val intent = Intent()

        intent.putExtra(SEVERITY, severity)

        return intent
    }

    private fun updateColor() {
        val infoColor = "#45AAF2"
        val requestColor = "#26DE82"
        val urgentColor = "#F7C930"
        val criticalColor = "#EB3B59"

        val whiteColor = "#FFFFFF"
        val blackColor = "#000000"

        labelInfo.setBackgroundColor(Color.parseColor(if (severity == "info") infoColor else whiteColor))
        labelRequest.setBackgroundColor(Color.parseColor(if (severity == "request") requestColor else whiteColor))
        labelUrgent.setBackgroundColor(Color.parseColor(if (severity == "urgent") urgentColor else whiteColor))
        labelCritical.setBackgroundColor(Color.parseColor(if (severity == "critical") criticalColor else whiteColor))

        labelInfo.setTextColor(Color.parseColor(if (severity == "info") whiteColor else blackColor))
        labelRequest.setTextColor(Color.parseColor(if (severity == "request") whiteColor else blackColor))
        labelUrgent.setTextColor(Color.parseColor(if (severity == "urgent") whiteColor else blackColor))
        labelCritical.setTextColor(Color.parseColor(if (severity == "critical") whiteColor else blackColor))
    }

    companion object {
        const val SEVERITY = "SEVERITY"
        private val LOG_TAG = CaseCreateSeverityDataFragment::class.java.simpleName

        fun newInstance(): CaseCreateSeverityDataFragment {
            val fragment = CaseCreateSeverityDataFragment()
            val severity = caseDataMap[CASE_DATA_SEVERITY]?.toString()
            severity?.let {
                fragment.severity = it
            } ?: kotlin.run {
                fragment.severity = ""
            }
            fragment.severity = severity
            return fragment
        }
    }
}
