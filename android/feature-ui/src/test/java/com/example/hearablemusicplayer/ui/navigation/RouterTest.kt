package com.example.hearablemusicplayer.ui.navigation

import androidx.navigation3.runtime.NavBackStack
import io.mockk.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Router 单元测试
 * 测试 navigateTo、popBackStack 等导航方法
 */
class RouterTest {

    private lateinit var mockNavBackStack: NavBackStack
    private lateinit var router: Router

    @Before
    fun setUp() {
        mockNavBackStack = mockk()
        router = Router(mockNavBackStack)
    }

    @Test
    fun `navigateTo should call add on NavBackStack`() {
        // Given
        val route = Routes.Main.Tabs
        every { mockNavBackStack.add(route) } just runs

        // When
        router.navigateTo(route)

        // Then
        verify { mockNavBackStack.add(route) }
    }

    @Test
    fun `navigateReplace should remove last and add when stack not empty`() {
        // Given
        val route = Routes.Main.Tabs
        every { mockNavBackStack.size } returns 2
        every { mockNavBackStack.removeLastOrNull() } returns null
        every { mockNavBackStack.add(route) } just runs

        // When
        router.navigateReplace(route)

        // Then
        verifyOrder {
            mockNavBackStack.size
            mockNavBackStack.removeLastOrNull()
            mockNavBackStack.add(route)
        }
    }

    @Test
    fun `navigateReplace should only add when stack empty`() {
        // Given
        val route = Routes.Main.Tabs
        every { mockNavBackStack.size } returns 0
        every { mockNavBackStack.add(route) } just runs

        // When
        router.navigateReplace(route)

        // Then
        verify(exactly = 0) { mockNavBackStack.removeLastOrNull() }
        verify { mockNavBackStack.add(route) }
    }

    @Test
    fun `navigateSingleTop should not add if already on top`() {
        // Given
        val route = Routes.Main.Tabs
        every { mockNavBackStack.lastOrNull() } returns route
        every { mockNavBackStack.indexOfFirst(any()) } returns 0
        every { mockNavBackStack.size } returns 1

        // When
        router.navigateSingleTop(route)

        // Then
        verify(exactly = 0) { mockNavBackStack.add(any()) }
    }

    @Test
    fun `navigateSingleTop should remove existing route and add if not on top`() {
        // Given
        val route = Routes.Main.Tabs
        every { mockNavBackStack.lastOrNull() } returns Routes.Main.Home
        every { mockNavBackStack.indexOfFirst(any()) } returns 1
        every { mockNavBackStack.size } returns 3
        every { mockNavBackStack.removeLastOrNull() } returns null
        every { mockNavBackStack.add(route) } just runs

        // When
        router.navigateSingleTop(route)

        // Then
        verify(exactly = 1) { mockNavBackStack.removeLastOrNull() }
        verify { mockNavBackStack.add(route) }
    }

    @Test
    fun `navigateSingleTop should add new route if not in stack`() {
        // Given
        val route = Routes.Main.Tabs
        every { mockNavBackStack.lastOrNull() } returns Routes.Main.Home
        every { mockNavBackStack.indexOfFirst(any()) } returns -1
        every { mockNavBackStack.add(route) } just runs

        // When
        router.navigateSingleTop(route)

        // Then
        verify { mockNavBackStack.add(route) }
    }

    @Test
    fun `popBackStack should call removeLastOrNull and return true when successful`() {
        // Given
        every { mockNavBackStack.removeLastOrNull() } returns Routes.Main.Home

        // When
        val result = router.popBackStack()

        // Then
        assertTrue(result)
        verify { mockNavBackStack.removeLastOrNull() }
    }

    @Test
    fun `popBackStack should return false when stack empty`() {
        // Given
        every { mockNavBackStack.removeLastOrNull() } returns null

        // When
        val result = router.popBackStack()

        // Then
        assertFalse(result)
        verify { mockNavBackStack.removeLastOrNull() }
    }

    @Test
    fun `popBackStackTo should return false when route not found`() {
        // Given
        val route = Routes.Main.Tabs
        every { mockNavBackStack.indexOfFirst(any()) } returns -1

        // When
        val result = router.popBackStackTo(route)

        // Then
        assertFalse(result)
    }

    @Test
    fun `popBackStackTo should remove routes above target when inclusive false`() {
        // Given
        val route = Routes.Main.Tabs
        every { mockNavBackStack.indexOfFirst(any()) } returns 2
        every { mockNavBackStack.size } returns 5
        every { mockNavBackStack.removeLastOrNull() } returns null

        // When
        val result = router.popBackStackTo(route, inclusive = false)

        // Then
        assertTrue(result)
        verify(exactly = 2) { mockNavBackStack.removeLastOrNull() } // 从索引4和3移除（大小为5，目标索引2+1=3，需要移除2个元素）
    }

    @Test
    fun `popBackStackTo should remove routes including target when inclusive true`() {
        // Given
        val route = Routes.Main.Tabs
        every { mockNavBackStack.indexOfFirst(any()) } returns 2
        every { mockNavBackStack.size } returns 5
        every { mockNavBackStack.removeLastOrNull() } returns null

        // When
        val result = router.popBackStackTo(route, inclusive = true)

        // Then
        assertTrue(result)
        verify(exactly = 3) { mockNavBackStack.removeLastOrNull() } // 从索引4、3、2移除（大小为5，目标索引2，需要移除3个元素）
    }

    @Test
    fun `currentRoute should return last element`() {
        // Given
        val expectedRoute = Routes.Main.Tabs
        every { mockNavBackStack.lastOrNull() } returns expectedRoute

        // When
        val result = router.currentRoute()

        // Then
        assertEquals(expectedRoute, result)
    }

    @Test
    fun `currentRoute should return null when stack empty`() {
        // Given
        every { mockNavBackStack.lastOrNull() } returns null

        // When
        val result = router.currentRoute()

        // Then
        assertNull(result)
    }

    @Test
    fun `containsRoute should return true when route exists`() {
        // Given
        val route = Routes.Main.Tabs
        every { mockNavBackStack.any(any()) } returns true

        // When
        val result = router.containsRoute(route)

        // Then
        assertTrue(result)
    }

    @Test
    fun `containsRoute should return false when route not exists`() {
        // Given
        val route = Routes.Main.Tabs
        every { mockNavBackStack.any(any()) } returns false

        // When
        val result = router.containsRoute(route)

        // Then
        assertFalse(result)
    }

    @Test
    fun `clearBackStack should remove all except first`() {
        // Given
        every { mockNavBackStack.size } returns 5
        every { mockNavBackStack.removeLastOrNull() } returns null

        // When
        router.clearBackStack()

        // Then
        verify(exactly = 4) { mockNavBackStack.removeLastOrNull() }
    }

    @Test
    fun `clearBackStack should do nothing when size 0 or 1`() {
        // Given
        every { mockNavBackStack.size } returns 1

        // When
        router.clearBackStack()

        // Then
        verify(exactly = 0) { mockNavBackStack.removeLastOrNull() }
    }
}