package com.example.hearablemusicplayer.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * 协程测试规则
 * 
 * 用于在测试中设置和清理协程测试调度器
 * 使用方法：
 * ```
 * @get:Rule
 * val coroutineRule = CoroutineTestRule()
 * ```
 */
@ExperimentalCoroutinesApi
class CoroutineTestRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    
    override fun starting(description: Description) {
        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }
    
    override fun finished(description: Description) {
        super.finished(description)
        Dispatchers.resetMain()
    }
}

/**
 * 运行测试协程的扩展函数
 * 
 * 简化测试中的协程执行
 */
@ExperimentalCoroutinesApi
fun runTest(block: suspend TestScope.() -> Unit) = kotlinx.coroutines.test.runTest {
    block()
}
