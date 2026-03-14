package ioc.andresgq.gamehubmobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UserSessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userSessionDao(): UserSessionDao
}
