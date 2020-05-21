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
        holder.view.menu_item_name.text = menuItem.menuItem.name
        holder.view.menu_item_allergens.removeAllViews()

        menuItem.allergens.forEach {
            // Get allergen enum
            val allergenType: AllergenType = AllergenType.fromName(it.name) ?: AllergenType.UNAVAILABLE

            val layoutInflater: LayoutInflater = holder.view.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val textView = layoutInflater.inflate(R.layout.allergen_item, null) as TextView
            textView.apply {
                text = allergenType.allergenName
            }

            holder.view.menu_item_allergens.addView(textView)
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