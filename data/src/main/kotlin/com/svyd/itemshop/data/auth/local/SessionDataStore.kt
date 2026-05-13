package com.svyd.itemshop.data.auth.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * Single Preferences DataStore instance for auth-related values. Using
 * the property delegate guarantees one DataStore per file across the
 * process, which is the contract DataStore requires.
 */
internal val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")
