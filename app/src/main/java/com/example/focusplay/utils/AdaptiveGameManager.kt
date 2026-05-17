package com.example.focusplay.utils

class AdaptiveGameManager(
    private var faseSekarang: Int = 1,
    private val modeAdaptifAktif: Boolean = true
) {

    private var benarBeruntun = 0
    private var salahBeruntun = 0

    fun prosesJawaban(jawabanBenar: Boolean): Int {
        if (!modeAdaptifAktif) {
            return faseSekarang
        }

        if (jawabanBenar) {
            benarBeruntun++
            salahBeruntun = 0

            if (benarBeruntun >= 5) {
                naikFase()
                benarBeruntun = 0
            }
        } else {
            salahBeruntun++
            benarBeruntun = 0

            if (salahBeruntun >= 3) {
                turunFase()
                salahBeruntun = 0
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
        benarBeruntun = 0
        salahBeruntun = 0
    }
}