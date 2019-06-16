package me.switchswap.uscdining.models

enum class AllergenType(val allergenName: String, val color: Int) {
    DAIRY("Dairy", 0x8224E3),
    EGGS("Eggs", 0xFFE751),
    FISH("Fish", 0x00A8FF),
    UNAVAILABLE("Unavailable", 0xFFFFFF),
    PEANUTS("Peanuts", 0x966600),
    PORK("Pork", 0xFF0000),
    SESAME("Sesame", 0x5B5B5B),
    SHELLFISH("Shellfish", 0x00F3DF),
    SOY("Soy", 0xDEC1FF),
    TREE_NUTS("Tree Nuts", 0xF78000),
    VEGAN("Vegan", 0x79FF02),
    VEGETARIAN("Vegetarian", 0x01944D),
    WHEAT_GLUTEN("Wheat / Gluten", 0xC42D7D);

    companion object {
        fun fromName(name: String): AllergenType? = values()
                .find { it.allergenName.toLowerCase() == name.toLowerCase() }
    }
}