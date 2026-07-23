package com.jiwei.app.domain.repository

import kotlinx.coroutines.flow.Flow

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

interface ThemeRepository {
    fun getThemeMode(): Flow<ThemeMode>

    suspend fun setThemeMode(mode: ThemeMode)
}
