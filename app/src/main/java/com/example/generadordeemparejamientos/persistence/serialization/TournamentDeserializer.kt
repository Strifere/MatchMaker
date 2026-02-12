package com.example.generadordeemparejamientos.persistence.serialization

import com.example.generadordeemparejamientos.domain.classes.Match
import com.example.generadordeemparejamientos.domain.classes.Player
import com.example.generadordeemparejamientos.domain.classes.Round
import com.example.generadordeemparejamientos.domain.classes.Set
import com.example.generadordeemparejamientos.domain.classes.Tournament
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Custom Gson deserializer for Tournament that ensures all Player references
 * throughout the tournament structure point to the same Player instances
 * (the ones in the players array), rather than creating new instances.
 */
class TournamentDeserializer : JsonDeserializer<Tournament> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Tournament {
        val jsonObject = json.asJsonObject

        // First, deserialize the players array
        val playersJson = jsonObject.getAsJsonArray("players")
        val players = Array(playersJson.size()) { index ->
            context.deserialize<Player>(playersJson[index], Player::class.java)
        }

        // Create a map of player names to player instances for reference resolution
        val playerMap = players.associateBy { it.name }

        // Deserialize rounds
        val roundsJson = jsonObject.getAsJsonArray("rounds")
        val rounds = mutableListOf<Round>()

        roundsJson.forEach { roundElement ->
            val roundObj = roundElement.asJsonObject
            val numero = roundObj.get("numero").asInt

            // Deserialize libre (may be null)
            val libreJson = roundObj.get("libre")
            val libre = if (libreJson != null && !libreJson.isJsonNull) {
                val libreName = libreJson.asJsonObject.get("name").asString
                playerMap[libreName] ?: context.deserialize(libreJson, Player::class.java)
            } else {
                null
            }

            // Deserialize matches and fix player references
            val matchesJson = roundObj.getAsJsonArray("matches")
            val matches = Array(matchesJson.size()) { index ->
                val matchJson = matchesJson[index].asJsonObject

                // Get player names and retrieve from playerMap
                val player1Name = matchJson.getAsJsonObject("player1").get("name").asString
                val player2Name = matchJson.getAsJsonObject("player2").get("name").asString

                val player1 = playerMap[player1Name] ?: context.deserialize(
                    matchJson.get("player1"),
                    Player::class.java
                )
                val player2 = playerMap[player2Name] ?: context.deserialize(
                    matchJson.get("player2"),
                    Player::class.java
                )

                // Get other match data
                val player1Sets = matchJson.get("player1Sets").asInt
                val player2Sets = matchJson.get("player2Sets").asInt
                val includeSetResults = matchJson.get("includeSetResults").asBoolean

                // Deserialize sets
                val setsJson = matchJson.getAsJsonObject("sets")
                val sets = linkedMapOf<Int, Set>()

                setsJson.entrySet().forEach { (key, value) ->
                    val setObj = value.asJsonObject
                    val set = Set(
                        setObj.get("player1Points").asInt,
                        setObj.get("player2Points").asInt
                    )
                    sets[key.toInt()] = set
                }

                // Create match with correct player references
                Match(
                    player1 = player1,
                    player2 = player2,
                    player1Sets = player1Sets,
                    player2Sets = player2Sets,
                    includeSetResults = includeSetResults,
                    sets = sets
                )
            }

            rounds.add(Round(numero, libre, matches))
        }

        // Create and return the tournament with correct references
        return Tournament(
            name = jsonObject.get("name").asString,
            rounds = rounds,
            players = players,
            bestOf = jsonObject.get("bestOf").asInt,
            includeSetResults = jsonObject.get("includeSetResults").asBoolean
        )
    }
}

