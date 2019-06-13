package me.switchswap.uscdining.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.switchswap.uscdining.R
import me.switchswap.uscdining.models.DiningHallType
import me.switchswap.uscdining.models.MealType
import me.switchswap.uscdining.models.Menu
import me.switchswap.uscdining.parser.MenuManager
import me.switchswap.uscdining.ui.adapters.MenuAdapter
import me.switchswap.uscdining.util.DateUtil
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

class MenuFragment : Fragment() {
    companion object{
        fun newInstance(diningHallType: DiningHallType, mealType: MealType): MenuFragment {
            return MenuFragment().apply {
                arguments = Bundle(2).apply {
                    putString("diningHallType", diningHallType.name)
                    putString("mealType", mealType.name)
                }
            }
        }
    }

    private var recyclerViewMenuItems : RecyclerView? = null

    /**
     * Called for initial creation of fragment
     * Called after [.onAttach] and before [.onCreateView]
     * Can still be called while fragment's activity is being created
     * To do work after the activity is created, use [.onActivityCreated]
     * Restored child fragments will be created before the base [Fragment.onCreate] method returns.
     *
     * @param savedInstanceState is the state if the fragment is being re-created from a previous
     * saved state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View? = inflater.inflate(R.layout.fragment_menu, container, false)
        recyclerViewMenuItems = view?.findViewById(R.id.recycler_view_menu_items)
        return view
    }

    /**
     * Called immediately after [.onCreateView] returns but before any saved state has been restored
     * into the view
     *
     * Gives subclasses a chance to initialize themselves once they know their view hierarchy has
     * been completely created. Fragment's view hierarchy is not attached to the parent's now.
     *
     * @param view is the View returned by [.onCreateView]
     * @param savedInstanceState is the fragment being re-created from a previous saved state if
     * not null
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewMenuItems?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = MenuAdapter(getMenu())
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }



    private fun getMenu(): Menu{
        val dateUtil = DateUtil()
        val diningHallType: DiningHallType = DiningHallType.valueOf(arguments?.getString("diningHallType")!!)
        val mealType: MealType = MealType.valueOf(arguments?.getString("mealType")!!)

        val menuManager = MenuManager(context!!)

        return Menu(menuManager.getMenu(diningHallType, mealType, dateUtil.readDate(activity)))
    }
}

