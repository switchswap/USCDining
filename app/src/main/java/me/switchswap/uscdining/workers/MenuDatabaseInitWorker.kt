package me.switchswap.uscdining.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.coroutineScope
import me.switchswap.uscdining.menu.AppDatabase
import me.switchswap.uscdining.menu.DiningHall
import models.DiningHallType

class MenuDatabaseInitWorker(
        context: Context,
        workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = coroutineScope {
        try {
            val database = AppDatabase.getInstance(applicationContext)

            // Create ArrayList of Dining Halls
            val diningHalls = ArrayList<DiningHall>()
            diningHalls.add(DiningHall(DiningHallType.EVK.id, DiningHallType.EVK.name))
            diningHalls.add(DiningHall(DiningHallType.PARKSIDE.id, DiningHallType.PARKSIDE.name))
            diningHalls.add(DiningHall(DiningHallType.VILLAGE.id, DiningHallType.VILLAGE.name))

            // Insert into database on creation
            database.menuDao().insertDiningHalls(diningHalls)
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing database", e)
            Result.failure()
        }
    }

    companion object {
        private val TAG = MenuDatabaseInitWorker::class.java.simpleName
    }
}