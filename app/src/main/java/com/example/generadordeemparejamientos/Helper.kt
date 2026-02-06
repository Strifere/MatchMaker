package com.example.generadordeemparejamientos

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend fun AlertDialog.await(tournament : Tournament, ronda : Ronda, player1 : String, player2 : String, matchInputView : View, context: Context) = suspendCancellableCoroutine<Boolean> { cont ->
    val listener = DialogInterface.OnClickListener { _, which ->
        if (which == AlertDialog.BUTTON_POSITIVE) {
            val error = saveMatchResult(tournament, ronda, Pair(player1, player2) , matchInputView, context)
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

suspend fun showMatchInputDialog(tournament : Tournament, ronda: Ronda, player1: String, player2: String, context: Context) : Boolean {
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

    player1Name.text = player1
    player2Name.text = player2

    val existingResult = ronda.resultados[Pair(player1, player2)]
    if (existingResult != null) {
        player1Score.setText(existingResult.player1Score.toString())
        player2Score.setText(existingResult.player2Score.toString())
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
        .setTitle("Introducir resultados - Partido $player1 - $player2")
        .setView(dialogView)
        .create().await(tournament, ronda, player1, player2, matchInputView, context)
}

private fun saveMatchResult(tournament: Tournament, ronda: Ronda, matchPair: Pair<String, String>, matchView: View, context: Context) : Boolean {
    val player1Score = matchView.findViewById<EditText>(R.id.resultPlayer1Score).text.toString().toIntOrNull() ?: 0
    val player2Score = matchView.findViewById<EditText>(R.id.resultPlayer2Score).text.toString().toIntOrNull() ?: 0

    val setResults = linkedMapOf<Int, SetResult>()
    if (tournament.includeSetResults) {
        val setsContainer = matchView.findViewById<LinearLayout>(R.id.setsContainer)
        for (j in 0 until setsContainer.childCount) {
            val setView = setsContainer.getChildAt(j)
            val setP1 = setView.findViewById<EditText>(R.id.setPlayer1Points).text.toString().toIntOrNull() ?: 0
            val setP2 = setView.findViewById<EditText>(R.id.setPlayer2Points).text.toString().toIntOrNull() ?: 0
            setResults[j] = SetResult(setP1, setP2)
        }
    }

    val result = tournament.generateMatchResult(player1Score, player2Score, setResults)
    // Save the result using the original match pair
    if (result.checkMatchResult(tournament.bestOf)) ronda.resultados[matchPair] = result
    else {
        Toast.makeText(context, "Resultado inválido según las reglas del torneo.", Toast.LENGTH_SHORT).show()
        return false
    }
    return true
}

fun isDarkModeEnabled(context : Context): Boolean {
    val nightModeFlags = context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
    return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
}