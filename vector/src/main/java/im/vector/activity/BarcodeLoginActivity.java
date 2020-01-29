/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package im.vector.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.jetbrains.annotations.Contract;
import org.matrix.androidsdk.HomeServerConnectionConfig;
import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.core.callback.SimpleApiCallback;
import org.matrix.androidsdk.core.model.MatrixError;
import org.matrix.androidsdk.rest.model.login.PasswordLoginParams;

import java.io.IOException;

import butterknife.OnClick;
import im.vector.LoginHandler;
import im.vector.Matrix;
import im.vector.R;
import im.vector.push.fcm.FcmHelper;
import im.vector.util.PermissionsToolsKt;
import im.vector.view.barcode.BarcodeGraphic;
import im.vector.view.barcode.BarcodeGraphicTracker;
import im.vector.view.barcode.BarcodeTrackerFactory;
import im.vector.view.camera.CameraSource;
import im.vector.view.camera.CameraSourcePreview;
import im.vector.view.camera.GraphicOverlay;

/**
 * This activity allows logins with a barcode. It detects barcodes and displays the value with the
 * rear facing camera. During detection overlay graphics are drawn to indicate the position,
 * size, and ID of each barcode.
 */
public final class BarcodeLoginActivity extends MXCActionBarActivity implements BarcodeGraphicTracker.BarcodeUpdateListener {
    private final String LOG_TAG = BarcodeLoginActivity.class.getSimpleName();

    // intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    // login handler
    private final LoginHandler mLoginHandler = new LoginHandler();

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay<BarcodeGraphic> mGraphicOverlay;

    public static final String OBFUSCATION_KEY = "wo9k5tep252qxsa5yde7366kugy6c01w7oeeya9hrmpf0t7ii7";

    @Override
    public int getLayoutRes() { return R.layout.activity_barcode_login; }

    /**
     * Initializes the UI and creates the detector pipeline.
     */
    @Override
    public void initUiAndData() {
        // warn that the application has started.
        CommonActivityUtils.onApplicationStarted(this);

        FcmHelper.ensureFcmTokenIsRetrieved(this);

        if (hasCredentials()) {
            Log.d(LOG_TAG, "## onCreate(): goToSplash because the credentials are already provided.");
            goToSplash();
            finish();
        }

        mPreview = findViewById(R.id.qr_scanner);
        mGraphicOverlay = findViewById(R.id.graphicOverlay);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        if (PermissionsToolsKt.checkPermissions(PermissionsToolsKt.PERMISSIONS_FOR_QR_CODE_LOGIN,
                this, PermissionsToolsKt.PERMISSION_REQUEST_CODE_QR_CODE_LOGIN)) {
            createCameraSource();
        }

        final Button button = findViewById(R.id.alternative_login);
        button.setOnClickListener(v -> onSwitchToLoginActivityClick());

        Uri loginUri = getIntent().getData();
        if (loginUri != null && loginUri.getFragment() != null) {
            login(loginUri, decode(loginUri.getFragment()));
        }
    }

