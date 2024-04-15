package com.imbaland.common.ui.util

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.assertThrows

class NavRouteTest {
    @Test fun navRoute_CreateRoute_ValidNavRoute() {
        val route = NavRoute("base",
            PathParams("path1","path2"),
            QueryParams("query1","query2"))
        assertEquals("base/value1/value2?query1=value3?query2=value4", route("value1","value2","value3","value4"))
    }
    @Test fun navRoute_CreateRoute_StructNavRoute() {
        val route = NavRoute("base",
            PathParams("path1","path2"),
            QueryParams("query1","query2"))
        assertEquals("base/{path1}/{path2}?query1={query1}?query2={query2}", route())
    }

    @Test fun navRoute_CreateRoute_BaseNavRoute() {
        val route = NavRoute("base")
        assertEquals("base", route())
    }

    @Test fun navRoute_CreateRoute_InvalidNavRoute() {
        assertThrows<IllegalArgumentException> {
            val route = NavRoute("", PathParams(), QueryParams())
        }
    }

    @Test fun navRoute_CreateRoute_InvalidParams() {
        val route = NavRoute("base", PathParams("path1","path2"), QueryParams("query1"))
        assertThrows<IndexOutOfBoundsException> {
            route("value2","value2")
        }
    }

    @Test fun navRoute_CreateRoute_ValidNullParams() {
        val route = NavRoute("base", PathParams("path1","path2"), QueryParams("query1"))
        assertEquals("base/value1/value2", route("value1","value2",null))
    }
}