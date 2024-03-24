package com.imbaland.movies.domain.model

data class SearchResult<MediaType>(
    val page: Int,
    val results: List<MediaType>
)