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
                .mapTo(packs) { StickerPack.valueOf(it) }
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
                Sticker(R.drawable.cat, 0),
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
        FINANCE(R.string.s_finance, 0, arrayOf(
                Sticker(R.drawable.s_finance_0, 1),
                Sticker(R.drawable.s_finance_1, 1),
                Sticker(R.drawable.s_finance_2, 1),
                Sticker(R.drawable.s_finance_3, 1),
                Sticker(R.drawable.s_finance_4, 1),
                Sticker(R.drawable.s_finance_5, 1),
                Sticker(R.drawable.s_finance_6, 1),
                Sticker(R.drawable.s_finance_7, 1),
                Sticker(R.drawable.s_finance_8, 1),
                Sticker(R.drawable.s_finance_9, 1),
                Sticker(R.drawable.s_finance_10, 1),
                Sticker(R.drawable.s_finance_11, 1)
        )),
        WORK(R.string.s_work, 0, arrayOf(
                Sticker(R.drawable.s_business_0, 2),
                Sticker(R.drawable.s_business_1, 2),
                Sticker(R.drawable.s_business_2, 2),
                Sticker(R.drawable.s_business_3, 2),
                Sticker(R.drawable.s_business_4, 2),
                Sticker(R.drawable.s_business_5, 2),
                Sticker(R.drawable.s_business_6, 2),
                Sticker(R.drawable.s_business_7, 2),
                Sticker(R.drawable.s_business_8, 2),
                Sticker(R.drawable.s_business_9, 2),
                Sticker(R.drawable.s_business_10, 2),
                Sticker(R.drawable.s_business_11, 2),
                Sticker(R.drawable.s_business_12, 2),
                Sticker(R.drawable.s_business_13, 2),
                Sticker(R.drawable.s_business_15, 2),
                Sticker(R.drawable.s_business_16, 2)
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