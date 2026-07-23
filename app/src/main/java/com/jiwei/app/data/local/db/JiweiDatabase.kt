package com.jiwei.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jiwei.app.data.local.dao.AttachmentDao
import com.jiwei.app.data.local.dao.EntryDao
import com.jiwei.app.data.local.dao.EntryLinkDao
import com.jiwei.app.data.local.dao.EntryTagDao
import com.jiwei.app.data.local.dao.TagDao
import com.jiwei.app.data.local.entity.AttachmentEntity
import com.jiwei.app.data.local.entity.EntryEntity
import com.jiwei.app.data.local.entity.EntryFts
import com.jiwei.app.data.local.entity.EntryLinkEntity
import com.jiwei.app.data.local.entity.EntryTagCrossRef
import com.jiwei.app.data.local.entity.TagEntity

@Database(
    entities = [
        EntryEntity::class,
        TagEntity::class,
        EntryTagCrossRef::class,
        EntryLinkEntity::class,
        AttachmentEntity::class,
        EntryFts::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JiweiDatabase : RoomDatabase() {

    abstract fun entryDao(): EntryDao
    abstract fun tagDao(): TagDao
    abstract fun entryTagDao(): EntryTagDao
    abstract fun entryLinkDao(): EntryLinkDao
    abstract fun attachmentDao(): AttachmentDao
}
