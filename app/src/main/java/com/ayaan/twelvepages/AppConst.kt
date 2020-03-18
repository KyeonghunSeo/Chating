package com.ayaan.twelvepages

const val INSTANCE_ADDRESS = "hellowo.de1a.cloud.realm.io"
const val AUTH_URL = "https://$INSTANCE_ADDRESS/auth"
const val USER_URL = "realms://$INSTANCE_ADDRESS/~/default"

const val SEC_MILL = 1000L
const val MIN_MILL = SEC_MILL * 60
const val HOUR_MILL = MIN_MILL * 60
const val DAY_MILL = HOUR_MILL * 24
const val WEEK_MILL = DAY_MILL * 7
const val YEAR_MILL = DAY_MILL * 365

const val ANIM_DUR = 275L

const val ID = "id"
const val TIME = "time"

const val NONE = 0
const val NO = 1
const val YES = 2

const val RC_PERMISSIONS = 0
const val RC_LOGIN = 9080
const val RC_LOGOUT = 9081
const val RC_PROFILE_IMAGE = 9090
const val RC_LOCATION = 9091
const val RC_OS_CALENDAR = 9092
const val RC_IMAGE_ATTACHMENT = 9093
const val RC_SETTING = 9094
const val RC_SHARE = 9095
const val RC_EXPORT_PERMISSION = 9096
const val RC_PHOTO_ON_DAYVIEW = 9097

const val RESULT_CALENDAR_SETTING = 100
const val RESULT_DAYVIEW_SETTING = 101

var mainBarHeight = dpToPx(50)
var smallMargin = dpToPx(10f)
var normalMargin = dpToPx(16f)
var bigMargin = dpToPx(20f)
var extraMargin = dpToPx(32f)

enum class ViewMode { CLOSED, OPENED, ANIMATING }