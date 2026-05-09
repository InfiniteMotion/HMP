package com.hmp.data.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

expect object DataStoreFactory {
    fun create(): DataStore<Preferences>
}
