package com.jiwei.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jiwei.app.data.local.entity.TagEntity

@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: TagEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tags: List<TagEntity>)

    @Query("DELETE FROM tags WHERE id = :tagId")
    suspend fun delete(tagId: String)

    @Query("SELECT * FROM tags ORDER BY name")
    suspend fun getAll(): List<TagEntity>

    @Query("SELECT * FROM tags WHERE id = :tagId")
    suspend fun getById(tagId: String): TagEntity?

    @Query("SELECT * FROM tags WHERE parentId = :parentId ORDER BY name")
    suspend fun getChildren(parentId: String): List<TagEntity>

    @Query("SELECT * FROM tags WHERE parentId IS NULL ORDER BY name")
    suspend fun getRootTags(): List<TagEntity>

    @Query("SELECT * FROM tags WHERE name LIKE '%' || :query || '%' ORDER BY name")
    suspend fun searchByName(query: String): List<TagEntity>

    @Query("SELECT * FROM tags WHERE id IN (:tagIds)")
    suspend fun getByIds(tagIds: List<String>): List<TagEntity>
}
