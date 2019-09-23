package im.vector.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import im.vector.R
import im.vector.activity.CaseCreateActivity
import kotlinx.android.synthetic.main.case_data_note_view.view.*

open class CaseCreateDataNoteView : CaseCreateDataLabelValueView {

    override fun fillView(type: Int) {
        label.text = context.getString(R.string.case_data_note)

        when (CaseCreateActivity.caseDataMap[CaseCreateActivity.CASE_DATA_SEVERITY] as? String) {
            "info" -> note.setBackgroundColor(Color.parseColor("#45abf2"))
            "request" -> note.setBackgroundColor(Color.parseColor("#26de82"))
            "urgent" -> note.setBackgroundColor(Color.parseColor("#f7c930"))
            "critical" -> note.setBackgroundColor(Color.parseColor("#eb3b59"))
            else -> note.setBackgroundColor(Color.WHITE)
        }
        val noteString = CaseCreateActivity.caseDataMap[CaseCreateActivity.CASE_DATA_NOTE] as? String
        noteString?.let {
            note.text = it
        } ?: run {
            note.text = ""
        }
    }

    override fun getLayoutRes(): Int {
        return R.layout.case_data_note_view
    }

    /**
     * constructors
     */
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)
}