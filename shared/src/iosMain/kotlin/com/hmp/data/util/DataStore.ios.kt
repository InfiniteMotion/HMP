package com.hmp.data.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

actual object DataStoreFactory {
    actual fun create(): DataStore<Preferences> {
        throw NotImplementedError("DataStore for iOS not implemented yet")
    }
}
