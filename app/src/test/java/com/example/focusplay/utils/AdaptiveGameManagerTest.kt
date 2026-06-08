package com.example.focusplay.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class AdaptiveGameManagerTest {

    @Test
    fun modeAdaptifNaikSetelahLimaJawabanBenar() {
        val manager = AdaptiveGameManager(faseSekarang = 1, modeAdaptifAktif = true)

        repeat(4) {
            assertEquals(1, manager.prosesJawaban(jawabanBenar = true))
        }

        assertEquals(2, manager.prosesJawaban(jawabanBenar = true))
    }

    @Test
    fun modeAdaptifTurunSetelahTigaJawabanSalah() {
        val manager = AdaptiveGameManager(faseSekarang = 2, modeAdaptifAktif = true)

        repeat(2) {
            assertEquals(2, manager.prosesJawaban(jawabanBenar = false))
        }

        assertEquals(1, manager.prosesJawaban(jawabanBenar = false))
    }

    @Test
    fun hitunganBenarDanSalahTidakHarusBeruntun() {
        val manager = AdaptiveGameManager(faseSekarang = 1, modeAdaptifAktif = true)

        repeat(4) {
            manager.prosesJawaban(jawabanBenar = true)
        }
        manager.prosesJawaban(jawabanBenar = false)

        assertEquals(2, manager.prosesJawaban(jawabanBenar = true))
    }

    @Test
    fun modeNonAdaptifSelaluTetapDiFaseSatu() {
        val manager = AdaptiveGameManager(faseSekarang = 3, modeAdaptifAktif = false)

        repeat(10) {
            assertEquals(1, manager.prosesJawaban(jawabanBenar = true))
            assertEquals(1, manager.prosesJawaban(jawabanBenar = false))
        }

        assertEquals(1, manager.getFaseSekarang())
    }
}
