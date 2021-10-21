package me.switchswap.uscdining.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.parcel.Parcelize
import me.switchswap.diningmenu.models.DiningHallType
import me.switchswap.diningmenu.models.ItemType
import me.switchswap.uscdining.R
import me.switchswap.uscdining.extensions.db
import me.switchswap.uscdining.ui.adapters.MenuAdapter
import me.switchswap.uscdining.ui.interfaces.IFragmentInteractionListener
import me.switchswap.uscdining.ui.viewmodels.MenuFragmentViewModel
import me.switchswap.uscdining.util.DateUtil

class MenuFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    private val viewModel: MenuFragmentViewModel by activityViewModels()

    private var recyclerViewMenuItems : RecyclerView? = null

    private var swipeRefreshLayout: SwipeRefreshLayout? = null

    private var interactionListener: IFragmentInteractionListener? = null

    private val textView by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById<TextView>(R.id.textView_no_items_available)
    }

    private val dateUtil by lazy {
        DateUtil(activity)
    }

    private val sharedPreferences by lazy {
        activity?.getPreferences(Context.MODE_PRIVATE)
    }

    private val menuPayload by lazy {
        arguments?.getParcelable<Payload>(PARAM_DINNING_PAYLOAD)!!
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        interactionListener = if (context is IFragmentInteractionListener) {
            context
        } else {
            throw RuntimeException("$context must implement FragmentInteractionListener")
        }
    }

    override fun onDetach() {
        interactionListener = null
        super.onDetach()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        viewModel.setManager(requireContext().db().menuDao())
    }
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
        recyclerViewMenuItems?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = MenuAdapter()
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        swipeRefreshLayout?.apply {
            setColorSchemeResources(R.color.colorAccent)

            setOnRefreshListener {
                reloadMenu(true)
            }
        }

        viewModel.loadingState.observe(viewLifecycleOwner, Observer {
            swipeRefreshLayout?.isRefreshing = it
            if (it == true) {
                // Disable nav view
                if(interactionListener != null){
                    interactionListener?.disableNavDrawer()
                }
            }
        })
    }

    /**
     * Called when the fragment is visible to the user and actively running
     * This is generally tied to [Fragment.onResume] of the containing Activity's lifecycle
     */
    override fun onResume() {
        super.onResume()
        dateUtil.subscribe(this)

        // If the menu doesn't exist in cache, force reload to grab data from web
        reloadMenu(!requireContext().db().menuDao().dateHasMenu(dateUtil.readDate()))
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
        // This shouldn't work but it does. I've decided to pretend it's fine.
        // 10/21: So I wrote the above statement around a year ago at the time of release. I tested
        // it just now and it seems that without the assignment of x, and without the runCatching,
        // the app crashes when refreshing and then spam refreshing tabs. So I guess this fixes it
        // somehow...
        val x = viewLifecycleOwner
        val cacheEnabled: Boolean = sharedPreferences?.getBoolean(getString(R.string.pref_cache_disabled), true) ?: true
        viewModel.getMenuData(menuPayload.diningHallType, menuPayload.itemType, dateUtil.readDate(), fullReload, cacheEnabled)
            .observe(x, Observer {
                kotlin.runCatching {
                    val adapter = recyclerViewMenuItems?.adapter
                    (adapter as MenuAdapter).setMenu(it)
                    configureDiningHalls()
                    configureBrunch()

                    if (it.isEmpty()) {
                        textView?.visibility = View.VISIBLE
                    }
                    else{
                        textView?.visibility = View.GONE
                    }
                }.onFailure {
                    // Failure: java.lang.IllegalStateException: FragmentManager is already executing transactions
                    Log.d(TAG, "Failure: $it")
                }
            })
        configureBrunch()
    }

    private fun configureDiningHalls() {
        // Signal the main activity to update the nav drawer accordingly
        if(interactionListener != null){
            interactionListener?.configureDiningHalls(dateUtil.readDate())
            interactionListener?.configureBrunch(menuPayload.diningHallType, dateUtil.readDate())
        }
    }

    private fun configureBrunch() {
        // Signal the main activity to update the nav drawer accordingly
        if(interactionListener != null){
            interactionListener?.configureBrunch(menuPayload.diningHallType, dateUtil.readDate())
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
            getString(R.string.pref_menu_date) -> {
                reloadMenu(!requireContext().db().menuDao().dateHasMenu(dateUtil.readDate()))
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "${menuPayload.diningHallType} ${menuPayload.itemType} Destroyed")
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