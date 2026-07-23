package com.jiwei.app.domain.repository

import android.net.Uri
import com.jiwei.app.domain.model.ImportResult
import com.jiwei.app.domain.model.ImportStrategy

interface ExportRepository {
    suspend fun exportToZip(): Uri

    suspend fun importFromZip(zipUri: Uri, strategy: ImportStrategy): ImportResult
}
