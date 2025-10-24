package cl.example.hirmi.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import cl.example.hirmi.model.User

@Database(entities = [User::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
