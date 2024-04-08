package com.imbaland.cinenigma.domain.model

import com.imbaland.movies.domain.model.Movie

data class Game(val movie: Movie? = null, val completed: Boolean = false) {
}