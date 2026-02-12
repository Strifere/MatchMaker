package com.example.generadordeemparejamientos.domain.classes

import java.io.Serializable
import java.util.SortedMap

/**
 * Class that represents the tournament.
 *
 * @property rounds List of rounds in the tournament, each round contains the matchups and results for that round.
 * @property numJugadores Number of players in the tournament.
 * @property nombres Array of player names, indexed by player number.
 * @property bestOf Number of maximum sets of a match. To win a match, a player has to win (bestOf / 2) + 1 sets (e.g., 2 for best of 3, 3 for best of 5).
 * @property includeSetResults Flag that indicates whether the tournament should keep track of individual set results for each match.
 * @property createdAt Timestamp (epoch millis) when the tournament was created.
 *
 * @constructor Initializes the tournament by generating the rounds and matchups based on the provided player names and matchup table.
 */
class Tournament (
    var name: String,
    var createdAt: Long = System.currentTimeMillis(),
    var modifiedAt: Long = System.currentTimeMillis(),
    var rounds: MutableList<Round> = mutableListOf(),
    val players: Array<Player>,
    val bestOf: Int,
    val includeSetResults : Boolean
) : Serializable {
    val numJugadores: Int
        get() = players.size

    val nombres: Array<String>
        get() = players.map { it.name }.toTypedArray()

    /**
     * Function that creates the data elements necessary for the tournament management and initializes them.
     * @param tabla A 2D list of integers representing the matchup table, where cell at row i and column j indicates the round number in which player i and player j will face each other.
     */
    fun initialize(tabla : List<List<Int>>) {
        val nRondas = if (numJugadores % 2 == 1) numJugadores else numJugadores - 1
        for (ronda in 0 until nRondas) {
            val usados = MutableList(numJugadores) { false }
            val matches = Array(numJugadores / 2) { Match(
                player1 = Player(name = ""),
                player2 = Player(name = ""),
                player1Sets = 0,
                player2Sets = 0,
                includeSetResults = includeSetResults
            ) }
            var matchIndex = 0
            for (i in 0 until numJugadores) {
                for (j in i + 1 until numJugadores) {
                    if (tabla[i][j] == ronda) {
                        usados[i] = true
                        usados[j] = true
                        matches[matchIndex].player1 = players[i]
                        matches[matchIndex].player2 = players[j]
                        ++matchIndex
                    }
                }
            }

            var libreIndex = -1
            for (i in 0 until numJugadores) {
                if (!usados[i]) {
                    libreIndex = i
                    break
                }
            }
            val libre = if (libreIndex != -1) players[libreIndex] else null
            rounds.add(Round(ronda + 1, libre, matches))
        }
    }

    /**
     * Generates a [Match] object for a given match, based on the provided player scores and set results. If the [includeSetResults] flag is true, it computes the overall match result from the set results and checks that it is consistent with the provided scores. If the flag is false, it simply creates the [Match] object with the provided scores.
      * @param player1Score Score of player 1 (number of sets won)
      * @param player2Score Score of player 2 (number of sets won)
      * @param sets Map of set index to [Set], representing the individual sets in the match
      * @return A [Match] object representing the match
     */
    fun generateMatchResult(player1: Player, player2: Player, player1Score : Int?, player2Score : Int?, sets: LinkedHashMap<Int, Set>): Match {
        // If the includeSetResults flag is true, it computes the overall result and checks that is correct
        if (includeSetResults) {
            var player1Sets = 0
            var player2Sets = 0
            for (i in 0 until sets.size) {
                val set = sets[i] ?: continue
                when (set.whoWonSet()) {
                    1 -> player1Sets++
                    2 -> player2Sets++
                    0 -> { /* Set not finished, do nothing */}
                }
            }
            return Match(
                player1 = player1,
                player2 = player2,
                player1Sets = player1Sets,
                player2Sets = player2Sets,
                includeSetResults = true,
                sets = sets
            )
        }
        // If the includeSetResults flag is false, it just creates the MatchResult object
        else {
            return Match(
                player1 = player1,
                player2 = player2,
                player1Sets = player1Score ?: 0,
                player2Sets = player2Score ?: 0,
                includeSetResults = false,
                sets = sets
            )
        }
    }

    /**
     * Computes the classification table for the tournament based on the match results recorded in the rounds. It calculates the statistics for each player (matches played, won, lost, sets for, sets against, and points) and sorts the players according to their points, set difference, sets for, and name. The resulting classification data is returned as a 2D array of strings, where each row corresponds to a player and contains their position, name, matches played, won, lost, sets for, sets against, and points.
      * @return A 2D array of strings representing the classification table of the tournament
     */
    fun computeClassificationData(): Array<Array<String>> {
        val table : Array<Array<String>> = Array(numJugadores) { Array(8) { "" } }

        // here we will update the stats for each player based on the match results in
        // tournament.rondas and generate the classification table
        val classification : SortedMap<String,Player> = sortedMapOf()
        updatePlayerStats()

        for (player in players) {
            insertIntoClassification(classification, player)
        }

        val data = classification.values.toTypedArray()
        val size = classification.size
        for (i in 0 until size) {
            val player = data[size-1 - i]
            table[i] = arrayOf(
                (i+1).toString(),
                player.name,
                player.pj.toString(),
                player.pg.toString(),
                player.pp.toString(),
                player.sf.toString(),
                player.sc.toString(),
                player.pts.toString()
            )
        }

        return table
    }

    /**
     * Updates the statistics of each player in the tournament.
     */
    fun updatePlayerStats() {
        val stats : SortedMap<String, Player> = initializeStats()
        for (ronda in rounds) {
            for (match in ronda.matches) {
                val player1name = match.player1.name
                val player2name = match.player2.name
                val player1score = match.player1Sets
                val player2score = match.player2Sets
                // Update sets for and against
                stats[player1name]?.sf += player1score
                stats[player1name]?.sc += player2score
                stats[player2name]?.sf += player2score
                stats[player2name]?.sc += player1score

                if (match.isMatchFinished(bestOf)) {
                    // Update matches played + 1 for both players
                    stats[player1name]?.pj += 1
                    stats[player2name]?.pj += 1
                    if (player1score > player2score) {
                        // player 1 wins
                        stats[player1name]?.pg += 1
                        stats[player2name]?.pp += 1
                        stats[player1name]?.pts += 2
                    } else if (player2score > player1score) {
                        // player 2 wins
                        stats[player2name]?.pg += 1
                        stats[player1name]?.pp += 1
                        stats[player2name]?.pts += 2
                    }
                }
            }
        }
        for (i in 0 until players.size) {
            val playerName = players[i].name
            val playerStats = stats[playerName]
            if (playerStats != null) {
                players[i].pj = playerStats.pj
                players[i].pg = playerStats.pg
                players[i].pp = playerStats.pp
                players[i].sf = playerStats.sf
                players[i].sc = playerStats.sc
                players[i].pts = playerStats.pts
            }
        }
    }

    /**
     * Initializes the statistics map for all players in the tournament. It creates a [SortedMap] where the keys are player names and the values are [Player] objects initialized with default values (0 matches played, won, lost, sets for, sets against, and points).
      * @return A [SortedMap] mapping player names to their corresponding [Player] objects
     */
    private fun initializeStats(): SortedMap<String, Player> {
        val stats : SortedMap<String, Player> = sortedMapOf()
        for (playerName in nombres) {
            stats[playerName] = Player(name = playerName)
        }
        return stats
    }

    /**
     * Inserts a player's statistics into the classification map. The classification is sorted by points, then by set difference, then by sets for, and finally alphabetically. The player's entry is formatted as "points-pg-sf-pp-[sf - sc]:playerName" to ensure correct sorting order in the [SortedMap].
      * @param classification The [SortedMap] representing the classification table, where player entries are stored as strings
      * @param player The object containing the info of the player (name, matches played, won, lost, sets for, sets against, and points)
     */
    private fun insertIntoClassification(classification: SortedMap<String,Player>, player: Player) {
        // We will sort the classification by points, then by set difference, then by sets for, and finally alphabetically
        val playerEntry = "${player.pts}-${player.pg}-${player.sf}-${player.pp}-${player.sf - player.sc}:${player.name}"
        classification[playerEntry] = player
    }
}
