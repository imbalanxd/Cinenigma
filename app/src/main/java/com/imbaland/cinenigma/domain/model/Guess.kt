package com.imbaland.cinenigma.domain.model

import com.imbaland.movies.domain.model.Movie

sealed class Guess: GameRound("Guess") {
    data object Holder: Guess()

    data class MovieGuess(val movie: Movie = Movie()): Guess() {
        override fun toString(): String {
            return movie.title
        }
    }
    val MovieGuess.poster: String
        get() = this.movie.image
    val MovieGuess.id: Int
        get() = this.movie.id
}