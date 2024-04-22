package com.imbaland.common.ui.util

class RouteUtils {
}

class NavRoute(
    private val base: String,
    private val pathParams: PathParams = PathParams(),
    private val queryParams: QueryParams = QueryParams()
) {
    init {
        if (base.isEmpty())
            throw IllegalArgumentException("No base route provided")
    }

    private val size = (pathParams.size) + (queryParams.size)
    operator fun invoke(vararg params: String?): String {
        if(params.isNotEmpty()) {
            if(params.size != size) {
                throw IndexOutOfBoundsException("Wrong number of parameters provided")
            }
            return base +
            pathParams(*params.sliceArray(0 until pathParams.size)) +
            queryParams(*params.sliceArray(pathParams.size until params.size))
        } else {
            return base + pathParams() + queryParams()
        }
    }
}

class QueryParams(vararg val template: String) {
    internal val size = template.size
    operator fun invoke(vararg params: String?): String {
        if (params.isNotEmpty() && params.size != template.size) {
            throw IndexOutOfBoundsException("Wrong number of parameters provided")
        }
        return if (params.isNotEmpty()) {
            template.zip(params).joinToString(separator = "") { paramPairs ->
                paramPairs.second?.let { value -> "?${paramPairs.first}=${value}" } ?: ""
            }
        } else {
            template.joinToString(separator = "") { templateName ->
                "?${templateName}={${templateName}}"
            }
        }
    }
}

class PathParams(vararg val template: String) {
    internal val size = template.size
    operator fun invoke(vararg params: String?): String {
        if (params.isNotEmpty() && (params.size != template.size || params.contains(null))) {
            throw IndexOutOfBoundsException("Path parameters are missing or null")
        }
        return if (params.isNotEmpty()) {
            params.joinToString(separator = "") {
                "/$it"
            }
        } else {
            template.joinToString(separator = "") {
                "/{$it}"
            }
        }
    }
}