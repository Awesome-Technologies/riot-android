/*
 * Copyright 2017 Vector Creations Ltd
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
import android.text.TextUtils;

import org.matrix.androidsdk.rest.model.publicroom.PublicRoom;

import java.util.Comparator;
import java.util.List;

class PublicRoomsAdapterSection extends AdapterSection<PublicRoom> {

    // tell if the
    private boolean mHasMoreResults;

    public PublicRoomsAdapterSection(Context context, String title, int headerSubViewResId, int contentResId, int headerViewType,
                                     int contentViewType, List<PublicRoom> items, Comparator<PublicRoom> comparator) {
        super(context, title, headerSubViewResId, contentResId, headerViewType, contentViewType, items, comparator);
    }

    @Override
    protected void updateTitle() {
        String newTitle;
        if (TextUtils.isEmpty(mCurrentFilterPattern)) {
            newTitle = mTitle;
        } else if (getNbItems() > 0) {
            if (mHasMoreResults) {
                newTitle = mTitle.concat("   " + getNbItems());
            } else {
                newTitle = mTitle.concat("   >" + getNbItems());
            }
        } else {
            newTitle = mTitle;
        }

        formatTitle(newTitle);
    }

    /**
     * Tells there is no more value to retrieve
     */
    public void setHasMoreResults(boolean noMore) {
        mHasMoreResults = noMore;
    }
}
