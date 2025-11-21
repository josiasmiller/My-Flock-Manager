package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_136_AA
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_136_AV
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_136_A_
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_136_VV
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_136_V_
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_136___
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_141_FF
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_141_FL
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_141_F_
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_141_LL
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_141_L_
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_141___
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_154_HH
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_154_H_
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_154_RH
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_154_RR
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_154_R_
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_154___
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_171_HH
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_171_HK
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_171_H_
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_171_KK
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_171_K_
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_171_QH
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_171_QK
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_171_QQ
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_171_QR
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_171_Q_
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_171_RH
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_171_RK
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_171_RR
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_171_R_
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic.Companion.ID_171___
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


@Parcelize
data class AnimalGeneticCharacteristics(
    val animalId: EntityId,
    val geneticCharacteristics: List<AnimalGeneticCharacteristic>
) : Parcelable

@Parcelize
data class AnimalGeneticCharacteristic(
    val id: EntityId,
    val geneticCharacteristicId: EntityId,
    val geneticCharacteristicValueId: EntityId,
    val geneticCharacteristic: GeneticCharacteristic,
    val calculationMethod: GeneticCharacteristic.CalculationMethod,
    val date: LocalDate,
    val time: LocalTime?
) : Parcelable {
    @IgnoredOnParcel
    val dateTime: LocalDateTime by lazy {
        date.atTime(time)
    }
}

sealed interface GeneticCharacteristic : Parcelable {
    companion object {

        const val ID_CODON_112_RAW = "91d9cd52-817c-446a-b6a3-d5c30dfb7ad6" //LEGACY ID = 1
        const val ID_CODON_136_RAW = "6612a679-deaf-44ec-ad3a-ea752f422840" //LEGACY ID = 2
        const val ID_CODON_141_RAW = "3d52128e-81c0-40b1-b10e-14a6ece16acc" //LEGACY ID = 3
        const val ID_CODON_154_RAW = "1ae30219-33da-42e2-b43a-0b840a062679" //LEGACY ID = 4
        const val ID_CODON_171_RAW = "82270054-40c2-4cfd-ba38-b82dec380821" //LEGACY ID = 5
        const val ID_HORN_TYPE_RAW = "52fee8a5-9b2d-4df7-b872-362eb9e2bc16" //LEGACY ID = 6
        const val ID_COAT_COLOR_RAW = "0972486b-7b99-427e-b942-fa5ec88c2678" //LEGACY ID = 7

        val ID_CODON_112 = EntityId(ID_CODON_112_RAW)
        val ID_CODON_136 = EntityId(ID_CODON_136_RAW)
        val ID_CODON_141 = EntityId(ID_CODON_141_RAW)
        val ID_CODON_154 = EntityId(ID_CODON_154_RAW)
        val ID_CODON_171 = EntityId(ID_CODON_171_RAW)
        val ID_HORN_TYPE = EntityId(ID_HORN_TYPE_RAW)
        val ID_COAT_COLOR = EntityId(ID_COAT_COLOR_RAW)
    }

    enum class Type(val id: Int) {
        CODON(0),
        COAT_COLOR(1),
        HORN_TYPE(2)
    }

    @Parcelize
    data class CalculationMethod(
        val id: EntityId,
        val name: String,
        val order: Int = -1
    ) : Parcelable {

        companion object {

            const val ID_DNA_RAW = "3fdbbcd5-cbde-4da7-8747-3400aee4005d" //LEGACY ID = 1
            val ID_DNA = EntityId(ID_DNA_RAW)

            const val ID_PEDIGREE_RAW = "1d809cb1-718b-41b2-bbb4-a79da6091624" //LEGACY ID = 2
            val ID_PEDIGREE = EntityId(ID_PEDIGREE_RAW)

            const val ID_OBSERVATION_RAW = "1ae4b983-104a-4e8e-b269-ff3790608c8d" //LEGACY ID = 3
            val ID_OBSERVATION = EntityId(ID_OBSERVATION_RAW)
        }
    }

