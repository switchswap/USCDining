package me.switchswap.uscdining.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import me.switchswap.diningmenu.models.DiningHallType
import me.switchswap.diningmenu.models.ItemType

class MenuFragmentViewModel: ViewModel() {
    private lateinit var menuRepository: MenuRepository

    val loadingState = MutableLiveData<Boolean>()

    fun getMenuData(diningHallType: DiningHallType, itemType: ItemType, date: Long, forceRefresh: Boolean): LiveData<List<MenuItemAndAllergens>> {
        if (forceRefresh) {
            viewModelScope.launch(IO) {
                loadingState.postValue(true)
                // Todo: Make caching variable again!
                menuRepository.getMenuFromWeb(date, true)
                loadingState.postValue(false)
            }
        }
        return menuRepository.getMenuFromDatabase(diningHallType, itemType, date)
    }

    fun setManager(menuDao: MenuDao) {
        menuRepository = MenuRepository(menuDao)
    }
}