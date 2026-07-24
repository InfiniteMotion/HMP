package com.hmp.desktop.ui.common.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopNavControllerTest {

    private fun createController(): NavController {
        return NavController(Routes.Main.Tabs)
    }

    @Test
    fun initialRoute_isCorrect() {
        val controller = createController()
        assertEquals(Routes.Main.Tabs, controller.currentRoute)
    }

    @Test
    fun initialSize_isOne() {
        val controller = createController()
        assertEquals(1, controller.size)
    }

    @Test
    fun canPop_initiallyFalse() {
        val controller = createController()
        assertFalse(controller.canPop())
    }

    @Test
    fun navigate_addsRoute() {
        val controller = createController()
        controller.navigate(Routes.Library.Search)
        assertEquals(Routes.Library.Search, controller.currentRoute)
        assertEquals(2, controller.size)
    }

    @Test
    fun navigate_duplicateRoute_doesNotAdd() {
        val controller = createController()
        controller.navigate(Routes.Library.Search)
        controller.navigate(Routes.Library.Search)
        assertEquals(2, controller.size)
    }

    @Test
    fun navigateReplace_replacesTop() {
        val controller = createController()
        controller.navigate(Routes.Library.Search)
        controller.navigateReplace(Routes.Player.Player)
        assertEquals(Routes.Player.Player, controller.currentRoute)
        assertEquals(2, controller.size)
    }

    @Test
    fun navigateSingleTop_alreadyOnTop_doesNothing() {
        val controller = createController()
        val initialSize = controller.size
        controller.navigateSingleTop(Routes.Main.Tabs)
        assertEquals(initialSize, controller.size)
    }

    @Test
    fun navigateSingleTop_existingRoute_popsBack() {
        val controller = createController()
        controller.navigate(Routes.Library.Search)
        controller.navigate(Routes.Player.Player)
        assertEquals(3, controller.size)
        controller.navigateSingleTop(Routes.Library.Search)
        assertEquals(Routes.Library.Search, controller.currentRoute)
        assertEquals(2, controller.size)
    }

    @Test
    fun navigateSingleTop_newRoute_adds() {
        val controller = createController()
        controller.navigateSingleTop(Routes.Player.Player)
        assertEquals(Routes.Player.Player, controller.currentRoute)
        assertEquals(2, controller.size)
    }

    @Test
    fun popBackStack_removesTop() {
        val controller = createController()
        controller.navigate(Routes.Library.Search)
        val result = controller.popBackStack()
        assertTrue(result)
        assertEquals(Routes.Main.Tabs, controller.currentRoute)
    }

    @Test
    fun popBackStack_singleItem_returnsTrue() {
        // popBackStack returns true when stack is not empty
        val controller = createController()
        val result = controller.popBackStack()
        assertTrue(result)
    }

    @Test
    fun popBackStackTo_removesAboveTarget() {
        val controller = createController()
        controller.navigate(Routes.Library.Search)
        controller.navigate(Routes.Player.Player)
        controller.navigate(Routes.AI.AI)
        val result = controller.popBackStackTo(Routes.Library.Search)
        assertTrue(result)
        assertEquals(Routes.Library.Search, controller.currentRoute)
    }

    @Test
    fun popBackStackTo_inclusive_removesTarget() {
        val controller = createController()
        controller.navigate(Routes.Library.Search)
        controller.navigate(Routes.Player.Player)
        val result = controller.popBackStackTo(Routes.Library.Search, inclusive = true)
        assertTrue(result)
        assertEquals(Routes.Main.Tabs, controller.currentRoute)
    }

    @Test
    fun popBackStackTo_notFound_returnsFalse() {
        val controller = createController()
        val result = controller.popBackStackTo(Routes.Player.Player)
        assertFalse(result)
    }

    @Test
    fun clearBackStack_keepsBottom() {
        val controller = createController()
        controller.navigate(Routes.Library.Search)
        controller.navigate(Routes.Player.Player)
        controller.clearBackStack()
        assertEquals(1, controller.size)
        assertEquals(Routes.Main.Tabs, controller.currentRoute)
    }

    @Test
    fun contains_existingRoute_returnsTrue() {
        val controller = createController()
        controller.navigate(Routes.Library.Search)
        assertTrue(controller.contains(Routes.Main.Tabs))
        assertTrue(controller.contains(Routes.Library.Search))
    }

    @Test
    fun contains_missingRoute_returnsFalse() {
        val controller = createController()
        assertFalse(controller.contains(Routes.Player.Player))
    }

    @Test
    fun canPop_afterNavigation_returnsTrue() {
        val controller = createController()
        controller.navigate(Routes.Library.Search)
        assertTrue(controller.canPop())
    }

    @Test
    fun lastOrNull_returnsTop() {
        val controller = createController()
        assertEquals(Routes.Main.Tabs, controller.lastOrNull())
    }

    @Test
    fun none_withPredicate() {
        val controller = createController()
        assertTrue(controller.none { it is Routes.Player.Player })
        assertFalse(controller.none { it is Routes.Main.Tabs })
    }

    @Test
    fun navigate_multipleRoutes_stackGrows() {
        val controller = createController()
        controller.navigate(Routes.Library.Search)
        controller.navigate(Routes.Player.Player)
        controller.navigate(Routes.AI.AI)
        assertEquals(4, controller.size)
        assertEquals(Routes.AI.AI, controller.currentRoute)
    }

    @Test
    fun clearBackStack_onSingleItem_doesNothing() {
        val controller = createController()
        controller.clearBackStack()
        assertEquals(1, controller.size)
    }
}