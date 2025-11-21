package com.weyr_associates.animaltrakkerfarmmobile.model

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
import org.junit.Assert
import org.junit.Test

class CodonCalculationTest {

    @Test
    fun test_calculateCodon136() {
        Assert.assertEquals(ID_136_AA, CodonCalculation.calculateCodon136(ID_136_AA, ID_136_AA))
        Assert.assertEquals(ID_136_A_, CodonCalculation.calculateCodon136(ID_136_A_, ID_136_AA))
        Assert.assertEquals(ID_136_A_, CodonCalculation.calculateCodon136(ID_136_AV, ID_136_AA))
        Assert.assertEquals(ID_136_A_, CodonCalculation.calculateCodon136(ID_136_V_, ID_136_AA))
        Assert.assertEquals(ID_136_AV, CodonCalculation.calculateCodon136(ID_136_VV, ID_136_AA))
        Assert.assertEquals(ID_136_A_, CodonCalculation.calculateCodon136(ID_136___, ID_136_AA))

        Assert.assertEquals(ID_136_AV, CodonCalculation.calculateCodon136(ID_136_AA, ID_136_VV))
        Assert.assertEquals(ID_136_V_, CodonCalculation.calculateCodon136(ID_136_A_, ID_136_VV))
        Assert.assertEquals(ID_136_V_, CodonCalculation.calculateCodon136(ID_136_AV, ID_136_VV))
        Assert.assertEquals(ID_136_V_, CodonCalculation.calculateCodon136(ID_136_V_, ID_136_VV))
        Assert.assertEquals(ID_136_VV, CodonCalculation.calculateCodon136(ID_136_VV, ID_136_VV))
        Assert.assertEquals(ID_136_V_, CodonCalculation.calculateCodon136(ID_136___, ID_136_VV))

        Assert.assertEquals(ID_136_A_, CodonCalculation.calculateCodon136(ID_136_AA, ID_136_A_))
        Assert.assertEquals(ID_136___, CodonCalculation.calculateCodon136(ID_136_A_, ID_136_A_))
        Assert.assertEquals(ID_136___, CodonCalculation.calculateCodon136(ID_136_AV, ID_136_A_))
        Assert.assertEquals(ID_136___, CodonCalculation.calculateCodon136(ID_136_V_, ID_136_A_))
        Assert.assertEquals(ID_136_V_, CodonCalculation.calculateCodon136(ID_136_VV, ID_136_A_))
        Assert.assertEquals(ID_136___, CodonCalculation.calculateCodon136(ID_136___, ID_136_A_))

        Assert.assertEquals(ID_136_A_, CodonCalculation.calculateCodon136(ID_136_AA, ID_136_AV))
        Assert.assertEquals(ID_136___, CodonCalculation.calculateCodon136(ID_136_A_, ID_136_AV))
        Assert.assertEquals(ID_136___, CodonCalculation.calculateCodon136(ID_136_AV, ID_136_AV))
        Assert.assertEquals(ID_136___, CodonCalculation.calculateCodon136(ID_136_V_, ID_136_AV))
        Assert.assertEquals(ID_136_V_, CodonCalculation.calculateCodon136(ID_136_VV, ID_136_AV))
        Assert.assertEquals(ID_136___, CodonCalculation.calculateCodon136(ID_136___, ID_136_AV))

        Assert.assertEquals(ID_136_A_, CodonCalculation.calculateCodon136(ID_136_AA, ID_136_V_))
        Assert.assertEquals(ID_136___, CodonCalculation.calculateCodon136(ID_136_A_, ID_136_V_))
        Assert.assertEquals(ID_136___, CodonCalculation.calculateCodon136(ID_136_AV, ID_136_V_))
        Assert.assertEquals(ID_136___, CodonCalculation.calculateCodon136(ID_136_V_, ID_136_V_))
        Assert.assertEquals(ID_136_V_, CodonCalculation.calculateCodon136(ID_136_VV, ID_136_V_))
        Assert.assertEquals(ID_136___, CodonCalculation.calculateCodon136(ID_136___, ID_136_V_))

        Assert.assertEquals(ID_136_A_, CodonCalculation.calculateCodon136(ID_136_AA, ID_136___))
        Assert.assertEquals(ID_136___, CodonCalculation.calculateCodon136(ID_136_A_, ID_136___))
        Assert.assertEquals(ID_136___, CodonCalculation.calculateCodon136(ID_136_AV, ID_136___))
        Assert.assertEquals(ID_136___, CodonCalculation.calculateCodon136(ID_136_V_, ID_136___))
        Assert.assertEquals(ID_136_V_, CodonCalculation.calculateCodon136(ID_136_VV, ID_136___))
        Assert.assertEquals(ID_136___, CodonCalculation.calculateCodon136(ID_136___, ID_136___))
    }

