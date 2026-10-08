package com.fra.autoplay.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fra.autoplay.R
import com.fra.autoplay.PreferencesHelper
import com.fra.autoplay.AutoPlayWidgetProvider

/**
 * Widget configuration activity - allows user to customize widget appearance.
 */
class WidgetConfigActivity : AppCompatActivity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var transparencySeekBar: SeekBar
    private lateinit var transparencyValue: TextView
    private lateinit var showIconSwitch: Switch
    private lateinit var showLabelSwitch: Switch
    private lateinit var compactModeSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        }

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContentView(R.layout.activity_widget_config)

        transparencySeekBar = findViewById(R.id.transparency_seekbar)
        transparencyValue = findViewById(R.id.transparency_value)
        showIconSwitch = findViewById(R.id.show_icon_switch)
        showLabelSwitch = findViewById(R.id.show_label_switch)
        compactModeSwitch = findViewById(R.id.compact_mode_switch)

        val prefs = getWidgetPrefs()
        val transparency = prefs.getInt("widget_transparency_$widgetId", 0)
        transparencySeekBar.progress = transparency
        transparencyValue.text = "$transparency%"
        showIconSwitch.isChecked = prefs.getBoolean("widget_show_icon_$widgetId", true)
        showLabelSwitch.isChecked = prefs.getBoolean("widget_show_label_$widgetId", true)
        compactModeSwitch.isChecked = prefs.getBoolean("widget_compact_mode_$widgetId", false)

        transparencySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                transparencyValue.text = "$progress%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        findViewById<Button>(R.id.save_button).setOnClickListener { saveAndFinish() }
        findViewById<Button>(R.id.cancel_button).setOnClickListener { finish() }
    }

    private fun saveAndFinish() {
        val prefs = getWidgetPrefs().edit().apply {
            putInt("widget_transparency_$widgetId", transparencySeekBar.progress)
            putBoolean("widget_show_icon_$widgetId", showIconSwitch.isChecked)
            putBoolean("widget_show_label_$widgetId", showLabelSwitch.isChecked)
            putBoolean("widget_compact_mode_$widgetId", compactModeSwitch.isChecked)
        }.apply()

        // Update the widget
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, AutoPlayWidgetProvider::class.java)
        val ids = appWidgetManager.getAppWidgetIds(componentName)
        for (id in ids) {
            if (id == widgetId || widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                AutoPlayWidgetProvider.updateAllWidgets(this)
            }
        }

        val resultValue = Intent().apply { putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId) }
        setResult(RESULT_OK, resultValue)
        finish()
    }

    private fun getWidgetPrefs(): SharedPreferences {
        return getSharedPreferences("autoplay_widget_config", Context.MODE_PRIVATE)
    }

    companion object {
        fun getTransparency(context: Context, widgetId: Int): Int {
            return context.getSharedPreferences("autoplay_widget_config", Context.MODE_PRIVATE)
                .getInt("widget_transparency_$widgetId", 0)
        }

        fun getShowIcon(context: Context, widgetId: Int): Boolean {
            return context.getSharedPreferences("autoplay_widget_config", Context.MODE_PRIVATE)
                .getBoolean("widget_show_icon_$widgetId", true)
        }

        fun getShowLabel(context: Context, widgetId: Int): Boolean {
            return context.getSharedPreferences("autoplay_widget_config", Context.MODE_PRIVATE)
                .getBoolean("widget_show_label_$widgetId", true)
        }

        fun getCompactMode(context: Context, widgetId: Int): Boolean {
            return context.getSharedPreferences("autoplay_widget_config", Context.MODE_PRIVATE)
            .getBoolean("widget_compact_mode_$widgetId", false)
        }
    }
}