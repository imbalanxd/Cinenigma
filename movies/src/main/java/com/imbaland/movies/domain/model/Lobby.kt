package com.imbaland.movies.domain.model

import java.util.Date

data class Lobby(val title: String = "", val host: String = "", val isOpen: Boolean = false, val createdAt: Date? = null) {

}