    val id: EntityId
    val name: String
    val type: Type
}

enum class Codon(val id: EntityId, val code: String) {
    CODE_112(GeneticCharacteristic.ID_CODON_112, "112"),
    CODE_136(GeneticCharacteristic.ID_CODON_136, "136"),
    CODE_141(GeneticCharacteristic.ID_CODON_141, "141"),
    CODE_154(GeneticCharacteristic.ID_CODON_154, "154"),
    CODE_171(GeneticCharacteristic.ID_CODON_171, "171");

    companion object {
        fun fromId(id: EntityId): Codon? {
            return entries.firstOrNull { it.id == id }
        }
    }
}

@Parcelize
data class CodonCharacteristic(
    override val id: EntityId,
    override val name: String,
    val codon: Codon,
    val alleles: String
) : GeneticCharacteristic, Parcelable {

    @IgnoredOnParcel
    override val type: GeneticCharacteristic.Type
        get() = GeneticCharacteristic.Type.CODON

    companion object {

        //  Constant         id_codon136id   codon136alleles
        //  ------------------------------------------------
        //  ID_136_AA        1               AA
        //  ID_136_A_        2               A?
        //  ID_136_AV        3               AV
        //  ID_136_V_        4               V?
        //  ID_136_VV        5               VV
        //  ID_136___        6               ??

        val ID_136_AA_RAW = "797d50c6-2265-40e0-915b-99559245ff38" //LEGACY ID = 1
        val ID_136_A__RAW = "719f52a6-fa07-4479-9284-cb3264eebe0f" //LEGACY ID = 2
        val ID_136_AV_RAW = "cf5dce03-96e7-45e6-96c3-36551c9e7b01" //LEGACY ID = 3
        val ID_136_V__RAW = "6f2a588f-38ab-4b57-b3f8-862769a01c85" //LEGACY ID = 4
        val ID_136_VV_RAW = "385820e9-3171-4836-abc7-c323f922e69c" //LEGACY ID = 5
        val ID_136____RAW = "57649339-9cbb-4c58-ac17-1f7e02c377b1" //LEGACY ID = 6

        val ID_136_AA = EntityId(ID_136_AA_RAW)
        val ID_136_A_ = EntityId(ID_136_A__RAW)
        val ID_136_AV = EntityId(ID_136_AV_RAW)
        val ID_136_V_ = EntityId(ID_136_V__RAW)
        val ID_136_VV = EntityId(ID_136_VV_RAW)
        val ID_136___ = EntityId(ID_136____RAW)

        fun coerceToCodon136(entityId: EntityId?): EntityId = when (entityId) {
            ID_136_AA,
            ID_136_A_,
            ID_136_AV,
            ID_136_V_,
            ID_136_VV,
            ID_136___ -> entityId
            else -> ID_136___
        }

        //  Constant         id_codon141id   codon141alleles
        //  ------------------------------------------------
        //  ID_141___        1               ??
        //  ID_141_FF        2               FF
        //  ID_141_F_        3               F?
        //  ID_141_FL        4               FL
        //  ID_141_L_        5               L?
        //  ID_141_LL        6               LL

        val ID_141____RAW = "5121aef6-4de5-43f7-afb7-7cfcf45746e3" //LEGACY ID = 1
        val ID_141_FF_RAW = "efc2380a-a897-4957-b874-1c2283a178b4" //LEGACY ID = 2
        val ID_141_F__RAW = "b14e669a-2bbf-4df3-a016-c092ed302fe0" //LEGACY ID = 3
        val ID_141_FL_RAW = "d8f50e03-a24a-43bc-a413-f2dadf7515c0" //LEGACY ID = 4
        val ID_141_L__RAW = "5c3941d5-fe39-4f58-8c50-8a344b5e3ffa" //LEGACY ID = 5
        val ID_141_LL_RAW = "30138deb-a385-49c4-89c9-427fc4020d0e" //LEGACY ID = 6

        val ID_141___ = EntityId(ID_141____RAW)
        val ID_141_FF = EntityId(ID_141_FF_RAW)
        val ID_141_F_ = EntityId(ID_141_F__RAW)
        val ID_141_FL = EntityId(ID_141_FL_RAW)
        val ID_141_L_ = EntityId(ID_141_L__RAW)
        val ID_141_LL = EntityId(ID_141_LL_RAW)

        fun coerceToCodon141(entityId: EntityId?): EntityId = when (entityId) {
            ID_141___,
            ID_141_FF,
            ID_141_F_,
            ID_141_FL,
            ID_141_L_,
            ID_141_LL -> entityId
            else -> ID_141___
        }

        //  Constant         id_codon154id   codon154alleles
        //  ------------------------------------------------
        //  ID_154___        1               ??
        //  ID_154_RR        2               RR
        //  ID_154_RH        3               RH
        //  ID_154_R_        4               R?
        //  ID_154_HH        5               HH
        //  ID_154_H_        6               H?

        val ID_154____RAW = "2a8c03f8-6736-424b-a6b0-ac9f48eaa170" //LEGACY ID = 1
        val ID_154_RR_RAW = "82d79342-0dbc-4723-b8d4-c135e66541ef" //LEGACY ID = 2
        val ID_154_RH_RAW = "01ffdede-601e-498f-8e3b-f8d905830e4c" //LEGACY ID = 3
        val ID_154_R__RAW = "02d66e08-a189-4127-a083-08488a081863" //LEGACY ID = 4
        val ID_154_HH_RAW = "22a18722-d5eb-478c-bc09-943a9af41162" //LEGACY ID = 5
        val ID_154_H__RAW = "23b669ab-6639-4d76-87ff-f0e7d3476241" //LEGACY ID = 6

        val ID_154___ = EntityId(ID_154____RAW)
        val ID_154_RR = EntityId(ID_154_RR_RAW)
        val ID_154_RH = EntityId(ID_154_RH_RAW)
        val ID_154_R_ = EntityId(ID_154_R__RAW)
        val ID_154_HH = EntityId(ID_154_HH_RAW)
        val ID_154_H_ = EntityId(ID_154_H__RAW)

        fun coerceToCodon154(entityId: EntityId?): EntityId = when (entityId) {
            ID_154___,
            ID_154_RR,
            ID_154_RH,
            ID_154_R_,
            ID_154_HH,
            ID_154_H_ -> entityId
            else -> ID_154___
        }

        //  Constant         id_codon171id   codon171alleles
        //  ------------------------------------------------
        //  ID_171_QQ        1               QQ
        //  ID_171_Q_        2               Q?
        //  ID_171_QR        3               QR
        //  ID_171_R_        4               R?
        //  ID_171_RR        5               RR
        //  ID_171___        6               ??
        //  ID_171_HH        7               HH
        //  ID_171_QH        8               QH
        //  ID_171_H_        9               H?
        //  ID_171_RH        10              RH
        //  ID_171_KK        11              KK
        //  ID_171_QK        12              QK
        //  ID_171_RK        13              RK
        //  ID_171_K_        14              K?
        //  ID_171_HK        15              HK

        //  Although we can code for all the alleles the US only concerns itself with Q and R
        //  Because current research is that H and K are like Q in terms of scrapie susceptibility
        //  that is what we are testing for now.
        //  Further research could allow for changes and the code can be modified then

        val ID_171_QQ_RAW = "c0bcd357-50b9-4112-9c00-fa0b9e58498d" //LEGACY ID = 1
        val ID_171_Q__RAW = "713c758b-60b3-499f-9869-aa9058c0fce6" //LEGACY ID = 2
        val ID_171_QR_RAW = "084bfbf1-75c8-446e-875d-437230be3e3e" //LEGACY ID = 3
        val ID_171_R__RAW = "20016fc6-ba2c-45f7-9663-8536eb2124df" //LEGACY ID = 4
        val ID_171_RR_RAW = "6fa2dd89-98da-4b0c-a62a-867fa42f2515" //LEGACY ID = 5
        val ID_171____RAW = "14947401-f58d-4995-b378-d62bf056c57d" //LEGACY ID = 6
        val ID_171_HH_RAW = "eca8cd5f-907e-4842-b032-d8f4d397355e" //LEGACY ID = 7
        val ID_171_QH_RAW = "63bb11a3-f22a-43fc-9996-2ed253398632" //LEGACY ID = 8
        val ID_171_H__RAW = "436e9f81-b639-4264-942c-23f0b0c43dfe" //LEGACY ID = 9
        val ID_171_RH_RAW = "b664abf9-6d96-44fe-8040-5876ee2997d7" //LEGACY ID = 10
        val ID_171_KK_RAW = "993d6455-11e2-48f2-a49a-782f1897d0d8" //LEGACY ID = 11
        val ID_171_QK_RAW = "9a712570-30a5-4c38-9b96-37d85ea22207" //LEGACY ID = 12
        val ID_171_RK_RAW = "fffd6b51-d3d7-4e9f-a12f-4071e5e406ec" //LEGACY ID = 13
        val ID_171_K__RAW = "234f4e31-e087-40d8-89f9-fb2f274e9bcd" //LEGACY ID = 14
        val ID_171_HK_RAW = "de636060-7952-4108-8459-7e7a3bc226d4" //LEGACY ID = 15

        val ID_171_QQ = EntityId(ID_171_QQ_RAW)
        val ID_171_Q_ = EntityId(ID_171_Q__RAW)
        val ID_171_QR = EntityId(ID_171_QR_RAW)
        val ID_171_R_ = EntityId(ID_171_R__RAW)
        val ID_171_RR = EntityId(ID_171_RR_RAW)
        val ID_171___ = EntityId(ID_171____RAW)
        val ID_171_HH = EntityId(ID_171_HH_RAW)
        val ID_171_QH = EntityId(ID_171_QH_RAW)
        val ID_171_H_ = EntityId(ID_171_H__RAW)
        val ID_171_RH = EntityId(ID_171_RH_RAW)
        val ID_171_KK = EntityId(ID_171_KK_RAW)
        val ID_171_QK = EntityId(ID_171_QK_RAW)
        val ID_171_RK = EntityId(ID_171_RK_RAW)
        val ID_171_K_ = EntityId(ID_171_K__RAW)
        val ID_171_HK = EntityId(ID_171_HK_RAW)

        fun coerceToCodon171(entityId: EntityId?): EntityId = when (entityId) {
            ID_171_QQ,
            ID_171_Q_,
            ID_171_QR,
            ID_171_R_,
            ID_171_RR,
            ID_171___,
            ID_171_HH,
            ID_171_QH,
            ID_171_H_,
            ID_171_RH,
            ID_171_KK,
            ID_171_QK,
            ID_171_RK,
            ID_171_K_,
            ID_171_HK -> entityId
            else -> ID_171___
        }
    }
}

