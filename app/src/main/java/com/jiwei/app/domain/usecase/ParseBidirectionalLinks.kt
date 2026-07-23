package com.jiwei.app.domain.usecase

import javax.inject.Inject

class ParseBidirectionalLinks @Inject constructor() {

    operator fun invoke(content: String): List<String> {
        val regex = Regex("\\[\\[([^\\]]+)\\]\\]")
        return regex.findAll(content)
            .map { it.groupValues[1].trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .toList()
    }
}
