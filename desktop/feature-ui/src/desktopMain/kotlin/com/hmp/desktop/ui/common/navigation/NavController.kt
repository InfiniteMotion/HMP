package com.hmp.desktop.ui.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

/** 路由键接口，所有路由必须实现此接口 */
interface NavKey

/**
 * 统一的导航控制器
 * 管理回退栈并提供类型安全的导航操作
 */
class NavController(initialRoute: NavKey) {
    private val _backStack = mutableStateListOf(initialRoute)

    /** 当前回退栈（只读） */
    val backStack: List<NavKey> get() = _backStack

    /** 当前栈顶路由 */
    val currentRoute: NavKey? get() = _backStack.lastOrNull()

    /** 回退栈大小 */
    val size: Int get() = _backStack.size

    /** 是否可以返回 */
    fun canPop(): Boolean = _backStack.size > 1

    /** 导航到新路由（压栈，防重复） */
    fun navigate(route: NavKey) {
        if (_backStack.lastOrNull() == route) return
        _backStack.add(route)
    }

    /** 替换栈顶路由 */
    fun navigateReplace(route: NavKey) {
        if (_backStack.isNotEmpty()) {
            _backStack.removeAt(_backStack.lastIndex)
        }
        _backStack.add(route)
    }

    /** 导航到路由，若已在栈中则 pop 回该位置 */
    fun navigateSingleTop(route: NavKey) {
        if (_backStack.lastOrNull() == route) return
        val index = _backStack.indexOfFirst { it == route }
        if (index >= 0) {
            while (_backStack.size > index + 1) {
                _backStack.removeAt(_backStack.lastIndex)
            }
            return
        }
        _backStack.add(route)
    }

    /** 弹出栈顶路由 */
    fun popBackStack(): Boolean {
        return if (_backStack.isNotEmpty()) {
            _backStack.removeAt(_backStack.lastIndex)
            true
        } else false
    }

    /** 弹出到指定路由 */
    fun popBackStackTo(route: NavKey, inclusive: Boolean = false): Boolean {
        val index = _backStack.indexOfFirst { it == route }
        if (index < 0) return false
        val targetIndex = if (inclusive) index else index + 1
        while (_backStack.size > targetIndex) {
            _backStack.removeAt(_backStack.lastIndex)
        }
        return true
    }

    /** 清空回退栈，只保留栈底 */
    fun clearBackStack() {
        while (_backStack.size > 1) {
            _backStack.removeAt(_backStack.lastIndex)
        }
    }

    /** 检查路由是否在栈中 */
    fun contains(route: NavKey): Boolean = _backStack.any { it == route }

    /** 栈中无匹配项时返回 true */
    fun none(predicate: (NavKey) -> Boolean): Boolean = _backStack.none(predicate)

    fun lastOrNull(): NavKey? = _backStack.lastOrNull()
}

/** 创建并记住一个 NavController */
@Composable
fun rememberNavController(initialRoute: NavKey): NavController {
    return remember { NavController(initialRoute) }
}

/** 路由条目提供者 */
class NavEntryProvider {
    val entries = mutableMapOf<Class<*>, @Composable (NavKey) -> Unit>()

    inline fun <reified T : NavKey> entry(noinline content: @Composable (T) -> Unit) {
        entries[T::class.java] = { key ->
            @Suppress("UNCHECKED_CAST")
            content(key as T)
        }
    }

    fun resolve(key: NavKey): (@Composable () -> Unit)? {
        val content = entries[key::class.java] ?: return null
        return { content(key) }
    }
}

/** 创建 entryProvider */
fun entryProvider(builder: NavEntryProvider.() -> Unit): NavEntryProvider {
    return NavEntryProvider().apply(builder)
}

/** 导航宿主，根据当前路由渲染对应内容 */
@Composable
fun NavHost(
    navController: NavController,
    entryProvider: NavEntryProvider
) {
    val currentRoute = navController.currentRoute ?: return
    val content = entryProvider.resolve(currentRoute)
    content?.invoke()
}
