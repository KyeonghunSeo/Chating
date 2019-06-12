package com.ayaan.twelvepages.manager

import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.l
import com.pixplicity.easyprefs.library.Prefs

object StickerManager {

    class Sticker(val resId: Int, val packNum: Int)

    var packs = ArrayList<StickerPack>()
    var recentPack = ArrayList<Sticker>()

    init {
        Prefs.getString("stickerPacks", StickerPack.BASIC.name)
                .split(",")
                .mapTo(packs) {
                    StickerManager.StickerPack.valueOf(it)
                }
        Prefs.getString("recentStickerPack", null)?.let { recentStickerPack ->
                recentStickerPack.split(",")
                        .mapTo(recentPack) {
                    val s = it.split(":")
                    StickerPack.valueOf(s[0]).items[s[1].toInt()]
                }
        }
    }

    enum class StickerPack(val titleId: Int, val type: Int, val items: Array<Sticker>) {
        BASIC(R.string.s_basic, 0, arrayOf(
                Sticker(R.drawable.s_1201, 0),
                Sticker(R.drawable.s_1202, 0),
                Sticker(R.drawable.s_1203, 0),
                Sticker(R.drawable.s_1204, 0),
                Sticker(R.drawable.s_1205, 0),
                Sticker(R.drawable.s_1206, 0),
                Sticker(R.drawable.s_1207, 0),
                Sticker(R.drawable.s_1208, 0),
                Sticker(R.drawable.s_1209, 0),
                Sticker(R.drawable.s_1210, 0),
                Sticker(R.drawable.s_1211, 0),
                Sticker(R.drawable.s_1212, 0),
                Sticker(R.drawable.s_1213, 0),
                Sticker(R.drawable.s_1214, 0),
                Sticker(R.drawable.s_1215, 0),
                Sticker(R.drawable.s_1216, 0),
                Sticker(R.drawable.s_1217, 0),
                Sticker(R.drawable.s_1218, 0),
                Sticker(R.drawable.s_1219, 0),
                Sticker(R.drawable.s_1220, 0)
        )),
        WORK(R.string.s_work, 0, arrayOf(
                Sticker(R.drawable.s_1212, 0),
                Sticker(R.drawable.s_1201, 0),
                Sticker(R.drawable.s_1201, 0),
                Sticker(R.drawable.s_1201, 0),
                Sticker(R.drawable.s_1201, 0),
                Sticker(R.drawable.s_1201, 0),
                Sticker(R.drawable.s_1201, 0),
                Sticker(R.drawable.s_1201, 0),
                Sticker(R.drawable.s_1201, 0),
                Sticker(R.drawable.s_1201, 0),
                Sticker(R.drawable.s_1201, 0)
        ));

        companion object {
            fun getRecentStickerKey(sticker: Sticker) : String {
                val pack = values()[sticker.packNum]
                val index = pack.items.indexOf(sticker)
                l("${pack.name}:$index")
                return "${pack.name}:$index"
            }
        }
    }

    fun updateRecentSticker(sticker: Sticker) {
        l("updateRecentSticker"+sticker.packNum)
        recentPack.remove(sticker)
        recentPack.add(0, sticker)
        if(recentPack.size > 20) {
            recentPack.removeAt(recentPack.lastIndex)
        }
        Prefs.putString("recentStickerPack", recentPack.joinToString(",") {
            StickerPack.getRecentStickerKey(it)
        })
    }

}