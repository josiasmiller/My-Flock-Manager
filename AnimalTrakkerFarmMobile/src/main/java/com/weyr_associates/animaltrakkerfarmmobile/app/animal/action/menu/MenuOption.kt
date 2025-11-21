package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.menu

import com.weyr_associates.animaltrakkerfarmmobile.R

enum class MenuOption(
    val titleResId: Int,
    val iconResId: Int
) {
    EDIT(R.string.text_edit, R.drawable.ic_edit),
    CLEAR(R.string.text_clear, R.drawable.ic_remove),
    DELETE(R.string.text_delete, R.drawable.ic_delete)
}
