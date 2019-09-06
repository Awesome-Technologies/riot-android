package im.vector.activity

import android.content.Intent
import android.view.View
import im.vector.R
import im.vector.fragments.CaseDataListFragment
import org.matrix.androidsdk.core.Log

class CaseDetailActivity : MXCActionBarActivity() {
    companion object {
        // the session
        @JvmField
        val EXTRA_MATRIX_ID = "CaseDetailActivity.EXTRA_MATRIX_ID"
        // the room id (string)
        @JvmField
        val EXTRA_ROOM_ID = "CaseDetailActivity.EXTRA_ROOM_ID"
    }

    val LOG_TAG = CaseDetailActivity::class.java.simpleName

    override fun getLayoutRes(): Int {
        return R.layout.activity_case_detail
    }

    override fun initUiAndData() {
        configureToolbar()

        mSession = getSession(intent)

        if (mSession == null || !mSession.isAlive) {
            Log.e(LOG_TAG, "No MXSession.")
            finish()
            return
        }

        val roomId = intent.getStringExtra(EXTRA_ROOM_ID)
        mRoom = mSession.dataHandler.getRoom(roomId)
        if (roomId == null) {
            Log.e(LOG_TAG, "No Room found.")
            finish()
            return
        }

        title = getString(R.string.case_detail_title)

        val fragment = CaseDataListFragment.newInstance(mSession, mRoom)
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
    }

    fun showChat(view: View) {
        Log.d(LOG_TAG, "## showChat(): Showing Chat")

        val intent = Intent(this, VectorRoomActivity::class.java)

        intent.putExtra(VectorRoomActivity.EXTRA_MATRIX_ID, mSession.myUserId)
        intent.putExtra(VectorRoomActivity.EXTRA_ROOM_ID, mRoom.roomId)

        startActivity(intent)
    }
}