package me.switchswap.uscdining.parser

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

class MenuStorage(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "MenuDatabase", null, 1) {
    companion object {
        private var instance: MenuStorage? = null

        @Synchronized
        fun getInstance(ctx: Context): MenuStorage {
            if (instance == null) {
                instance = MenuStorage(ctx.getApplicationContext())
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Here you create tables
        db.createTable("Customer", true,
                "id" to INTEGER + PRIMARY_KEY + UNIQUE,
                "name" to TEXT,
                "photo" to BLOB)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Here you can upgrade tables, as usual
        db.dropTable("User", true)
    }
}
/*

Menu Item
- itemID (Primary key)
- Food name
- Type (breakfast, brunch, lunch, dinner)
- Date Served(4/3/19)
- Dining hall
- Alergens [Not included since the allergens table will link to the menu item]

Alergens
- allergenID (Primary Key)
- Allergen Name
- itemID (foreign key)
*/