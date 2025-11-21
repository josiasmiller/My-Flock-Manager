package com.weyr_associates.animaltrakkerfarmmobile.model

import org.junit.Assert
import org.junit.Test

class CodonCharacteristicTest {

    @Test
    fun exercise_coerceToCodon136() {
        Assert.assertEquals(CodonCharacteristic.ID_136_AA, CodonCharacteristic.coerceToCodon136(CodonCharacteristic.ID_136_AA))
        Assert.assertEquals(CodonCharacteristic.ID_136_VV, CodonCharacteristic.coerceToCodon136(CodonCharacteristic.ID_136_VV))
        Assert.assertEquals(CodonCharacteristic.ID_136_AV, CodonCharacteristic.coerceToCodon136(CodonCharacteristic.ID_136_AV))
        Assert.assertEquals(CodonCharacteristic.ID_136_A_, CodonCharacteristic.coerceToCodon136(CodonCharacteristic.ID_136_A_))
        Assert.assertEquals(CodonCharacteristic.ID_136_V_, CodonCharacteristic.coerceToCodon136(CodonCharacteristic.ID_136_V_))
        Assert.assertEquals(CodonCharacteristic.ID_136___, CodonCharacteristic.coerceToCodon136(CodonCharacteristic.ID_136___))
        Assert.assertEquals(CodonCharacteristic.ID_136___, CodonCharacteristic.coerceToCodon136(EntityId("00000000-0000-0000-0000-000000000000")))
        Assert.assertEquals(CodonCharacteristic.ID_136___, CodonCharacteristic.coerceToCodon136(null))
    }

    @Test
    fun exercise_coerceToCodon141() {
        Assert.assertEquals(CodonCharacteristic.ID_141___, CodonCharacteristic.coerceToCodon141(CodonCharacteristic.ID_141___))
        Assert.assertEquals(CodonCharacteristic.ID_141_FF, CodonCharacteristic.coerceToCodon141(CodonCharacteristic.ID_141_FF))
        Assert.assertEquals(CodonCharacteristic.ID_141_F_, CodonCharacteristic.coerceToCodon141(CodonCharacteristic.ID_141_F_))
        Assert.assertEquals(CodonCharacteristic.ID_141_FL, CodonCharacteristic.coerceToCodon141(CodonCharacteristic.ID_141_FL))
        Assert.assertEquals(CodonCharacteristic.ID_141_L_, CodonCharacteristic.coerceToCodon141(CodonCharacteristic.ID_141_L_))
        Assert.assertEquals(CodonCharacteristic.ID_141_LL, CodonCharacteristic.coerceToCodon141(CodonCharacteristic.ID_141_LL))
        Assert.assertEquals(CodonCharacteristic.ID_141___, CodonCharacteristic.coerceToCodon141(EntityId("00000000-0000-0000-0000-000000000000")))
        Assert.assertEquals(CodonCharacteristic.ID_141___, CodonCharacteristic.coerceToCodon141(null))
    }

    @Test
    fun exercise_coerceToCodon154() {
        Assert.assertEquals(CodonCharacteristic.ID_154___, CodonCharacteristic.coerceToCodon154(CodonCharacteristic.ID_154___))
        Assert.assertEquals(CodonCharacteristic.ID_154_RR, CodonCharacteristic.coerceToCodon154(CodonCharacteristic.ID_154_RR))
        Assert.assertEquals(CodonCharacteristic.ID_154_RH, CodonCharacteristic.coerceToCodon154(CodonCharacteristic.ID_154_RH))
        Assert.assertEquals(CodonCharacteristic.ID_154_R_, CodonCharacteristic.coerceToCodon154(CodonCharacteristic.ID_154_R_))
        Assert.assertEquals(CodonCharacteristic.ID_154_HH, CodonCharacteristic.coerceToCodon154(CodonCharacteristic.ID_154_HH))
        Assert.assertEquals(CodonCharacteristic.ID_154_H_, CodonCharacteristic.coerceToCodon154(CodonCharacteristic.ID_154_H_))
        Assert.assertEquals(CodonCharacteristic.ID_154___, CodonCharacteristic.coerceToCodon154(EntityId("00000000-0000-0000-0000-000000000000")))
        Assert.assertEquals(CodonCharacteristic.ID_154___, CodonCharacteristic.coerceToCodon154(null))
    }

    @Test
    fun exercise_coerceToCodon171() {
        Assert.assertEquals(CodonCharacteristic.ID_171_QQ, CodonCharacteristic.coerceToCodon171(CodonCharacteristic.ID_171_QQ))
        Assert.assertEquals(CodonCharacteristic.ID_171_Q_, CodonCharacteristic.coerceToCodon171(CodonCharacteristic.ID_171_Q_))
        Assert.assertEquals(CodonCharacteristic.ID_171_QR, CodonCharacteristic.coerceToCodon171(CodonCharacteristic.ID_171_QR))
        Assert.assertEquals(CodonCharacteristic.ID_171_R_, CodonCharacteristic.coerceToCodon171(CodonCharacteristic.ID_171_R_))
        Assert.assertEquals(CodonCharacteristic.ID_171_RR, CodonCharacteristic.coerceToCodon171(CodonCharacteristic.ID_171_RR))
        Assert.assertEquals(CodonCharacteristic.ID_171___, CodonCharacteristic.coerceToCodon171(CodonCharacteristic.ID_171___))
        Assert.assertEquals(CodonCharacteristic.ID_171_HH, CodonCharacteristic.coerceToCodon171(CodonCharacteristic.ID_171_HH))
        Assert.assertEquals(CodonCharacteristic.ID_171_QH, CodonCharacteristic.coerceToCodon171(CodonCharacteristic.ID_171_QH))
        Assert.assertEquals(CodonCharacteristic.ID_171_H_, CodonCharacteristic.coerceToCodon171(CodonCharacteristic.ID_171_H_))
        Assert.assertEquals(CodonCharacteristic.ID_171_RH, CodonCharacteristic.coerceToCodon171(CodonCharacteristic.ID_171_RH))
        Assert.assertEquals(CodonCharacteristic.ID_171_KK, CodonCharacteristic.coerceToCodon171(CodonCharacteristic.ID_171_KK))
        Assert.assertEquals(CodonCharacteristic.ID_171_QK, CodonCharacteristic.coerceToCodon171(CodonCharacteristic.ID_171_QK))
        Assert.assertEquals(CodonCharacteristic.ID_171_RK, CodonCharacteristic.coerceToCodon171(CodonCharacteristic.ID_171_RK))
        Assert.assertEquals(CodonCharacteristic.ID_171_K_, CodonCharacteristic.coerceToCodon171(CodonCharacteristic.ID_171_K_))
        Assert.assertEquals(CodonCharacteristic.ID_171_HK, CodonCharacteristic.coerceToCodon171(CodonCharacteristic.ID_171_HK))
        Assert.assertEquals(CodonCharacteristic.ID_171___, CodonCharacteristic.coerceToCodon171(EntityId("00000000-0000-0000-0000-000000000000")))
        Assert.assertEquals(CodonCharacteristic.ID_171___, CodonCharacteristic.coerceToCodon171(null))
    }
}
