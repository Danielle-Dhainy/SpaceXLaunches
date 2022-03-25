package com.android.spacexlaunches.models

data class LaunchItem(
    /* var auto_update: Boolean,
     var capsules: List<Any>,
     var cores: List<Core>,
     var crew: List<Any>,
     var date_local: String,
     var date_precision: String,*/
    // var date_unix: Int,
    var date_utc: String,
     var details: String?=null,
    /* var failures: List<Any>,
    // var fairings: Fairings,*/
    var flight_number: Int,
    var id: String,
    /* var launch_library_id: String,
     var launchpad: String,*/
    var links: Links,
    var name: String,
    /*var net: Boolean,
    var payloads: List<String>,*/
    var rocket: String,
    /* var ships: List<Any>,
     var static_fire_date_unix: Int,
     var static_fire_date_utc: String,*/
    var success: Boolean,
    /*  var tbd: Boolean,*/
    var upcoming: Boolean,
    /*var window: Int,*/
    // added by me
    var progress: Int = 0
)