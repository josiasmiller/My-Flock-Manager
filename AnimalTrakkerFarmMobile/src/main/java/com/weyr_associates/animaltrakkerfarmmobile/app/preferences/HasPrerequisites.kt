package com.weyr_associates.animaltrakkerfarmmobile.app.preferences

import androidx.activity.result.ActivityResultCaller

interface HasPrerequisites {
    fun registerPrerequisiteFulfillment(caller: ActivityResultCaller)
}
