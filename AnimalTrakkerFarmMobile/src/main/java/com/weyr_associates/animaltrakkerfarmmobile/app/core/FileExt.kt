package com.weyr_associates.animaltrakkerfarmmobile.app.core

import java.io.File

/**
 * Safe version of delete that catches
 * [SecurityException] and returns false.
 */
fun File.deleteSafely(): Boolean {
    return try { delete() }
    catch(ex: SecurityException) { false }
}
