package im.vector.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import im.vector.R
import im.vector.fragments.CaseDataListFragment
import kotlinx.android.synthetic.main.case_data_note_view.view.*
import org.matrix.androidsdk.rest.model.Event

open class CaseDataNoteView : CaseDataLabelValueView {

    override fun displayEvent(event: Event?, dataType: Int) {
        if (event == null || dataType == CaseDataListFragment.CASE_DATA_NONE) {
            label.text = ""
            note.text = ""
        } else {
            val obj = event.contentAsJsonObject
            if (dataType == CaseDataListFragment.CASE_DATA_NOTE) {
                if (!obj.has("note")) {
                    visibility = View.GONE
                    return
                }

                visibility = View.VISIBLE

                label.text = context.getString(R.string.case_data_note)
                note.text = obj["note"].asString

                val severity = obj["severity"].asString

                when (severity) {
                    "info" -> note.setBackgroundColor(Color.parseColor("#45abf2"))
                    "request" -> note.setBackgroundColor(Color.parseColor("#26de82"))
                    "urgent" -> note.setBackgroundColor(Color.parseColor("#f7c930"))
                    "critical" -> note.setBackgroundColor(Color.parseColor("#eb3b59"))
                }
            }
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