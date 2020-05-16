package me.switchswap.uscdining.parser

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import models.DiningHallType
import models.DiningMenu
import org.jetbrains.anko.db.*

class MenuStorage(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "MenuDatabase", null, 1) {
    init {
        instance = this
    }

    companion object {
        private var instance: MenuStorage? = null

        @Synchronized
        fun getInstance(ctx: Context): MenuStorage {
            if (instance == null) {
                instance = MenuStorage(ctx.applicationContext)
            }
            return instance!!
        }
    }

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
    override fun onCreate(db: SQLiteDatabase) {
        db.createTable("DiningHalls", true,
                "id" to INTEGER + PRIMARY_KEY,
                "hallName" to TEXT + NOT_NULL)

        db.createTable("MenuItems", true,
                "id" to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                "itemName" to TEXT + NOT_NULL,
                "itemType" to TEXT + NOT_NULL,
                "itemCategory" to TEXT + NOT_NULL,
                "itemDate" to INTEGER + NOT_NULL,
                "hallId" to INTEGER + NOT_NULL,
                FOREIGN_KEY("hallId", "DiningHalls", "id"))

        db.createTable("ItemAllergens", true,
                "id" to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                "allergenName" to TEXT + NOT_NULL,
                "menuItemId" to TEXT + NOT_NULL,
                 FOREIGN_KEY("menuItemId", "MenuItems", "id"))

        // Add dining halls
        db.transaction {
            db.insert("DiningHalls",
                    "id" to DiningHallType.PARKSIDE.id,
                    "hallName" to "parkside")
            db.insert("DiningHalls",
                    "id" to DiningHallType.EVK.id,
                    "hallName" to "evk")
            db.insert("DiningHalls",
                    "id" to DiningHallType.PARKSIDE.id,
                    "hallName" to "village")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d("onUpgrade", "Upgraded database!")
        db.dropTable("DiningHalls", true)
        db.dropTable("MealItems", true)
        db.dropTable("ItemAllergens", true)
        onCreate(db)
    }
}
// Access property for Context
val Context.database: MenuStorage
    get() = MenuStorage.getInstance(this)
