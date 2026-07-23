package com.jiwei.app.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = EntryEntity::class)
@Entity(tableName = "entries_fts")
data class EntryFts(
    val title: String,
    val content: String,
    val tagNames: String
)
