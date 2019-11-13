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

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import im.vector.R
import im.vector.view.CaseDataSectionHeaderView
import kotlinx.android.synthetic.main.fragment_case_data_list.*
import org.matrix.androidsdk.MXSession
import org.matrix.androidsdk.core.JsonUtils
import org.matrix.androidsdk.core.Log
import org.matrix.androidsdk.core.callback.ApiCallback
import org.matrix.androidsdk.core.model.MatrixError
import org.matrix.androidsdk.data.Room
import org.matrix.androidsdk.data.RoomState
import org.matrix.androidsdk.listeners.MXEventListener
import org.matrix.androidsdk.rest.model.Event
import org.matrix.androidsdk.rest.model.TokensChunkEvents
import org.matrix.androidsdk.rest.model.message.Message

class CaseDataListFragment : Fragment(), AbsHomeFragment.OnRoomChangedListener {
    private var mOnRoomChangedListener: AbsHomeFragment.OnRoomChangedListener? = null

    protected var mNextBatch: String? = null

    private val MESSAGES_PAGINATION_LIMIT = 80
    private var mCanPaginateBack = false

    private var mRoom: Room? = null
    private var mSession: MXSession? = null

    // crypto management
    private val mTimeLineId = System.currentTimeMillis().toString() + ""

    /*
     * *********************************************************************************************
     * Fragment lifecycle
     * *********************************************************************************************
     */

    val layoutResId: Int
        get() = R.layout.fragment_case_data_list

