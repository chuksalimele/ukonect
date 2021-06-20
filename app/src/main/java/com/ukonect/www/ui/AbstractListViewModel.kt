package com.ukonect.www.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class AbstractListViewModel <T>:ViewModel() {

    abstract fun getList(): MutableLiveData<MutableList<T>>

}