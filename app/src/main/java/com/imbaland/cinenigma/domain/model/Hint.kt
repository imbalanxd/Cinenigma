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
                    data["topLeft"] as Pair<Float, Float>,
                    data["bottomRight"] as Pair<Float, Float>,
                    data["blurValue"] as Float
                )
            }

            HintType.Keyword -> {
                Hint.KeywordHint(
                    data["keyword"] as String,
                    data["start"] as Long,
                    data["end"] as Long
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
                        "end" to this.end)
                )
            }

            is PosterHint -> {
                HintRound(
                    HintType.Poster(),
                    mapOf(
                        "url" to this.url,
                        "topLeft" to this.topLeft,
                        "bottomRight" to this.bottomRight,
                        "blurValue" to this.blurValue
                    )
                )
            }
        }
    }

    data object EmptyHint : Hint()
    data class PosterHint(
        val url: String = "",
        val topLeft: Pair<Float, Float> = 0f to 0f,
        val bottomRight: Pair<Float, Float> = 1f to 1f,
        val blurValue: Float = 1f
    ) : Hint()

    data class KeywordHint(val keyword: String = "", val start: Long = 0, val end: Long = 0) : Hint()
}

enum class HintType {
    Poster, Keyword, Empty
}

operator fun HintType.invoke(): String {
    return this.name
}