package im.vector.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.widget.Toolbar.LayoutParams
import com.google.gson.JsonObject
import im.vector.R
import im.vector.activity.CaseCreateRecipientActivity.Companion.EXTRA_OUT_SELECTED_RECIPIENT
import im.vector.adapters.ParticipantAdapterItem
import im.vector.fragments.CaseCreateAnamnesisDataFragment.Companion.LAST_DEFECATION
import im.vector.fragments.CaseCreateAnamnesisDataFragment.Companion.MISC
import im.vector.fragments.CaseCreateAnamnesisDataFragment.Companion.PAIN
import im.vector.fragments.CaseCreateAnamnesisDataFragment.Companion.RESPONSIVENESS
import im.vector.fragments.CaseCreateFragment
import im.vector.fragments.CaseCreatePicturesDataFragment.Companion.PICTURES
import im.vector.fragments.CaseCreateSeverityDataFragment.Companion.SEVERITY
import im.vector.fragments.CaseCreateVitalsDataFragment.Companion.BLOOD_PRESSURE
import im.vector.fragments.CaseCreateVitalsDataFragment.Companion.BODY_TEMPERATURE
import im.vector.fragments.CaseCreateVitalsDataFragment.Companion.BODY_WEIGHT
import im.vector.fragments.CaseCreateVitalsDataFragment.Companion.GLUCOSE
import im.vector.fragments.CaseCreateVitalsDataFragment.Companion.OXYGEN
import im.vector.fragments.CaseCreateVitalsDataFragment.Companion.PULSE
import im.vector.util.*
import org.matrix.androidsdk.core.callback.ApiCallback
import org.matrix.androidsdk.core.callback.SimpleApiCallback
import org.matrix.androidsdk.core.model.MatrixError
import org.matrix.androidsdk.crypto.MXCRYPTO_ALGORITHM_MEGOLM
import org.matrix.androidsdk.crypto.MXCryptoError
import org.matrix.androidsdk.crypto.data.MXDeviceInfo
import org.matrix.androidsdk.crypto.data.MXUsersDevicesMap
import org.matrix.androidsdk.data.Room
import org.matrix.androidsdk.data.RoomMediaMessage
import org.matrix.androidsdk.rest.model.CreateRoomParams
import org.matrix.androidsdk.rest.model.Event
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CaseCreateActivity : MXCActionBarActivity() {
    val LOG_TAG = CaseCreateActivity::class.java.simpleName
    private var fragment: CaseCreateFragment? = null
    private lateinit var createCaseButton: ImageButton

    override fun getLayoutRes(): Int {
        return R.layout.activity_case_create
    }

    override fun initUiAndData() {
        mSession = getSession(intent)

        if (mSession == null || !mSession.isAlive) {
            Log.e(LOG_TAG, "No MXSession.")
            finish()
            return
        }

        caseDataMap.clear()

        waitingView = findViewById(R.id.waiting_view)

        toolbar.title = getString(R.string.case_create_title)
        configureToolbar()

        createCaseButton = ImageButton(this)

        createCaseButton.setImageResource(R.drawable.ic_material_send_white)
        createCaseButton.setOnClickListener {
            createCase()
        }
        createCaseButton.setBackgroundColor(Color.TRANSPARENT)
        toolbar.addView(createCaseButton)
        val param = createCaseButton.layoutParams as LayoutParams
        param.gravity = Gravity.END
        param.setMargins(0, 0, 24, 0)
        createCaseButton.layoutParams = param

        // Fill data with default values
        if (!caseDataMap.containsKey(CASE_DATA_SEVERITY)) {
            caseDataMap[CASE_DATA_SEVERITY] = "info"
        }

        updateCreateCaseButtonState()

        val args = Bundle()
        args.putString(CaseCreateFragment.ARG_MATRIX_ID, mSession.myUserId)
        fragment = CaseCreateFragment()
        fragment!!.arguments = args

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment!!, null)
                .commit()
    }

    internal fun updateCreateCaseButtonState() {
        createCaseButton.isEnabled = caseDataMap.containsKey(CASE_DATA_TITLE) && caseDataMap.containsKey(CASE_DATA_SEVERITY) && caseDataMap.containsKey(CASE_DATA_RECIPIENT)
        if (createCaseButton.isEnabled) {
            createCaseButton.alpha = 1f
        } else {
            createCaseButton.alpha = .35f
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.e(LOG_TAG, "## onActivityResult() : Coming back from case create item activity")
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        data?.let {} ?: return

        when (requestCode) {
            CASE_DATA_RECIPIENT -> {
                if (data.hasExtra(EXTRA_OUT_SELECTED_RECIPIENT)) {
                    val value = data.getSerializableExtra(EXTRA_OUT_SELECTED_RECIPIENT)
                    caseDataMap[CASE_DATA_RECIPIENT] = value
                }
            }
            CASE_DATA_PATIENT -> {
                val patientData = HashMap<String, String>()
                if (data.hasExtra(PATIENT_NAME)) {
                    val value = data.getStringExtra(PATIENT_NAME)
                    patientData[PATIENT_NAME] = value
                }
                if (data.hasExtra(PATIENT_GENDER)) {
                    val value = data.getStringExtra(PATIENT_GENDER)
                    patientData[PATIENT_GENDER] = value
                }
                if (data.hasExtra(PATIENT_BIRTHDATE)) {
                    val value = data.getStringExtra(PATIENT_BIRTHDATE)
                    patientData[PATIENT_BIRTHDATE] = value
                }
                if (patientData.size > 0) {
                    caseDataMap[CASE_DATA_PATIENT] = patientData
                } else {
                    caseDataMap.remove(CASE_DATA_PATIENT)
                }
            }
            CASE_DATA_SEVERITY -> {
                if (data.hasExtra(SEVERITY)) {
                    caseDataMap[CASE_DATA_SEVERITY] = data.getStringExtra(SEVERITY)
                } else {
                    caseDataMap.remove(CASE_DATA_SEVERITY)
                }
            }
            CASE_DATA_ANAMNESIS -> {
                var extra = RESPONSIVENESS
                if (data.hasExtra(extra)) {
                    caseDataMap[CASE_DATA_RESPONSIVENESS] = data.getSerializableExtra(extra)
                } else {
                    caseDataMap.remove(CASE_DATA_RESPONSIVENESS)
                }
                extra = PAIN
                if (data.hasExtra(extra)) {
                    caseDataMap[CASE_DATA_PAIN] = data.getSerializableExtra(extra)
                } else {
                    caseDataMap.remove(CASE_DATA_PAIN)
                }
                extra = MISC
                if (data.hasExtra(extra)) {
                    caseDataMap[CASE_DATA_MISC] = data.getSerializableExtra(extra)
                } else {
                    caseDataMap.remove(CASE_DATA_MISC)
                }
                extra = LAST_DEFECATION
                if (data.hasExtra(extra)) {
                    caseDataMap[CASE_DATA_LAST_DEFECATION] = data.getSerializableExtra(extra)
                } else {
                    caseDataMap.remove(CASE_DATA_LAST_DEFECATION)
                }
            }
            CASE_DATA_VITALS -> {
                var extra = BODY_WEIGHT
                if (data.hasExtra(extra)) {
                    caseDataMap[CASE_DATA_BODY_WEIGHT] = data.getSerializableExtra(extra)
                } else {
                    caseDataMap.remove(CASE_DATA_BODY_WEIGHT)
                }
                extra = BODY_TEMPERATURE
                if (data.hasExtra(extra)) {
                    caseDataMap[CASE_DATA_BODY_TEMPERATURE] = data.getSerializableExtra(extra)
                } else {
                    caseDataMap.remove(CASE_DATA_BODY_TEMPERATURE)
                }
                extra = GLUCOSE
                if (data.hasExtra(extra)) {
                    caseDataMap[CASE_DATA_GLUCOSE] = data.getSerializableExtra(extra)
                } else {
                    caseDataMap.remove(CASE_DATA_GLUCOSE)
                }
                extra = BLOOD_PRESSURE
                if (data.hasExtra(extra)) {
                    caseDataMap[CASE_DATA_BLOOD_PRESSURE] = data.getSerializableExtra(extra)
                } else {
                    caseDataMap.remove(CASE_DATA_BLOOD_PRESSURE)
                }
                extra = PULSE
                if (data.hasExtra(extra)) {
                    caseDataMap[CASE_DATA_PULSE] = data.getSerializableExtra(extra)
                } else {
                    caseDataMap.remove(CASE_DATA_PULSE)
                }
                extra = OXYGEN
                if (data.hasExtra(extra)) {
                    caseDataMap[CASE_DATA_OXYGEN] = data.getSerializableExtra(extra)
                } else {
                    caseDataMap.remove(CASE_DATA_OXYGEN)
                }
            }
            CASE_DATA_PICTURES -> {
                if (data.hasExtra(PICTURES)) {
                    caseDataMap[CASE_DATA_PICTURES] = data.getSerializableExtra(PICTURES)
                } else {
                    caseDataMap.remove(CASE_DATA_PICTURES)
                }
            }
        }

        fragment!!.populateView()
        updateCreateCaseButtonState()
    }

    private fun createCase() {
        Log.d(LOG_TAG, "## createCase : Creating case...")

        showWaitingView()

        val roomParams = CreateRoomParams()
        roomParams.setDirectMessage()
        roomParams.addCryptoAlgorithm(MXCRYPTO_ALGORITHM_MEGOLM)

        (caseDataMap[CASE_DATA_RECIPIENT] as? ParticipantAdapterItem)?.let { recipient ->
            roomParams.invitedUserIds = listOf(recipient.mUserId)
        } ?: run {
            return
        }

        mSession.createRoom(roomParams, object : SimpleApiCallback<String>() {

            override fun onSuccess(roomId: String) {
                val room = mSession.dataHandler.getRoom(roomId)
                Log.d(LOG_TAG, "## createCase : Creating room successful. Now Sending events...")

                sendCaseCoreEvent(room)
                sendPatientEvent(room)
                sendObservationEvents(room)
                sendPictures(room)

                val intent = Intent()
                intent.putExtra(CaseDetailActivity.EXTRA_ROOM_ID, roomId)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }

            private fun onError(message: String) {
                Toast.makeText(this@CaseCreateActivity, message, Toast.LENGTH_SHORT).show()
                hideWaitingView()
            }

            override fun onNetworkError(e: Exception) {
                onError(e.localizedMessage)
            }

            override fun onMatrixError(e: MatrixError) {
                if (MatrixError.M_CONSENT_NOT_GIVEN == e.errcode) {
                    hideWaitingView()

                    consentNotGivenHelper.displayDialog(e)
                } else {
                    onError(e.localizedMessage)
                }
            }

            override fun onUnexpectedError(e: Exception) {
                onError(e.localizedMessage)
            }
        })
    }

    private fun sendPictures(room: Room) {
        (caseDataMap[CASE_DATA_PICTURES] as? ArrayList<Uri>)?.let {pictures ->
            pictures.forEach { uri ->
                // 100 is default
                room.sendMediaMessage(RoomMediaMessage(uri), 100, 100, null)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun sendObservationEvents(room: Room) {
        var patientData = HashMap<String, String>()
        (caseDataMap[CASE_DATA_PATIENT] as? HashMap<String, String>)?.let { data ->
            patientData = data
        }

        var patientName: String? = null
        patientData[PATIENT_NAME]?.let { name ->
            patientName = name
        }

        val types = listOf(CASE_DATA_LAST_DEFECATION,
                CASE_DATA_MISC,
                CASE_DATA_PAIN,
                CASE_DATA_RESPONSIVENESS,
                CASE_DATA_BODY_TEMPERATURE,
                CASE_DATA_GLUCOSE,
                CASE_DATA_PULSE,
                CASE_DATA_OXYGEN,
                CASE_DATA_BLOOD_PRESSURE,
                CASE_DATA_BODY_WEIGHT)
        val filteredList = caseDataMap.filterKeys {key ->
            types.contains(key)
        }
        filteredList.forEach { pair ->
            var json: JsonObject? = null
            when (pair.key) {
                // Anamnesis
                CASE_DATA_RESPONSIVENESS -> {
                    (pair.value as? Pair<String, Date?>)?.let {
                        json = newResponsivenessObservation(it, patientName)
                    }
                }
                CASE_DATA_PAIN -> {
                    (pair.value as? Pair<String, Date?>)?.let {
                        json = newPainObservation(it, patientName)
                    }
                }
                CASE_DATA_MISC -> {
                    (pair.value as? Pair<String, Date?>)?.let {
                        json = newMiscObservation(it, patientName)
                    }
                }
                CASE_DATA_LAST_DEFECATION -> {
                    (pair.value as? Date)?.let {
                        json = newLastDefecationObservation(it, patientName)
                    }
                }
                // Vitals
                CASE_DATA_BODY_WEIGHT -> {
                    (pair.value as? Pair<Float, Date?>)?.let {
                        json = newBodyWeightObservation(it, patientName)
                    }
                }
                CASE_DATA_BODY_TEMPERATURE -> {
                    (pair.value as? Pair<Float, Date?>)?.let {
                        json = newBodyTemperatureObservation(it, patientName)
                    }
                }
                CASE_DATA_GLUCOSE -> {
                    (pair.value as? Pair<Float, Date?>)?.let {
                        json = newGlucoseObservation(it, patientName)
                    }
                }
                CASE_DATA_OXYGEN -> {
                    (pair.value as? Pair<Float, Date?>)?.let {
                        json = newOxygenObservation(it, patientName)
                    }
                }
                CASE_DATA_BLOOD_PRESSURE -> {
                    (pair.value as? Triple<Float, Float, Date?>)?.let {
                        json = newBloodPressureObservation(it, patientName)
                    }
                }
                CASE_DATA_PULSE -> {
                    (pair.value as? Pair<Float, Date?>)?.let {
                        json = newPulseObservation(it, patientName)
                    }
                }
            }
            json?.let {
                Log.d(LOG_TAG, "## sendObservation : Sending observation with id ${it.get("id")}")
                val event = Event("care.amp.observation", it, mSession.myUserId, room.roomId)
                sendEvent(room, event)
            }
        }
    }

    private fun sendCaseCoreEvent(room: Room): Boolean {
        Log.d(LOG_TAG, "## sendEvent : Sending event with case data")
        val jsonObject = JsonObject()

        (caseDataMap[CASE_DATA_TITLE] as? String)?.let { title ->
            jsonObject.addProperty("title", title)
        } ?: run {
            return false
        }

        (caseDataMap[CASE_DATA_SEVERITY] as? String)?.let { severity ->
            jsonObject.addProperty("severity", severity)
        } ?: run {
            return false
        }

        (caseDataMap[CASE_DATA_NOTE] as? String)?.let { note ->
            jsonObject.addProperty("note", note)
        }

        val ref = JsonObject()

        var name = mSession.myUser.displayname
        if (name == null) {
            name = mSession.myUserId
        }

        ref.addProperty("reference", name)
        jsonObject.add("requester", ref)

        val event = Event("care.amp.case", jsonObject, mSession.myUserId, room.roomId)

        sendEvent(room, event, isStateEvent = true)

        return true
    }

    private fun sendPatientEvent(room: Room) {
        Log.d(LOG_TAG, "## sendEvent : Sending event with patient data")
        var patientData = HashMap<String, String>()
        (caseDataMap[CASE_DATA_PATIENT] as? HashMap<String, String>)?.let { data ->
            patientData = data
        } ?: kotlin.run {
            return
        }

        val jsonObject = JsonObject()

        patientData[PATIENT_NAME]?.let { name ->
            jsonObject.addProperty("name", name)
        }

        patientData[PATIENT_GENDER]?.let { gender ->
            jsonObject.addProperty("gender", gender)
        }

        patientData[PATIENT_BIRTHDATE]?.let { birthDate ->
            jsonObject.addProperty("birthDate", birthDate)
        }

        if (jsonObject.size() == 0) {
            return
        }

        val event = Event("care.amp.patient", jsonObject, mSession.myUserId, room.roomId)

        sendEvent(room, event, isStateEvent = true)
    }

    private fun sendEvent(room: Room, event: Event, isStateEvent: Boolean = false) {
        if (isStateEvent) {
            // Follow documentation where stateKey == eventType
            event.stateKey = event.getType()
        }
        Log.d(LOG_TAG, "## sendEvent : Sending event with type ${event.getType()}")
        room.storeOutgoingEvent(event)
        room.sendEvent(event, object : ApiCallback<Void?> {

            private fun onError(errorMessage: String) {
                Log.e(LOG_TAG, "## sendEvent : Sending event with type ${event.getType()} failed: $errorMessage")
            }

            override fun onNetworkError(e: Exception) {
                onError(e.localizedMessage)
            }

            override fun onMatrixError(e: MatrixError) {
                onError(e.message)
                val cryptoError = e as? MXCryptoError
                cryptoError?.let {
                    if (cryptoError.errcode == MXCryptoError.UNKNOWN_DEVICES_CODE) {
                        val devicesMap = cryptoError.mExceptionData as MXUsersDevicesMap<MXDeviceInfo>
                        val devicesList = getDevicesList(devicesMap)

                        val runnable = Runnable {
                            sendEvent(room, event, isStateEvent)
                        }

                        setDevicesKnown(devicesList, runnable)
                    }
                }
            }

            override fun onUnexpectedError(e: Exception) {
                onError(e.localizedMessage)
            }

            override fun onSuccess(info: Void?) {
                Log.d(LOG_TAG, "## sendEvent : Sending event with type ${event.getType()} successful")
            }
        })
    }

    /**
     * Convert a MXUsersDevicesMap to a list of device info
     *
     * @return the device info list
     */
    private fun getDevicesList(devicesMap: MXUsersDevicesMap<MXDeviceInfo>): List<MXDeviceInfo> {
        val res = ArrayList<MXDeviceInfo>()

        val userIds = devicesMap.userIds

        for (userId in userIds) {
            val deviceInfos = ArrayList<MXDeviceInfo>()
            val deviceIds = devicesMap.getUserDeviceIds(userId)

            for (deviceId in deviceIds!!) {
                deviceInfos.add(devicesMap.getObject(deviceId, userId))
            }
            res.addAll(deviceInfos)
        }

        return res
    }

    /**
     * Update the devices verifications status.
     *
     * @param devicesList the devices list.
     */
    private fun setDevicesKnown(devicesList: List<MXDeviceInfo>, func: Runnable) {
        mSession.crypto?.setDevicesKnown(devicesList, object : ApiCallback<Void> {
            private fun onDone() {
                Log.d(LOG_TAG, "## setDevicesKnown(): Made devices known")
                func.run()
            }
            override fun onSuccess(info: Void?) {
                onDone()
            }

            override fun onUnexpectedError(e: java.lang.Exception?) {
                onDone()
            }

            override fun onNetworkError(e: java.lang.Exception?) {
                onDone()
            }

            override fun onMatrixError(e: MatrixError?) {
                onDone()
            }
        })
    }

    companion object {
        const val REQUEST_CODE = 11990
        var caseDataMap = HashMap<Int, Any>()

        const val PATIENT_NAME = "PATIENT_NAME"
        const val PATIENT_GENDER = "PATIENT_GENDER"
        const val PATIENT_BIRTHDATE = "PATIENT_BIRTHDATE"

        const val CASE_DATA_NONE = -1

        const val CASE_DATA_PATIENT = 0
        const val CASE_DATA_RECIPIENT = 1
        const val CASE_DATA_SEVERITY = 2
        const val CASE_DATA_TITLE = 3
        const val CASE_DATA_NOTE = 4
        const val CASE_DATA_RESPONSIVENESS = 5
        const val CASE_DATA_PAIN = 6
        const val CASE_DATA_MISC = 7
        const val CASE_DATA_LAST_DEFECATION = 8
        const val CASE_DATA_BODY_WEIGHT = 9
        const val CASE_DATA_BODY_TEMPERATURE = 10
        const val CASE_DATA_GLUCOSE = 11
        const val CASE_DATA_BLOOD_PRESSURE = 12
        const val CASE_DATA_PULSE = 13
        const val CASE_DATA_PICTURES = 14
        const val CASE_DATA_VITALS = 15
        const val CASE_DATA_ANAMNESIS = 16
        const val CASE_DATA_OXYGEN = 17
    }
}