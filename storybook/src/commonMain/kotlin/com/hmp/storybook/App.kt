package com.hmp.storybook

import androidx.compose.runtime.Composable
import com.hmp.storybook.layout.StorybookLayout
import com.hmp.storybook.navigation.StorybookNavHost
import com.hmp.storybook.navigation.rememberStorybookNavController
import com.hmp.storybook.theme.StorybookTheme

@Composable
fun App() {
    StorybookTheme {
        val navController = rememberStorybookNavController()
        StorybookLayout(navController = navController) {
            StorybookNavHost(navController = navController)
        }
    }
}
