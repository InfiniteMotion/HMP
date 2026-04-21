package com.hmp.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual fun provideDatabaseBuilder(context: Any): AppDatabase.Builder {
    val driver: SqlDriver = NativeSqliteDriver(
        schema = AppDatabase.Schema,
        name = "hmp_database"
    )
    return AppDatabase(driver)
}