@Parcelize
data class CoatColorCharacteristic(
    override val id: EntityId,
    override val name: String,
    val coatColor: String,
    val coatColorAbbreviation: String
) : GeneticCharacteristic, Parcelable, HasName {
    @IgnoredOnParcel
    override val type: GeneticCharacteristic.Type
        get() = GeneticCharacteristic.Type.COAT_COLOR
}

@Parcelize
data class HornTypeCharacteristic(
    override val id: EntityId,
    override val name: String,
    val hornType: String,
    val hornTypeAbbreviation: String
) : GeneticCharacteristic, Parcelable {
    @IgnoredOnParcel
    override val type: GeneticCharacteristic.Type
        get() = GeneticCharacteristic.Type.HORN_TYPE
}

object CodonCalculation {

    fun calculateCodon136(sireCodon136: EntityId?, damCodon136: EntityId?): EntityId {
        val sireCodon136 = CodonCharacteristic.coerceToCodon136(sireCodon136)
        val damCodon136 = CodonCharacteristic.coerceToCodon136(damCodon136)
        return when (damCodon136) {
            ID_136_AA -> when (sireCodon136) {
                ID_136_AA -> ID_136_AA
                ID_136_VV -> ID_136_AV
                else -> ID_136_A_
            }
            ID_136_VV -> when (sireCodon136) {
                ID_136_AA -> ID_136_AV
                ID_136_VV -> ID_136_VV
                else -> ID_136_V_
            }
            else -> when (sireCodon136) {
                ID_136_AA -> ID_136_A_
                ID_136_VV -> ID_136_V_
                else -> ID_136___
            }
        }
    }

