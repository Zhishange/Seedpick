package com.jiwei.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "entry_links",
    primaryKeys = ["sourceEntryId", "targetEntryTitle"],
    indices = [Index("targetEntryTitle")]
)
data class EntryLinkEntity(
    val sourceEntryId: String,
    val targetEntryTitle: String
)
