package com.odorik.odorikbuddy.data.model

import com.google.gson.annotations.SerializedName

/**
 * A speed dial (`/speed_dials.json` or `/lines/<line>/speed_dials.json`).
 * [number] is a dial target like a routing `ringing_number`: it may be `*100`, `skype:…`
 * etc., so it is never filtered or validated in the app.
 *
 * Every field has a default (no-arg constructor for Gson, kept by proguard-rules.pro) and a
 * @SerializedName (R8 renames fields in release builds).
 */
data class SpeedDial(
    @SerializedName("shortcut") val shortcut: Int = 0,
    @SerializedName("number") val number: String = "",
    @SerializedName("name") val name: String? = null,
    /** Line endpoint only: "account" or "line". */
    @SerializedName("owner") val owner: String? = null,
    /** Line endpoint only: the account number this line's own speed dial overrides, else null. */
    @SerializedName("overrides") val overrides: String? = null
) {
    val isLineOwn: Boolean get() = owner == OWNER_LINE

    companion object {
        const val OWNER_LINE = "line"
    }
}
