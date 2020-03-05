/*
 * Copyright 2020 Awesome Technologies Innovationslabor GmbH
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

package im.vector.activity

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import im.vector.R
import kotlinx.android.synthetic.main.activity_local_authentication.*
import org.matrix.androidsdk.core.Log

/**
 * Class to check the users authentication either by biometrics or passcode.
 */
class LocalAuthenticationActivity : VectorAppCompatActivity() {
    override fun getLayoutRes(): Int {
        return R.layout.activity_local_authentication
    }

    override fun initUiAndData() {
        super.initUiAndData()

        Log.d(LOG_TAG, "## initUiAndData : Setting up UI")
        toolbar.title = getString(R.string.local_auth_title)

        retry_button.setOnClickListener {
            authenticate()
        }
    }

    private fun showRetryButton() {
        retry_button.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        authenticate()
    }

    private fun authenticate() {
        if (isAuthenticated) {
            finish()
        }
        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int,
                                                       errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Log.d(LOG_TAG, "## authenticate : Error authenticating")
                        Log.e(LOG_TAG, errString.toString())
                        showRetryButton()
                    }

                    override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        Log.d(LOG_TAG, "## authenticate : Authentication successful!")
                        Toast.makeText(applicationContext,
                                getString(R.string.local_auth_success), Toast.LENGTH_SHORT)
                                .show()
                        isAuthenticated = true
                        biometricPrompt = null
                        finish()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Log.d(LOG_TAG, "## authenticate : Failed to authenticate")
                        showRetryButton()
                    }
                })

        val keyguardMgr: KeyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        val deviceHasFallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyguardMgr.isDeviceSecure
        } else {
            keyguardMgr.isKeyguardSecure
        }

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.local_auth_title))
                .setDescription(getString(R.string.local_auth_explanation_prompt))

        if (deviceHasFallback) {
            Log.d(LOG_TAG, "## authenticate : User has fallback (pin,password,etc.)")
            promptInfoBuilder.setDeviceCredentialAllowed(true)
        } else {
            promptInfoBuilder.setNegativeButtonText(getString(R.string.cancel))
        }

        val promptInfo = promptInfoBuilder.build()

        // Check if the user can authenticate locally at all
        if (BiometricManager.from(this).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS || deviceHasFallback) {
            Log.d(LOG_TAG, "## authenticate : Prompting user to authenticate")
            biometricPrompt?.authenticate(promptInfo) ?: run {
                showRetryButton()
            }
        } else {
            Log.d(LOG_TAG, "## authenticate : User has no authentication set up")
            explanation.text = getString(R.string.local_auth_not_setup)
            showRetryButton()
        }
    }

    companion object {
        private val LOG_TAG = LocalAuthenticationActivity::class.simpleName
        private var biometricPrompt: BiometricPrompt? = null
        @JvmStatic
        var isAuthenticated: Boolean = false
            private set

        @JvmStatic
        fun invalidate() {
            isAuthenticated = false
            biometricPrompt?.cancelAuthentication()
            biometricPrompt = null
        }
    }
}