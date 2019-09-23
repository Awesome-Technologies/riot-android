/*
 * Copyright 2017 Vector Creations Ltd
 * Copyright 2018 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import im.vector.Matrix
import im.vector.R
import im.vector.activity.CaseCreateActivity
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_ANAMNESIS
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_NOTE
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_PATIENT
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_PICTURES
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_RECIPIENT
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_SEVERITY
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_TITLE
import im.vector.activity.CaseCreateActivity.Companion.CASE_DATA_VITALS
import im.vector.activity.CaseCreateActivity.Companion.caseDataMap
import im.vector.activity.CaseCreateItemDetailActivity
import im.vector.activity.CaseCreateRecipientActivity
import im.vector.activity.MXCActionBarActivity
import im.vector.view.CaseCreateDataPicturesView
import im.vector.view.CaseDataSectionHeaderView.Companion.CASE_DATA_SECTION_DATA
import im.vector.view.CaseDataSectionHeaderView.Companion.CASE_DATA_SECTION_MESSAGE
import kotlinx.android.synthetic.main.fragment_case_create.*
import org.matrix.androidsdk.MXSession

internal const val CREATE_CREATE_DATA_TYPE = "CREATE_CREATE_DATA_TYPE"

class CaseCreateFragment : Fragment() {
    private val LOG_TAG = CaseCreateFragment::class.java.simpleName
    private var mSession: MXSession? = null

    /*
     * *********************************************************************************************
     * Fragment lifecycle
     * *********************************************************************************************
     */

    val layoutResId: Int
        get() = R.layout.fragment_case_create

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments

        args?.let {
            val matrixId = args.getString(ARG_MATRIX_ID)
            mSession = Matrix.getInstance(activity)!!.getSession(matrixId)
        }

        if (null == mSession || !mSession!!.isAlive) {
            return
        }

        populateView()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutResId, null)
    }

    override fun onDestroy() {
        super.onDestroy()

        CaseCreateDataPicturesView.pictures = ArrayList()
        CaseCreateVitalsDataFragment.values.clear()
        CaseCreateVitalsDataFragment.times.clear()
    }

    /*
     * *********************************************************************************************
     * UI management
     * *********************************************************************************************
     */

    fun populateView() {
        // Patient
        case_create_data_patient_view.fillView(CASE_DATA_PATIENT)
        case_create_data_patient_view.setOnClickListener {
            val newIntent = Intent(context, CaseCreateItemDetailActivity::class.java).apply {
                putExtra(CREATE_CREATE_DATA_TYPE, CASE_DATA_PATIENT)
            }
            activity?.startActivityForResult(newIntent, CASE_DATA_PATIENT)
        }

        // Recipient
        case_create_data_recipient_view.fillView(CASE_DATA_RECIPIENT)
        case_create_data_recipient_view.setOnClickListener {
            val newIntent = Intent(context, CaseCreateRecipientActivity::class.java).apply {
                putExtra(CREATE_CREATE_DATA_TYPE, CASE_DATA_RECIPIENT)
                putExtra(MXCActionBarActivity.EXTRA_MATRIX_ID, mSession!!.myUserId)
            }
            activity?.startActivityForResult(newIntent, CASE_DATA_RECIPIENT)
        }

        // Severity
        case_create_data_severity_view.fillView(CASE_DATA_SEVERITY)
        case_create_data_severity_view.setOnClickListener {
            val newIntent = Intent(context, CaseCreateItemDetailActivity::class.java).apply {
                putExtra(CREATE_CREATE_DATA_TYPE, CASE_DATA_SEVERITY)
            }
            activity?.startActivityForResult(newIntent, CASE_DATA_SEVERITY)
        }

        // Title
        case_create_data_title_view.fillView(CASE_DATA_TITLE)
        case_create_data_title_view.setOnClickListener {
            showTextInputDialog(false, CASE_DATA_TITLE)
        }

        // Note
        case_create_data_note_view.fillView(CASE_DATA_NOTE)
        case_create_data_note_view.setOnClickListener {
            showTextInputDialog(true, CASE_DATA_NOTE)
        }

        // Anamnesis
        case_create_data_anamnesis_view.fillView(CASE_DATA_ANAMNESIS)
        case_create_data_anamnesis_view.setOnClickListener {
            val newIntent = Intent(context, CaseCreateItemDetailActivity::class.java).apply {
                putExtra(CREATE_CREATE_DATA_TYPE, CASE_DATA_ANAMNESIS)
            }
            activity?.startActivityForResult(newIntent, CASE_DATA_ANAMNESIS)
        }

        // Vitals
        case_create_data_vitals_view.fillView(CASE_DATA_VITALS)
        case_create_data_vitals_view.setOnClickListener {
            val newIntent = Intent(context, CaseCreateItemDetailActivity::class.java).apply {
                putExtra(CREATE_CREATE_DATA_TYPE, CASE_DATA_VITALS)
            }
            activity?.startActivityForResult(newIntent, CASE_DATA_VITALS)
        }

        // Pictures
        case_create_data_pictures_view.fillView(CASE_DATA_PICTURES)
        case_create_data_pictures_view.setOnClickListener {
            val newIntent = Intent(context, CaseCreateItemDetailActivity::class.java).apply {
                putExtra(CREATE_CREATE_DATA_TYPE, CASE_DATA_PICTURES)
            }
            activity?.startActivityForResult(newIntent, CASE_DATA_PICTURES)
        }

        case_create_data_section_header.show(CASE_DATA_SECTION_DATA)
        case_create_message_section_header.show(CASE_DATA_SECTION_MESSAGE)
    }

    private fun showTextInputDialog(multiline: Boolean, key: Int) {
        val builder = AlertDialog.Builder(context!!)
        val input = EditText(context)

        when (key) {
            CASE_DATA_TITLE -> builder.setTitle(context!!.getString(R.string.case_data_title))
            CASE_DATA_NOTE -> builder.setTitle(context!!.getString(R.string.case_data_note))
        }

        val value = caseDataMap[key] as? String
        value?.let {
            input.setText(it)
        }

        input.inputType = if (multiline) InputType.TYPE_TEXT_FLAG_MULTI_LINE else InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        val hideKeyboard = {
            val view = activity!!.currentFocus
            if (view != null) {
                val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            } else {
                activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            }
        }

        builder.setPositiveButton(context!!.getString(R.string.save)) {
            _, _ ->
            run {
                caseDataMap[key] = input.text.toString()
                populateView()
                (activity as? CaseCreateActivity)?.let {
                    it.updateCreateCaseButtonState()
                }
                hideKeyboard.invoke()
            }
        }

        builder.setNegativeButton(context!!.getString(R.string.cancel)) {
            dialog, _ -> dialog.cancel()
            hideKeyboard.invoke()
        }

        builder.show()
    }

    companion object {
        public val ARG_MATRIX_ID = "CaseCreateFragment.ARG_MATRIX_ID"
    }
}
