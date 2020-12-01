package com.example.app.jasper.voicetotext

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.app.jasper.voicetotext.model.Language

class ViewModel(application: Application) : AndroidViewModel(application) {

    val fullList = ArrayList<Language>()
    val filteredList = ArrayList<Language>()
    val resultList = MutableLiveData<List<Language>>()
    val currentLanguage = MutableLiveData<Language>()

    fun setCurrentLanguage(language: Language) {
        currentLanguage.value = language
    }

    fun sortListBy(textEntered: String) {
        filteredList.clear()
        for (i in 0 until fullList.size) {
            val language = fullList[i].displayName.toLowerCase()
            if (language.contains(textEntered)) {
                filteredList.add(fullList[i])
            }
        }
        resultList.value = filteredList
    }



    fun setInitialList(initialList: ArrayList<Language>) {
        for (i in 0 until initialList.size) {
            initialList[i].displayName = initialList[i].displayName
            fullList.add(initialList[i])
        }
    }
}