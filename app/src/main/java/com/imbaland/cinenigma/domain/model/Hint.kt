package com.imbaland.cinenigma.domain.model

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
                    data["movieId"] as Long,
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

    data class KeywordHint(
        val keyword: String = "",
        val start: Long = 0,
        val end: Long = 0
    ) : Hint()

    data class CastMovieHint(
        val castId: Long = -1,
        val movieId: Long = -1,
        val title: String = "",
        val poster: String = ""
    ) : Hint()
}

val Hint.KeywordHint.range: IntRange
    get() = IntRange(this.start.toInt(), this.end.toInt())

enum class HintType {
    Poster, Keyword, CastMovie, Empty
}

operator fun HintType.invoke(): String {
    return this.name
}