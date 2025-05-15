package com.customtabs.core.session

import androidx.browser.customtabs.CustomTabsSession

fun interface CustomTabsSessionProvider {
  fun getSession(packageName: String?): CustomTabsSession?
}
