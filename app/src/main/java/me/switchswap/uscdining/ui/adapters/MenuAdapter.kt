package me.switchswap.uscdining.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.layout_menu_item.view.*
import me.switchswap.uscdining.R
import me.switchswap.uscdining.models.Menu

class MenuAdapter(val menu: Menu) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    /**
     * Creates view holder to be passed into [MenuViewHolder]
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        return MenuViewHolder(
            LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_menu_item, parent, false)
        )
    }

    /**
     * Returns size of our list
     */
    override fun getItemCount(): Int {
        return menu.menuItems.size
    }

    /**
     * Binds the actual data with the ViewHolder
     */
    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menuItem = menu.menuItems[position]
        holder.view.menu_item_name.text = menuItem.itemName
        holder.view.menu_item_allergens.text = menuItem.getAllergenString()
    }

    /**
     * Creates a ViewHolder for our RecyclerView
     *
     * @param view is the layout to use
     * In this case it comes from `layout_menu_item`
     */
    class MenuViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}