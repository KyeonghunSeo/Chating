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
        SCHOOL(R.string.s_school, 0, Array(36) { i -> Sticker(resource.getIdentifier("s_school_$i", "drawable", packageName), 1) } ),
        WORK(R.string.s_work, 0, Array(50){ i -> Sticker(resource.getIdentifier("s_buisiness_$i", "drawable", packageName), 2) } ),
        CALIGRAPHY(R.string.s_caligraphy, 0, Array(16){ i -> Sticker(resource.getIdentifier("s_cali_$i", "drawable", packageName), 3) } ),
        BODY(R.string.s_body, 0, Array(25){ i -> Sticker(resource.getIdentifier("s_body_$i", "drawable", packageName), 4) } ),
        FOOD(R.string.s_food, 0, Array(35){ i -> Sticker(resource.getIdentifier("s_food_$i", "drawable", packageName), 5) } ),
        WEATHER(R.string.s_weather, 0, Array(24) { i -> Sticker(resource.getIdentifier("s_weather_$i", "drawable", packageName), 6) } ),
        DOGS(R.string.s_dogs, 0, Array(30) { i -> Sticker(resource.getIdentifier("s_dog_$i", "drawable", packageName), 7) } ),
        PEOPLE(R.string.s_peaple, 0, Array(20) { i -> Sticker(resource.getIdentifier("s_people_$i", "drawable", packageName), 8) } ),
        PET(R.string.s_pet, 0, Array(50) { i -> Sticker(resource.getIdentifier("s_pet_$i", "drawable", packageName), 9) } ),
        AGRI(R.string.s_agri, 0, Array(80) { i -> Sticker(resource.getIdentifier("s_agri_$i", "drawable", packageName), 10) } ),
        OFFICE(R.string.s_office, 0, Array(30) { i -> Sticker(resource.getIdentifier("s_office_$i", "drawable", packageName), 11) } ),
        BAKERY(R.string.s_bakery, 0, Array(36) { i -> Sticker(resource.getIdentifier("s_bakery_$i", "drawable", packageName), 12) } ),
        MENTAL(R.string.s_mental, 0, Array(36) { i -> Sticker(resource.getIdentifier("s_mental_$i", "drawable", packageName), 13) } ),
        OPERATION(R.string.s_operation, 0, Array(30) { i -> Sticker(resource.getIdentifier("s_operation_$i", "drawable", packageName), 14) } ),
        CLOTH(R.string.s_cloth, 0, Array(100) { i -> Sticker(resource.getIdentifier("s_cloth_$i", "drawable", packageName), 15) } )
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