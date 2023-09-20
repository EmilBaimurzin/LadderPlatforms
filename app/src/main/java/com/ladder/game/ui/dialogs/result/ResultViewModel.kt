package com.ladder.game.ui.dialogs.result

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ladder.game.domain.DataRepository
import kotlinx.coroutines.launch

class ResultViewModel (private val yourResult: Int): ViewModel() {
    private val repository = DataRepository()
    private val _results = MutableLiveData<List<Pair<Int, Int>>>()
    val results: LiveData<List<Pair<Int, Int>>> = _results

    init {
        viewModelScope.launch {
            _results.postValue(repository.getResultList(yourResult))
        }
    }
}

class ResultViewModelFactory(private val yourResult: Int): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ResultViewModel(yourResult) as T
    }
}