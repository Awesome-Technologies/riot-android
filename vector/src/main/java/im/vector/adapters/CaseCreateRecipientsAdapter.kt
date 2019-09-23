/*
 * Copyright 2016 OpenMarket Ltd
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

package im.vector.adapters

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import im.vector.R
import im.vector.contacts.ContactsManager
import im.vector.settings.VectorLocale
import im.vector.util.VectorUtils
import org.matrix.androidsdk.MXSession
import org.matrix.androidsdk.core.Log
import org.matrix.androidsdk.core.callback.ApiCallback
import org.matrix.androidsdk.core.callback.SimpleApiCallback
import org.matrix.androidsdk.core.model.MatrixError
import org.matrix.androidsdk.rest.model.search.SearchUsersResponse
import java.util.*

/**
 * This class displays the recipient list when creating a case.
 */
class CaseCreateRecipientsAdapter
/**
 * Create a room member adapter.
 * If a room id is defined, the adapter is in edition mode : the user can add / remove dynamically members or leave the room.
 * If there is none, the room is in creation mode : the user can add/remove members to create a new room.
 *
 * @param context                the context.
 * @param cellLayoutResourceId   the cell layout.
 * @param headerLayoutResourceId the header layout
 * @param session                the session.
 */
(// layout info
        private val mContext: Context,
        // used layouts
        private val mCellLayoutResourceId: Int,
        private val mHeaderLayoutResourceId: Int,
        // account info
        private val mSession: MXSession) : BaseExpandableListAdapter() {
    private val mLayoutInflater: LayoutInflater

    // participants list
    private var mUnusedParticipants: List<ParticipantAdapterItem>? = null
    private var mContactsParticipants: List<ParticipantAdapterItem>? = null
    private var mUsedMemberUserIds: MutableSet<String>? = null
    private var mDisplayNamesList: List<String>? = null
    private var mPattern: String? = ""

    private var mItemsToHide: List<ParticipantAdapterItem> = ArrayList()

    // the participant sort method
    private val mSortMethod: Comparator<ParticipantAdapterItem>

    // define the first entry to set
    private var mFirstEntry: ParticipantAdapterItem? = null

    // the participants can be split in sections
    private val mParticipantsList = ArrayList<ParticipantAdapterItem>()

    // tell if the known contacts list is limited
    private var mKnownContactsLimited: Boolean = false

    // tell if the contacts search has been done offline
    private var mIsOfflineContactsSearch: Boolean = false

    /**
     * @return true if the known members list has been initialized.
     */
    val isKnownMembersInitialized: Boolean
        get() {
            val res: Boolean

            synchronized(LOG_TAG) {
                res = null != mDisplayNamesList
            }

            return res
        }

    // search events listener
    interface OnParticipantsSearchListener {
        /**
         * The search is ended.
         *
         * @param count the number of matched user
         */
        fun onSearchEnd(count: Int)
    }

    init {

        mLayoutInflater = LayoutInflater.from(mContext)

        mSortMethod = ParticipantAdapterItem.getComparator(mSession)
    }

    /**
     * Reset the adapter content
     */
    fun reset() {
        mParticipantsList.clear()
        mPattern = null

        notifyDataSetChanged()
    }

    /**
     * Search a pattern in the known members list.
     *
     * @param pattern        the pattern to search
     * @param firstEntry     the entry to display in the results list.
     * @param searchListener the search result listener
     */
    fun setSearchedPattern(pattern: String?, firstEntry: ParticipantAdapterItem?, searchListener: OnParticipantsSearchListener?) {
        var pattern = pattern
        if (null == pattern) {
            pattern = ""
        } else {
            pattern = pattern.toLowerCase().trim { it <= ' ' }.toLowerCase(VectorLocale.applicationLocale)
        }

        if (pattern != mPattern || TextUtils.isEmpty(mPattern)) {
            mPattern = pattern
            refresh(firstEntry, searchListener)
        } else if (null != searchListener) {
            searchListener.onSearchEnd(mParticipantsList.size)
        }
    }

    /**
     * Add the contacts participants
     *
     * @param list the participantItem indexed by their matrix Id
     */
    private fun addContacts(list: MutableList<ParticipantAdapterItem>) {
        val contacts = ContactsManager.getInstance().localContactsSnapshot

        if (null != contacts) {
            for (contact in contacts) {

            }
        }
    }

    private fun fillUsedMembersList(callback: ApiCallback<Void?>) {
        // Used members (ids) which should be removed from the final list
        mUsedMemberUserIds = HashSet()

        for (item in mItemsToHide) {
            mUsedMemberUserIds!!.add(item.mUserId)
        }

        callback.onSuccess(null)
    }

    /**
     * Refresh the un-invited members
     */
    private fun listOtherMembers() {
        fillUsedMembersList(object : SimpleApiCallback<Void?>() {
            override fun onSuccess(info: Void?) {
                val participants = ArrayList<ParticipantAdapterItem>()
                // Add all known matrix users
                participants.addAll(VectorUtils.listKnownParticipants(mSession).values)
                // Add phone contacts which have an email address
                addContacts(participants)

                // List of display names
                val displayNamesList = ArrayList<String>()

                val iterator = participants.iterator()
                while (iterator.hasNext()) {
                    val item = iterator.next()
                    if (!mUsedMemberUserIds!!.isEmpty() && mUsedMemberUserIds!!.contains(item.mUserId)) {
                        // Remove the used members from the final list
                        iterator.remove()
                    } else if (!TextUtils.isEmpty(item.mDisplayName)) {
                        // Add to the display names list
                        displayNamesList.add(item.mDisplayName.toLowerCase(VectorLocale.applicationLocale))
                    }
                }

                synchronized(LOG_TAG) {
                    mDisplayNamesList = displayNamesList
                    mUnusedParticipants = participants
                }
            }
        })
    }

    /**
     * Some contacts pids have been updated.
     */
    fun onPIdsUpdate() {
        var gotUpdates = false

        var unusedParticipants: List<ParticipantAdapterItem> = ArrayList()
        var contactsParticipants: List<ParticipantAdapterItem> = ArrayList()

        synchronized(LOG_TAG) {
            if (null != mUnusedParticipants) {
                unusedParticipants = ArrayList(mUnusedParticipants!!)
            }

            if (null != mContactsParticipants) {
                val newContactList = ArrayList<ParticipantAdapterItem>()
                addContacts(newContactList)
                if (!mContactsParticipants!!.containsAll(newContactList)) {
                    // Force update
                    gotUpdates = true
                    mContactsParticipants = null
                } else {
                    contactsParticipants = ArrayList(mContactsParticipants!!)
                }
            }
        }

        for (item in unusedParticipants) {
            gotUpdates = gotUpdates or item.retrievePids()
        }

        for (item in contactsParticipants) {
            gotUpdates = gotUpdates or item.retrievePids()
        }

        if (gotUpdates) {
            refresh(mFirstEntry, null)
        }
    }

    /**
     * Defines a set of participant items to hide.
     *
     * @param itemsToHide the set to hide
     */
    fun setHiddenParticipantItems(itemsToHide: List<ParticipantAdapterItem>?) {
        if (null != itemsToHide) {
            mItemsToHide = itemsToHide
        }
    }

    /**
     * Refresh the display.
     *
     * @param theFirstEntry  the first entry in the result.
     * @param searchListener the search result listener
     */
    private fun refresh(theFirstEntry: ParticipantAdapterItem?, searchListener: OnParticipantsSearchListener?) {
        if (!mSession.isAlive) {
            Log.e(LOG_TAG, "refresh : the session is not anymore active")
            return
        }

        if (!TextUtils.isEmpty(mPattern)) {
            fillUsedMembersList(object : SimpleApiCallback<Void?>() {
                override fun onSuccess(info: Void?) {
                    val fPattern = mPattern

                    mSession.searchUsers(mPattern, MAX_USERS_SEARCH_COUNT, mUsedMemberUserIds, object : ApiCallback<SearchUsersResponse> {
                        override fun onSuccess(searchUsersResponse: SearchUsersResponse) {
                            if (TextUtils.equals(fPattern, mPattern)) {
                                val participantItemList = ArrayList<ParticipantAdapterItem>()

                                if (null != searchUsersResponse.results) {
                                    for (user in searchUsersResponse.results) {
                                        participantItemList.add(ParticipantAdapterItem(user))
                                    }
                                }

                                mIsOfflineContactsSearch = false
                                mKnownContactsLimited = if (null != searchUsersResponse.limited) searchUsersResponse.limited else false

                                searchAccountKnownContacts(theFirstEntry, participantItemList, false, searchListener)
                            }
                        }

                        private fun onError() {
                            if (TextUtils.equals(fPattern, mPattern)) {
                                mIsOfflineContactsSearch = true
                                searchAccountKnownContacts(theFirstEntry, ArrayList(), true, searchListener)
                            }
                        }

                        override fun onNetworkError(e: Exception) {
                            onError()
                        }

                        override fun onMatrixError(e: MatrixError) {
                            onError()
                        }

                        override fun onUnexpectedError(e: Exception) {
                            onError()
                        }
                    })
                }
            })
        } else {
            searchAccountKnownContacts(theFirstEntry, ArrayList(), true, searchListener)
        }
    }

    /**
     * Search the known contacts from the account known users list.
     *
     * @param theFirstEntry        the adapter first entry
     * @param participantItemList  the participants initial list
     * @param sortRoomContactsList true to sort the room contacts list
     * @param searchListener       the listener
     */
    private fun searchAccountKnownContacts(theFirstEntry: ParticipantAdapterItem?,
                                           participantItemList: MutableList<ParticipantAdapterItem>,
                                           sortRoomContactsList: Boolean,
                                           searchListener: OnParticipantsSearchListener?) {
        // the list is not anymore limited
        mKnownContactsLimited = false

        // displays something only if there is a pattern
        if (!TextUtils.isEmpty(mPattern)) {
            // the list members are refreshed in background to avoid UI locks
            if (null == mUnusedParticipants) {
                val t = Thread(Runnable {
                    // populate full contact list
                    listOtherMembers()

                    val handler = Handler(Looper.getMainLooper())

                    handler.post { searchAccountKnownContacts(theFirstEntry, participantItemList, sortRoomContactsList, searchListener) }
                })

                t.priority = Thread.MIN_PRIORITY
                t.start()

                return
            }

            var unusedParticipants: List<ParticipantAdapterItem> = ArrayList()

            synchronized(LOG_TAG) {
                if (null != mUnusedParticipants) {
                    unusedParticipants = ArrayList(mUnusedParticipants!!)
                }
            }

            for (item in unusedParticipants) {
                if (match(item, mPattern)) {
                    participantItemList.add(item)
                }
            }
        } else {
            // display only the contacts
            if (null == mContactsParticipants) {
                val t = Thread(Runnable {
                    fillUsedMembersList(object : SimpleApiCallback<Void?>() {
                        override fun onSuccess(info: Void?) {
                            val list = ArrayList<ParticipantAdapterItem>()
                            addContacts(list)

                            synchronized(LOG_TAG) {
                                mContactsParticipants = list
                            }

                            val handler = Handler(Looper.getMainLooper())
                            handler.post { refresh(theFirstEntry, searchListener) }
                        }
                    })
                })

                t.priority = Thread.MIN_PRIORITY
                t.start()

                return
            } else {
                var contactsParticipants: MutableList<ParticipantAdapterItem> = ArrayList()

                synchronized(LOG_TAG) {
                    if (null != mContactsParticipants) {
                        contactsParticipants = ArrayList(mContactsParticipants!!)
                    }
                }

                val iterator = contactsParticipants.iterator()
                while (iterator.hasNext()) {
                    val item = iterator.next()
                    if (!mUsedMemberUserIds!!.isEmpty() && mUsedMemberUserIds!!.contains(item.mUserId)) {
                        // Remove the used members from the contact list
                        iterator.remove()
                    }
                }
            }

            synchronized(LOG_TAG) {
                if (null != mContactsParticipants) {
                    participantItemList.addAll(mContactsParticipants!!)
                }
            }
        }

        onKnownContactsSearchEnd(participantItemList, theFirstEntry, sortRoomContactsList, searchListener)
    }

    /**
     * The known contacts search is ended.
     * Search the local contacts
     *
     * @param participantItemList the known contacts list
     * @param theFirstEntry       the adapter first entry
     * @param sort                true to sort participantItemList
     * @param searchListener      the search listener
     */
    private fun onKnownContactsSearchEnd(participantItemList: MutableList<ParticipantAdapterItem>,
                                         theFirstEntry: ParticipantAdapterItem?,
                                         sort: Boolean,
                                         searchListener: OnParticipantsSearchListener?) {
        // ensure that the PIDs have been retrieved
        // it might have failed
        ContactsManager.getInstance().retrievePids()

        // the caller defines a first entry to display
        var firstEntry = theFirstEntry

        // detect if the user ID is defined in the known members list
        if (null != mUsedMemberUserIds && null != firstEntry) {
            if (mUsedMemberUserIds!!.contains(theFirstEntry!!.mUserId)) {
                firstEntry = null
            }
        }

        if (null != firstEntry) {
            participantItemList.add(0, firstEntry)

            // avoid multiple definitions of the written email
            for (pos in 1 until participantItemList.size) {
                val item = participantItemList[pos]

                if (TextUtils.equals(item.mUserId, firstEntry.mUserId)) {
                    participantItemList.removeAt(pos)
                    break
                }
            }

            mFirstEntry = firstEntry
        } else {
            mFirstEntry = null
        }

        mParticipantsList.clear()

        if (!TextUtils.isEmpty(mPattern)) {
            if (participantItemList.size > 0 && sort) {
                Collections.sort(participantItemList, mSortMethod)
            }
            mParticipantsList.addAll(participantItemList)
        }

        searchListener?.onSearchEnd(mParticipantsList.size)

        notifyDataSetChanged()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        val item = getChild(groupPosition, childPosition) as ParticipantAdapterItem?
        return groupPosition != 0 || item!!.mIsValid
    }


    override fun getGroupCount(): Int {
        return 1
    }

    /**
     * Tells if the session could contain some unused participants.
     *
     * @return true if the session could contains some unused participants.
     */
    private fun couldHaveUnusedParticipants(): Boolean {
        // if the mUnusedParticipants has been initialised
        if (null != mUnusedParticipants) {
            return mUnusedParticipants!!.isNotEmpty()
        } else { // else if there are rooms with more than one user
            val rooms = mSession.dataHandler.store.rooms

            for (room in rooms) {
                if (room.numberOfMembers > 1) {
                    return true
                }
            }
            return false
        }
    }

    private fun getGroupTitle(position: Int): String {
        val groupSize = mParticipantsList.size
        val titleExtra: String

        if (TextUtils.isEmpty(mPattern) && couldHaveUnusedParticipants()) {
            titleExtra = "-"
        } else if (mIsOfflineContactsSearch) {
            titleExtra = mContext.getString(R.string.offline) + ", " + groupSize.toString()
        } else {
            titleExtra = (if (mKnownContactsLimited) ">" else "") + groupSize.toString()
        }

        return mContext.getString(R.string.people_search_user_directory, titleExtra)
    }

    override fun getGroup(groupPosition: Int): Any {
        return getGroupTitle(groupPosition)
    }

    override fun getGroupId(groupPosition: Int): Long {
        return getGroupTitle(groupPosition).hashCode().toLong()
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return mParticipantsList.size
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any? {
        if (childPosition < mParticipantsList.size && childPosition >= 0) {
            return mParticipantsList[childPosition]
        }
        return null
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        val item = getChild(groupPosition, childPosition)

        return item?.hashCode()?.toLong() ?: 0L

    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (null == convertView) {
            convertView = mLayoutInflater.inflate(mHeaderLayoutResourceId, null)
        }

        val sectionNameTxtView = convertView!!.findViewById<TextView>(R.id.people_header_text_view)

        if (null != sectionNameTxtView) {
            val title = getGroupTitle(groupPosition)
            sectionNameTxtView.text = title
        }

        val subLayout = convertView.findViewById<View>(R.id.people_header_sub_layout)

        // reported by GA
        if (null == subLayout) {
            Log.e(LOG_TAG, "## getGroupView() : null subLayout")
            return convertView
        }

        subLayout.visibility = View.VISIBLE

        val loadingView = subLayout.findViewById<View>(R.id.heading_loading_view)

        // reported by GA
        if (null == loadingView) {
            Log.e(LOG_TAG, "## getGroupView() : null loadingView")
            return convertView
        }

        loadingView.visibility = View.GONE

        val imageView = convertView.findViewById<ImageView>(R.id.heading_image)
        val matrixView = convertView.findViewById<View>(R.id.people_header_matrix_contacts_layout)

        // reported by GA
        if (null == imageView || null == matrixView) {
            Log.e(LOG_TAG, "## getGroupView() : null UI items")
            return convertView
        }

        imageView.setImageDrawable(null)
        val checkBox = convertView.findViewById<CheckBox>(R.id.contacts_filter_checkbox)
        checkBox.visibility = View.GONE
        matrixView.visibility = View.GONE

        if (parent is ExpandableListView) {
            parent.expandGroup(groupPosition)
        }

        return convertView
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
        val convertView = mLayoutInflater.inflate(mCellLayoutResourceId, parent, false)

        if (childPosition >= mParticipantsList.size) {
            Log.e(LOG_TAG, "## getChildView() : invalid child position")
            return convertView
        }

        val participant = mParticipantsList[childPosition]

        // retrieve the ui items
        val nameTextView = convertView.findViewById<TextView>(R.id.filtered_list_name)

        // reported by GA
        // it should never happen but it happened...
        if (null == nameTextView) {
            Log.e(LOG_TAG, "## getChildView() : nameTextView is null")
            return convertView
        }

        synchronized(LOG_TAG) {
            nameTextView.text = participant.getUniqueDisplayName(mDisplayNamesList)
        }

        // Add alpha if cannot be invited
        convertView.alpha = if (participant.mIsValid) 1f else 0.5f

        return convertView
    }

    companion object {
        private val LOG_TAG = CaseCreateRecipientsAdapter::class.java.simpleName

        private const val MAX_USERS_SEARCH_COUNT = 100

        /**
         * Tells if an item fulfills the search method.
         *
         * @param item    the item to test
         * @param pattern the pattern
         * @return true if match the search method
         */
        private fun match(item: ParticipantAdapterItem, pattern: String?): Boolean {
            return item.startsWith(pattern)
        }
    }
}
