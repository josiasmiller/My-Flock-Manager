package com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system

data class DatabaseVersion(
    val major: Int,
    val minor: Int,
    val patch: Int
) {
    companion object {
        fun fromString(versionString: String): DatabaseVersion? {
            val splits = versionString.split('.')
            val major = splits.takeIf { it.isNotEmpty() }?.let { it[0].toIntOrNull() }
            val minor = splits.takeIf { 1 < it.size }?.let { it[1].toIntOrNull() }
            val patch = splits.takeIf { 2 < it.size }?.let { it[2].toIntOrNull() }
            if (major != null && 0 <= major && minor != null && 0 <= minor && patch != null && 0 <= patch) {
                return DatabaseVersion(major, minor, patch)
            }
            return null
        }
    }

    init {
        require(0 <= major) { "Major version must be greater than or equal to zero." }
        require(0 <= minor) { "Minor version must be greater than or equal to zero." }
        require(0 <= patch) { "Patch version must be greater than or equal to zero." }
    }
}
