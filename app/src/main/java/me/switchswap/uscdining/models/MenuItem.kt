package me.switchswap.uscdining.models

/* Custom data type for MenuItems */
data class MenuItem (val itemName: String, val allergens: ArrayList<String>,
                     val mealType: MealType) {
    fun hasAllergen(allergen: String): Boolean{
        return allergen in allergens
    }
}