package com.example.generadordeemparejamientos.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.generadordeemparejamientos.R
import com.example.generadordeemparejamientos.domain.classes.Player
import com.example.generadordeemparejamientos.domain.classes.Round
import com.example.generadordeemparejamientos.domain.classes.Set
import com.example.generadordeemparejamientos.domain.classes.Tournament
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Extension function to await the result of an AlertDialog button click in a suspend function.
 * This allows us to use AlertDialogs in a more coroutine-friendly way, without blocking the main thread.
 */
suspend fun AlertDialog.await(tournament : Tournament, round : Round, player1 : Player, player2 : Player, matchInputView : View, context: Context) = suspendCancellableCoroutine { cont ->
    val listener = DialogInterface.OnClickListener { _, which ->
        if (which == AlertDialog.BUTTON_POSITIVE) {
            val error = saveMatchResult(tournament, round, player1, player2, matchInputView, context)
            cont.resume(error)
        }
        else if (which == AlertDialog.BUTTON_NEGATIVE) cont.resume(false)
    }

    setButton(AlertDialog.BUTTON_POSITIVE, "Guardar", listener)
    setButton(AlertDialog.BUTTON_NEGATIVE, "Cancelar", listener)

    // we can either decide to cancel the coroutine if the dialog
    // itself gets cancelled, or resume the coroutine with the
    // value [false]
    setOnCancelListener { cont.cancel() }

    // if we make this coroutine cancellable, we should also close the
    // dialog when the coroutine is cancelled
    cont.invokeOnCancellation { dismiss() }

    // remember to show the dialog before returning from the block,
    // you won't be able to do it after this function is called!
    show()
}

/**
 * Helper function to show a dialog for inputting match results, including set scores if the tournament is configured to include them.
 * @param tournament The tournament for which the results are being inputted, used to determine if set results should be included and to validate the input.
 * @param round The current round, used to save the results in the correct place.
 * @param player1 The first player, used to display in the dialog and to save the results.
 * @param player2 The second player, used to display in the dialog and to save the results.
 * @param context The context in which to show the dialog, used to inflate the layout and to show Toast messages for validation errors.
 * @return true if the results were successfully saved, false if the user cancelled or if there was a validation error with the input.
 */
suspend fun showMatchInputDialog(tournament : Tournament, round: Round, player1: Player, player2: Player, context: Context) : Boolean {
    val inflater = LayoutInflater.from(context)
    val dialogView = inflater.inflate(R.layout.dialog_input_results, null)
    val matchesContainer = dialogView.findViewById<LinearLayout>(R.id.resultsMatchesContainer)

    val matchInputView = inflater.inflate(R.layout.item_result_input, matchesContainer, false)
    val setInputLayout = matchInputView.findViewById<LinearLayout>(R.id.setLayout)
    val player1Name = matchInputView.findViewById<TextView>(R.id.resultPlayer1Name)
    val player2Name = matchInputView.findViewById<TextView>(R.id.resultPlayer2Name)
    val player1Score = matchInputView.findViewById<EditText>(R.id.resultPlayer1Score)
    val player2Score = matchInputView.findViewById<EditText>(R.id.resultPlayer2Score)
    val setsContainer = matchInputView.findViewById<LinearLayout>(R.id.setsContainer)

    player1Name.text = player1.name
    player2Name.text = player2.name

    val existingResult = round.getMatchByNames(player1.name, player2.name)
    if (existingResult != null) {
        player1Score.setText(existingResult.player1Sets.toString())
        player2Score.setText(existingResult.player2Sets.toString())
    }

    if (tournament.includeSetResults) {
        setInputLayout.visibility = View.GONE
        for (setIndex in 0 until tournament.bestOf) {
            val setView = inflater.inflate(R.layout.item_set_input, setsContainer, false)
            val setTitle = setView.findViewById<TextView>(R.id.setTitle)
            val setPlayer1 = setView.findViewById<EditText>(R.id.setPlayer1Points)
            val setPlayer2 = setView.findViewById<EditText>(R.id.setPlayer2Points)

            setTitle.text = "Set ${setIndex + 1}"

            if (existingResult != null && existingResult.sets.containsKey(setIndex)) {
                val existingSet = existingResult.sets[setIndex]
                setPlayer1.setText(existingSet?.player1Points.toString())
                setPlayer2.setText(existingSet?.player2Points.toString())
            }

            setsContainer.addView(setView)
        }
    }

    matchesContainer.addView(matchInputView)

    return AlertDialog.Builder(context)
        .setTitle("Introducir resultados - Partido ${player1.name} - ${player2.name}")
        .setView(dialogView)
        .create().await(tournament, round, player1, player2, matchInputView, context)
}

/**
 * Helper function to save the match result entered in the dialog, including validation of the input according to the tournament rules.
 * @param tournament The tournament for which the results are being saved, used to validate the input according to the tournament rules.
 * @param round The current round, used to save the results in the correct place.
 * @param player1 The first player, used to save the results in the correct match.
 * @param player2 The second player, used to save the results in the correct match.
 * @param matchView The view containing the input fields for the match result, used to extract the entered scores and set results.
 * @param context The context used to show Toast messages for validation errors.
 * @return true if the result was successfully saved, false if there was a validation error with the input.
 */
private fun saveMatchResult(tournament: Tournament, round: Round, player1: Player, player2: Player, matchView: View, context: Context) : Boolean {
    val player1Score = matchView.findViewById<EditText>(R.id.resultPlayer1Score).text.toString().toIntOrNull() ?: 0
    val player2Score = matchView.findViewById<EditText>(R.id.resultPlayer2Score).text.toString().toIntOrNull() ?: 0

    val setResults = linkedMapOf<Int, Set>()
    if (tournament.includeSetResults) {
        val setsContainer = matchView.findViewById<LinearLayout>(R.id.setsContainer)
        for (j in 0 until setsContainer.childCount) {
            val setView = setsContainer.getChildAt(j)
            val setP1 = setView.findViewById<EditText>(R.id.setPlayer1Points).text.toString().toIntOrNull() ?: 0
            val setP2 = setView.findViewById<EditText>(R.id.setPlayer2Points).text.toString().toIntOrNull() ?: 0
            setResults[j] = Set(setP1, setP2)
        }
    }

    val result = tournament.generateMatchResult(player1, player2, player1Score, player2Score, setResults)
    // Save the result using the original match pair
    if (result.checkMatchResult(tournament.bestOf)) round.insertResult(result)
    else {
        Toast.makeText(context, "Resultado inválido según las reglas del torneo.", Toast.LENGTH_SHORT).show()
        return false
    }
    return true
}

/**
 * Helper function to check if the device is currently in dark mode, used to adjust the dialog theme accordingly.
 * @param context The context used to access the current configuration and resources.
 * @return true if dark mode is currently enabled, false otherwise.
 */
fun isDarkModeEnabled(context : Context): Boolean {
    val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
}