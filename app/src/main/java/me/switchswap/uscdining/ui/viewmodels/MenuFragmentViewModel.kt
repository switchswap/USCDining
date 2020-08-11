package me.switchswap.uscdining.ui.viewmodels

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import me.switchswap.diningmenu.models.DiningHallType
import me.switchswap.diningmenu.models.ItemType
import me.switchswap.uscdining.data.MenuDao
import me.switchswap.uscdining.data.MenuItemAndAllergens
import me.switchswap.uscdining.data.MenuRepository
import java.util.concurrent.atomic.AtomicBoolean

class MenuFragmentViewModel: ViewModel() {
    private lateinit var menuRepository: MenuRepository

    private val isLoading = AtomicBoolean(false)
    val loadingState = MutableLiveData<Boolean>()

    private val breakfastData = MutableLiveData<List<MenuItemAndAllergens>>()
    private val lunchData = MediatorLiveData<List<MenuItemAndAllergens>>()
    private val dinnerData = MutableLiveData<List<MenuItemAndAllergens>>()

    fun getMenuData(diningHallType: DiningHallType, itemType: ItemType, date: Long, forceRefresh: Boolean, cacheData: Boolean): LiveData<List<MenuItemAndAllergens>> {
        if (forceRefresh && !isLoading.get()) {
            viewModelScope.launch(IO) {
                isLoading.set(true)
                loadingState.postValue(true)
                menuRepository.getMenuFromWeb(date, cacheData)
                loadingState.postValue(false)
                isLoading.set(false)

                // Update live data
                if (menuRepository.hallHasBrunch(diningHallType, date)) {
                    breakfastData.postValue(menuRepository.getMenuFromDatabase(diningHallType, ItemType.BRUNCH, date))
                }
                else {
                    breakfastData.postValue(menuRepository.getMenuFromDatabase(diningHallType, ItemType.BREAKFAST, date))
                }
                lunchData.postValue(menuRepository.getMenuFromDatabase(diningHallType, ItemType.LUNCH, date))
                dinnerData.postValue(menuRepository.getMenuFromDatabase(diningHallType, ItemType.DINNER, date))
            }
        }

        // Update live data
        if (menuRepository.hallHasBrunch(diningHallType, date)) {
            breakfastData.value = menuRepository.getMenuFromDatabase(diningHallType, ItemType.BRUNCH, date)
        }
        else {
            breakfastData.value = menuRepository.getMenuFromDatabase(diningHallType, ItemType.BREAKFAST, date)
        }
        lunchData.value = menuRepository.getMenuFromDatabase(diningHallType, ItemType.LUNCH, date)
        dinnerData.value = menuRepository.getMenuFromDatabase(diningHallType, ItemType.DINNER, date)

        return when (itemType) {
            ItemType.BREAKFAST -> breakfastData
            ItemType.LUNCH -> lunchData
            ItemType.DINNER -> dinnerData
            else -> MutableLiveData()
        }
    }


    fun setManager(menuDao: MenuDao) {
        menuRepository = MenuRepository(menuDao)
    }

    companion object {
        val TAG = MenuFragmentViewModel::class.java.simpleName
    }
}