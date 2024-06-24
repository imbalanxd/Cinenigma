package com.imbaland.movies.domain.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Person(
    override val id: Int = 0,
    override val name: String = "",
    override val popularity: Double = 0.0,
    val adult: Boolean = true,
    val gender: Int = 0,
    val cast_id: Int = 0,
    val known_for_department: String? = "",
    val department: String? = "",
    @get:Exclude val profile_path: String? = "",
    override val media_type: String = "person",
    override val image: String = if(profile_path.isNullOrBlank()) "" else "https://image.tmdb.org/t/p/w500$profile_path",
    @get:Exclude val movie_credits:MovieCredits = MovieCredits(),
    val filmography: List<Movie> = if(department == "Directing") movie_credits.directingCredits else movie_credits.actingCredits
): Media()

data class Credits(private val cast: List<Person> = listOf(), val crew: List<Person> = listOf()) {
    private val ACTOR_COUNT = 3
    val actors: List<Person> = cast.take(ACTOR_COUNT)
    val director: Person? = crew.firstOrNull { it.department == "Directing" }
}

data class MovieCredits(private val cast: List<Movie> = listOf(), val crew: List<Movie> = listOf()) {
    private val MOVIE_COUNT = 3
    val actingCredits: List<Movie> = cast.take(MOVIE_COUNT)
    val directingCredits: List<Movie> = crew.filter { it.department == "Directing" }.take(MOVIE_COUNT)
}