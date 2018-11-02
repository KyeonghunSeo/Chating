package com.hellowo.journey

const val INSTANCE_ADDRESS = "chating.us1.cloud.realm.io"
const val AUTH_URL = "https://$INSTANCE_ADDRESS/auth"
const val USER_URL = "realms://$INSTANCE_ADDRESS/~/default"

const val SEC_MILL = 1000L
const val MIN_MILL = SEC_MILL * 60
const val HOUR_MILL = MIN_MILL * 60
const val DAY_MILL = HOUR_MILL * 24
const val WEEK_MILL = DAY_MILL * 7
const val YEAR_MILL = WEEK_MILL * 365

const val ANIM_DUR = 220L

const val ID = "id"
const val TIME = "time"

const val RC_PERMISSIONS = 0
const val RC_FILEPICKER = 9090
const val RC_LOCATION = 9091

var mainBarHeight = dpToPx(50)
var smallMargin = dpToPx(10f)
var normalMargin = dpToPx(16f)
var bigMargin = dpToPx(20f)
var extraMargin = dpToPx(32f)

enum class ViewMode {
    CLOSED, OPENED, ANIMATING
}