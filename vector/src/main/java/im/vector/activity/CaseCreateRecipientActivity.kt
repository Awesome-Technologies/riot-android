/*
 * Copyright 2014 OpenMarket Ltd
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

package im.vector.activity

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.widget.ExpandableListView
import butterknife.BindView
import im.vector.Matrix
import im.vector.R
import im.vector.adapters.CaseCreateRecipientsAdapter
import im.vector.adapters.ParticipantAdapterItem
import im.vector.contacts.Contact
import im.vector.contacts.ContactsManager
import im.vector.util.VectorUtils
import org.matrix.androidsdk.core.Log
import org.matrix.androidsdk.listeners.MXEventListener
import org.matrix.androidsdk.rest.model.Event
import org.matrix.androidsdk.rest.model.User

/**
 * This class provides a way to search other user to invite them in a dedicated room
 */
class CaseCreateRecipientActivity : VectorBaseSearchActivity() {

    // account data
    private var mMatrixId: String? = null

    // main UI items
    @JvmField
    @BindView(R.id.recipient_list)
    var mListView: ExpandableListView? = null

    // adapter
    private var mAdapter: CaseCreateRecipientsAdapter? = null

    // retrieve a matrix Id from an email
    private val mContactsListener = object : ContactsManager.ContactsManagerListener {
        override fun onRefresh() {
            runOnUiThread { onPatternUpdate(false) }
        }

        override fun onContactPresenceUpdate(contact: Contact, matrixId: String) {}

        override fun onPIDsUpdate() {
            runOnUiThread { mAdapter!!.onPIdsUpdate() }
        }
    }

    // refresh the presence asap
    private val mEventsListener = object : MXEventListener() {
        override fun onPresenceUpdate(event: Event?, user: User?) {
            runOnUiThread {
                val visibleChildViews = VectorUtils.getVisibleChildViews(mListView!!, mAdapter)

                for (groupPosition in visibleChildViews.keys) {
                    val childPositions = visibleChildViews[groupPosition]

                    for (childPosition in childPositions!!) {
                        val item = mAdapter!!.getChild(groupPosition!!, childPosition!!)

                        if (item is ParticipantAdapterItem) {

                            if (TextUtils.equals(user!!.user_id, item.mUserId)) {
                                mAdapter!!.notifyDataSetChanged()
                                break
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getLayoutRes(): Int {
        return R.layout.activity_case_create_recipient_list
    }

    override fun initUiAndData() {
        super.initUiAndData()

        if (CommonActivityUtils.shouldRestartApp(this)) {
            Log.e(LOG_TAG, "Restart the application.")
            CommonActivityUtils.restartApp(this)
            return
        }

        if (CommonActivityUtils.isGoingToSplash(this)) {
            Log.d(LOG_TAG, "onCreate : Going to splash screen")
            return
        }

        val intent = intent

        if (intent.hasExtra(EXTRA_MATRIX_ID)) {
            mMatrixId = intent.getStringExtra(EXTRA_MATRIX_ID)
        }

        // get current session
        mSession = Matrix.getInstance(applicationContext)!!.getSession(mMatrixId)

        if (null == mSession || !mSession.isAlive) {
            finish()
            return
        }

        // the user defines a
        if (null != mPatternToSearchEditText) {
            mPatternToSearchEditText.setHint(R.string.room_participants_invite_search_another_user)
        }

        waitingView = findViewById(R.id.search_in_progress_view)

        // the chevron is managed in the header view
        mListView!!.setGroupIndicator(null)

        mAdapter = CaseCreateRecipientsAdapter(this,
                R.layout.adapter_item_case_create_recipient,
                R.layout.adapter_item_vector_people_header,
                mSession)
        mAdapter!!.setHiddenParticipantItems(listOf(ParticipantAdapterItem(mSession!!.myUser)))
        mListView!!.setAdapter(mAdapter)

        mListView!!.setOnChildClickListener(ExpandableListView.OnChildClickListener { parent, v, groupPosition, childPosition, id ->
            val item = mAdapter!!.getChild(groupPosition, childPosition)

            if (item is ParticipantAdapterItem && item.mIsValid) {
                finish(item)
                return@OnChildClickListener true
            }
            false
        })
    }

    override fun onResume() {
        super.onResume()
        mSession.dataHandler.addListener(mEventsListener)
        ContactsManager.getInstance().addListener(mContactsListener)
    }

    override fun onPause() {
        super.onPause()
        mSession.dataHandler.removeListener(mEventsListener)
        ContactsManager.getInstance().removeListener(mContactsListener)
    }

    /**
     * The search pattern has been updated
     */
    override fun onPatternUpdate(isTypingUpdate: Boolean) {
        var pattern = mPatternToSearchEditText.text.toString()

        // display a spinner while the other room members are listed
        if (!mAdapter!!.isKnownMembersInitialized) {
            showWaitingView()
        }

        // wait that the local contacts are populated
        if (!ContactsManager.getInstance().didPopulateLocalContacts()) {
            Log.d(LOG_TAG, "## onPatternUpdate() : The local contacts are not yet populated")
            mAdapter!!.reset()
            showWaitingView()
            return
        }

        if (pattern == "") {
            pattern = mSession.homeServerConfig.homeserverUri.host.toString()
        }

        mAdapter!!.setSearchedPattern(pattern, null, object : CaseCreateRecipientsAdapter.OnParticipantsSearchListener {
            override fun onSearchEnd(count: Int) {
                if (mListView == null) {
                    // Activity is dead
                    return
                }

                mListView!!.post { hideWaitingView() }
            }

        })
    }

    /**
     * Close activity with selected participant.
     *
     * @param participantAdapterItem the selected participant
     */
    private fun finish(participantAdapterItem: ParticipantAdapterItem) {
        // returns the selected user
        val intent = Intent()
        intent.putExtra(EXTRA_OUT_SELECTED_RECIPIENT, participantAdapterItem)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    companion object {
        private val LOG_TAG = CaseCreateRecipientActivity::class.java.simpleName

        // room identifier
        const val EXTRA_ROOM_ID = "CaseCreateRecipientActivity.EXTRA_ROOM_ID"

        const val EXTRA_OUT_SELECTED_RECIPIENT = "CaseCreateRecipientActivity.SELECTED_RECIPIENT"
    }
}
