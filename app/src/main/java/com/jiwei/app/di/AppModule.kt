package com.jiwei.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.jiwei.app.data.local.dao.AttachmentDao
import com.jiwei.app.data.local.dao.EntryDao
import com.jiwei.app.data.local.dao.EntryLinkDao
import com.jiwei.app.data.local.dao.EntryTagDao
import com.jiwei.app.data.local.dao.TagDao
import com.jiwei.app.data.local.db.JiweiDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): JiweiDatabase {
        return Room.databaseBuilder(
            context,
            JiweiDatabase::class.java,
            "jiwei.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    fun provideEntryDao(db: JiweiDatabase): EntryDao = db.entryDao()

    @Provides
    fun provideTagDao(db: JiweiDatabase): TagDao = db.tagDao()

    @Provides
    fun provideEntryTagDao(db: JiweiDatabase): EntryTagDao = db.entryTagDao()

    @Provides
    fun provideEntryLinkDao(db: JiweiDatabase): EntryLinkDao = db.entryLinkDao()

    @Provides
    fun provideAttachmentDao(db: JiweiDatabase): AttachmentDao = db.attachmentDao()
}