    @Test
    fun test_calculateCodon141() {
        Assert.assertEquals(ID_141_FF, CodonCalculation.calculateCodon141(ID_141_FF, ID_141_FF))
        Assert.assertEquals(ID_141_F_, CodonCalculation.calculateCodon141(ID_141_F_, ID_141_FF))
        Assert.assertEquals(ID_141_F_, CodonCalculation.calculateCodon141(ID_141_FL, ID_141_FF))
        Assert.assertEquals(ID_141_F_, CodonCalculation.calculateCodon141(ID_141_L_, ID_141_FF))
        Assert.assertEquals(ID_141_FL, CodonCalculation.calculateCodon141(ID_141_LL, ID_141_FF))
        Assert.assertEquals(ID_141_F_, CodonCalculation.calculateCodon141(ID_141___, ID_141_FF))

        Assert.assertEquals(ID_141_FL, CodonCalculation.calculateCodon141(ID_141_FF, ID_141_LL))
        Assert.assertEquals(ID_141_L_, CodonCalculation.calculateCodon141(ID_141_F_, ID_141_LL))
        Assert.assertEquals(ID_141_L_, CodonCalculation.calculateCodon141(ID_141_FL, ID_141_LL))
        Assert.assertEquals(ID_141_L_, CodonCalculation.calculateCodon141(ID_141_L_, ID_141_LL))
        Assert.assertEquals(ID_141_LL, CodonCalculation.calculateCodon141(ID_141_LL, ID_141_LL))
        Assert.assertEquals(ID_141_L_, CodonCalculation.calculateCodon141(ID_141___, ID_141_LL))

        Assert.assertEquals(ID_141_F_, CodonCalculation.calculateCodon141(ID_141_FF, ID_141_F_))
        Assert.assertEquals(ID_141___, CodonCalculation.calculateCodon141(ID_141_F_, ID_141_F_))
        Assert.assertEquals(ID_141___, CodonCalculation.calculateCodon141(ID_141_FL, ID_141_F_))
        Assert.assertEquals(ID_141___, CodonCalculation.calculateCodon141(ID_141_L_, ID_141_F_))
        Assert.assertEquals(ID_141_L_, CodonCalculation.calculateCodon141(ID_141_LL, ID_141_F_))
        Assert.assertEquals(ID_141___, CodonCalculation.calculateCodon141(ID_141___, ID_141_F_))

        Assert.assertEquals(ID_141_F_, CodonCalculation.calculateCodon141(ID_141_FF, ID_141_FL))
        Assert.assertEquals(ID_141___, CodonCalculation.calculateCodon141(ID_141_F_, ID_141_FL))
        Assert.assertEquals(ID_141___, CodonCalculation.calculateCodon141(ID_141_FL, ID_141_FL))
        Assert.assertEquals(ID_141___, CodonCalculation.calculateCodon141(ID_141_L_, ID_141_FL))
        Assert.assertEquals(ID_141_L_, CodonCalculation.calculateCodon141(ID_141_LL, ID_141_FL))
        Assert.assertEquals(ID_141___, CodonCalculation.calculateCodon141(ID_141___, ID_141_FL))

        Assert.assertEquals(ID_141_F_, CodonCalculation.calculateCodon141(ID_141_FF, ID_141_L_))
        Assert.assertEquals(ID_141___, CodonCalculation.calculateCodon141(ID_141_F_, ID_141_L_))
        Assert.assertEquals(ID_141___, CodonCalculation.calculateCodon141(ID_141_FL, ID_141_L_))
        Assert.assertEquals(ID_141___, CodonCalculation.calculateCodon141(ID_141_L_, ID_141_L_))
        Assert.assertEquals(ID_141_L_, CodonCalculation.calculateCodon141(ID_141_LL, ID_141_L_))
        Assert.assertEquals(ID_141___, CodonCalculation.calculateCodon141(ID_141___, ID_141_L_))

        Assert.assertEquals(ID_141_F_, CodonCalculation.calculateCodon141(ID_141_FF, ID_141___))
        Assert.assertEquals(ID_141___, CodonCalculation.calculateCodon141(ID_141_F_, ID_141___))
        Assert.assertEquals(ID_141___, CodonCalculation.calculateCodon141(ID_141_FL, ID_141___))
        Assert.assertEquals(ID_141___, CodonCalculation.calculateCodon141(ID_141_L_, ID_141___))
        Assert.assertEquals(ID_141_L_, CodonCalculation.calculateCodon141(ID_141_LL, ID_141___))
        Assert.assertEquals(ID_141___, CodonCalculation.calculateCodon141(ID_141___, ID_141___))
    }

