package me.switchswap.uscdining.models

/* Custom data type for MenuItems */
data class MenuItem (val itemName: String,
                     val allergens: ArrayList<String>,
                     val mealType: MealType,
                     val diningHallType: DiningHallType) {

    fun hasAllergen(allergen: String): Boolean {
        return allergen in allergens
    }

    fun getAllergenString(): String {
        return allergens.joinToString(separator = ", ")
    }
}