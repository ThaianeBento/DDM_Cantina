package com.example.appcantina.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appcantina.data.local.AppDatabase
import com.example.appcantina.data.remote.RetrofitClient
import com.example.appcantina.data.repository.AuthRepository
import com.example.appcantina.data.repository.CanteenRepository

class AppViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val appContext = context.applicationContext
    private val database = AppDatabase.getDatabase(appContext)
    private val canteenRepository = CanteenRepository(database, RetrofitClient.api)
    private val authRepository = AuthRepository()

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SessionViewModel::class.java) ->
                SessionViewModel(authRepository) as T

            modelClass.isAssignableFrom(MenuViewModel::class.java) ->
                MenuViewModel(canteenRepository) as T

            modelClass.isAssignableFrom(NewsViewModel::class.java) ->
                NewsViewModel(canteenRepository) as T

            modelClass.isAssignableFrom(FormViewModel::class.java) ->
                FormViewModel(canteenRepository, appContext) as T

            modelClass.isAssignableFrom(OrderViewModel::class.java) ->
                OrderViewModel(canteenRepository) as T

            else -> throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
        }
    }
}