    @Test
    fun test_calculateCodon154() {
        Assert.assertEquals(ID_154_RR, CodonCalculation.calculateCodon154(ID_154_RR, ID_154_RR))
        Assert.assertEquals(ID_154_R_, CodonCalculation.calculateCodon154(ID_154_R_, ID_154_RR))
        Assert.assertEquals(ID_154_R_, CodonCalculation.calculateCodon154(ID_154_RH, ID_154_RR))
        Assert.assertEquals(ID_154_R_, CodonCalculation.calculateCodon154(ID_154_H_, ID_154_RR))
        Assert.assertEquals(ID_154_RH, CodonCalculation.calculateCodon154(ID_154_HH, ID_154_RR))
        Assert.assertEquals(ID_154_R_, CodonCalculation.calculateCodon154(ID_154___, ID_154_RR))

        Assert.assertEquals(ID_154_RH, CodonCalculation.calculateCodon154(ID_154_RR, ID_154_HH))
        Assert.assertEquals(ID_154_H_, CodonCalculation.calculateCodon154(ID_154_R_, ID_154_HH))
        Assert.assertEquals(ID_154_H_, CodonCalculation.calculateCodon154(ID_154_RH, ID_154_HH))
        Assert.assertEquals(ID_154_H_, CodonCalculation.calculateCodon154(ID_154_H_, ID_154_HH))
        Assert.assertEquals(ID_154_HH, CodonCalculation.calculateCodon154(ID_154_HH, ID_154_HH))
        Assert.assertEquals(ID_154_H_, CodonCalculation.calculateCodon154(ID_154___, ID_154_HH))

        Assert.assertEquals(ID_154_R_, CodonCalculation.calculateCodon154(ID_154_RR, ID_154_R_))
        Assert.assertEquals(ID_154___, CodonCalculation.calculateCodon154(ID_154_R_, ID_154_R_))
        Assert.assertEquals(ID_154___, CodonCalculation.calculateCodon154(ID_154_RH, ID_154_R_))
        Assert.assertEquals(ID_154___, CodonCalculation.calculateCodon154(ID_154_H_, ID_154_R_))
        Assert.assertEquals(ID_154_H_, CodonCalculation.calculateCodon154(ID_154_HH, ID_154_R_))
        Assert.assertEquals(ID_154___, CodonCalculation.calculateCodon154(ID_154___, ID_154_R_))

        Assert.assertEquals(ID_154_R_, CodonCalculation.calculateCodon154(ID_154_RR, ID_154_RH))
        Assert.assertEquals(ID_154___, CodonCalculation.calculateCodon154(ID_154_R_, ID_154_RH))
        Assert.assertEquals(ID_154___, CodonCalculation.calculateCodon154(ID_154_RH, ID_154_RH))
        Assert.assertEquals(ID_154___, CodonCalculation.calculateCodon154(ID_154_H_, ID_154_RH))
        Assert.assertEquals(ID_154_H_, CodonCalculation.calculateCodon154(ID_154_HH, ID_154_RH))
        Assert.assertEquals(ID_154___, CodonCalculation.calculateCodon154(ID_154___, ID_154_RH))

        Assert.assertEquals(ID_154_R_, CodonCalculation.calculateCodon154(ID_154_RR, ID_154_H_))
        Assert.assertEquals(ID_154___, CodonCalculation.calculateCodon154(ID_154_R_, ID_154_H_))
        Assert.assertEquals(ID_154___, CodonCalculation.calculateCodon154(ID_154_RH, ID_154_H_))
        Assert.assertEquals(ID_154___, CodonCalculation.calculateCodon154(ID_154_H_, ID_154_H_))
        Assert.assertEquals(ID_154_H_, CodonCalculation.calculateCodon154(ID_154_HH, ID_154_H_))
        Assert.assertEquals(ID_154___, CodonCalculation.calculateCodon154(ID_154___, ID_154_H_))

        Assert.assertEquals(ID_154_R_, CodonCalculation.calculateCodon154(ID_154_RR, ID_154___))
        Assert.assertEquals(ID_154___, CodonCalculation.calculateCodon154(ID_154_R_, ID_154___))
        Assert.assertEquals(ID_154___, CodonCalculation.calculateCodon154(ID_154_RH, ID_154___))
        Assert.assertEquals(ID_154___, CodonCalculation.calculateCodon154(ID_154_H_, ID_154___))
        Assert.assertEquals(ID_154_H_, CodonCalculation.calculateCodon154(ID_154_HH, ID_154___))
        Assert.assertEquals(ID_154___, CodonCalculation.calculateCodon154(ID_154___, ID_154___))
    }

