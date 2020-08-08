package me.switchswap.uscdining.data

import androidx.lifecycle.LiveData
import androidx.room.*
import me.switchswap.diningmenu.models.DiningHallType
import me.switchswap.diningmenu.models.ItemType

@Dao
interface MenuDao {
    /**
     * Check if menu exists for a given dining hall type and a given date
     */
    @Query("SELECT EXISTS(SELECT 1 FROM MenuItems WHERE date = :date AND hall_id = :hallId LIMIT 1)")
    fun hallHasMenu(hallId: Int, date: Long): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM MenuItems WHERE date = :date LIMIT 1)")
    fun dateHasMenu(date: Long): Boolean

    @Query("SELECT * FROM DiningHalls")
    fun getDiningHalls(): List<DiningHall>

    /**
     * Check if given dining hall on a given day is serving brunch
     */
    @Query("SELECT EXISTS(SELECT 1 FROM MenuItems where date = :date AND hall_id = :diningHallType AND type = \"BRUNCH\")")
    fun hallHasBrunch(diningHallType: DiningHallType, date: Long): Boolean

    /**
     * Retrieves an ArrayList of MenuItems from SQLite database for a given dining hall, item type,
     * and date
     */
    @Transaction
    @Query("SELECT * FROM MenuItems WHERE hall_id = :diningHallType AND type = :itemType AND date = :date")
    fun getMenuItems(diningHallType: DiningHallType, itemType: ItemType, date: Long): List<MenuItemAndAllergens>
    @Transaction
    @Query("SELECT * FROM MenuItems WHERE hall_id = :hallId AND type = :itemType AND date = :date")
    fun getMenuItems(hallId: Int, itemType: ItemType, date: Long): List<MenuItemAndAllergens>

    @Insert
    suspend fun insertMenuItem(menuItem: MenuItem): Long

    @Insert
    suspend fun insertAllergens(allergens: List<Allergen>)

    /**
     * Inserts multiple Dining Halls into database
     */
    @Insert
    suspend fun insertDiningHalls(diningHalls: List<DiningHall>): List<Long>

    /**
     * Deletes all MenuItems
     * By the schema, the corresponding allergens will also be deleted
     */
    @Query("DELETE FROM MenuItems")
    suspend fun dropAllMenuItems()

    @Query("DELETE FROM MenuItems WHERE date = :date")
    suspend fun dropMenuItems(date: Long)
}