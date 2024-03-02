package by.bashlikovvv.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val id: Long,
    val name: String
)