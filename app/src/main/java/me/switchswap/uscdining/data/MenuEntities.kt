package me.switchswap.uscdining.data

import androidx.room.*

/**
 * DiningHalls
 *  - id : int
 *  - hallName : text
 *
 * MenuItems
 *  - id : int
 *  - itemName : text
 *  - itemType : text
 *  - itemCategory: text
 *  - date: int
 *  - hallId : int -> DiningHalls.id
 *
 * ItemAllergens
 *  - id : int
 *  - allergenName : text
 *  - menuItemId : int -> MenuItems.id
 */

@Entity(tableName = "DiningHalls", indices = [Index("id", unique = true)])
data class DiningHall(
        @PrimaryKey val id: Int,
        val name: String
)

@Entity(tableName = "MenuItems", foreignKeys = [ForeignKey(entity = DiningHall::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("hall_id"),
        onDelete = ForeignKey.CASCADE)], indices = [Index("id", unique = true)])
data class MenuItem(
        @PrimaryKey(autoGenerate = true) val id: Int,
        val name: String,
        val type: String,
        val category: String,
        val date: Long,
        @ColumnInfo(name = "hall_id") val hallId: Int
)

@Entity(tableName = "Allergens", foreignKeys = [ForeignKey(entity = MenuItem::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("menu_item_id"),
        onDelete = ForeignKey.CASCADE)], indices = [Index("id", unique = true)])
data class Allergen (
        @PrimaryKey(autoGenerate = true) val id: Int,
        val name: String,
        @ColumnInfo(name = "menu_item_id") val menuItemId: Int
)

data class MenuItemAndAllergens (
        @Embedded val menuItem: MenuItem,
        @Relation (
                parentColumn = "id",
                entityColumn = "menu_item_id"
        )
        val allergens: List<Allergen>
)