    fun calculateCodon141(sireCodon141: EntityId?, damCodon141: EntityId?): EntityId {
        val sireCodon141 = CodonCharacteristic.coerceToCodon141(sireCodon141)
        val damCodon141 = CodonCharacteristic.coerceToCodon141(damCodon141)
        return when (damCodon141) {
            ID_141_FF -> when (sireCodon141) {
                ID_141_FF -> ID_141_FF
                ID_141_LL -> ID_141_FL
                else -> ID_141_F_
            }
            ID_141_LL -> when (sireCodon141) {
                ID_141_FF -> ID_141_FL
                ID_141_LL -> ID_141_LL
                else -> ID_141_L_
            }
            else -> when (sireCodon141) {
                ID_141_FF -> ID_141_F_
                ID_141_LL -> ID_141_L_
                else -> ID_141___
            }
        }
    }

    fun calculateCodon154(sireCodon154: EntityId?, damCodon154: EntityId?): EntityId {
        val sireCodon154 = CodonCharacteristic.coerceToCodon154(sireCodon154)
        val damCodon154 = CodonCharacteristic.coerceToCodon154(damCodon154)
        return when (damCodon154) {
            ID_154_RR -> when (sireCodon154) {
                ID_154_RR -> ID_154_RR
                ID_154_HH -> ID_154_RH
                else -> ID_154_R_
            }
            ID_154_HH -> when (sireCodon154) {
                ID_154_RR -> ID_154_RH
                ID_154_HH -> ID_154_HH
                else -> ID_154_H_
            }
            else -> when (sireCodon154) {
                ID_154_RR -> ID_154_R_
                ID_154_HH -> ID_154_H_
                else -> ID_154___
            }
        }
    }

