package org.example.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CounterViewModel : ViewModel() {

    val container = CounterContainer()

    init {
        viewModelScope.launch {
            container.store.start(this).awaitUntilClosed()
        }
    }

    override fun onCleared() {
        super.onCleared()
        container.store.close()
    }
}
