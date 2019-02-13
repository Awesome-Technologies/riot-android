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

package im.vector.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.matrix.androidsdk.MXPatterns;
import org.matrix.androidsdk.listeners.MXEventListener;
import org.matrix.androidsdk.rest.callback.SimpleApiCallback;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.RoomMember;
import org.matrix.androidsdk.rest.model.User;
import org.matrix.androidsdk.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.OnClick;
import im.vector.Matrix;
import im.vector.R;
import im.vector.adapters.ParticipantAdapterItem;
import im.vector.adapters.VectorParticipantsAdapter;
import im.vector.util.VectorUtils;
import im.vector.view.VectorAutoCompleteTextView;

/**
 * This class provides a way to search other user to invite them in a dedicated room
 */
public class VectorRoomInviteMembersActivity extends VectorBaseSearchActivity {
    private static final String LOG_TAG = VectorRoomInviteMembersActivity.class.getSimpleName();

    // room identifier
    public static final String EXTRA_ROOM_ID = "VectorInviteMembersActivity.EXTRA_ROOM_ID";

    // participants to hide in the list
    public static final String EXTRA_HIDDEN_PARTICIPANT_ITEMS = "VectorInviteMembersActivity.EXTRA_HIDDEN_PARTICIPANT_ITEMS";

    // boolean : true displays a dialog to confirm the member selection
    public static final String EXTRA_ADD_CONFIRMATION_DIALOG = "VectorInviteMembersActivity.EXTRA_ADD_CONFIRMATION_DIALOG";

    // the selected user ids list
    public static final String EXTRA_OUT_SELECTED_USER_IDS = "VectorInviteMembersActivity.EXTRA_OUT_SELECTED_USER_IDS";

    // the selected participants list
    public static final String EXTRA_OUT_SELECTED_PARTICIPANT_ITEMS = "VectorInviteMembersActivity.EXTRA_OUT_SELECTED_PARTICIPANT_ITEMS";

    // account data
    private String mMatrixId;

    // main UI items
    @BindView(R.id.room_details_members_list)
    ExpandableListView mListView;

    // main UI items
    @BindView(R.id.search_invite_hint)
    TextView mInviteHint;

    // participants list
    private List<ParticipantAdapterItem> mHiddenParticipantItems = new ArrayList<>();

    // adapter
    private VectorParticipantsAdapter mAdapter;

    // tell if a confirmation dialog must be displayed to validate the user ids list
    private boolean mAddConfirmationDialog;

