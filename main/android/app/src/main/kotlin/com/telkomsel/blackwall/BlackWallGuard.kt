package com.telkomsel.blackwall

import android.content.Context

/**
 * JNI Bridge to BlackWall C++ RASP Engine (libblackwall.so)
 */
object BlackWallGuard {
    @JvmStatic
    external fun initNativeGuard(context: Context?, expectedHash: String?): Boolean

    @JvmStatic
    external fun getSecurityThreatBitmask(): Int
}