    @Test
    fun test_calculateCodon171() {
        Assert.assertEquals(ID_171_QQ, CodonCalculation.calculateCodon171(ID_171_QQ, ID_171_QQ))
        Assert.assertEquals(ID_171_Q_, CodonCalculation.calculateCodon171(ID_171_QR, ID_171_QQ))
        Assert.assertEquals(ID_171_Q_, CodonCalculation.calculateCodon171(ID_171_QH, ID_171_QQ))
        Assert.assertEquals(ID_171_Q_, CodonCalculation.calculateCodon171(ID_171_QK, ID_171_QQ))
        Assert.assertEquals(ID_171_Q_, CodonCalculation.calculateCodon171(ID_171_Q_, ID_171_QQ))
        Assert.assertEquals(ID_171_QR, CodonCalculation.calculateCodon171(ID_171_RR, ID_171_QQ))
        Assert.assertEquals(ID_171_Q_, CodonCalculation.calculateCodon171(ID_171_RH, ID_171_QQ))
        Assert.assertEquals(ID_171_Q_, CodonCalculation.calculateCodon171(ID_171_RK, ID_171_QQ))
        Assert.assertEquals(ID_171_Q_, CodonCalculation.calculateCodon171(ID_171_R_, ID_171_QQ))
        Assert.assertEquals(ID_171_QH, CodonCalculation.calculateCodon171(ID_171_HH, ID_171_QQ))
        Assert.assertEquals(ID_171_Q_, CodonCalculation.calculateCodon171(ID_171_HK, ID_171_QQ))
        Assert.assertEquals(ID_171_Q_, CodonCalculation.calculateCodon171(ID_171_H_, ID_171_QQ))
        Assert.assertEquals(ID_171_QK, CodonCalculation.calculateCodon171(ID_171_KK, ID_171_QQ))
        Assert.assertEquals(ID_171_Q_, CodonCalculation.calculateCodon171(ID_171_K_, ID_171_QQ))
        Assert.assertEquals(ID_171_Q_, CodonCalculation.calculateCodon171(ID_171___, ID_171_QQ))

        Assert.assertEquals(ID_171_QR, CodonCalculation.calculateCodon171(ID_171_QQ, ID_171_RR))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QR, ID_171_RR))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QH, ID_171_RR))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QK, ID_171_RR))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_Q_, ID_171_RR))
        Assert.assertEquals(ID_171_RR, CodonCalculation.calculateCodon171(ID_171_RR, ID_171_RR))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RH, ID_171_RR))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RK, ID_171_RR))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_R_, ID_171_RR))
        Assert.assertEquals(ID_171_RH, CodonCalculation.calculateCodon171(ID_171_HH, ID_171_RR))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_HK, ID_171_RR))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_H_, ID_171_RR))
        Assert.assertEquals(ID_171_RK, CodonCalculation.calculateCodon171(ID_171_KK, ID_171_RR))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_K_, ID_171_RR))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171___, ID_171_RR))

        Assert.assertEquals(ID_171_QH, CodonCalculation.calculateCodon171(ID_171_QQ, ID_171_HH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QR, ID_171_HH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QH, ID_171_HH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QK, ID_171_HH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_Q_, ID_171_HH))
        Assert.assertEquals(ID_171_RH, CodonCalculation.calculateCodon171(ID_171_RR, ID_171_HH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RH, ID_171_HH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RK, ID_171_HH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_R_, ID_171_HH))
        Assert.assertEquals(ID_171_HH, CodonCalculation.calculateCodon171(ID_171_HH, ID_171_HH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_HK, ID_171_HH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_H_, ID_171_HH))
        Assert.assertEquals(ID_171_HK, CodonCalculation.calculateCodon171(ID_171_KK, ID_171_HH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_K_, ID_171_HH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171___, ID_171_HH))

        Assert.assertEquals(ID_171_QK, CodonCalculation.calculateCodon171(ID_171_QQ, ID_171_KK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QR, ID_171_KK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QH, ID_171_KK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QK, ID_171_KK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_Q_, ID_171_KK))
        Assert.assertEquals(ID_171_RK, CodonCalculation.calculateCodon171(ID_171_RR, ID_171_KK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RH, ID_171_KK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RK, ID_171_KK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_R_, ID_171_KK))
        Assert.assertEquals(ID_171_HK, CodonCalculation.calculateCodon171(ID_171_HH, ID_171_KK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_HK, ID_171_KK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_H_, ID_171_KK))
        Assert.assertEquals(ID_171_KK, CodonCalculation.calculateCodon171(ID_171_KK, ID_171_KK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_K_, ID_171_KK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171___, ID_171_KK))

        Assert.assertEquals(ID_171_Q_, CodonCalculation.calculateCodon171(ID_171_QQ, ID_171_QR))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QR, ID_171_QR))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QH, ID_171_QR))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QK, ID_171_QR))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_Q_, ID_171_QR))
        Assert.assertEquals(ID_171_R_, CodonCalculation.calculateCodon171(ID_171_RR, ID_171_QR))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RH, ID_171_QR))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RK, ID_171_QR))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_R_, ID_171_QR))
        Assert.assertEquals(ID_171_H_, CodonCalculation.calculateCodon171(ID_171_HH, ID_171_QR))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_HK, ID_171_QR))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_H_, ID_171_QR))
        Assert.assertEquals(ID_171_K_, CodonCalculation.calculateCodon171(ID_171_KK, ID_171_QR))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_K_, ID_171_QR))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171___, ID_171_QR))

        Assert.assertEquals(ID_171_Q_, CodonCalculation.calculateCodon171(ID_171_QQ, ID_171_QH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QR, ID_171_QH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QH, ID_171_QH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QK, ID_171_QH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_Q_, ID_171_QH))
        Assert.assertEquals(ID_171_R_, CodonCalculation.calculateCodon171(ID_171_RR, ID_171_QH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RH, ID_171_QH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RK, ID_171_QH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_R_, ID_171_QH))
        Assert.assertEquals(ID_171_H_, CodonCalculation.calculateCodon171(ID_171_HH, ID_171_QH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_HK, ID_171_QH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_H_, ID_171_QH))
        Assert.assertEquals(ID_171_K_, CodonCalculation.calculateCodon171(ID_171_KK, ID_171_QH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_K_, ID_171_QH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171___, ID_171_QH))

        Assert.assertEquals(ID_171_Q_, CodonCalculation.calculateCodon171(ID_171_QQ, ID_171_QK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QR, ID_171_QK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QH, ID_171_QK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QK, ID_171_QK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_Q_, ID_171_QK))
        Assert.assertEquals(ID_171_R_, CodonCalculation.calculateCodon171(ID_171_RR, ID_171_QK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RH, ID_171_QK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RK, ID_171_QK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_R_, ID_171_QK))
        Assert.assertEquals(ID_171_H_, CodonCalculation.calculateCodon171(ID_171_HH, ID_171_QK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_HK, ID_171_QK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_H_, ID_171_QK))
        Assert.assertEquals(ID_171_K_, CodonCalculation.calculateCodon171(ID_171_KK, ID_171_QK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_K_, ID_171_QK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171___, ID_171_QK))

        Assert.assertEquals(ID_171_Q_, CodonCalculation.calculateCodon171(ID_171_QQ, ID_171_Q_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QR, ID_171_Q_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QH, ID_171_Q_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QK, ID_171_Q_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_Q_, ID_171_Q_))
        Assert.assertEquals(ID_171_R_, CodonCalculation.calculateCodon171(ID_171_RR, ID_171_Q_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RH, ID_171_Q_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RK, ID_171_Q_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_R_, ID_171_Q_))
        Assert.assertEquals(ID_171_H_, CodonCalculation.calculateCodon171(ID_171_HH, ID_171_Q_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_HK, ID_171_Q_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_H_, ID_171_Q_))
        Assert.assertEquals(ID_171_K_, CodonCalculation.calculateCodon171(ID_171_KK, ID_171_Q_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_K_, ID_171_Q_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171___, ID_171_Q_))

        Assert.assertEquals(ID_171_Q_, CodonCalculation.calculateCodon171(ID_171_QQ, ID_171_RH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QR, ID_171_RH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QH, ID_171_RH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QK, ID_171_RH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_Q_, ID_171_RH))
        Assert.assertEquals(ID_171_R_, CodonCalculation.calculateCodon171(ID_171_RR, ID_171_RH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RH, ID_171_RH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RK, ID_171_RH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_R_, ID_171_RH))
        Assert.assertEquals(ID_171_H_, CodonCalculation.calculateCodon171(ID_171_HH, ID_171_RH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_HK, ID_171_RH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_H_, ID_171_RH))
        Assert.assertEquals(ID_171_K_, CodonCalculation.calculateCodon171(ID_171_KK, ID_171_RH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_K_, ID_171_RH))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171___, ID_171_RH))

        Assert.assertEquals(ID_171_Q_, CodonCalculation.calculateCodon171(ID_171_QQ, ID_171_RK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QR, ID_171_RK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QH, ID_171_RK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QK, ID_171_RK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_Q_, ID_171_RK))
        Assert.assertEquals(ID_171_R_, CodonCalculation.calculateCodon171(ID_171_RR, ID_171_RK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RH, ID_171_RK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RK, ID_171_RK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_R_, ID_171_RK))
        Assert.assertEquals(ID_171_H_, CodonCalculation.calculateCodon171(ID_171_HH, ID_171_RK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_HK, ID_171_RK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_H_, ID_171_RK))
        Assert.assertEquals(ID_171_K_, CodonCalculation.calculateCodon171(ID_171_KK, ID_171_RK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_K_, ID_171_RK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171___, ID_171_RK))

        Assert.assertEquals(ID_171_Q_, CodonCalculation.calculateCodon171(ID_171_QQ, ID_171_R_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QR, ID_171_R_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QH, ID_171_R_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QK, ID_171_R_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_Q_, ID_171_R_))
        Assert.assertEquals(ID_171_R_, CodonCalculation.calculateCodon171(ID_171_RR, ID_171_R_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RH, ID_171_R_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RK, ID_171_R_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_R_, ID_171_R_))
        Assert.assertEquals(ID_171_H_, CodonCalculation.calculateCodon171(ID_171_HH, ID_171_R_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_HK, ID_171_R_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_H_, ID_171_R_))
        Assert.assertEquals(ID_171_K_, CodonCalculation.calculateCodon171(ID_171_KK, ID_171_R_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_K_, ID_171_R_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171___, ID_171_R_))

        Assert.assertEquals(ID_171_Q_, CodonCalculation.calculateCodon171(ID_171_QQ, ID_171_HK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QR, ID_171_HK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QH, ID_171_HK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QK, ID_171_HK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_Q_, ID_171_HK))
        Assert.assertEquals(ID_171_R_, CodonCalculation.calculateCodon171(ID_171_RR, ID_171_HK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RH, ID_171_HK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RK, ID_171_HK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_R_, ID_171_HK))
        Assert.assertEquals(ID_171_H_, CodonCalculation.calculateCodon171(ID_171_HH, ID_171_HK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_HK, ID_171_HK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_H_, ID_171_HK))
        Assert.assertEquals(ID_171_K_, CodonCalculation.calculateCodon171(ID_171_KK, ID_171_HK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_K_, ID_171_HK))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171___, ID_171_HK))

        Assert.assertEquals(ID_171_Q_, CodonCalculation.calculateCodon171(ID_171_QQ, ID_171_H_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QR, ID_171_H_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QH, ID_171_H_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QK, ID_171_H_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_Q_, ID_171_H_))
        Assert.assertEquals(ID_171_R_, CodonCalculation.calculateCodon171(ID_171_RR, ID_171_H_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RH, ID_171_H_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RK, ID_171_H_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_R_, ID_171_H_))
        Assert.assertEquals(ID_171_H_, CodonCalculation.calculateCodon171(ID_171_HH, ID_171_H_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_HK, ID_171_H_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_H_, ID_171_H_))
        Assert.assertEquals(ID_171_K_, CodonCalculation.calculateCodon171(ID_171_KK, ID_171_H_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_K_, ID_171_H_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171___, ID_171_H_))

        Assert.assertEquals(ID_171_Q_, CodonCalculation.calculateCodon171(ID_171_QQ, ID_171_K_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QR, ID_171_K_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QH, ID_171_K_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QK, ID_171_K_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_Q_, ID_171_K_))
        Assert.assertEquals(ID_171_R_, CodonCalculation.calculateCodon171(ID_171_RR, ID_171_K_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RH, ID_171_K_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RK, ID_171_K_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_R_, ID_171_K_))
        Assert.assertEquals(ID_171_H_, CodonCalculation.calculateCodon171(ID_171_HH, ID_171_K_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_HK, ID_171_K_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_H_, ID_171_K_))
        Assert.assertEquals(ID_171_K_, CodonCalculation.calculateCodon171(ID_171_KK, ID_171_K_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_K_, ID_171_K_))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171___, ID_171_K_))

        Assert.assertEquals(ID_171_Q_, CodonCalculation.calculateCodon171(ID_171_QQ, ID_171___))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QR, ID_171___))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QH, ID_171___))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_QK, ID_171___))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_Q_, ID_171___))
        Assert.assertEquals(ID_171_R_, CodonCalculation.calculateCodon171(ID_171_RR, ID_171___))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RH, ID_171___))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_RK, ID_171___))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_R_, ID_171___))
        Assert.assertEquals(ID_171_H_, CodonCalculation.calculateCodon171(ID_171_HH, ID_171___))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_HK, ID_171___))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_H_, ID_171___))
        Assert.assertEquals(ID_171_K_, CodonCalculation.calculateCodon171(ID_171_KK, ID_171___))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171_K_, ID_171___))
        Assert.assertEquals(ID_171___, CodonCalculation.calculateCodon171(ID_171___, ID_171___))
    }
}
