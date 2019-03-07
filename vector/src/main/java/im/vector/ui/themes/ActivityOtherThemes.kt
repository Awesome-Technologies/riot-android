/*
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

package im.vector.ui.themes

import android.support.annotation.StyleRes
import im.vector.R

/**
 * Class to manage Activity other possible themes.
 * Note that style for light theme is default and is declared in the Android Manifest
 */
sealed class ActivityOtherThemes(@StyleRes val dark: Int) {

    object Default : ActivityOtherThemes(
            R.style.AppTheme_Dark
    )

    object NoActionBarFullscreen : ActivityOtherThemes(
            R.style.AppTheme_NoActionBar_FullScreen_Dark
    )

    object Home : ActivityOtherThemes(
            R.style.HomeActivityTheme_Dark
    )

    object Group : ActivityOtherThemes(
            R.style.GroupAppTheme_Dark
    )

    object Picker : ActivityOtherThemes(
            R.style.CountryPickerTheme_Dark
    )

    object Lock : ActivityOtherThemes(
            R.style.Theme_Vector_Lock_Dark
    )

    object Search : ActivityOtherThemes(
            R.style.SearchesAppTheme_Dark
    )

    object Call : ActivityOtherThemes(
            R.style.CallActivityTheme_Dark
    )
}