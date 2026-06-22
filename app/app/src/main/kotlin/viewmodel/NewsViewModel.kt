package com.example.appcantina.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcantina.data.local.NewsEntity
import com.example.appcantina.data.repository.CanteenRepository
import kotlinx.coroutines.launch

class NewsViewModel(
    private val repository: CanteenRepository
) : ViewModel() {
    val news = repository.observeNews()

    var message by mutableStateOf<String?>(null)
        private set

    fun saveNews(title: String, body: String) {
        if (title.isBlank() || body.isBlank()) {
            message = "Preencha titulo e mensagem."
            return
        }

        viewModelScope.launch {
            repository.saveNews(title, body)
            message = "Aviso publicado."
        }
    }

    fun deleteNews(news: NewsEntity) {
        viewModelScope.launch {
            repository.deleteNews(news)
            message = "Aviso removido."
        }
    }

    fun clearMessage() {
        message = null
    }
}
