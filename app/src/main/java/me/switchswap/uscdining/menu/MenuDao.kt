package me.switchswap.uscdining.menu

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction

@Dao
public interface MenuDao {
    /**
     * Check if menu exists for a given dining hall type and a given date
     */
    @Query("SELECT EXISTS(SELECT 1 FROM MenuItems WHERE date = :date AND hall_id = :hallId LIMIT 1)")
    fun hallHasMenu(hallId: Int, date: Long): Int

    @Query("SELECT EXISTS(SELECT 1 FROM MenuItems WHERE date = :date LIMIT 1)")
    fun dateHasMenu(date: Long): Int

    /**
     * Retrieves an ArrayList of MenuItems from SQLite database for a given dining hall, item type,
     * and date
     */
    @Transaction
    @Query("SELECT * FROM MenuItems WHERE hall_id = :hallId AND type = :type AND date = :date")
    fun getMenuItems(hallId: Int, type: String, date: Long): List<MenuItemAndAllergens>
}