package im.vector.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import im.vector.R
import kotlinx.android.synthetic.main.case_data_section_header.view.*

class CaseDataSectionHeaderView : RelativeLayout {
    private var mPrefix = -1
    private var mSectionType = CASE_DATA_SECTION_MESSAGE

    fun show(sectionType: Int, prefix: Int = -1) {
        mSectionType = sectionType
        mPrefix = prefix
        visibility = View.VISIBLE
        updateTitle()
    }

    fun incrementPrefix() {
        mPrefix++
        updateTitle()
    }

    private fun updateTitle() {
        var newTitle = ""
        when (mSectionType) {
            CASE_DATA_SECTION_MESSAGE -> newTitle = context.getString(R.string.case_data_section_header_message)
            CASE_DATA_SECTION_ANAMNESIS -> newTitle = context.getString(R.string.case_data_section_header_anamnesis)
            CASE_DATA_SECTION_VITALS -> newTitle = context.getString(R.string.case_data_section_header_vitals)
            CASE_DATA_SECTION_PICTURES -> newTitle = context.getString(R.string.case_data_section_header_pictures)
        }

        section_title.text = if (mPrefix >= 0) "$mPrefix $newTitle" else newTitle
    }

    fun getLayoutRes() : Int {
        return R.layout.case_data_section_header
    }

    /**
     * constructors
     */
    constructor(context: Context) : super(context) {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        setup()
    }

    /*
     * *********************************************************************************************
     * Private methods
     * *********************************************************************************************
     */

    /**
     * Setup the view
     */
    private fun setup() {
        LayoutInflater.from(context).inflate(getLayoutRes(), this)
    }

    companion object {
        val CASE_DATA_SECTION_MESSAGE = 0
        val CASE_DATA_SECTION_ANAMNESIS = 1
        val CASE_DATA_SECTION_VITALS = 2
        val CASE_DATA_SECTION_PICTURES = 3
    }
}