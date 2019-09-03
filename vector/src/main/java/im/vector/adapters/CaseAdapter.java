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

package im.vector.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.matrix.androidsdk.core.Log;
import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.rest.model.publicroom.PublicRoom;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import im.vector.R;
import im.vector.util.RoomUtils;
import im.vector.util.VectorUtils;

public class CaseAdapter extends AbsAdapter {
    private static final String LOG_TAG = CaseAdapter.class.getSimpleName();

    private final AdapterSection<Room> mRoomsSection;

    private final OnSelectItemListener mListener;

    /*
     * *********************************************************************************************
     * Constructor
     * *********************************************************************************************
     */

    public CaseAdapter(final Context context,
                       final OnSelectItemListener listener,
                       final RoomInvitationListener invitationListener,
                       final MoreRoomActionListener moreActionListener) {
        super(context, invitationListener, moreActionListener);

        mListener = listener;

        mRoomsSection = new AdapterSection<>(context, context.getString(R.string.cases_header), -1,
                R.layout.adapter_item_room_view, TYPE_HEADER_DEFAULT, TYPE_ROOM, new ArrayList<>(), RoomUtils.getRoomsDateComparator(mSession, false));
        mRoomsSection.setEmptyViewPlaceholder(context.getString(R.string.no_room_placeholder), context.getString(R.string.no_result_placeholder));

        addSection(mRoomsSection);
    }

    /*
     * *********************************************************************************************
     * Abstract methods implementation
     * *********************************************************************************************
     */

    @Override
    protected RecyclerView.ViewHolder createSubViewHolder(ViewGroup viewGroup, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        View itemView;

        switch (viewType) {
            case TYPE_ROOM:
                itemView = inflater.inflate(R.layout.adapter_item_case_view, viewGroup, false);
                return new CaseViewHolder(itemView);
        }
        return null;
    }

    @Override
    protected void populateViewHolder(int viewType, RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewType) {
            case TYPE_ROOM:
                final CaseViewHolder caseViewHolder = (CaseViewHolder) viewHolder;
                final Room room = (Room) getItemForPosition(position);
                caseViewHolder.populateViews(mContext, mSession, room, false, mMoreRoomActionListener);
                caseViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onSelectItem(room, -1);
                    }
                });
                break;
        }
    }

    @Override
    protected int applyFilter(String pattern) {
        int nbResults = 0;

        nbResults += filterRoomSection(mRoomsSection, pattern);

        // The public rooms search is done by a server request.
        // The result is also paginated so it make no sense to be done in the adapter

        return nbResults;
    }

    /*
     * *********************************************************************************************
     * Public methods
     * *********************************************************************************************
     */

    public void setRooms(final List<Room> rooms) {
        mRoomsSection.setItems(rooms, mCurrentFilterPattern);
        if (!TextUtils.isEmpty(mCurrentFilterPattern)) {
            filterRoomSection(mRoomsSection, String.valueOf(mCurrentFilterPattern));
        }
        updateSections();
    }

    /*
     * *********************************************************************************************
     * Inner classes
     * *********************************************************************************************
     */

    public interface OnSelectItemListener {
        void onSelectItem(Room item, int position);
    }
}
