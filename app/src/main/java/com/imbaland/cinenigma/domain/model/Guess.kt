package com.imbaland.cinenigma.domain.model

sealed class Guess: GameRound("Guess") {
    data object Holder: Guess()

    data class TitleGuess(val title: String = ""): Guess() {
        override fun toString(): String {
            return title
        }
    }
}