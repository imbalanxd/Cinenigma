package com.imbaland.cinenigma.domain.model

import com.imbaland.common.domain.auth.AuthenticatedUser
import com.imbaland.movies.domain.model.Movie
import com.imbaland.movies.domain.model.MovieDetails
import java.util.Date

data class Game(
    val movie: MovieDetails? = null,
    val startedAt: Date = Date(),
    val completed: Boolean = false,
    val hinter: AuthenticatedUser? = null,
    val hints: List<HintRound>? = null,
    val guesses: List<Guess.TitleGuess>? = null) {
    fun isHinter(userId: String): Boolean {
        return hinter?.id == userId
    }
    val round: Int
        get() = (hints?.size?:0) + (guesses?.size?:0)
    val currentRound: GameRound?
        get() = if(round%2 == 0) {
                guesses?.lastOrNull()
            } else {
                hints?.lastOrNull()
            }
    sealed class State(open val game: Game) {
        data class Loading(override val game: Game): State(game)
        data class Hinting(override val game: Game): State(game) {
            val remainingTime: Int
                get() = game.currentRound?.timeRemaining?:0
        }
        data class Guessing(override val game: Game): State(game) {
            val remainingTime: Int
                get() = game.currentRound?.timeRemaining?:0
        }
        data class Completed(override val game: Game): State(game)
    }
}

val Game.state: Game.State
    get() = when {
        movie == null -> {
            Game.State.Loading(this)
        }
        currentRound is Guess || currentRound == null -> {
            if(movie.title == (currentRound as? Guess.TitleGuess)?.title) {
                Game.State.Completed(this)
            } else {
                Game.State.Hinting(this)
            }
        }
        currentRound is HintRound -> {
            Game.State.Guessing(this)
        }
        else -> {
            Game.State.Completed(this)
        }
    }
