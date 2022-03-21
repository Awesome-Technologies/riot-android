package im.vector.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import android.widget.Toast
import im.vector.Matrix
import im.vector.R
import im.vector.VectorApp
import im.vector.activity.BarcodeLoginActivity
import im.vector.activity.LoginActivity
import im.vector.activity.VectorHomeActivity
import im.vector.activity.VectorRoomActivity
import org.matrix.androidsdk.MXSession
import org.matrix.androidsdk.core.Log
import org.matrix.androidsdk.core.callback.ApiCallback
import org.matrix.androidsdk.core.callback.SimpleApiCallback
import org.matrix.androidsdk.core.model.MatrixError
import org.matrix.androidsdk.data.Room
import org.matrix.androidsdk.data.store.IMXStore
import org.matrix.androidsdk.features.identityserver.IdentityServerNotConfiguredException
import org.matrix.androidsdk.rest.model.CreateRoomParams
import org.matrix.androidsdk.rest.model.RoomMember
import kotlin.math.min

data class SendMessageModel(val message: String, val recipient: String, val sendNow: Boolean = false): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readByte() != 0.toByte()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(message)
        parcel.writeString(recipient)
        parcel.writeByte(if (sendNow) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SendMessageModel> {
        override fun createFromParcel(parcel: Parcel): SendMessageModel {
            return SendMessageModel(parcel)
        }

        override fun newArray(size: Int): Array<SendMessageModel?> {
            return arrayOfNulls(size)
        }
    }
}

class AMPSendMessageReceiver: BroadcastReceiver() {
    private val LOG_TAG = AMPSendMessageReceiver::class.java.simpleName

    // send message right away
    private val EXTRA_MESSAGE_SEND_NOW = "EXTRA_MESSAGE_SEND_NOW"

    // message to send
    private val EXTRA_MESSAGE = "EXTRA_MESSAGE"

    // message recipient
    private val EXTRA_MESSAGE_RECIPIENT = "EXTRA_MESSAGE_RECIPIENT"

    // message data to pass
    private val EXTRA_MESSAGE_DATA = "EXTRA_MESSAGE_DATA"

    // the session
    var mSession: MXSession? = null

    // the context
    lateinit var mContext: Context

    override fun onReceive(context: Context?, aIntent: Intent?) {
        Log.d(LOG_TAG, "## onReceive() action = chat.amp.messenger.SEND_MESSAGE")

        if (aIntent == null || context == null) {
            return
        }
        mContext = context

        var messageData = aIntent.getParcelableExtra<SendMessageModel>(EXTRA_MESSAGE_DATA)

        var message = aIntent.getStringExtra(EXTRA_MESSAGE)
        val recipient = aIntent.getStringExtra(EXTRA_MESSAGE_RECIPIENT)
        val sendNow: Boolean = aIntent.getBooleanExtra(EXTRA_MESSAGE_SEND_NOW, false)

        if ((message.isNullOrEmpty() || recipient.isNullOrEmpty()) && messageData == null) {
            Log.d(LOG_TAG, "## onReceive() message or recipient not there. Exiting…")
            return
        }

        if (messageData == null) {
            message = message!!.substring(0, min(message.length, 300))
            messageData = SendMessageModel(message, recipient!!, sendNow)
        }

        Log.d(LOG_TAG, "## onReceive() All good!")

        // get session
        mSession = Matrix.getInstance(context).defaultSession

        // user is not yet logged in
        if (null == mSession) {
            Log.e(LOG_TAG, "## onReceive() Warning - Unable to proceed: Session is null")

            // No user is logged => no session. Just forward request to the login activity
            val intent = Intent(context, getLaunchActivity())
            intent.putExtra(EXTRA_MESSAGE_DATA, messageData)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return
        }

        val isSessionActive: Boolean = mSession!!.isAlive
        val isLoginStepDone: Boolean = mSession!!.dataHandler.isInitialSyncComplete

        if (!isSessionActive) {
            Log.w(LOG_TAG, "## onReceive() Warning: Session is not alive")
        }

        if (!isLoginStepDone) {
            Log.w(LOG_TAG, "## onReceive() Warning: Session is not complete - start Login Activity")

            // Start the login activity and wait for BROADCAST_ACTION_AMP_SEND_MESSAGE_RESUME.
            // Once the login process flow is complete, BROADCAST_ACTION_AMP_SEND_MESSAGE_RESUME is
            // sent back to resume the URL link processing.
            val intent = Intent(context, getLaunchActivity())
            intent.putExtra(EXTRA_MESSAGE_DATA, messageData)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            manageChatActivity(context, messageData)
        }
    }

    private fun getLaunchActivity(): Class<out Activity> {
        return if (mContext.resources.getBoolean(R.bool.login_with_barcode)) {
            BarcodeLoginActivity::class.java
        } else {
            LoginActivity::class.java
        }
    }

    /**
     * try to find a suitable direct chat with the given recipient or create one
     *
     * @param aContext the context.
     */
    private fun manageChatActivity(aContext: Context?, messageData: SendMessageModel) {
        doesDirectChatRoomAlreadyExist(messageData.recipient, object : SimpleApiCallback<String>() {
            override fun onSuccess(roomId: String?) {
                if (roomId == null) {
                    createRoom(listOf(mSession!!.credentials.userId, messageData.recipient), messageData)
                } else {
                    startChatActivity(aContext, roomId, messageData)
                }
            }
        })
    }

