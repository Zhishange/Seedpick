package com.jiwei.app.di

import com.jiwei.app.data.repository.AttachmentRepositoryImpl
import com.jiwei.app.data.repository.EntryRepositoryImpl
import com.jiwei.app.data.repository.ExportRepositoryImpl
import com.jiwei.app.data.repository.LinkRepositoryImpl
import com.jiwei.app.data.repository.TagRepositoryImpl
import com.jiwei.app.data.repository.ThemeRepositoryImpl
import com.jiwei.app.domain.repository.AttachmentRepository
import com.jiwei.app.domain.repository.EntryRepository
import com.jiwei.app.domain.repository.ExportRepository
import com.jiwei.app.domain.repository.LinkRepository
import com.jiwei.app.domain.repository.TagRepository
import com.jiwei.app.domain.repository.ThemeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindEntryRepository(impl: EntryRepositoryImpl): EntryRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(impl: TagRepositoryImpl): TagRepository

    @Binds
    @Singleton
    abstract fun bindLinkRepository(impl: LinkRepositoryImpl): LinkRepository

    @Binds
    @Singleton
    abstract fun bindAttachmentRepository(impl: AttachmentRepositoryImpl): AttachmentRepository

    @Binds
    @Singleton
    abstract fun bindExportRepository(impl: ExportRepositoryImpl): ExportRepository

    @Binds
    @Singleton
    abstract fun bindThemeRepository(impl: ThemeRepositoryImpl): ThemeRepository
}
