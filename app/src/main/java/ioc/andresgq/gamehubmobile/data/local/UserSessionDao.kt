package ioc.andresgq.gamehubmobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserSessionDao {
    @Query("SELECT * FROM user_session WHERE id = 0 LIMIT 1")
    suspend fun getSession(): UserSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: UserSessionEntity)

    @Query("DELETE FROM user_session")
    suspend fun clear()
}
