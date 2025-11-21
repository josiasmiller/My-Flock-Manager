package com.weyr_associates.animaltrakkerfarmmobile.app.animal

import android.content.Context
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Sex
import com.weyr_associates.animaltrakkerfarmmobile.model.SexStandard

object SexPresentation {

    fun sexNameFor(context: Context, sexStandard: SexStandard): String {
        return context.getString(
            when (sexStandard) {
                SexStandard.UNKNOWN -> R.string.text_sex_standard_unknown
                SexStandard.MALE -> R.string.text_sex_standard_male
                SexStandard.FEMALE -> R.string.text_sex_standard_female
                SexStandard.CASTRATE -> R.string.text_sex_standard_castrate
            }
        )
    }

    fun sexNameFor(context: Context, sexId: EntityId): String {
        return context.getString(
            when (sexId) {
                Sex.ID_SHEEP_RAM -> R.string.text_sex_ram
                Sex.ID_SHEEP_EWE -> R.string.text_sex_ewe
                Sex.ID_SHEEP_WETHER -> R.string.text_sex_wether
                Sex.ID_SHEEP_UNKNOWN -> R.string.text_sex_unknown
                Sex.ID_GOAT_BUCK -> R.string.text_sex_buck
                Sex.ID_GOAT_DOE -> R.string.text_sex_doe
                Sex.ID_GOAT_WETHER -> R.string.text_sex_wether
                Sex.ID_GOAT_UNKNOWN -> R.string.text_sex_unknown
                Sex.ID_CATTLE_BULL -> R.string.text_sex_bull
                Sex.ID_CATTLE_COW -> R.string.text_sex_cow
                Sex.ID_CATTLE_STEER -> R.string.text_sex_steer
                Sex.ID_CATTLE_UNKNOWN -> R.string.text_sex_unknown
                Sex.ID_HORSE_STALLION -> R.string.text_sex_stallion
                Sex.ID_HORSE_MARE -> R.string.text_sex_mare
                Sex.ID_HORSE_GELDING -> R.string.text_sex_gelding
                Sex.ID_HORSE_UNKNOWN -> R.string.text_sex_unknown
                Sex.ID_DONKEY_JACK -> R.string.text_sex_jack
                Sex.ID_DONKEY_JENNY -> R.string.text_sex_jenny
                Sex.ID_DONKEY_GELDING -> R.string.text_sex_gelding
                Sex.ID_DONKEY_UNKNOWN -> R.string.text_sex_unknown
                Sex.ID_PIG_BOAR -> R.string.text_sex_boar
                Sex.ID_PIG_SOW -> R.string.text_sex_sow
                Sex.ID_PIG_BARROW -> R.string.text_sex_barrow
                Sex.ID_PIG_UNKNOWN -> R.string.text_sex_unknown
                else -> R.string.text_sex_unknown
            }
        )
    }
}
