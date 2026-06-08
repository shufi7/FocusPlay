package com.example.focusplay.utils

class AdaptiveGameManager(
    private var faseSekarang: Int = 1,
    private val modeAdaptifAktif: Boolean = true
) {

    private var jumlahBenar = 0
    private var jumlahSalah = 0

    init {
        if (!modeAdaptifAktif) {
            faseSekarang = 1
        }
    }

    fun prosesJawaban(jawabanBenar: Boolean): Int {
        if (!modeAdaptifAktif) {
            faseSekarang = 1
            return 1
        }

        if (jawabanBenar) {
            jumlahBenar++

            if (jumlahBenar >= 5) {
                naikFase()
                resetHitungan()
            }
        } else {
            jumlahSalah++

            if (jumlahSalah >= 3) {
                turunFase()
                resetHitungan()
            }
        }

        return faseSekarang
    }

    private fun naikFase() {
        if (faseSekarang < 3) {
            faseSekarang++
        }
    }

    private fun turunFase() {
        if (faseSekarang > 1) {
            faseSekarang--
        }
    }

    fun getFaseSekarang(): Int {
        return faseSekarang
    }

    fun resetHitungan() {
        jumlahBenar = 0
        jumlahSalah = 0
    }
}
