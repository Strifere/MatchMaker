package com.example.generadordeemparejamientos.domain.classes

import java.io.Serializable
/**
 * Class that represents a player in the tournament.
 * @property name Nombre del jugador (player name)
 * @property pj Partidos jugados (matches played)
 * @property pg Partidos ganados (matches won)
 * @property pp Partidos perdidos (matches lost)
 * @property sf Sets a favor (sets won)
 * @property sc Sets en contra (sets lost)
 * @property pts Puntos (points, typically 2 for a win, 0 for a loss)
 */
class Player (
    val name: String = "",
    var pj: Int = 0,
    var pg: Int = 0,
    var pp: Int = 0,
    var sf: Int = 0,
    var sc: Int = 0,
    var pts: Int = 0
) : Serializable {

}