package com.imbaland.movies.domain.model

import com.google.firebase.firestore.Exclude

data class Person(
    val adult: Boolean = true,
    val gender: Int = 0,
    override val id: Int = 0,
    val known_for: List<Movie> = listOf(),
    val known_for_department: String? = "",
    val department: String? = "",
    override val name: String = "",
    val original_name: String? = "",
    override val popularity: Double = 0.0,
    val profile_path: String? = "",
    @Exclude override val media_type: String = "person",
    @Exclude override val image: String = "https://image.tmdb.org/t/p/w500$profile_path"
): Media()

data class Credits(private val cast: List<Person> = listOf(), val crew: List<Person> = listOf()) {
    private val ACTOR_COUNT = 3
    val actors: List<Person> = cast.take(ACTOR_COUNT)
    val director: Person? = crew.firstOrNull { it.department == "Directing" }
}