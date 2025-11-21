package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.drug

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.requireAs
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import java.util.UUID

object ConfigureDrugAction {
    const val ACTION_CONFIGURE = "ACTION_CONFIGURE"
    const val ACTION_EDIT = "ACTION_EDIT"

    const val EXTRA_DRUG_TYPE_ID = "EXTRA_DRUG_TYPE_ID"
    const val EXTRA_EDIT_ACTION_ID = "EXTRA_EDIT_ACTION_ID"
    const val EXTRA_EXCLUDED_DRUG_IDS = "EXTRA_EXCLUDED_DRUG_IDS"

    const val EXTRA_DRUG_ACTION_CONFIGURATION = "EXTRA_DRUG_ACTION_CONFIGURATION"

    data class ConfigureRequest(
        val drugTypeId: EntityId,
        val excludedDrugIds: Set<EntityId>
    )

    data class EditRequest(
        val actionId: UUID,
        val configuration: DrugAction.Configuration,
        val excludedDrugIds: Set<EntityId>
    )

    data class EditResult(
        val actionId: UUID,
        val configuration: DrugAction.Configuration
    )

    class ConfigureContract : ActivityResultContract<ConfigureRequest, DrugAction.Configuration?>() {
        override fun createIntent(context: Context, input: ConfigureRequest): Intent {
            return DrugActionConfigurationActivity.newIntentToConfigure(
                context,
                input.drugTypeId,
                input.excludedDrugIds
            )
        }

        override fun parseResult(resultCode: Int, intent: Intent?): DrugAction.Configuration? {
            return intent?.getParcelableExtra(EXTRA_DRUG_ACTION_CONFIGURATION)
        }
    }

    class EditContract : ActivityResultContract<EditRequest, EditResult?>() {
        override fun createIntent(context: Context, input: EditRequest): Intent {
            return DrugActionConfigurationActivity.newIntentToEdit(
                context,
                input.actionId,
                input.configuration,
                input.excludedDrugIds
            )
        }

        override fun parseResult(resultCode: Int, intent: Intent?): EditResult? {
            return intent?.let {
                EditResult(
                    actionId = requireNotNull(
                        it.getSerializableExtra(EXTRA_EDIT_ACTION_ID)
                            .requireAs<UUID>()
                    ),
                    configuration = requireNotNull(
                        it.getParcelableExtra(
                            EXTRA_DRUG_ACTION_CONFIGURATION
                        )
                    )
                )
            }
        }
    }
}