    fun calculateCodon171(sireCodon171: EntityId?, damCodon171: EntityId?): EntityId {
        val sireCodon171 = CodonCharacteristic.coerceToCodon171(sireCodon171)
        val damCodon171 = CodonCharacteristic.coerceToCodon171(damCodon171)
        return when (damCodon171) {
            ID_171_QQ -> when (sireCodon171) {
                ID_171_QQ -> ID_171_QQ
                ID_171_RR -> ID_171_QR
                ID_171_HH -> ID_171_QH
                ID_171_KK -> ID_171_QK
                else -> ID_171_Q_
            }
            ID_171_RR -> when (sireCodon171) {
                ID_171_QQ -> ID_171_QR
                ID_171_RR -> ID_171_RR
                ID_171_HH -> ID_171_RH
                ID_171_KK -> ID_171_RK
                else -> ID_171___
            }
            ID_171_HH -> when (sireCodon171) {
                ID_171_QQ -> ID_171_QH
                ID_171_RR -> ID_171_RH
                ID_171_HH -> ID_171_HH
                ID_171_KK -> ID_171_HK
                else -> ID_171___
            }
            ID_171_KK -> when (sireCodon171) {
                ID_171_QQ -> ID_171_QK
                ID_171_RR -> ID_171_RK
                ID_171_HH -> ID_171_HK
                ID_171_KK -> ID_171_KK
                else -> ID_171___
            }
            else -> when (sireCodon171) {
                ID_171_QQ -> ID_171_Q_
                ID_171_RR -> ID_171_R_
                ID_171_HH -> ID_171_H_
                ID_171_KK -> ID_171_K_
                else -> ID_171___
            }
        }
    }
}