    private val mEventsListener = object : MXEventListener() {
        override fun onLiveEvent(event: Event, roomState: RoomState) {
            if (activity != null) {
                activity?.runOnUiThread {
                    populateViewForEvent(event)
                }
            } else {
                populateViewForEvent(event)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        mRoom?.addEventListener(mEventsListener)

        mOnRoomChangedListener = this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_case_data_list, null)
        return view
    }

    /*
     * *********************************************************************************************
     * UI management
     * *********************************************************************************************
     */

    private fun initViews() {
        case_data_message_section_header.show(CaseDataSectionHeaderView.CASE_DATA_SECTION_MESSAGE)
        case_data_pictures_section_header.show(CaseDataSectionHeaderView.CASE_DATA_SECTION_PICTURES, 0)

        // Hide all other views first
        case_data_patient_view.visibility = View.GONE
        case_data_note_view.visibility = View.GONE
        case_data_requester_view.visibility = View.GONE
        case_data_responsiveness_view.visibility = View.GONE
        case_data_pain_view.visibility = View.GONE
        case_data_misc_view.visibility = View.GONE
        case_data_last_defecation_view.visibility = View.GONE
        case_data_body_weight_view.visibility = View.GONE
        case_data_body_temperature_view.visibility = View.GONE
        case_data_glucose_view.visibility = View.GONE
        case_data_blood_pressure_view.visibility = View.GONE
        case_data_pulse_view.visibility = View.GONE
        case_data_pictures_view.visibility = View.GONE
        case_data_oxygen_view.visibility = View.GONE

        case_data_anamnesis_section_header.visibility = View.GONE
        case_data_vitals_section_header.visibility = View.GONE

        pullData(ArrayList(), object : ApiCallback<java.util.ArrayList<Event>> {
            override fun onSuccess(pulledEvents: java.util.ArrayList<Event>?) {

                for (event: Event in pulledEvents!!.iterator()) {
                    if (activity != null) {
                        activity?.runOnUiThread {
                            populateViewForEvent(event)
                        }
                    } else {
                        populateViewForEvent(event)
                    }
                }
            }

            private fun onError(errorMessage: String) {
                Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show()
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
    }

    private fun populateViewForEvent(event: Event) {
        if (event.getType() == "care.amp.patient") {
            case_data_patient_view.displayEvent(event, CASE_DATA_PATIENT)
        } else if (event.getType() == "care.amp.case") {
            case_data_title_view.displayEvent(event, CASE_DATA_TITLE)
            case_data_note_view.displayEvent(event, CASE_DATA_NOTE)
            case_data_requester_view.displayEvent(event, CASE_DATA_REQUESTER)
            case_data_severity_view.displayEvent(event, CASE_DATA_SEVERITY)
        } else if (event.getType() == Event.EVENT_TYPE_STATE_ROOM_CREATE) {
            case_data_created_view.displayEvent(event, CASE_DATA_CREATED)
        } else if (event.getType() == "care.amp.observation") {
            when (event.contentAsJsonObject["id"].asString) {
                "responsiveness" -> {
                    case_data_responsiveness_view.displayEvent(event, CASE_DATA_RESPONSIVENESS)

                    case_data_anamnesis_section_header.show(CaseDataSectionHeaderView.CASE_DATA_SECTION_ANAMNESIS)
                }
                "pain" -> {
                    case_data_pain_view.displayEvent(event, CASE_DATA_PAIN)

                    case_data_anamnesis_section_header.show(CaseDataSectionHeaderView.CASE_DATA_SECTION_ANAMNESIS)
                }
                "misc" -> {
                    case_data_misc_view.displayEvent(event, CASE_DATA_MISC)

                    case_data_anamnesis_section_header.show(CaseDataSectionHeaderView.CASE_DATA_SECTION_ANAMNESIS)
                }
                "last-defecation" -> {
                    case_data_last_defecation_view.displayEvent(event, CASE_DATA_LAST_DEFECATION)

                    case_data_anamnesis_section_header.show(CaseDataSectionHeaderView.CASE_DATA_SECTION_ANAMNESIS)
                }
                "body-weight" -> {
                    case_data_body_weight_view.displayEvent(event, CASE_DATA_BODY_WEIGHT)

                    case_data_vitals_section_header.show(CaseDataSectionHeaderView.CASE_DATA_SECTION_VITALS)
                }
                "body-temperature" -> {
                    case_data_body_temperature_view.displayEvent(event, CASE_DATA_BODY_TEMPERATURE)

                    case_data_vitals_section_header.show(CaseDataSectionHeaderView.CASE_DATA_SECTION_VITALS)
                }
                "glucose" -> {
                    case_data_glucose_view.displayEvent(event, CASE_DATA_GLUCOSE)

                    case_data_vitals_section_header.show(CaseDataSectionHeaderView.CASE_DATA_SECTION_VITALS)
                }
                "blood-pressure" -> {
                    case_data_blood_pressure_view.displayEvent(event, CASE_DATA_BLOOD_PRESSURE)

                    case_data_vitals_section_header.show(CaseDataSectionHeaderView.CASE_DATA_SECTION_VITALS)
                }
                "heart-rate" -> {
                    case_data_pulse_view.displayEvent(event, CASE_DATA_PULSE)

                    case_data_vitals_section_header.show(CaseDataSectionHeaderView.CASE_DATA_SECTION_VITALS)
                }
                "oxygen" -> {
                    case_data_oxygen_view.displayEvent(event, CASE_DATA_OXYGEN)

                    case_data_vitals_section_header.show(CaseDataSectionHeaderView.CASE_DATA_SECTION_VITALS)
                }
            }
        } else if (event.getType() == Event.EVENT_TYPE_MESSAGE) {
            val message = JsonUtils.toMessage(event.content)

            if (Message.MSGTYPE_IMAGE == message.msgtype
                    || Message.MSGTYPE_VIDEO == message.msgtype) {
                case_data_pictures_view.mSession = mSession
                case_data_pictures_view.displayEvent(event, CASE_DATA_PICTURES)
                case_data_pictures_section_header.incrementPrefix()
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
            var addEvent = false

            if (Event.EVENT_TYPE_MESSAGE == event.getType()) {
                val message = JsonUtils.toMessage(event.content)

                if (Message.MSGTYPE_IMAGE == message.msgtype
                        || Message.MSGTYPE_VIDEO == message.msgtype) {
                    addEvent = true
                }
            } else if (event.getType().startsWith("care.amp.") || event.getType() == Event.EVENT_TYPE_STATE_ROOM_CREATE) {
                addEvent = true
            }
            if (addEvent) {
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
    fun pullData(events: java.util.ArrayList<Event>, callback: ApiCallback<java.util.ArrayList<Event>>) {
        val store = mRoom?.timeline?.store
        val dataHandler = mRoom?.dataHandler

        dataHandler?.dataRetriever?.backPaginate(store, mRoom?.roomId, mNextBatch, MESSAGES_PAGINATION_LIMIT, dataHandler.paginationFilter, object : ApiCallback<TokensChunkEvents> {

            private fun onError(errorMessage: String) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
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

    /*
     * *********************************************************************************************
     * Listeners
     * *********************************************************************************************
     */

    override fun onToggleDirectChat(roomId: String, isDirectChat: Boolean) {}

    override fun onRoomLeft(roomId: String) {}

    override fun onRoomForgot(roomId: String) {
        // there is no sync event when a room is forgotten
    }

    /*
     * *********************************************************************************************
     * Companion
     * *********************************************************************************************
     */

    companion object {
        private val LOG_TAG = CaseDataListFragment::class.java.simpleName
        val CASE_DATA_NONE = -1
        val CASE_DATA_PATIENT = 0
        val CASE_DATA_CREATED = 1
        val CASE_DATA_REQUESTER = 2
        val CASE_DATA_SEVERITY = 3
        val CASE_DATA_TITLE = 4
        val CASE_DATA_NOTE = 5
        val CASE_DATA_RESPONSIVENESS = 6
        val CASE_DATA_PAIN = 7
        val CASE_DATA_MISC = 8
        val CASE_DATA_LAST_DEFECATION = 9
        val CASE_DATA_BODY_WEIGHT = 10
        val CASE_DATA_BODY_TEMPERATURE = 11
        val CASE_DATA_GLUCOSE = 12
        val CASE_DATA_BLOOD_PRESSURE = 13
        val CASE_DATA_PULSE = 14
        val CASE_DATA_PICTURES = 15
        val CASE_DATA_OXYGEN = 16

        fun newInstance(session: MXSession, room: Room): CaseDataListFragment {
            val fragment = CaseDataListFragment()
            fragment.mSession = session
            fragment.mRoom = room
            return fragment
        }
    }
}
