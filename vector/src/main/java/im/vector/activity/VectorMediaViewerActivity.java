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

import android.content.Intent;
import android.view.View;

import androidx.viewpager.widget.ViewPager;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.core.Log;

import java.util.List;

import im.vector.Matrix;
import im.vector.R;
import im.vector.adapters.VectorMediaViewerAdapter;
import im.vector.util.SlidableMediaInfo;

/**
 * Display a medias list.
 */
public class VectorMediaViewerActivity extends MXCActionBarActivity {
    private static final String LOG_TAG = VectorMediaViewerActivity.class.getSimpleName();

    public static final String KEY_INFO_LIST = "ImageSliderActivity.KEY_INFO_LIST";
    public static final String KEY_INFO_LIST_INDEX = "ImageSliderActivity.KEY_INFO_LIST_INDEX";

    public static final String KEY_THUMBNAIL_WIDTH = "ImageSliderActivity.KEY_THUMBNAIL_WIDTH";
    public static final String KEY_THUMBNAIL_HEIGHT = "ImageSliderActivity.KEY_THUMBNAIL_HEIGHT";

    public static final String EXTRA_MATRIX_ID = "ImageSliderActivity.EXTRA_MATRIX_ID";

    // session
    private MXSession mSession;

    // the pager
    private ViewPager mViewPager;

    // the pager adapter
    private VectorMediaViewerAdapter mAdapter;

    // the medias list
    private List<SlidableMediaInfo> mMediasList;

    // Pending data during permission request
    private int mPendingPosition;
    private int mPendingAction;

    // the slide effect
    public class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }

    @Override
    public int getLayoutRes() {
        return R.layout.activity_vector_media_viewer;
    }

    @Override
    public void initUiAndData() {
        configureToolbar();

        if (CommonActivityUtils.shouldRestartApp(this)) {
            Log.d(LOG_TAG, "onCreate : restart the application");
            CommonActivityUtils.restartApp(this);
            return;
        }

        if (CommonActivityUtils.isGoingToSplash(this)) {
            Log.d(LOG_TAG, "onCreate : Going to splash screen");
            return;
        }

        String matrixId = null;
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_MATRIX_ID)) {
            matrixId = intent.getStringExtra(EXTRA_MATRIX_ID);
        }

        mSession = Matrix.getInstance(getApplicationContext()).getSession(matrixId);

        if ((null == mSession) || !mSession.isAlive()) {
            finish();
            Log.d(LOG_TAG, "onCreate : invalid session");
            return;
        }

        mMediasList = (List<SlidableMediaInfo>) intent.getSerializableExtra(KEY_INFO_LIST);

        if ((null == mMediasList) || (0 == mMediasList.size())) {
            finish();
            return;
        }

        mViewPager = findViewById(R.id.view_pager);

        int position = Math.min(intent.getIntExtra(KEY_INFO_LIST_INDEX, 0), mMediasList.size() - 1);
        int maxImageWidth = intent.getIntExtra(KEY_THUMBNAIL_WIDTH, 0);
        int maxImageHeight = intent.getIntExtra(VectorMediaViewerActivity.KEY_THUMBNAIL_HEIGHT, 0);

        mAdapter = new VectorMediaViewerAdapter(this, mSession, mSession.getMediaCache(), mMediasList, maxImageWidth, maxImageHeight);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setPageTransformer(true, new DepthPageTransformer());
        mAdapter.autoPlayItemAt(position);
        mViewPager.setCurrentItem(position);

        if (null != getSupportActionBar()) {
            getSupportActionBar().setTitle(mMediasList.get(position).mFileName);
        }
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (null != getSupportActionBar()) {
                    getSupportActionBar().setTitle(mMediasList.get(position).mFileName);
                }

                // disable shared for encrypted files as they are saved in a tmp folder
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        // stop any playing video
        mAdapter.stopPlayingVideo();
    }
}
