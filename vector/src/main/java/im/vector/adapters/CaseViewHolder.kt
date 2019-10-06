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

package im.vector.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.TextUtils
import android.view.View
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import butterknife.ButterKnife
import im.vector.R
import im.vector.ui.themes.ThemeUtils
import im.vector.util.RoomUtils
import im.vector.util.setRoundBackground
import kotlinx.android.synthetic.main.adapter_item_case_view.view.*
import kotlinx.android.synthetic.main.adapter_item_case_view.view.room_unread_count
import kotlinx.android.synthetic.main.adapter_item_circular_room_view.view.*
import org.matrix.androidsdk.MXSession
import org.matrix.androidsdk.core.Log
import org.matrix.androidsdk.core.callback.ApiCallback
import org.matrix.androidsdk.core.model.MatrixError
import org.matrix.androidsdk.data.Room
import org.matrix.androidsdk.rest.model.Event
import org.matrix.androidsdk.rest.model.TokensChunkEvents
import java.util.*

class CaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var mNextBatch: String? = null

    private val MESSAGES_PAGINATION_LIMIT = 80
    private var mCanPaginateBack = false

    private var mRoom: Room? = null
    private var mSession: MXSession? = null

    // crypto management
    private val mTimeLineId = System.currentTimeMillis().toString() + ""

    private var context: Context? = null

    init {
        ButterKnife.bind(this, itemView)
    }

    /**
     * Refresh the holder layout
     *
     * @param room                   the room
     * @param isInvitation           true when the room is an invitation one
     * @param moreRoomActionListener
     */
    fun populateViews(context: Context,
                      session: MXSession?,
                      room: Room?,
                      isInvitation: Boolean,
                      moreRoomActionListener: AbsAdapter.MoreRoomActionListener?) {

        // Reset view if different room to wait for latest data
        if (mRoom != room) {
            itemView.case_title.text = ""
            itemView.case_patient.text = ""
            itemView.case_severity.setBackgroundColor(Color.parseColor("#FFFFFF"))
            itemView.room_update_date.text = ""
            itemView.activity_indicator.visibility = View.VISIBLE
        }

        mRoom = room
        mSession = session
        this.context = context
        // sanity check
        if (null == room) {
            Log.e(LOG_TAG, "## populateViews() : null room")
            return
        }

        if (null == session) {
            Log.e(LOG_TAG, "## populateViews() : null session")
            return
        }

        val store = session.dataHandler.getStore(room.roomId)

        if (null == store) {
            Log.e(LOG_TAG, "## populateViews() : null Store")
            return
        }

        val roomSummary = store.getSummary(room.roomId)

        if (null == roomSummary) {
            Log.e(LOG_TAG, "## populateViews() : null roomSummary")
            return
        }

        val unreadMsgCount = roomSummary.unreadEventsCount
        val highlightCount: Int
        var notificationCount: Int

        highlightCount = roomSummary.highlightCount
        notificationCount = roomSummary.notificationCount

        // fix a crash reported by GA
        if (null != room.dataHandler && room.dataHandler.bingRulesManager.isRoomMentionOnly(room.roomId)) {
            notificationCount = highlightCount
        }

        val bingUnreadColor: Int

        if (isInvitation || 0 != highlightCount) {
            bingUnreadColor = ContextCompat.getColor(context, R.color.vector_fuchsia_color)
        } else if (0 != notificationCount) {
            bingUnreadColor = ThemeUtils.getColor(context, R.attr.vctr_notice_secondary)
        } else if (0 != unreadMsgCount) {
            bingUnreadColor = ThemeUtils.getColor(context, R.attr.vctr_unread_room_indent_color)
        } else {
            bingUnreadColor = Color.TRANSPARENT
        }

        if (isInvitation || notificationCount > 0) {
            itemView.room_unread_count.text = if (isInvitation) "!" else RoomUtils.formatUnreadMessagesCounter(notificationCount)
            itemView.room_unread_count.setTypeface(null, Typeface.BOLD)
            itemView.room_unread_count.setRoundBackground(bingUnreadColor)
            itemView.room_unread_count.visibility = View.VISIBLE
        } else {
            itemView.room_unread_count.visibility = View.GONE
        }

        mNextBatch = mRoom?.state?.token

        pullData(ArrayList(), object : ApiCallback<ArrayList<Event>> {
            override fun onSuccess(pulledEvents: ArrayList<Event>?) {
                var caseTitle = ""
                var caseSeverity = ""
                var patientName = "-"

                for (event: Event in pulledEvents!!.iterator()) {
                    val obj = event.contentAsJsonObject
                    if (event.getType() == "care.amp.case") {
                        caseTitle = obj!!.get("title").asString
                        caseSeverity = obj.get("severity").asString
                    } else if (event.getType() == "care.amp.patient") {
                        if (obj!!.has("name")) {
                            patientName = obj.get("name").asString
                        }
                    } else if (event.getType() == Event.EVENT_TYPE_STATE_ROOM_CREATE) {
                        if (itemView.room_update_date != null) {
                            itemView.room_update_date.text = RoomUtils.getRoomTimestamp(context, event)
                        }
                    }
                }

                if (itemView.room_name_server != null) {
                    itemView.case_title.setLines(2)
                    itemView.room_name_server.visibility = View.GONE
                    itemView.case_title.text = caseTitle
                } else {
                    itemView.case_title.text = caseTitle
                }
                itemView.case_title.setTypeface(null, if (0 != unreadMsgCount) Typeface.BOLD else Typeface.NORMAL)

                // Display patient name
                itemView.case_patient.text = patientName

                itemView.case_severity.visibility = View.VISIBLE
                when (caseSeverity) {
                    "info" -> itemView.case_severity.setBackgroundColor(Color.parseColor("#45abf2"))
                    "request" -> itemView.case_severity.setBackgroundColor(Color.parseColor("#26de82"))
                    "urgent" -> itemView.case_severity.setBackgroundColor(Color.parseColor("#f7c930"))
                    "critical" -> itemView.case_severity.setBackgroundColor(Color.parseColor("#eb3b59"))
                    else -> itemView.case_severity.visibility = View.GONE
                }

                itemView.activity_indicator.visibility = View.GONE
            }

            private fun onError(errorMessage: String) {
                makeText(context, errorMessage, LENGTH_SHORT).show()
                itemView.activity_indicator.visibility = View.GONE
            }

            override fun onNetworkError(e: Exception) {
                onError(e.localizedMessage)
            }

            override fun onMatrixError(e: MatrixError) {
                onError(e.localizedMessage)
            }

            override fun onUnexpectedError(e: Exception) {
                onError(e.localizedMessage)
            }
        })

        if (itemView.indicator_unread_message != null) {
            // set bing view background colour
            itemView.indicator_unread_message.setBackgroundColor(bingUnreadColor)
            itemView.indicator_unread_message.visibility = if (roomSummary.isInvited) View.INVISIBLE else View.VISIBLE
        }

        if (itemView.room_more_action_click_area != null && itemView.room_more_action_anchor != null) {
            itemView.room_more_action_click_area.setOnClickListener {
                moreRoomActionListener?.onMoreActionClick(itemView.room_more_action_anchor, room)
            }
        }
    }

    /**
     * Filter and append the found events
     *
     * @param events         the matched events list
     * @param eventsToAppend the retrieved events list.
     */
    private fun appendEvents(events: ArrayList<Event>, eventsToAppend: List<Event>) {
        // filter
        val filteredEvents = ArrayList<Event>(eventsToAppend.size)
        for (event in eventsToAppend) {
            if (event.getType() == "care.amp.patient" || event.getType() == "care.amp.case" || event.getType() == Event.EVENT_TYPE_STATE_ROOM_CREATE) {
                filteredEvents.add(event)
            }
        }

        events.addAll(filteredEvents)
    }

    /**
     * Back paginate until the room creation event is found so
     * all case events are pulled from the server.
     *
     * @param events   the result events lists
     * @param callback the result callback
     */
    fun pullData(events: ArrayList<Event>, callback: ApiCallback<ArrayList<Event>>) {
        val store = mRoom?.timeline?.store
        val dataHandler = mRoom?.dataHandler

        dataHandler?.dataRetriever?.backPaginate(store, mRoom?.roomId, mNextBatch, MESSAGES_PAGINATION_LIMIT, dataHandler.paginationFilter, object : ApiCallback<TokensChunkEvents> {

            private fun onError(errorMessage: String) {
                makeText(context, errorMessage, LENGTH_SHORT).show()
                callback.onSuccess(events)
            }

            override fun onNetworkError(e: Exception) {
                onError(e.localizedMessage)
            }

            override fun onMatrixError(e: MatrixError) {
                onError(e.localizedMessage)
            }

            override fun onUnexpectedError(e: Exception) {
                onError(e.localizedMessage)
            }

            override fun onSuccess(eventsChunk: TokensChunkEvents) {
                if (null == mNextBatch || TextUtils.equals(eventsChunk.start, mNextBatch)) {
                    // no more message in the history
                    if (TextUtils.equals(eventsChunk.start, eventsChunk.end)) {
                        mCanPaginateBack = false
                        callback.onSuccess(events)
                    } else {
                        // decrypt the encrypted events
                        if (mRoom!!.isEncrypted) {
                            for (event in eventsChunk.chunk) {
                                if (event.type == Event.EVENT_TYPE_MESSAGE_ENCRYPTED && !mSession?.dataHandler?.decryptEvent(event, mTimeLineId)!!) {
                                    Log.e(LOG_TAG, "Couldn't decrypt Event")
                                }
                            }
                        }

                        // append the retrieved one
                        appendEvents(events, eventsChunk.chunk)
                        mNextBatch = eventsChunk.end

                        pullData(events, callback)
                    }
                }
            }
        })
    }

    companion object {
        private val LOG_TAG = CaseViewHolder::class.java.simpleName
    }
}
