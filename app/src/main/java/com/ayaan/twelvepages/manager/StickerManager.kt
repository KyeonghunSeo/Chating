package com.ayaan.twelvepages.manager

import com.ayaan.twelvepages.R
import com.pixplicity.easyprefs.library.Prefs
import kotlin.collections.ArrayList

object StickerManager {

    class Sticker(val resId: Int, val packNum: Int)

    var packs = ArrayList<StickerPack>()
    var recentPack = ArrayList<Sticker>()

    init {
        Prefs.getString("stickerPacks", StickerPack.BASIC.name)
                .split(",")
                .mapTo(packs) { StickerManager.StickerPack.valueOf(it) }
        Prefs.getString("recentStickerPack", null)?.let { recentStickerPack ->
                recentStickerPack.split(",")
                        .mapTo(recentPack) {
                    val stickerKey = it.toInt()
                    StickerPack.values()[stickerKey / 10000].items[stickerKey % 10000]
                }
        }
    }

    enum class StickerPack(val titleId: Int, val type: Int, val items: Array<Sticker>) {
        BASIC(R.string.s_basic, 0, arrayOf(
                Sticker(R.drawable.s_basic_0, 0),
                Sticker(R.drawable.s_basic_1, 0),
                Sticker(R.drawable.s_basic_2, 0),
                Sticker(R.drawable.s_basic_3, 0),
                Sticker(R.drawable.s_basic_4, 0),
                Sticker(R.drawable.s_basic_5, 0),
                Sticker(R.drawable.s_basic_6, 0),
                Sticker(R.drawable.s_basic_7, 0),
                Sticker(R.drawable.s_basic_8, 0),
                Sticker(R.drawable.s_basic_9, 0),
                Sticker(R.drawable.s_basic_10, 0),
                Sticker(R.drawable.s_basic_11, 0),
                Sticker(R.drawable.s_basic_12, 0),
                Sticker(R.drawable.s_basic_13, 0),
                Sticker(R.drawable.s_basic_14, 0),
                Sticker(R.drawable.s_basic_15, 0),
                Sticker(R.drawable.s_basic_16, 0),
                Sticker(R.drawable.s_basic_17, 0),
                Sticker(R.drawable.s_basic_18, 0),
                Sticker(R.drawable.s_basic_19, 0),
                Sticker(R.drawable.s_basic_20, 0),
                Sticker(R.drawable.s_basic_21, 0),
                Sticker(R.drawable.s_basic_22, 0)
        )),
        WORK(R.string.s_work, 0, arrayOf(
                Sticker(R.drawable.s_basic_0, 1)
        ));
    }

    fun getSticker(stickerKey: Int) = StickerPack.values()[stickerKey / 10000].items[stickerKey % 10000]
    fun getStickerKey(sticker: Sticker) : Int {
        val pack = StickerPack.values()[sticker.packNum]
        val index = pack.items.indexOf(sticker)
        return pack.ordinal * 10000 + index
    }

    fun updateRecentSticker(sticker: Sticker) {
        recentPack.remove(sticker)
        recentPack.add(0, sticker)
        if(recentPack.size > 20) {
            recentPack.removeAt(recentPack.lastIndex)
        }
        Prefs.putString("recentStickerPack", recentPack.joinToString(",") {
            getStickerKey(it).toString()
        })
    }

    fun saveCurrentPack() {
        Prefs.putString("stickerPacks", packs.joinToString(","){it.name})
    }

    fun deletePack(pack: StickerPack) {
        packs.remove(pack)
        saveCurrentPack()
    }

}