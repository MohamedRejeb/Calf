package com.mohamedrejeb.calf.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.mohamedrejeb.calf.navigation.internal.stackWithDetail
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCalfNavigationApi::class)
class SelectDetailTest {

    private data class Key(val name: String) : NavKey

    @Test
    fun noDetailOpen_pushesTheDetail() {
        assertEquals(listOf("Home", "List", "Detail1"), stackWithDetail(listOf("Home", "List"), list = "List", detail = "Detail1"))
    }

    @Test
    fun detailSubStackOpen_isReplacedWithTheNewDetail() {
        assertEquals(
            listOf("Home", "List", "Detail2"),
            stackWithDetail(listOf("Home", "List", "Detail1", "Child"), list = "List", detail = "Detail2"),
        )
    }

    @Test
    fun sameDetailWithAChildOpen_dropsTheChild() {
        assertEquals(listOf("List", "Detail1"), stackWithDetail(listOf("List", "Detail1", "Child"), list = "List", detail = "Detail1"))
    }

    @Test
    fun sameDetailAlreadyOnTop_isUnchanged() {
        val stack = listOf("List", "Detail1")

        assertEquals(stack, stackWithDetail(stack, list = "List", detail = "Detail1"))
    }

    @Test
    fun listTwiceOnTheStack_usesTheTopOne() {
        assertEquals(
            listOf("List", "Detail1", "List", "Detail2"),
            stackWithDetail(listOf("List", "Detail1", "List", "Detail9"), list = "List", detail = "Detail2"),
        )
    }

    @Test
    fun listNotOnTheStack_justPushes() {
        assertEquals(listOf("Home", "Detail1"), stackWithDetail(listOf("Home"), list = "List", detail = "Detail1"))
    }

    @Test
    fun backStackExtension_appliesTheSameRule() {
        val backStack = NavBackStack<NavKey>(Key("List"), Key("Detail1"), Key("Child"))

        backStack.selectDetail(list = Key("List"), detail = Key("Detail2"))

        assertEquals(listOf<NavKey>(Key("List"), Key("Detail2")), backStack.toList())
    }

    @Test
    fun backStackExtension_keepsTheEntriesBelowUntouched() {
        val home = Key("Home")
        val backStack = NavBackStack<NavKey>(home, Key("List"), Key("Detail1"))

        backStack.selectDetail(list = Key("List"), detail = Key("Detail1"))

        assertEquals(listOf<NavKey>(home, Key("List"), Key("Detail1")), backStack.toList())
    }
}
