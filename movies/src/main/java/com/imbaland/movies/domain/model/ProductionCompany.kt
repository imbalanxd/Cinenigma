package com.imbaland.movies.domain.model

data class ProductionCompany(
    val id: Int = 0,
    val logo_path: String? = null,
    val name: String = "",
    val origin_country: String = ""
)