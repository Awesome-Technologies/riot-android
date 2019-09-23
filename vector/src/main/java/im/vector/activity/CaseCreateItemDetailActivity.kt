package im.vector.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import im.vector.R
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_ANAMNESIS
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_NONE
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_PATIENT
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_PICTURES
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_SEVERITY
import im.vector.activity.CaseCreateActivity.Companion.caseDataMap
import im.vector.fragments.*


class CaseCreateItemDetailActivity : MXCActionBarActivity() {
    private val LOG_TAG = CaseCreateItemDetailActivity::class.java.simpleName

    private var infoType = 0

    override fun getLayoutRes(): Int {
        return R.layout.activity_case_create_item_detail
    }

    override fun initUiAndData() {
        if (intent.hasExtra(CREATE_CREATE_DATA_TYPE)){

            infoType = intent.getIntExtra(CREATE_CREATE_DATA_TYPE, CASE_DATA_NONE)
            var fragment: CaseCreateDataFragment? = null
            when (infoType) {
                CASE_DATA_NONE -> {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
                CASE_DATA_PATIENT -> {
                    toolbar.title = getString(R.string.case_data_patient)
                    fragment = CaseCreatePatientDataFragment()
                }
                CASE_DATA_SEVERITY -> {
                    toolbar.title = getString(R.string.case_data_severity)
                    fragment = CaseCreateSeverityDataFragment.newInstance()
                }
                CASE_DATA_ANAMNESIS -> {
                    toolbar.title = getString(R.string.case_data_section_header_anamnesis)
                    fragment = CaseCreateAnamnesisDataFragment.newInstance()
                }
                CaseCreateActivity.CASE_DATA_VITALS -> {
                    toolbar.title = getString(R.string.case_data_section_header_vitals)
                    fragment = CaseCreateVitalsDataFragment.newInstance()
                }
                CASE_DATA_PICTURES -> {
                    toolbar.title = getString(R.string.case_data_section_header_pictures)
                    var pictures = caseDataMap[CASE_DATA_PICTURES] as? ArrayList<Uri>
                    if (pictures == null) {
                        pictures = ArrayList()
                    }
                    fragment = CaseCreatePicturesDataFragment.newInstance(pictures)
                }
            }
            fragment?.let {
                it.detailActivity = this
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_detail, fragment, fragment::class.java.simpleName)
                        .commit()
            }
        }
        configureToolbar()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.e(LOG_TAG, "## onActivityResult: Processing photo")
        if (requestCode == CaseCreatePicturesDataFragment.REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val fragment = supportFragmentManager.findFragmentByTag(CaseCreatePicturesDataFragment::class.java.simpleName) as? CaseCreatePicturesDataFragment
            fragment?.let {
                fragment.mLatestTakePictureCameraUri?.let {uriString ->
                    fragment.addPicture(Uri.parse(uriString))
                }
            }
        }
    }

    fun saveData(fragment: CaseCreateDataFragment){
        Log.e(LOG_TAG, "## saveData: Saving data coming from fragment: " + fragment::class.java.simpleName)

        setResult(Activity.RESULT_OK, fragment.getResultIntent())
        finish()
    }
}
