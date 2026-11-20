
package cl.duoc.nutriexpert.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `planes` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `sectorId` INTEGER NOT NULL,
                `nombreTemporada` TEXT NOT NULL,
                `remoteId` INTEGER,
                `sincronizado` INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}
