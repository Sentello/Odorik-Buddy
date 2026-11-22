package com.odorik.odorikbuddy.data.backup

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.odorik.odorikbuddy.BuildConfig
import com.odorik.odorikbuddy.data.local.AppPreferences
import com.odorik.odorikbuddy.data.local.AppTheme
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.data.local.ThemeManager
import com.odorik.odorikbuddy.data.local.ThemeMode
import com.odorik.odorikbuddy.data.local.TileDao
import com.odorik.odorikbuddy.data.repository.UserRepository
import com.odorik.odorikbuddy.ui.calls.CallViewModel
import com.odorik.odorikbuddy.ui.calls.TileSize
import com.odorik.odorikbuddy.ui.widget.WidgetUpdateManager
import com.odorik.odorikbuddy.worker.UpdateWorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

data class BackupPreview(
    val tileCount: Int,
    val createdAt: String?,
    val backupUsername: String?,
    val differentAccount: Boolean
)

sealed interface ReadResult {
    data class Ok(val backup: SettingsBackup, val preview: BackupPreview) : ReadResult
    data object NotABackup : ReadResult
    data object NewerVersion : ReadResult
    data object TooLarge : ReadResult
    data object ReadFailed : ReadResult
}

data class ImportOptions(val tiles: Boolean, val settings: Boolean)

data class ImportReport(
    val tilesImported: Int?,
    val directCallsSkipped: Boolean,
    val autoUpdateSkipped: Boolean,
    val languageToApply: String?
)

