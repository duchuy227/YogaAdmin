package com.example.yogaappadmin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yogaappadmin.data.FirebaseService
import com.example.yogaappadmin.data.YogaClass
import com.example.yogaappadmin.data.YogaDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class YogaViewModel(private val yogaDao: YogaDao, private val firebaseService: FirebaseService) : ViewModel() {
    val allYogaClasses: Flow<List<YogaClass>> = yogaDao.getAllYogaClasses()
    fun getAllYogaClasses(onResult: (List<YogaClass>) -> Unit) {
        viewModelScope.launch {
            yogaDao.getAllYogaClasses().collect { classes ->
                onResult(classes)
            }
        }
    }

    fun insertYogaClass(yogaClass: YogaClass) {
        viewModelScope.launch {
            val newId = yogaDao.insertYogaClass(yogaClass)
            val updatedClass = yogaClass.copy(id = newId.toInt())

        }
    }

    fun updateYogaClass(yogaClass: YogaClass) {
        viewModelScope.launch {
            try {
                println("Updating class: $yogaClass")
                yogaDao.updateYogaClass(yogaClass)
//                firebaseService.syncWithFirestore(listOf(yogaClass)) //
                println("Update completed!")
            } catch (e: Exception) {
                println("Error updating class: ${e.message}")
            }
        }
    }


    fun deleteYogaClass(id: Int) {
        viewModelScope.launch {
            yogaDao.deleteYogaClass(id)
            // Xóa từ Firestore
//            firebaseService.deleteFromFirestore(id)
        }
    }

    fun syncWithFirestore() {
        viewModelScope.launch {
            val yogaClasses = yogaDao.getAllYogaClasses().collect { classes ->
                firebaseService.syncWithFirestore(classes)
            }
        }
    }

    fun syncFromFirestoreToLocal() {
        viewModelScope.launch {
            try {
                val firebaseClasses = firebaseService.getAllFromFirestore()
                for (yogaClass in firebaseClasses) {
                    yogaDao.insertYogaClass(yogaClass) // Lưu vào SQLite
                }
            } catch (e: Exception) {
                println("Error syncing from Firestore: ${e.message}")
            }
        }
    }
}

