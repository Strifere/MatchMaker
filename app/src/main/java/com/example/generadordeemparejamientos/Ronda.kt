package com.example.generadordeemparejamientos

import java.io.Serializable

class Ronda(
    val numero: Int,
    val emparejamientos: List<Pair<String, String>>,
    val libre: String?,
    val resultados: MutableMap<Pair<String, String>, MatchResult> = mutableMapOf()
) : Serializable {



}