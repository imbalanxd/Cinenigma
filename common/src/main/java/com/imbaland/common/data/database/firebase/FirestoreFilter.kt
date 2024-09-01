package com.imbaland.common.data.database.firebase

import com.google.firebase.firestore.Filter

sealed class FirestoreFilter() {
    abstract fun build(): Filter

    override fun toString(): String {
        return "filter error string"
    }
}

sealed class FirestoreValueFilter(open val key: String, open val value: Any?, private val operator: String): FirestoreFilter() {
    class EqualsFilter(override val key: String, override val value: Any?): FirestoreValueFilter(key, value, "==")
    class NotEqualsFilter(override val key: String, override val value: Any?): FirestoreValueFilter(key, value, "!=")

    override fun build():Filter {
        return when(this) {
            is EqualsFilter -> {
                Filter.equalTo(this.key, this.value)
            }

            is NotEqualsFilter -> {
                Filter.notEqualTo(this.key, this.value)
            }
        }
    }

    override fun toString(): String {
        return "$key $operator $value"
    }
}

sealed class FirestoreFilterGroup(private val filters: List<FirestoreValueFilter>, private val concat: String): FirestoreFilter() {
    class AndFilterGroup(vararg filters: FirestoreValueFilter): FirestoreFilterGroup(filters.asList(), "and")
    class OrFilterGroup(vararg filters: FirestoreValueFilter): FirestoreFilterGroup(filters.asList(), "or")

    override fun build():Filter {
        return when(this) {
            is AndFilterGroup -> {
                Filter.and(*(filters.map { it.build() }.toTypedArray()))
            }

            is OrFilterGroup -> {
                Filter.or(*(filters.map { it.build() }.toTypedArray()))
            }
        }
    }

    override fun toString(): String {
        return filters.joinToString(separator = " $concat ") { it.toString() }
    }
}
