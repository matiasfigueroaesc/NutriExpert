package cl.duoc.nutriexpert.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE planes ADD COLUMN fechaInicio TEXT DEFAULT NULL"
        )
        db.execSQL(
            "ALTER TABLE planes ADD COLUMN fechaTermino TEXT DEFAULT NULL"
        )
    }
}