    // refresh the presence asap
    private final MXEventListener mEventsListener = new MXEventListener() {
        @Override
        public void onPresenceUpdate(final Event event, final User user) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Map<Integer, List<Integer>> visibleChildViews = VectorUtils.getVisibleChildViews(mListView, mAdapter);

                    for (Integer groupPosition : visibleChildViews.keySet()) {
                        List<Integer> childPositions = visibleChildViews.get(groupPosition);

                        for (Integer childPosition : childPositions) {
                            Object item = mAdapter.getChild(groupPosition, childPosition);

                            if (item instanceof ParticipantAdapterItem) {
                                ParticipantAdapterItem participantAdapterItem = (ParticipantAdapterItem) item;

                                if (TextUtils.equals(user.user_id, participantAdapterItem.mUserId)) {
                                    mAdapter.notifyDataSetChanged();
                                    break;
                                }
                            }
                        }
                    }
                }
            });
        }
    };

    @Override
    public int getLayoutRes() {
        return R.layout.activity_vector_invite_members;
    }

    @Override
    public void initUiAndData() {
        super.initUiAndData();

        if (CommonActivityUtils.shouldRestartApp(this)) {
            Log.e(LOG_TAG, "Restart the application.");
            CommonActivityUtils.restartApp(this);
            return;
        }

        if (CommonActivityUtils.isGoingToSplash(this)) {
            Log.d(LOG_TAG, "onCreate : Going to splash screen");
            return;
        }

        Intent intent = getIntent();

        if (intent.hasExtra(EXTRA_MATRIX_ID)) {
            mMatrixId = intent.getStringExtra(EXTRA_MATRIX_ID);
        }

        // get current session
        mSession = Matrix.getInstance(getApplicationContext()).getSession(mMatrixId);

        if ((null == mSession) || !mSession.isAlive()) {
            finish();
            return;
        }

        if (intent.hasExtra(EXTRA_HIDDEN_PARTICIPANT_ITEMS)) {
            mHiddenParticipantItems = (List<ParticipantAdapterItem>) intent.getSerializableExtra(EXTRA_HIDDEN_PARTICIPANT_ITEMS);
        }

        String roomId = intent.getStringExtra(EXTRA_ROOM_ID);

        if (null != roomId) {
            mRoom = mSession.getDataHandler().getStore().getRoom(roomId);
        }

        // tell if a confirmation dialog must be displayed.
        mAddConfirmationDialog = intent.getBooleanExtra(EXTRA_ADD_CONFIRMATION_DIALOG, false);

        // the user defines a
        if (null != mPatternToSearchEditText) {
            mPatternToSearchEditText.setHint(R.string.room_participants_invite_search_another_user);

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mPatternToSearchEditText, InputMethodManager.SHOW_IMPLICIT);
        }

        setWaitingView(findViewById(R.id.search_in_progress_view));

        // the chevron is managed in the header view
        mListView.setGroupIndicator(null);

        mAdapter = new VectorParticipantsAdapter(this,
                R.layout.adapter_item_vector_add_participants,
                R.layout.adapter_item_vector_people_header,
                mSession, roomId, true);
        mAdapter.setHiddenParticipantItems(mHiddenParticipantItems);
        mListView.setAdapter(mAdapter);

        mListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Object item = mAdapter.getChild(groupPosition, childPosition);

                if (item instanceof ParticipantAdapterItem && ((ParticipantAdapterItem) item).mIsValid) {
                    ParticipantAdapterItem participantAdapterItem = (ParticipantAdapterItem) item;
                    finish(new ArrayList<>(Arrays.asList(participantAdapterItem)));
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSession.getDataHandler().addListener(mEventsListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSession.getDataHandler().removeListener(mEventsListener);
    }

    /**
     * The search pattern has been updated
     */
    @Override
    protected void onPatternUpdate(boolean isTypingUpdate) {
        String pattern = mPatternToSearchEditText.getText().toString();

        mInviteHint.setVisibility(pattern.isEmpty() ? View.VISIBLE : View.GONE);

        // display a spinner while the other room members are listed
        if (!mAdapter.isKnownMembersInitialized()) {
            showWaitingView();
        }

        mAdapter.setSearchedPattern(pattern, null, new VectorParticipantsAdapter.OnParticipantsSearchListener() {
            @Override
            public void onSearchEnd(final int count) {
                if (mListView == null) {
                    // Activity is dead
                    return;
                }

                mListView.post(new Runnable() {
                    @Override
                    public void run() {
                        hideWaitingView();
                    }
                });
            }
        });
    }

    /**
     * Display a selection confirmation dialog.
     *
     * @param participantAdapterItems the selected participants
     */
    private void finish(final List<ParticipantAdapterItem> participantAdapterItems) {
        final List<String> hiddenUserIds = new ArrayList<>();

        // list the hidden user Ids
        for (ParticipantAdapterItem item : mHiddenParticipantItems) {
            hiddenUserIds.add(item.mUserId);
        }

        // if a room is defined
        if (null != mRoom) {
            // the room members must not be added again
            mRoom.getDisplayableMembersAsync(new SimpleApiCallback<List<RoomMember>>() {
                @Override
                public void onSuccess(List<RoomMember> members) {
                    for (RoomMember member : members) {
                        if (TextUtils.equals(member.membership, RoomMember.MEMBERSHIP_JOIN)
                                || TextUtils.equals(member.membership, RoomMember.MEMBERSHIP_INVITE)) {
                            hiddenUserIds.add(member.getUserId());
                        }
                    }

                    finishStep2(participantAdapterItems, hiddenUserIds);
                }
            });
        } else {
            finishStep2(participantAdapterItems, hiddenUserIds);
        }
    }

    private void finishStep2(final List<ParticipantAdapterItem> participantAdapterItems, List<String> hiddenUserIds) {
        final List<String> userIds = new ArrayList<>();
        final List<String> displayNames = new ArrayList<>();

        // build the output lists
        for (ParticipantAdapterItem item : participantAdapterItems) {
            // check if the user id can be added
            if (!hiddenUserIds.contains(item.mUserId)) {
                userIds.add(item.mUserId);
                // display name
                if (MXPatterns.isUserId(item.mUserId)) {
                    User user = mSession.getDataHandler().getStore().getUser(item.mUserId);
                    if ((null != user) && !TextUtils.isEmpty(user.displayname)) {
                        displayNames.add(user.displayname);
                    } else {
                        displayNames.add(item.mUserId);
                    }
                } else {
                    displayNames.add(item.mUserId);
                }
            }
        }

        // a confirmation dialog has been requested
        if (mAddConfirmationDialog && (displayNames.size() > 0)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_title_confirmation);

            String message = "";
            String msgPartA = "";
            String msgPartB = "";

            if (displayNames.size() == 1) {
                message = displayNames.get(0);
            } else {
                for (int i = 0; i < (displayNames.size() - 2); i++) {
                    msgPartA += getString(R.string.room_participants_invite_join_names, displayNames.get(i));
                }

                msgPartB = getString(R.string.room_participants_invite_join_names_and,
                        displayNames.get(displayNames.size() - 2),
                        displayNames.get(displayNames.size() - 1));
                message = getString(R.string.room_participants_invite_join_names_combined,
                        msgPartA, msgPartB);
            }

            builder.setMessage(getString(R.string.room_participants_invite_prompt_msg, message))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // returns the selected users
                            Intent intent = new Intent();
                            intent.putExtra(EXTRA_OUT_SELECTED_USER_IDS, (ArrayList) userIds);
                            intent.putExtra(EXTRA_OUT_SELECTED_PARTICIPANT_ITEMS, (ArrayList) participantAdapterItems);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            // returns the selected users
            Intent intent = new Intent();
            intent.putExtra(EXTRA_OUT_SELECTED_USER_IDS, (ArrayList) userIds);
            intent.putExtra(EXTRA_OUT_SELECTED_PARTICIPANT_ITEMS, (ArrayList) participantAdapterItems);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
