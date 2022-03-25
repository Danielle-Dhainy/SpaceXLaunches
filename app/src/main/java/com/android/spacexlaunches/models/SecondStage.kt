package com.android.spacexlaunches.models

data class SecondStage(
    val burn_time_sec: Any,
    val engines: Int,
    val fuel_amount_tons: Int,
    val payloads: Payloads,
    val reusable: Boolean,
    val thrust: Thrust
)