package im.vector.fragments

import android.content.Intent
import androidx.fragment.app.Fragment
import im.vector.activity.CaseCreateItemDetailActivity


abstract class CaseCreateDataFragment : Fragment()  {
    lateinit var detailActivity: CaseCreateItemDetailActivity
    abstract fun getResultIntent(): Intent
}
