package me.switchswap.uscdining.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import me.switchswap.uscdining.R
import me.switchswap.uscdining.extensions.db
import me.switchswap.uscdining.data.MenuItemAndAllergens
import me.switchswap.uscdining.data.MenuManager
import me.switchswap.uscdining.ui.adapters.MenuAdapter
import me.switchswap.uscdining.util.DateUtil
import models.DiningHallType
import models.ItemType
import org.jetbrains.anko.longToast

class MenuFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    private var recyclerViewMenuItems : RecyclerView? = null

    private var swipeRefreshLayout: SwipeRefreshLayout? = null

    private val menu: ArrayList<MenuItemAndAllergens> = ArrayList()

    private val dateUtil by lazy {
        DateUtil(activity)
    }

    private val sharedPreferences by lazy {
        activity?.getPreferences(Context.MODE_PRIVATE)
    }

    private val menuPayload by lazy {
        arguments?.getParcelable<Payload>(PARAM_DINNING_PAYLOAD)!!
    }

    private lateinit var menuManager: MenuManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View? = inflater.inflate(R.layout.fragment_menu, container, false)
        recyclerViewMenuItems = view?.findViewById(R.id.recycler_view_menu_items)
        swipeRefreshLayout = view?.findViewById(R.id.swipe_refresh_layout)
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
        menuManager = MenuManager(context.db()?.menuDao())
        recyclerViewMenuItems?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = MenuAdapter(menu)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        swipeRefreshLayout?.apply {
            setColorSchemeResources(R.color.colorAccent)

            setOnRefreshListener {
                reloadMenu(true)
            }
        }
    }

    /**
     * Called when the fragment is visible to the user and actively running
     * This is generally tied to [Fragment.onResume] of the containing Activity's lifecycle
     */
    override fun onResume() {
        super.onResume()
        dateUtil.subscribe(this)
        reloadMenu(false)
    }

    /**
     * Called when the Fragment is no longer resumed.
     * Generally tied to [Fragment.onPause] of containing Activity's lifecycle
     */
    override fun onPause() {
        // Calling unSubscribe before super.onPause() because we want the android system to  do it's
        // onPause() magic after we've dealt with our logic ;)
        // Thanks Wax!
        dateUtil.unSubscribe(this)
        super.onPause()
    }

    /**
     * Reloads menu by checking database for items
     * If no items are found for a given day, populate database from website and load from there
     */
    private fun reloadMenu(fullReload: Boolean) {
        if (getRefreshing()) {
            swipeRefreshLayout?.isRefreshing = true
            return
        }
        else {
            if (fullReload) {
                // Get menu from web
                // UI should be updated from the shared preference listener
                setRefreshing(true)
                swipeRefreshLayout?.isRefreshing = true

                CoroutineScope(IO).launch {
                    longTask()
                    // If user changes dining hall while this coroutine is running, it causes
                    // crashes so check if context exists first
                    // Todo: Learn why this happens and fix this temp patch
                    if(context != null) setRefreshing(false)
                }.invokeOnCompletion { throwable ->
                    if (throwable != null) {
                        Log.e(TAG, throwable.message + "")
                    }
                }
            }
            else {
                // Update list
                updateMenu()
            }
        }
    }

    private suspend fun longTask() {
        val date = dateUtil.readDate()
        kotlin.runCatching {
            menuManager.getMenuFromWeb(date)
        }.getOrElse {
            withContext(Main) {
                activity?.longToast("Something went wrong!")
                Log.e(TAG, "" + it.message)
            }
        }
    }

    /**
     * We are subscribing to preference changes through [DateUtil.subscribe] in our [onResume],
     * this means once this fragment has been added to a container any changes to through
     * [DateUtil.writeDate] will cause this method to be invoked at which point we will reload our menu
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(context == null) return
        when(key) {
            getString(R.string.pref_menu_date) -> reloadMenu(true)
            getString(R.string.pref_refreshing) -> {
                // If refresh status changed to false, then reset refresh status icon
                if (!getRefreshing()) {
                    swipeRefreshLayout?.isRefreshing = false

                    // Update menu since refresh is complete
                    updateMenu()
                }
            }
        }
    }

    private fun getMenu(): ArrayList<MenuItemAndAllergens> {
        return menuManager.getMenuFromDatabase(
                diningHallType = menuPayload.diningHallType,
                itemType = menuPayload.itemType,
                date = dateUtil.readDate()
        )
    }

    private fun updateMenu() {
        // Clear old list
        menu.clear()

        runBlocking {
            // Add new list
            val tempMenu: ArrayList<MenuItemAndAllergens> = getMenu()
            Log.d(TAG, "${menuPayload.itemType} Got ${tempMenu.size} items from database")
            menu.addAll(tempMenu)

            Log.d(TAG, "${menuPayload.itemType} Menu now displays ${menu.size} items")
        }

        // Notify adapter of change
        recyclerViewMenuItems?.adapter?.notifyDataSetChanged()
    }

    private fun setRefreshing(status: Boolean) {
        sharedPreferences?.edit()?.apply {
            putBoolean(getString(R.string.pref_refreshing), status)
            apply()
        }
    }

    private fun getRefreshing(): Boolean {
        return sharedPreferences?.getBoolean(getString(R.string.pref_refreshing), false) ?: false
    }

    override fun onDestroy() {
        Log.d(TAG, "${menuPayload.diningHallType} ${menuPayload.itemType} Destroyed")
        if (getRefreshing()) {
            CoroutineScope(IO).cancel()
            setRefreshing(false)
        }
        super.onDestroy()
    }


    companion object {
        val TAG = MenuFragment::class.java.simpleName

        /**
         * Key for what we are using as input into [MenuFragment.setArguments]
         */
        private const val PARAM_DINNING_PAYLOAD = "PARAM_DINNING_PAYLOAD"

        fun newInstance(payload: Payload): MenuFragment {
            return MenuFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(PARAM_DINNING_PAYLOAD, payload)
                }
            }
        }
    }

    /**
     * This class is just an easier way to handle passing parameters without relying on
     * converting strings back and forth to enums
     */
    @Parcelize
    data class Payload(
            val diningHallType: DiningHallType,
            val itemType: ItemType
    ) : Parcelable
}