    private fun startChatActivity(aContext: Context?, roomId: String, messageData: SendMessageModel) {
        val currentActivity = VectorApp.getCurrentActivity()
        if (null != currentActivity) {

            val startChatIntent = Intent(currentActivity, VectorRoomActivity::class.java)
            startChatIntent.putExtra(VectorRoomActivity.EXTRA_ROOM_ID, roomId)
            startChatIntent.putExtra(VectorRoomActivity.EXTRA_MATRIX_ID, mSession!!.credentials.userId)
            startChatIntent.putExtra(VectorRoomActivity.EXTRA_SEND_MESSAGE_DATA, messageData)
            currentActivity.startActivity(startChatIntent)
        } else {
            // clear the activity stack to home activity
            val intent = Intent(aContext, VectorHomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

            val params = HashMap<String, Any>()

            params[VectorRoomActivity.EXTRA_MATRIX_ID] = mSession!!.credentials.userId
            params[VectorRoomActivity.EXTRA_ROOM_ID] = roomId
            params[VectorRoomActivity.EXTRA_SEND_MESSAGE_DATA] = messageData

            intent.putExtra(VectorHomeActivity.EXTRA_JUMP_TO_ROOM_PARAMS, params)
            aContext!!.startActivity(intent)
        }
    }

    //================================================================================
    // Room creation
    //================================================================================
    /**
     * Return the first direct chat room for a given user ID.
     *
     * @param aUserId  user ID to search for
     * @param callback callback to return a room ID if search succeed, null otherwise.
     */
    private fun doesDirectChatRoomAlreadyExist(aUserId: String, callback: ApiCallback<String?>) {
        val store: IMXStore? = mSession!!.dataHandler.store
        val directChatRoomsDict: Map<String, List<String>>
        if (null != store?.directChatRoomsDict) {
            directChatRoomsDict = HashMap(store.directChatRoomsDict)
            if (directChatRoomsDict.containsKey(aUserId)) {
                val roomIdsList: List<String> = ArrayList(directChatRoomsDict.getValue(aUserId))
                doesDirectChatRoomAlreadyExistRecursive(roomIdsList, 0, aUserId, callback)
            } else {
                callback.onSuccess(null)
            }
        } else {
            callback.onSuccess(null)
        }
    }

    private fun doesDirectChatRoomAlreadyExistRecursive(roomIdsList: List<String>,
                                                        index: Int,
                                                        aUserId: String,
                                                        callback: ApiCallback<String?>) {
        if (index >= roomIdsList.size) {
            Log.d(LOG_TAG, "## doesDirectChatRoomAlreadyExist(): for user=$aUserId no found room")
            callback.onSuccess(null)
        } else {
            val room: Room? = mSession!!.dataHandler.getRoom(roomIdsList[index], false)

            // check if the room is already initialized
            if (room != null && room.isReady && !room.isInvited && !room.isLeaving) {
                room.getActiveMembersAsync(object : SimpleApiCallback<List<RoomMember?>?>(callback) {
                    override fun onSuccess(members: List<RoomMember?>?) {
                        if (members == null) return
                        // test if the member did not leave the room
                        for (member in members) {
                            if (TextUtils.equals(member?.userId, aUserId)) {
                                Log.d(LOG_TAG, "## doesDirectChatRoomAlreadyExist(): for user=" + aUserId + " roomFound=" + roomIdsList[index])
                                callback.onSuccess(roomIdsList[index])
                                return
                            }
                        }

                        // Try next one
                        doesDirectChatRoomAlreadyExistRecursive(roomIdsList, index + 1, aUserId, callback)
                    }
                })
            } else {
                // Try next one
                doesDirectChatRoomAlreadyExistRecursive(roomIdsList, index + 1, aUserId, callback)
            }
        }
    }

    /**
     * Create a room with a list of participants.
     *
     * @param participants the list of participant
     */
    private fun createRoom(participants: List<String>, messageData: SendMessageModel) {
        val params = CreateRoomParams()

        // First participant is self, so remove
        val participantsWithoutMe = participants.subList(1, participants.size)
        val ids: MutableList<String> = ArrayList()
        for (item in participantsWithoutMe) {
            ids.add(item)
        }
        try {
            val (first, second) = mSession!!.identityServerManager.getInvite3pid(mSession!!.homeServerConfig.credentials.userId, ids)
            params.invite3pids = first
            params.invitedUserIds = second
            mSession!!.createRoom(params, object : ApiCallback<String?> {
                override fun onSuccess(roomId: String?) {
                    if (roomId != null) {
                        startChatActivity(mContext, roomId, messageData)
                    } else {
                        onError(mContext.getString(R.string.network_error))
                    }
                }

                private fun onError(message: String?) {
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show()
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
        } catch (e: IdentityServerNotConfiguredException) {
            Toast.makeText(mContext, R.string.identity_server_not_defined, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val EXTRA_MESSAGE_DATA = "EXTRA_MESSAGE_DATA"
        const val BROADCAST_ACTION_AMP_SEND_MESSAGE = "im.vector.receiver.AMP_SEND_MESSAGE"
        const val SEND_MESSAGE_ACTION = "chat.amp.messenger.SEND_MESSAGE"
    }
}
