package com.imbaland.cinenigma.domain.model

sealed class Guess: GameRound() {
    data object Holder: Guess()

    data class TitleGuess(val title: String): Guess() {
        override fun toString(): String {
            return title
        }
    }
}