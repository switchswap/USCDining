package me.switchswap.uscdining.menu

import androidx.room.*

/**
 * Create SQL tables
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
@Entity(tableName = "DiningHalls")
data class DiningHall (
    @PrimaryKey val id: Long,
    val name: String
)

@Entity(tableName = "MenuItems")
data class MenuItem (
        @PrimaryKey(autoGenerate = true)
        val id: Long,
        val name: String,
        val type: String,
        val category: String,
        val date: Long,
        @ColumnInfo(name = "hall_id")
        val hallId: Int
)

data class DiningHallAndMenuItems (
        @Embedded val diningHall: DiningHall,
        @Relation (
                parentColumn = "id",
                entityColumn = "hall_id"
        )
        val menuItems: List<MenuItem>
)

@Entity(tableName = "Allergens")
data class Allergen (
        @PrimaryKey(autoGenerate = true)
        val id: Int,
        @ColumnInfo(name = "name")
        val name: String,
        @ColumnInfo(name = "menu_item_id")
        val menuItemId: Int
)

data class MenuItemAndAllergens (
        @Embedded val menuItem: MenuItem,
        @Relation (
                parentColumn = "id",
                entityColumn = "menu_item_id"
        )
        val allergens: List<Allergen>
)