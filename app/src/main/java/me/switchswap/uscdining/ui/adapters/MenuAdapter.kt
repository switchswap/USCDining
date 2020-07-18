package me.switchswap.uscdining.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.menu_item.view.*
import me.switchswap.uscdining.R
import me.switchswap.uscdining.data.MenuItemAndAllergens
import me.switchswap.uscdining.models.AllergenType

class MenuAdapter(val menu: List<MenuItemAndAllergens>) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    /**
     * Creates view holder to be passed into [MenuViewHolder]
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        return MenuViewHolder(
            LayoutInflater.from(parent.context)
                    .inflate(R.layout.menu_item, parent, false)
        )
    }

    /**
     * Returns size of our list
     */
    override fun getItemCount(): Int {
        return menu.size
    }

    /**
     * Binds the actual data with the ViewHolder
     */
    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menuItem = menu[position]
        holder.view.label_item_name.text = menuItem.menuItem.name

        // Todo: If this isn't here, the Cereal allergens on 5/21/20 change when tab switching. Fix that to skip this call.
        holder.view.label_item_allergens.text = "" // Clear the TextView

        val allergenString: StringBuilder = StringBuilder()
        menuItem.allergens.forEach {
            // Get allergen enum
            val allergenType: AllergenType = AllergenType.fromName(it.name) ?: AllergenType.UNAVAILABLE
            if (allergenType != AllergenType.UNAVAILABLE) {
                allergenString.append(allergenType.allergenName).append(" ")
            }
        }

        if (allergenString.trim().isNotEmpty()) {
            holder.view.label_item_allergens.text = allergenString.toString().dropLast(1) // Drop last space in string
        }
        else {
            holder.view.label_item_allergens.text = holder.view.resources.getString(R.string.item_allergens_default_text)
        }
    }

    /**
     * Creates a ViewHolder for our RecyclerView
     *
     * @param view is the layout to use
     * In this case it comes from `menu_item`
     */
    class MenuViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}