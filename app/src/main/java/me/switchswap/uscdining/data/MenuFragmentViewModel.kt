package me.switchswap.uscdining.data

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import me.switchswap.diningmenu.models.DiningHallType
import me.switchswap.diningmenu.models.ItemType
import java.util.concurrent.atomic.AtomicBoolean

class MenuFragmentViewModel: ViewModel() {
    private lateinit var menuRepository: MenuRepository

    private val isLoading = AtomicBoolean(false)
    val loadingState = MutableLiveData<Boolean>()

    private val breakfastData = MutableLiveData<List<MenuItemAndAllergens>>()
    private var lunchData = MediatorLiveData<List<MenuItemAndAllergens>>()
    private val dinnerData = MutableLiveData<List<MenuItemAndAllergens>>()

    fun getMenuData(diningHallType: DiningHallType, itemType: ItemType, date: Long, forceRefresh: Boolean, cacheData: Boolean): LiveData<List<MenuItemAndAllergens>> {
        if (forceRefresh && !isLoading.get()) {
            viewModelScope.launch(IO) {
                isLoading.set(true)
                loadingState.postValue(true)
                menuRepository.getMenuFromWeb(date, true)
                loadingState.postValue(false)
                isLoading.set(false)

                // Update live data
                if (menuRepository.hallHasBrunch(diningHallType, date)) {
                    lunchData.postValue(menuRepository.getMenuFromDatabase(diningHallType, ItemType.BRUNCH, date))
                }
                else {
                    lunchData.postValue(menuRepository.getMenuFromDatabase(diningHallType, ItemType.LUNCH, date))
                }
                breakfastData.postValue(menuRepository.getMenuFromDatabase(diningHallType, ItemType.BREAKFAST, date))
                dinnerData.postValue(menuRepository.getMenuFromDatabase(diningHallType, ItemType.DINNER, date))
            }
        }

        if (menuRepository.hallHasBrunch(diningHallType, date)) {
            lunchData.value = menuRepository.getMenuFromDatabase(diningHallType, ItemType.BRUNCH, date)
        }
        else {
            lunchData.value = menuRepository.getMenuFromDatabase(diningHallType, ItemType.LUNCH, date)
        }
        breakfastData.value = menuRepository.getMenuFromDatabase(diningHallType, ItemType.BREAKFAST, date)
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