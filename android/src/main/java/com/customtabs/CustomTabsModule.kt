package com.customtabs

import android.app.Activity
import android.app.ActivityManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import com.customtabs.core.CustomTabsIntentFactory
import com.customtabs.core.ExternalBrowserLauncher
import com.customtabs.core.NativeAppLauncher
import com.customtabs.core.PartialCustomTabsLauncher
import com.customtabs.core.session.CustomTabsSessionManager
import androidx.core.net.toUri
import androidx.core.content.getSystemService
import androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableMapKeySetIterator
import com.facebook.react.bridge.ReadableType
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import android.util.Log

private fun sendLogToJs(reactContext: ReactApplicationContext, message: String) {
  reactContext
    .getJSModule(RCTDeviceEventEmitter::class.java)
    .emit("CustomTabsLog", message)
}

fun ReadableMap.toMap(reactContext: ReactApplicationContext): Map<String, Any?> {
  val iterator: ReadableMapKeySetIterator = this.keySetIterator()
  val map = mutableMapOf<String, Any?>()

  while (iterator.hasNextKey()) {
    val key = iterator.nextKey()
    when (getType(key)) {
      ReadableType.String -> map[key] = getString(key)
      ReadableType.Number -> {
        val value = getDouble(key)
        map[key] = if (value % 1 == 0.0) value.toInt() else value
      }
      ReadableType.Boolean -> map[key] = getBoolean(key)
      ReadableType.Map -> map[key] = getMap(key)?.toMap(reactContext)
      ReadableType.Array -> map[key] = getArray(key) // Handle as needed
      ReadableType.Null -> map[key] = null
      else -> map[key] = null
    }
  }
  sendLogToJs(reactContext, map.toString())
  return map
}

