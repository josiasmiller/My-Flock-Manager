package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import android.content.Context
import android.database.Cursor
import androidx.preference.PreferenceManager
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.DefaultSettingsTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getBoolean
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getFloat
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.model.DefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.model.ItemEntry

object DefaultSettingsTable : TableSpec<Columns> {

    const val NAME = "animaltrakker_default_settings_table"

    private val DEFAULT_SETTINGS_QUERY get() =
        "SELECT * FROM $NAME WHERE ${Columns.ID} = ?"

    @JvmStatic
    fun readAsMap(context: Context): Map<String,Int> {
        DatabaseManager.getInstance(context).createDatabaseHandler().use { databaseHandler ->
            return readAsMapFrom(context, databaseHandler)
        }
    }

    @JvmStatic
    fun readAsMapFrom(context: Context, databaseHandler: DatabaseHandler): Map<String,Int> {
        val activeDefaultSettings = ActiveDefaultSettings(
            PreferenceManager.getDefaultSharedPreferences(context)
        )
        val activeDefaultSettingsId = activeDefaultSettings.loadActiveDefaultSettingsId()
        databaseHandler.readableDatabase.rawQuery(
            DEFAULT_SETTINGS_QUERY,
            arrayOf(activeDefaultSettingsId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                return readAsMapFrom(cursor)
            } else {
                throw IllegalStateException ("No default settings records found.")
            }
        }
    }

    @JvmStatic
    fun readAsMapFrom(cursor: Cursor): Map<String,Int> {
        //TODO: This is temporary to get off of indices and move to column names.
        return buildMap {
            for (index: Int in 0 until cursor.columnCount) {
                put(cursor.getColumnName(index), cursor.getInt(index))
            }
        }
    }

