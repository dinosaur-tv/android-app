package com.dinotv.home

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.util.UUID

/** Home-screen widgets. Each button opens the app on the action it names. */
object Widgets {
    const val EXTRA_ACTION = "com.dinotv.home.WIDGET_ACTION"
    const val EXTRA_KEY = "com.dinotv.home.WIDGET_KEY"
    private const val PREFS = "dino-home"

    /**
     * The launcher activity is exported, so any app could start it with an action. Only intents
     * carrying this per-install key — known to our own widgets alone — are acted on.
     */
    fun key(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.getString("widgetKey", null)?.let { return it }
        val fresh = UUID.randomUUID().toString()
        prefs.edit().putString("widgetKey", fresh).apply()
        return fresh
    }

    fun actionOf(context: Context, intent: Intent?): WidgetAction? {
        val action = WidgetAction.from(intent?.getStringExtra(EXTRA_ACTION)) ?: return null
        return action.takeIf { intent?.getStringExtra(EXTRA_KEY) == key(context) }
    }

    private fun open(context: Context, action: WidgetAction): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .setAction("com.dinotv.home.widget.${action.id}")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .putExtra(EXTRA_ACTION, action.id)
            .putExtra(EXTRA_KEY, key(context))
        return PendingIntent.getActivity(context, action.ordinal, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun music(context: Context) = RemoteViews(context.packageName, R.layout.widget_music).apply {
        setOnClickPendingIntent(R.id.widget_root, open(context, WidgetAction.MUSIC))
        setOnClickPendingIntent(R.id.music_previous, open(context, WidgetAction.MUSIC_PREVIOUS))
        setOnClickPendingIntent(R.id.music_toggle, open(context, WidgetAction.MUSIC_TOGGLE))
        setOnClickPendingIntent(R.id.music_next, open(context, WidgetAction.MUSIC_NEXT))
    }

    fun remote(context: Context) = RemoteViews(context.packageName, R.layout.widget_remote).apply {
        setOnClickPendingIntent(R.id.widget_root, open(context, WidgetAction.REMOTE))
    }

    fun night(context: Context) = RemoteViews(context.packageName, R.layout.widget_night).apply {
        val state = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("nightState", "").orEmpty()
        setTextViewText(R.id.night_state, state.ifEmpty { "нажмите, чтобы переключить" })
        setOnClickPendingIntent(R.id.widget_root, open(context, WidgetAction.NIGHT_TOGGLE))
    }

    /** The console told us what night mode is now; show it on every night widget. */
    fun rememberNight(context: Context, state: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (state.isEmpty() || prefs.getString("nightState", "") == state) return
        prefs.edit().putString("nightState", state).apply()
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, NightWidget::class.java))
        if (ids.isNotEmpty()) manager.updateAppWidget(ids, night(context))
    }
}

class MusicWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) =
        manager.updateAppWidget(ids, Widgets.music(context))
}

class RemoteWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) =
        manager.updateAppWidget(ids, Widgets.remote(context))
}

class NightWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) =
        manager.updateAppWidget(ids, Widgets.night(context))
}
