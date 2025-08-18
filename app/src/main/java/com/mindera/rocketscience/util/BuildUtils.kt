package com.mindera.rocketscience.util

import android.content.Context
import android.content.pm.ApplicationInfo

/**
 * Utility object for build-related checks and configurations
 */
object BuildUtils {
    
    /**
     * Checks if the application is running in debug mode
     * @param context Application context
     * @return true if running in debug mode, false otherwise
     */
    fun isDebugBuild(context: Context): Boolean =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    
    /**
     * Checks if the application is running in release mode
     * @param context Application context
     * @return true if running in release mode, false otherwise
     */
    fun isReleaseBuild(context: Context): Boolean = !isDebugBuild(context)
}