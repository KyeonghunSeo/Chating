package com.ayaan.twelvepages.manager

import com.ayaan.twelvepages.App
import com.ayaan.twelvepages.R
import com.pixplicity.easyprefs.library.Prefs
import java.lang.Exception
import kotlin.collections.ArrayList

object StickerManager {

    class Sticker(val resId: Int, val packNum: Int)

    var packs = ArrayList<StickerPack>()
    var recentPack = ArrayList<Sticker>()
    val packageName = App.context.packageName
    val resource = App.resource

    init {
        Prefs.getString("stickerPacks", StickerPack.BASIC.name)
                .split(",")
                .mapTo(packs) {
                    try{
                        StickerPack.valueOf(it)
                    }catch (e: Exception) {
                        StickerPack.BASIC
                    }
                }
        Prefs.getString("recentStickerPack", null)?.let { recentStickerPack ->
                recentStickerPack.split(",")
                        .mapTo(recentPack) {
                            val stickerKey = it.toInt()
                            try{
                                StickerPack.values()[stickerKey / 10000].items[stickerKey % 10000]
                            }catch (e: Exception) {
                                StickerPack.BASIC.items.first()
                            }
                }
        }
    }

    enum class StickerPack(val titleId: Int, val type: Int, val items: Array<Sticker>) {
        BASIC(R.string.s_basic, 0, Array(100) { i -> Sticker(resource.getIdentifier("s_basic_$i", "drawable", packageName), 0) } ),
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
        )),
        CALIGRAPHY(R.string.s_caligraphy, 0, Array(16){ i -> Sticker(resource.getIdentifier("s_cali_$i", "drawable", packageName), 3) } ),
        BODY(R.string.s_body, 0, arrayOf(
                Sticker(R.drawable.s_body_0, 4),
                Sticker(R.drawable.s_body_1, 4),
                Sticker(R.drawable.s_body_2, 4),
                Sticker(R.drawable.s_body_3, 4),
                Sticker(R.drawable.s_body_4, 4),
                Sticker(R.drawable.s_body_5, 4),
                Sticker(R.drawable.s_body_6, 4),
                Sticker(R.drawable.s_body_7, 4),
                Sticker(R.drawable.s_body_8, 4),
                Sticker(R.drawable.s_body_9, 4),
                Sticker(R.drawable.s_body_10, 4),
                Sticker(R.drawable.s_body_11, 4),
                Sticker(R.drawable.s_body_12, 4),
                Sticker(R.drawable.s_body_13, 4),
                Sticker(R.drawable.s_body_14, 4),
                Sticker(R.drawable.s_body_15, 4),
                Sticker(R.drawable.s_body_16, 4),
                Sticker(R.drawable.s_body_17, 4),
                Sticker(R.drawable.s_body_18, 4),
                Sticker(R.drawable.s_body_19, 4),
                Sticker(R.drawable.s_body_20, 4),
                Sticker(R.drawable.s_body_21, 4),
                Sticker(R.drawable.s_body_22, 4),
                Sticker(R.drawable.s_body_23, 4),
                Sticker(R.drawable.s_body_24, 4)
        )),
        FOOD(R.string.s_food, 0, arrayOf(
                Sticker(R.drawable.s_food_0, 5),
                Sticker(R.drawable.s_food_1, 5),
                Sticker(R.drawable.s_food_2, 5),
                Sticker(R.drawable.s_food_3, 5),
                Sticker(R.drawable.s_food_4, 5),
                Sticker(R.drawable.s_food_5, 5),
                Sticker(R.drawable.s_food_6, 5),
                Sticker(R.drawable.s_food_7, 5),
                Sticker(R.drawable.s_food_8, 5),
                Sticker(R.drawable.s_food_9, 5),
                Sticker(R.drawable.s_food_10, 5),
                Sticker(R.drawable.s_food_11, 5),
                Sticker(R.drawable.s_food_12, 5),
                Sticker(R.drawable.s_food_13, 5),
                Sticker(R.drawable.s_food_14, 5),
                Sticker(R.drawable.s_food_15, 5),
                Sticker(R.drawable.s_food_16, 5),
                Sticker(R.drawable.s_food_17, 5),
                Sticker(R.drawable.s_food_18, 5),
                Sticker(R.drawable.s_food_19, 5),
                Sticker(R.drawable.s_food_20, 5),
                Sticker(R.drawable.s_food_21, 5),
                Sticker(R.drawable.s_food_22, 5),
                Sticker(R.drawable.s_food_23, 5),
                Sticker(R.drawable.s_food_24, 5)
        )),
        WEATHER(R.string.s_weather, 0, Array(27) { i -> Sticker(resource.getIdentifier("s_weather_$i", "drawable", packageName), 6) } ),
        DOGS(R.string.s_dogs, 0, Array(30) { i -> Sticker(resource.getIdentifier("s_dog_$i", "drawable", packageName), 7) } ),
        PEOPLE(R.string.s_peaple, 0, Array(20) { i -> Sticker(resource.getIdentifier("s_people_$i", "drawable", packageName), 8) } ),
        PET(R.string.s_pet, 0, Array(50) { i -> Sticker(resource.getIdentifier("s_pet_$i", "drawable", packageName), 9) } )
    }

    fun getSticker(stickerKey: Int) = try {
        StickerPack.values()[stickerKey / 10000].items[stickerKey % 10000]
    }catch (e: Exception){
        StickerPack.BASIC.items.first()
    }

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