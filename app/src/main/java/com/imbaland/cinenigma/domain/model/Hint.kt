package com.imbaland.cinenigma.domain.model

import android.graphics.RectF
import com.imbaland.movies.domain.model.Movie
import com.imbaland.movies.domain.model.Person

data class HintRound(
    override val type: String = HintType.Keyword(),
    val data: Map<String, Any> = mapOf()
) : GameRound() {
    fun hint(): Hint {
        return when (HintType.valueOf(type)) {
            HintType.Poster -> {
                Hint.PosterHint(
                    data["url"] as String,
                    (data["x"] as Double).toFloat(),
                    (data["y"] as Double).toFloat(),
                    (data["size"] as Double).toFloat(),
                    (data["blurValue"] as Double).toFloat()
                )
            }

            HintType.Keyword -> {
                Hint.KeywordHint(
                    data["keyword"] as String,
                    data["start"] as Long,
                    data["end"] as Long
                )
            }

            HintType.CastMovie -> {
                Hint.CastMovieHint(
                    data["castId"] as Long,
                    data["castCount"] as Long,
                    data["movieId"] as Long,
                    data["movieCount"] as Long,
                    data["title"] as String,
                    data["poster"] as String
                )
            }

            HintType.Empty -> Hint.EmptyHint
        }
    }
}

sealed class Hint {
    fun toRound(): HintRound {
        return when (this) {
            EmptyHint -> {
                HintRound(HintType.Empty(), mapOf())
            }

            is KeywordHint -> {
                HintRound(
                    HintType.Keyword(),
                    mapOf(
                        "keyword" to this.keyword,
                        "start" to this.start,
                        "end" to this.end
                    )
                )
            }

            is PosterHint -> {
                HintRound(
                    HintType.Poster(),
                    mapOf(
                        "url" to this.url,
                        "x" to this.x,
                        "y" to this.y,
                        "size" to this.size,
                        "blurValue" to this.blurValue
                    )
                )
            }

            is CastMovieHint -> {
                HintRound(
                    HintType.CastMovie(),
                    mapOf(
                        "castId" to this.castId,
                        "movieId" to this.movieId,
                        "castCount" to this.castCount,
                        "movieCount" to this.movieCount,
                        "title" to this.title,
                        "poster" to this.poster,
                    )
                )
            }
        }
    }

    data object EmptyHint : Hint()
    data class PosterHint(
        val url: String = "",
        val x: Float = 0f,
        val y: Float = 0f,
        val size: Float = 0f,
        val blurValue: Float = 1f
    ) : Hint()

    fun PosterHint.toRectF(): RectF {
        return RectF(this.x,this.y,this.x +this.size,this.y +this.size)
    }

    data class KeywordHint(
        val keyword: String = "",
        val start: Long = 0,
        val end: Long = 0
    ) : Hint()

    data class CastMovieHint(
        val castId: Long = -1,
        val castCount: Long = 0,
        val movieId: Long = -1,
        val movieCount: Long = 0,
        val title: String = "",
        val poster: String = ""
    ) : Hint()
}

enum class HintType {
    Poster, Keyword, CastMovie, Empty
}
fun HintRound.toType(): HintType {
    return when(this.hint()) {
        is Hint.CastMovieHint -> HintType.CastMovie
        Hint.EmptyHint -> HintType.Empty
        is Hint.KeywordHint -> HintType.Keyword
        is Hint.PosterHint -> HintType.Poster
    }
}

val Hint.KeywordHint.range: IntRange
    get() = IntRange(this.start.toInt(), this.end.toInt())

operator fun HintType.invoke(): String {
    return this.name
}

fun List<Hint.CastMovieHint>.createDisplay(): List<Person> {
    return this.firstOrNull()?.let { first ->
        List(first.castCount.toInt()) { castId ->
            this.filter { it.castId == castId.toLong() }.let { credits ->
                Person(filmography = List(first.movieCount.toInt()) { movieId ->
                    credits.find { it.movieId == movieId.toLong() }?.let { hint ->
                        Movie(title = hint.title, image = hint.poster)
                    }?:Movie()
                })
            }
        }
    } ?: listOf()
}