    object Columns {
        val ID = Column.NotNull("id_animaltrakkerdefaultsettingsid")
        val NAME = Column.NotNull("default_settings_name")
        val OWNER_CONTACT_ID = Column.Nullable("owner_id_contactid")
        val OWNER_COMPANY_ID = Column.Nullable("owner_id_companyid")
        val OWNER_PREMISE_ID = Column.NotNull("owner_id_premiseid")
        val BREEDER_CONTACT_ID = Column.Nullable("breeder_id_contactid")
        val BREEDER_COMPANY_ID = Column.Nullable("breeder_id_companyid")
        val BREEDER_PREMISE_ID = Column.NotNull("breeder_id_premiseid")
        val VET_CONTACT_ID = Column.Nullable("vet_id_contactid")
        val VET_PREMISE_ID = Column.Nullable("vet_id_premiseid")
        val LAB_COMPANY_ID = Column.Nullable("lab_id_companyid")
        val LAB_PREMISE_ID = Column.Nullable("lab_id_premiseid")
        val REGISTRY_COMPANY_ID = Column.Nullable("id_registry_id_companyid")
        val REGISTRY_PREMISE_ID = Column.Nullable("registry_id_premiseid")
        val FLOCK_PREFIX_ID = Column.Nullable("id_flockprefixid")
        val BREED_ID = Column.NotNull("id_breedid")
        val SEX_ID = Column.NotNull("id_sexid")
        val ID_TYPE_ID_PRIMARY = Column.NotNull("id_idtypeid_primary")
        val ID_TYPE_ID_SECONDARY = Column.Nullable("id_idtypeid_secondary")
        val ID_TYPE_ID_TERTIARY = Column.Nullable("id_idtypeid_tertiary")
        val EID_TAG_MALE_COLOR_FEMALE_COLOR_SAME = Column.NotNull("id_eid_tag_male_color_female_color_same")
        val EID_TAG_COLOR_MALE = Column.Nullable("eid_tag_color_male")
        val EID_TAG_COLOR_FEMALE = Column.Nullable("eid_tag_color_female")
        val EID_TAG_LOCATION = Column.Nullable("eid_tag_location")
        val FARM_TAG_MALE_COLOR_FEMALE_COLOR_SAME = Column.NotNull("id_farm_tag_male_color_female_color_same")
        val FARM_TAG_BASED_ON_EID_TAG = Column.NotNull("farm_tag_based_on_eid_tag")
        val FARM_TAG_NUMBER_DIGITS_FROM_EID = Column.Nullable("farm_tag_number_digits_from_eid")
        val FARM_TAG_COLOR_MALE = Column.Nullable("farm_tag_color_male")
        val FARM_TAG_COLOR_FEMALE = Column.Nullable("farm_tag_color_female")
        val FARM_TAG_LOCATION = Column.Nullable("farm_tag_location")
        val FED_TAG_MALE_COLOR_FEMALE_COLOR_SAME = Column.NotNull("id_fed_tag_male_color_female_color_same")
        val FED_TAG_COLOR_MALE = Column.Nullable("fed_tag_color_male")
        val FED_TAG_COLOR_FEMALE = Column.Nullable("fed_tag_color_female")
        val FED_TAG_LOCATION = Column.Nullable("fed_tag_location")
        val NUES_TAG_MALE_COLOR_FEMALE_COLOR_SAME = Column.NotNull("id_nues_tag_male_color_female_color_same")
        val NUES_TAG_COLOR_MALE = Column.Nullable("nues_tag_color_male")
        val NUES_TAG_COLOR_FEMALE = Column.Nullable("nues_tag_color_female")
        val NUES_TAG_LOCATION = Column.Nullable("nues_tag_location")
        val TRICH_TAG_MALE_COLOR_FEMALE_COLOR_SAME =
            Column.NotNull("id_trich_tag_male_color_female_color_same")
        val TRICH_TAG_COLOR_MALE = Column.Nullable("trich_tag_color_male")
        val TRICH_TAG_COLOR_FEMALE = Column.Nullable("trich_tag_color_female")
        val TRICH_TAG_LOCATION = Column.Nullable("trich_tag_location")
        val TRICH_TAG_AUTO_INCREMENT = Column.NotNull("trich_tag_auto_increment")
        val TRICH_TAG_NEXT_TAG_NUMBER = Column.Nullable("trich_tag_next_tag_number")
        val BANGS_TAG_MALE_COLOR_FEMALE_COLOR_SAME =
            Column.NotNull("id_bangs_tag_male_color_female_color_same")
        val BANGS_TAG_COLOR_MALE = Column.Nullable("bangs_tag_color_male")
        val BANGS_TAG_COLOR_FEMALE = Column.Nullable("bangs_tag_color_female")
        val BANGS_TAG_LOCATION = Column.Nullable("bangs_tag_location")
        val SALE_ORDER_TAG_MALE_COLOR_FEMALE_COLOR_SAME =
            Column.NotNull("id_sale_order_tag_male_color_female_color_same")
        val SALE_ORDER_TAG_COLOR_MALE = Column.Nullable("sale_order_tag_color_male")
        val SALE_ORDER_TAG_COLOR_FEMALE = Column.Nullable("sale_order_tag_color_female")
        val SALE_ORDER_TAG_LOCATION = Column.Nullable("sale_order_tag_location")
        val USE_PAINT_MARKS = Column.NotNull("use_paint_marks")
        val PAINT_MARK_COLOR = Column.Nullable("paint_mark_color")
        val PAINT_MARK_LOCATION = Column.Nullable("paint_mark_location")
        val TATTOO_COLOR = Column.Nullable("tattoo_color")
        val TATTOO_LOCATION = Column.Nullable("tattoo_location")
        val FREEZE_BRAND_LOCATION = Column.Nullable("freeze_brand_location")
        val REMOVE_REASON_ID = Column.NotNull("id_idremovereasonid")
        val TISSUE_SAMPLE_TYPE_ID = Column.Nullable("id_tissuesampletypeid")
        val TISSUE_TEST_ID = Column.Nullable("id_tissuetestid")
        val TISSUE_CONTAINER_TYPE_ID = Column.NotNull("id_tissuesamplecontainertypeid")
        val BIRTH_TYPE = Column.NotNull("birth_type")
        val REAR_TYPE = Column.Nullable("rear_type")
        val MINIMUM_BIRTH_WEIGHT = Column.NotNull("minimum_birth_weight")
        val MAXIMUM_BIRTH_WEIGHT = Column.NotNull("maximum_birth_weight")
        val BIRTH_WEIGHT_UNITS_ID = Column.NotNull("birth_weight_id_unitsid")
        val WEIGHT_UNITS_ID = Column.NotNull("weight_id_unitsid")
        val SALE_PRICE_UNITS_ID = Column.Nullable("sale_price_id_unitsid")
        val DEATH_REASON_CONTACT_ID = Column.Nullable("death_reason_id_contactid")
        val DEATH_REASON_COMPANY_ID = Column.Nullable("death_reason_id_companyid")
        val DEATH_REASON_ID = Column.NotNull("id_deathreasonid")
        val TRANSFER_REASON_ID = Column.NotNull("id_transferreasonid")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