    /**
     * The user clicks on the alternative login button
     */
    @OnClick(R.id.alternative_login)
    void onSwitchToLoginActivityClick() {
        Log.d("Button", "onSwitchToLoginActivityClick");

        // go to login page
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     * <p>
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource() {
        Context context = getApplicationContext();

        // A barcode detector is created to track barcodes.  An associated multi-processor instance
        // is set to receive the barcode detection results, track the barcodes, and maintain
        // graphics for each barcode on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each barcode.
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context).build();
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(mGraphicOverlay, this);
        barcodeDetector.setProcessor(new MultiProcessor.Builder<>(barcodeFactory).build());

        if (!barcodeDetector.isOperational()) {
            // Note: The first time that an app using the barcode or face API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any barcodes
            // and/or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(LOG_TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(LOG_TAG, getString(R.string.low_storage_error));
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        CameraSource.Builder builder = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1024, 1024)
                .setRequestedFps(15.0f)
                .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        mCameraSource = builder.build();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PermissionsToolsKt.PERMISSION_REQUEST_CODE_QR_CODE_LOGIN) {
            if (PermissionsToolsKt.checkPermissions(PermissionsToolsKt.PERMISSIONS_FOR_QR_CODE_LOGIN,
                    this, PermissionsToolsKt.PERMISSION_REQUEST_CODE_QR_CODE_LOGIN)) {
                createCameraSource();
            } else {
                Log.e(LOG_TAG, "Permission not granted: results len = " + grantResults.length +
                        " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
                Toast.makeText(this, R.string.permissions_action_not_performed_missing_permissions, Toast.LENGTH_LONG).show();
                onSwitchToLoginActivityClick();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    @Override
    public void onBarcodeDetected(Barcode barcode) {
        Log.d(LOG_TAG, "onBarcodeDetected: raw=" + barcode.rawValue);
        Uri barcodeUri = Uri.parse(barcode.rawValue);
        if (barcodeUri != null && barcodeUri.getFragment() != null) {
            Uri loginUri = Uri.parse(barcode.rawValue.substring(0, barcode.rawValue.indexOf("#")));
            login(loginUri, decode(barcodeUri.getFragment()));
        } else {
            runOnUiThread(() -> Toast.makeText(this, getString(R.string.login_error_invalid_qr_code), Toast.LENGTH_SHORT).show());
        }
    }

    private PasswordLoginParams decode(String uriFragment) {
        String decodedFragment = new String(xorWithKey(Base64.decode(uriFragment, Base64.DEFAULT),
                BarcodeLoginActivity.OBFUSCATION_KEY.getBytes()));
        String[] credentials = decodedFragment.split("&");
        String username = credentials[0].split("=")[1];
        String password = credentials[1].split("=")[1];
        PasswordLoginParams params = new PasswordLoginParams();
        params.setUserIdentifier(username, password);
        return params;
    }

    @Contract(pure = true)
    private byte[] xorWithKey(byte[] a, byte[] key) {
        byte[] out = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = (byte) (a[i] ^ key[i % key.length]);
        }
        return out;
    }

    private void login(Uri loginUri, PasswordLoginParams auth) {
        try {
            if (loginUri.getHost() == null) {
                return;
            }
            HomeServerConnectionConfig hsConfig = new HomeServerConnectionConfig.Builder()
                    .withHomeServerUri(loginUri)
                    .withIdentityServerUri(loginUri)
                    .build();
            mLoginHandler.login(this, hsConfig, auth.user, "", "", auth.password, new SimpleApiCallback<Void>(this) {
                @Override
                public void onSuccess(Void avoid) {
                    goToSplash();
                    finish();
                }

                @Override
                public void onNetworkError(Exception e) {
                    Log.e(LOG_TAG, "onLoginClick : Network Error: " + e.getMessage(), e);
                    Toast.makeText(getApplicationContext(), getString(R.string.login_error_network_error), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onUnexpectedError(Exception e) {
                    Log.e(LOG_TAG, "onLoginClick : onUnexpectedError" + e.getMessage(), e);
                    String msg = getString(R.string.login_error_unable_login) + " : " + e.getMessage();
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onMatrixError(MatrixError e) {
                    Log.e(LOG_TAG, "onLoginClick : onMatrixError " + e.getLocalizedMessage());
                    Toast.makeText(BarcodeLoginActivity.this, getString(R.string.login_error_invalid_qr_code), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (RuntimeException e) {
            Log.e(LOG_TAG, "getHsConfig fails " + e.getLocalizedMessage(), e);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.login_error_invalid_qr_code), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * @return true if some credentials have been saved.
     */
    private boolean hasCredentials() {
        try {
            MXSession session = Matrix.getInstance(this).getDefaultSession();
            return null != session && session.isAlive();

        } catch (Exception e) {
            Log.e(LOG_TAG, "## Exception: " + e.getMessage(), e);
        }

        Log.e(LOG_TAG, "## hasCredentials() : invalid credentials");

        runOnUiThread(() -> {
            try {
                // getDefaultSession could trigger an exception if the login data are corrupted
                CommonActivityUtils.logout(BarcodeLoginActivity.this);
            } catch (Exception e) {
                Log.w(LOG_TAG, "## Exception: " + e.getMessage(), e);
            }
        });

        return false;
    }

    /**
     * Some sessions have been registered, skip the login process.
     */
    private void goToSplash() {
        Log.d(LOG_TAG, "## gotoSplash(): Go to splash.");

        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
    }
}
