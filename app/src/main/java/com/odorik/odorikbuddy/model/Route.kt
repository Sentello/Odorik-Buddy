package com.odorik.odorikbuddy.model

data class Route(
    val id: Long,
    val publicNumber: String,
    val sourceNumber: String,
    val ringingNumber: String
)