class CustomTabsModule(val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

  private val customTabsIntentFactory: CustomTabsIntentFactory = CustomTabsIntentFactory();
  private val customTabsSessionManager: CustomTabsSessionManager = CustomTabsSessionManager();
  private val nativeAppLauncher: NativeAppLauncher = NativeAppLauncher();
  private val externalBrowserLauncher: ExternalBrowserLauncher = ExternalBrowserLauncher();
  private val partialCustomTabsLauncher: PartialCustomTabsLauncher = PartialCustomTabsLauncher();


  override fun getName(): String {
    return NAME
  }
//  @ReactMethod
//  fun launch(urlString: String, prefersDeepLink: Boolean, options: ReadableMap?, promise: Promise) {
//    val activity = reactContext.currentActivity
//      ?: return promise.reject(
//        CODE_LAUNCH_ERROR,
//        "Launching a Custom Tab requires a foreground activity"
//      )
//
//    val uri = urlString.toUri()
//    if (prefersDeepLink && nativeAppLauncher.launch(activity, uri)) {
//      return
//    }
//
//    try {
//      @Suppress("UNCHECKED_CAST")
//      val optionsMap = options?.toMap(reactContext)?.filterValues { it != null } as? Map<String, Any> ?: null
//      val customTabsOptions = customTabsIntentFactory.createIntentOptions(optionsMap)
//      if (externalBrowserLauncher.launch(activity, uri, customTabsOptions)) {
//        return promise.resolve(true)
//      }
//
//      val customTabsIntent = customTabsIntentFactory.createIntent(
//        activity,
//        requireNotNull(customTabsOptions),
//        customTabsSessionManager
//      )
//      if (partialCustomTabsLauncher.launch(activity, uri, customTabsIntent)) {
//        return promise.resolve(true)
//      }
//      customTabsIntent.launchUrl(activity, uri)
//    } catch (e: ActivityNotFoundException) {
//      promise.reject(CODE_LAUNCH_ERROR, e.message)
//    }
//  }

  @ReactMethod
  fun launch(
    urlString: String,
    prefersDeepLink: Boolean,
    options: ReadableMap?,
    promise: Promise
  ) {
    val activity = reactContext.currentActivity
    if (activity == null) {
      promise.reject(
        CODE_LAUNCH_ERROR,
        "Launching a Custom Tab requires a foreground activity"
      )
      return
    }

    val uri = urlString.toUri()

    // Try native app deep link if preferred
    if (prefersDeepLink && nativeAppLauncher.launch(activity, uri)) {
      promise.resolve(true)
      return
    }

    try {
      // Safely parse and filter options
      @Suppress("UNCHECKED_CAST")
      val optionsMap = options
        ?.toMap(reactContext)
        ?.filterValues { it != null } as? Map<String, Any>

      val customTabsOptions = customTabsIntentFactory.createIntentOptions(optionsMap)

      // Try external browser launch
      if (externalBrowserLauncher.launch(activity, uri, customTabsOptions)) {
        promise.resolve(true)
        return
      }

      // Fallback to Custom Tab launch
      val customTabsIntent = customTabsIntentFactory.createIntent(
        activity,
        requireNotNull(customTabsOptions),
        customTabsSessionManager
      )

      if (partialCustomTabsLauncher.launch(activity, uri, customTabsIntent)) {
        promise.resolve(true)
        return
      }

      // Final fallback: direct custom tab launch
      customTabsIntent.launchUrl(activity, uri)
      promise.resolve(true)
    } catch (e: ActivityNotFoundException) {
      promise.reject(CODE_LAUNCH_ERROR, e.message)
    }
  }


  @ReactMethod
  fun closeAllIfPossible(promise: Promise) {
//    val activity = reactContext.currentActivity
//      ?: return;
    closeActivityIfCustomTabsPresent(reactContext.currentActivity, promise)
//    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//      return
//    }
//
//    val am = activity.getSystemService<ActivityManager>()
//    val selfActivityName = ComponentName(activity, activity.javaClass)
//    for (appTask in requireNotNull(am).appTasks) {
//      val taskInfo = appTask.taskInfo
//      if (selfActivityName != taskInfo.baseActivity || taskInfo.topActivity == null) {
//        continue
//      }
//
//      val serviceIntent = Intent(ACTION_CUSTOM_TABS_CONNECTION)
//        .setPackage(taskInfo.topActivity?.packageName)
//      if (resolveService(activity.packageManager, serviceIntent) != null) {
//        try {
//          val intent = Intent(activity, activity.javaClass)
//            .setFlags(FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_SINGLE_TOP)
//          activity.startActivity(intent)
//        } catch (ignored: ActivityNotFoundException) {
//        }
//        break
//      }
//    }
  }

  private fun closeActivityIfCustomTabsPresent(activity: Activity?, promise: Promise) {
    if (activity == null) {
      promise.reject("NO_ACTIVITY", "Current activity is null")
      return
    }

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      promise.reject("UNSUPPORTED_VERSION", "Requires Android Marshmallow (API 23) or higher")
      return
    }

    val am = activity.getSystemService(ActivityManager::class.java) ?: run {
      promise.reject("NO_ACTIVITY_MANAGER", "Unable to access ActivityManager")
      return
    }

    val selfComponent = ComponentName(activity, activity.javaClass)

    for (task in am.appTasks) {
      val info = task.taskInfo ?: continue

      val isCurrentActivity = info.baseActivity == selfComponent && info.topActivity != null
      if (!isCurrentActivity) continue

      val serviceIntent = Intent(ACTION_CUSTOM_TABS_CONNECTION)
        .setPackage(info.topActivity?.packageName)

      if (resolveService(activity.packageManager, serviceIntent) != null) {
        try {
          val restartIntent = Intent(activity, activity.javaClass).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
          }
          activity.startActivity(restartIntent)
          promise.resolve(true)
          return
        } catch (e: ActivityNotFoundException) {
          promise.reject("ACTIVITY_NOT_FOUND", "Could not restart activity", e)
          return
        }
      }
    }

    // No matching task/service found
    promise.reject("NO_MATCHING_TASK", "No Custom Tabs-related task found to close")
  }


  @ReactMethod
  fun warmup(options: ReadableMap?, promise: Promise) {
    val activity = reactContext.currentActivity
      ?: return promise.resolve(null)

    val optionsMap = options?.toMap(reactContext)?.filterValues { it != null } as? Map<String, Any> ?: null
    val sessionOptions = customTabsSessionManager.createSessionOptions(optionsMap)
    val sessionController = customTabsSessionManager.createSessionController(activity, sessionOptions) ?: return promise.resolve(null)

   val response =  if (sessionController.bindCustomTabsService(activity)) {
      sessionController.packageName
    } else {
      null
    }
    promise.resolve(response)
  }

  @ReactMethod
  fun mayLaunch(urls: ReadableArray, sessionPackageName: String) {
    val controller = customTabsSessionManager.getSessionController(sessionPackageName) ?: return
    val urlList = mutableListOf<String>()
    for (i in 0 until urls.size()) {
      urlList.add(urls.getString(i) ?: continue)
    }
    controller.mayLaunchUrls(urlList)

  }

  @ReactMethod
  fun invalidate(sessionPackageName: String) {
    customTabsSessionManager.invalidateSession(sessionPackageName)
  }

  /**
   * @noinspection SameParameterValue
   */
  @Suppress("deprecation")
  private fun resolveService(
    pm: PackageManager,
    intent: Intent,
    flags: Int = 0
  ): ResolveInfo? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      pm.resolveService(
        intent,
        PackageManager.ResolveInfoFlags.of(flags.toLong())
      )
    } else {
      pm.resolveService(intent, flags)
    }
  }

  companion object {
    const val NAME = "CustomTabs"
    const val CODE_LAUNCH_ERROR = "LAUNCH_ERROR"
  }
}