@Singleton
class SettingsBackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences,
    private val themeManager: ThemeManager,
    private val localeManager: LocaleManager,
    private val userRepository: UserRepository,
    private val tileDao: TileDao,
    private val updateWorkManager: UpdateWorkManager,
    private val widgetUpdateManager: WidgetUpdateManager
) {

    suspend fun createBackup(): SettingsBackup = withContext(Dispatchers.IO) {
        val tiles = tileDao.getAllTiles().first().map(BackupTileMapper::toBackup)
        SettingsBackup(
            app = BackupCodec.APP_MARKER,
            formatVersion = BackupCodec.FORMAT_VERSION,
            createdAt = Instant.now().toString(),
            appVersion = BuildConfig.VERSION_NAME,
            account = BackupAccount(username = userRepository.getUserId()),
            display = BackupDisplay(
                themeMode = themeManager.themeMode.value.name,
                appTheme = themeManager.appTheme.value.name,
                language = localeManager.getPreferredLanguage(),
                tileSize = TileSize.fromKey(appPreferences.getString(AppPreferences.KEY_TILE_SIZE)).name
            ),
            appSettings = BackupAppSettings(
                historyPeriodDays = appPreferences.historyPeriodDays,
                autoUpdateEnabled = appPreferences.autoUpdateEnabled,
                directCallsEnabled = appPreferences.directCallsEnabled
            ),
            calls = BackupCalls(
                personalPhoneNumber = appPreferences.getString(KEY_PHONE_NUMBER),
                callerId = appPreferences.getString(KEY_CALLER_ID),
                callbackRecipient = appPreferences.getString(KEY_RECIPIENT),
                oneShotRecipient = appPreferences.getString(KEY_ONESHOT_RECIPIENT),
                selectedLine = appPreferences.getString(KEY_SELECTED_LINE),
                useCallerIdPrefix = appPreferences.getString(KEY_USE_CALLER_ID_PREFIX)?.toBoolean(),
                tabOrder = appPreferences.getString(KEY_TAB_ORDER)
            ),
            tiles = tiles
        )
    }

    suspend fun writeBackup(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val backup = createBackup()
            val json = BackupCodec.encode(backup)
            val stream = context.contentResolver.openOutputStream(uri, "wt")
                ?: return@withContext Result.failure(IOException("No output stream"))
            stream.use { it.write(json.toByteArray(Charsets.UTF_8)) }
            Result.success(backup.tiles.orEmpty().size)
        } catch (e: IOException) {
            Log.w(TAG, "Backup export failed", e)
            Result.failure(e)
        } catch (e: SecurityException) {
            Log.w(TAG, "Backup export failed", e)
            Result.failure(e)
        }
    }

    suspend fun readBackup(uri: Uri): ReadResult = withContext(Dispatchers.IO) {
        val bytes = try {
            val stream = context.contentResolver.openInputStream(uri)
                ?: return@withContext ReadResult.ReadFailed
            stream.use { it.readNBytesCompat(BackupCodec.MAX_BYTES + 1) }
        } catch (e: IOException) {
            Log.w(TAG, "Backup read failed", e)
            return@withContext ReadResult.ReadFailed
        } catch (e: SecurityException) {
            Log.w(TAG, "Backup read failed", e)
            return@withContext ReadResult.ReadFailed
        }
        if (bytes.size > BackupCodec.MAX_BYTES) return@withContext ReadResult.TooLarge

        when (val decoded = BackupCodec.decode(bytes.toString(Charsets.UTF_8))) {
            DecodeResult.NotABackup -> ReadResult.NotABackup
            DecodeResult.NewerVersion -> ReadResult.NewerVersion
            is DecodeResult.Ok -> {
                val backup = decoded.backup
                val backupUser = backup.account?.username?.takeIf { it.isNotBlank() }
                val currentUser = userRepository.getUserId()
                ReadResult.Ok(
                    backup = backup,
                    preview = BackupPreview(
                        tileCount = backup.tiles.orEmpty().size,
                        createdAt = backup.createdAt,
                        backupUsername = backupUser,
                        differentAccount = backupUser != null && currentUser != null && backupUser != currentUser
                    )
                )
            }
        }
    }

    suspend fun applyBackup(backup: SettingsBackup, options: ImportOptions): ImportReport {
        var tilesImported: Int? = null
        if (options.tiles) {
            val entities = BackupTileMapper.toEntities(backup.tiles.orEmpty())
            withContext(Dispatchers.IO) { tileDao.replaceAll(entities) }
            widgetUpdateManager.refreshAllQuickDialWidgets()
            tilesImported = entities.size
        }

        var directCallsSkipped = false
        var autoUpdateSkipped = false
        var languageToApply: String? = null
        if (options.settings) {
            backup.display?.let { display ->
                withContext(Dispatchers.Main) {
                    display.themeMode.toEnumOrNull<ThemeMode>()?.let(themeManager::setThemeMode)
                    display.appTheme.toEnumOrNull<AppTheme>()?.let(themeManager::setAppTheme)
                }
                display.tileSize
                    ?.takeIf { key -> TileSize.values().any { it.name == key } }
                    ?.let { appPreferences.saveString(AppPreferences.KEY_TILE_SIZE, it) }
                languageToApply = display.language
                    ?.takeIf { it in SUPPORTED_LANGUAGES && it != localeManager.getPreferredLanguage() }
            }

            backup.appSettings?.let { app ->
                app.historyPeriodDays
                    ?.takeIf { it in HISTORY_PERIODS }
                    ?.let { appPreferences.historyPeriodDays = it }
                app.autoUpdateEnabled?.let { enabled ->
                    if (enabled && !hasNotificationPermission()) {
                        updateWorkManager.setAutoUpdateEnabled(false)
                        autoUpdateSkipped = true
                    } else {
                        updateWorkManager.setAutoUpdateEnabled(enabled)
                    }
                }
                app.directCallsEnabled?.let { enabled ->
                    if (enabled && !hasCallPermission()) {
                        appPreferences.directCallsEnabled = false
                        directCallsSkipped = true
                    } else {
                        appPreferences.directCallsEnabled = enabled
                    }
                }
            }

            backup.calls?.let { calls ->
                calls.personalPhoneNumber?.let { appPreferences.saveString(KEY_PHONE_NUMBER, it) }
                calls.callerId?.let { appPreferences.saveString(KEY_CALLER_ID, it) }
                calls.callbackRecipient?.let { appPreferences.saveString(KEY_RECIPIENT, it) }
                calls.oneShotRecipient?.let { appPreferences.saveString(KEY_ONESHOT_RECIPIENT, it) }
                calls.selectedLine?.let { appPreferences.saveString(KEY_SELECTED_LINE, it) }
                calls.useCallerIdPrefix?.let { appPreferences.saveString(KEY_USE_CALLER_ID_PREFIX, it.toString()) }
                calls.tabOrder
                    ?.takeIf(::isValidTabOrder)
                    ?.let { appPreferences.saveString(KEY_TAB_ORDER, it) }
            }
        }

        return ImportReport(tilesImported, directCallsSkipped, autoUpdateSkipped, languageToApply)
    }

    private fun hasNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    private fun hasCallPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) ==
            PackageManager.PERMISSION_GRANTED

    companion object {
        private const val TAG = "SettingsBackup"

        private const val KEY_PHONE_NUMBER = "phone_number"
        private const val KEY_CALLER_ID = "caller_id"
        private const val KEY_RECIPIENT = "recipient"
        private const val KEY_ONESHOT_RECIPIENT = "oneshot_recipient"
        private const val KEY_SELECTED_LINE = "selected_line"
        private const val KEY_USE_CALLER_ID_PREFIX = "use_caller_id_prefix"
        private const val KEY_TAB_ORDER = "calls_tab_order"

        private val SUPPORTED_LANGUAGES = setOf("en", "cs")

        private val HISTORY_PERIODS = setOf(7, 30, 90, 180, 365)

        internal fun isValidTabOrder(order: String): Boolean {
            val items = order.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            return items.isNotEmpty() &&
                items.all { it in CallViewModel.DEFAULT_TAB_ORDER } &&
                items.size == items.toSet().size
        }

        private inline fun <reified T : Enum<T>> String?.toEnumOrNull(): T? =
            this?.let { name -> enumValues<T>().firstOrNull { it.name == name } }

        private fun java.io.InputStream.readNBytesCompat(limit: Int): ByteArray {
            val out = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(8 * 1024)
            while (out.size() < limit) {
                val read = read(buffer, 0, minOf(buffer.size, limit - out.size()))
                if (read < 0) break
                out.write(buffer, 0, read)
            }
            return out.toByteArray()
        }
    }
}
