package com.hellowo.chating.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.hellowo.chating.model.ChatRoom
import io.realm.Realm
import java.util.*
import io.realm.Sort
import io.realm.SyncUser
import io.realm.RealmResults

class MainViewModel : ViewModel() {
    val realm = Realm.getDefaultInstance()
    var loading = MutableLiveData<Boolean>()

    init {
    }

    fun clear() {
        realm.close()
    }

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
        result.addChangeListener { _,changeSet ->
            Log.d("loadChatRoom", changeSet.isCompleteResult.toString())
        }
        return result
    }
}