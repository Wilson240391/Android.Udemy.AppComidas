package com.tutorials.eu.favdish.viewmodel

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.*
import com.tutorials.eu.favdish.R
import com.tutorials.eu.favdish.model.data.FavDish
import com.tutorials.eu.favdish.model.data.FavDishRepository
import kotlinx.coroutines.launch

class FavDishViewModel(private val repository: FavDishRepository, application: Application) : ViewModel() {

    val resources = application.applicationContext.resources

    fun isEntryValid(mImagePath: String, title: String, type: String, category: String, ingredients: String, cookingTimeInMinutes: String,
                     cookingDirection: String): String {
        var result: String = ""

        when {
            TextUtils.isEmpty(mImagePath) -> {
                result = resources.getString(R.string.err_msg_select_dish_image)
            }
            TextUtils.isEmpty(title) -> {
                result = resources.getString(R.string.err_msg_enter_dish_title)
            }
            TextUtils.isEmpty(type) -> {
                result = resources.getString(R.string.err_msg_select_dish_type)
            }
            TextUtils.isEmpty(category) -> {
                result = resources.getString(R.string.err_msg_select_dish_category)
            }
            TextUtils.isEmpty(ingredients) -> {
                result = resources.getString(R.string.err_msg_enter_dish_ingredients)
            }
            TextUtils.isEmpty(cookingTimeInMinutes) -> {
                result = resources.getString(R.string.err_msg_select_dish_cooking_time)
            }
            TextUtils.isEmpty(cookingDirection) -> {
                result = resources.getString(R.string.err_msg_enter_dish_cooking_instructions)
            }
        }
        return result
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way.
     */
    fun insert(dish: FavDish) = viewModelScope.launch {
        // Call the repository function and pass the details.
        repository.insertFavDishData(dish)
    }

    /** Using LiveData and caching what allDishes returns has several benefits:
     * We can put an observer on the data (instead of polling for changes) and only
     * update the UI when the data actually changes.
     * Repository is completely separated from the UI through the ViewModel.
     */
    val allDishesList: LiveData<List<FavDish>> = repository.allDishesList.asLiveData()

    /**
     * Launching a new coroutine to update the data in a non-blocking way
     */
    fun update(dish: FavDish) = viewModelScope.launch {
        repository.updateFavDishData(dish)
    }

    /** Using LiveData and caching what favoriteDishes returns has several benefits:
     * We can put an observer on the data (instead of polling for changes) and only
     * update the UI when the data actually changes.
     * Repository is completely separated from the UI through the ViewModel.
     */
    val favoriteDishes: LiveData<List<FavDish>> = repository.favoriteDishes.asLiveData()

    /**
     * Launching a new coroutine to delete the data in a non-blocking way.
     */
    fun delete(dish: FavDish) = viewModelScope.launch {
        // Call the repository function and pass the details.
        repository.deleteFavDishData(dish)
    }

    // TODO Step 3: Get the filtered list of dishes based on the dish type selection.
    // START
    /**
     * A function to get the filtered list of dishes based on the dish type selection.
     *
     * @param value - dish type selection
     */
    fun getFilteredList(value: String): LiveData<List<FavDish>> = repository.filteredListDishes(value).asLiveData()
}

/**
 * To create the ViewModel we implement a ViewModelProvider.Factory that gets as a parameter the dependencies
 * needed to create FavDishViewModel: the FavDishRepository.
 * By using viewModels and ViewModelProvider.Factory then the framework will take care of the lifecycle of the ViewModel.
 * It will survive configuration changes and even if the Activity is recreated,
 * you'll always get the right instance of the FavDishViewModel class.
 */
class FavDishViewModelFactory(private val repository: FavDishRepository, private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavDishViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavDishViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}