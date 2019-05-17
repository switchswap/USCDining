package me.switchswap.uscdining.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import me.switchswap.uscdining.R
import me.switchswap.uscdining.models.DiningHallType
import me.switchswap.uscdining.models.MealType

class MenuFragment : Fragment() {
    companion object{
        fun newInstance(diningHallType: DiningHallType, mealType: MealType): MenuFragment{
            val fragment = MenuFragment()
            val bundle = Bundle(1)
            bundle.putString("msg", diningHallType.id.toString() + mealType.typeName)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View? = inflater.inflate(R.layout.fragment_menu, container, false)
        val textView: TextView? = view?.findViewById(R.id.menu_fragment_text)
        textView?.text =  arguments?.getString("msg")
        return view
    }

    fun setText(text: String){
        view?.findViewById<TextView>(R.id.menu_fragment_text)?.text = text
    }
}

