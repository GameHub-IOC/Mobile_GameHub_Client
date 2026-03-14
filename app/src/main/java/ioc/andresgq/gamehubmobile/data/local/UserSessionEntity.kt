package ioc.andresgq.gamehubmobile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_session")
data class UserSessionEntity(
    @PrimaryKey val id: Int = 0,
    val token: String,
    val username: String,
    val userType: String
)
