package com.hellowo.chating.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Log
import com.hellowo.chating.USER_URL
import com.hellowo.chating.model.ChatRoom
import io.realm.*
import java.util.*

class MainViewModel : ViewModel() {
    val realm = Realm.getDefaultInstance()
    var loading = MutableLiveData<Boolean>()

    init {
        SyncUser.current()?.let {
            val config = SyncConfiguration.Builder(it, USER_URL).partialRealm().build()
            Realm.setDefaultConfiguration(config)
        }
    }

    fun clear() {}

    fun insert(name: String) {
        realm.executeTransactionAsync { realm ->
            val chatRoom = ChatRoom(UUID.randomUUID().toString(), name, Date(), null)
            realm.insert(chatRoom)
        }
    }

    fun loadChatRoom(): RealmResults<ChatRoom>? {
        val result = realm.where(ChatRoom::class.java)
                .sort("timestamp", Sort.DESCENDING)
                .findAllAsync()
        result.addChangeListener { result ,changeSet ->
            Log.d("result.isLoaded", result.isLoaded.toString())
            Log.d("changeSet", changeSet.isCompleteResult.toString())
        }
        return result
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }
}