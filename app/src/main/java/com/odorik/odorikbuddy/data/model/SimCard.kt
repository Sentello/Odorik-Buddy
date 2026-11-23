package com.odorik.odorikbuddy.data.model

import com.google.gson.annotations.SerializedName

/**
 * An Odorik mobile SIM (`/sim_cards.json`, `/sim_cards/<number>.json`).
 *
 * Built from the docs only (https://www.odorik.cz/w/api:sim_cards) — nobody on the team has
 * an Odorik SIM, so everything is nullable and unknown values are shown as they come.
 * `data_used` is deliberately not mapped: the docs only show `null` and give no unit.
 * Every field has a default (no-arg constructor for Gson, kept by proguard-rules.pro) and a
 * @SerializedName (R8 renames fields in release builds).
 */
data class SimCard(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("sim_number") val simNumber: String? = null,
    /** "active", "suspended", "new", … (full list not documented). */
    @SerializedName("state") val state: String? = null,
    @SerializedName("changes_in_progress") val changesInProgress: List<Any>? = null,
    @SerializedName("data_package") val dataPackage: String? = null,
    @SerializedName("data_package_for_next_month") val dataPackageNextMonth: String? = null,
    @SerializedName("voice_package") val voicePackage: String? = null,
    @SerializedName("voice_package_for_next_month") val voicePackageNextMonth: String? = null,
    @SerializedName("mobile_data") val mobileData: Boolean? = null,
    @SerializedName("lte") val lte: Boolean? = null,
    @SerializedName("missed_calls_register") val missedCallsRegister: Boolean? = null,
    /** A string such as "eu_data_outgoing", not a boolean. */
    @SerializedName("roaming") val roaming: String? = null,
    @SerializedName("premium_services") val premiumServices: String? = null
) {
    val hasChangesInProgress: Boolean get() = !changesInProgress.isNullOrEmpty()
    val isActive: Boolean get() = state == STATE_ACTIVE
    val isSuspended: Boolean get() = state == STATE_SUSPENDED

    companion object {
        const val STATE_ACTIVE = "active"
        const val STATE_SUSPENDED = "suspended"
    }
}

/** One record of `/sim_cards/<number>/mobile_data.json` (bytes are documented as bytes). */
data class SimMobileData(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("date") val date: String? = null,
    @SerializedName("bytes_total") val bytesTotal: Long? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null
)
