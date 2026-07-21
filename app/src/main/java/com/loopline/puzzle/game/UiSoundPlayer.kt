package com.loopline.puzzle.game

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.loopline.puzzle.R

/**
 * A single, app-lifetime SoundPool for the one generic "tap" cue used by
 * every ordinary button in the app (mode cards on Home, Settings toggles,
 * the Pause icon, dialog buttons like Continue/Go Home, etc.).
 *
 * This is deliberately separate from [SoundPlayer]: that class is scoped
 * to a single GameScreen visit (created/released with the screen) because
 * it owns gameplay-specific clips (connect/success/wrong-move/reset) that
 * only make sense while a puzzle is open. The button tap, by contrast,
 * needs to fire from screens all over the app - Home, Settings, dialogs -
 * so it lives as one lazily-initialized pool for the whole process
 * instead of being recreated per screen.
 */
object UiSoundPlayer {
    private var pool: SoundPool? = null
    private var tapSoundId: Int = 0
    // Same bug/fix as SoundPlayer: load() is async, so the very first tap
    // sound of the whole app session (fired right as ensureLoaded() is
    // first called) could silently not play if it landed before the pool
    // finished decoding the clip.
    private var tapSoundLoaded = false

    private fun ensureLoaded(context: Context) {
        if (pool != null) return
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val newPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attributes)
            .build()
        newPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) tapSoundLoaded = true
        }
        tapSoundId = newPool.load(context.applicationContext, R.raw.button_tap, 1)
        pool = newPool
    }

    /** Plays the shared button-tap cue, gated by the sound setting.
     * Safe to call from any composable's onClick - loads itself on first
     * use and is never released, since it's meant to live as long as the
     * process does. */
    fun playTap(context: Context, volume: Float = 0.7f) {
        if (!SettingsStore.soundEnabled(context)) return
        ensureLoaded(context)
        if (tapSoundLoaded) {
            pool?.play(tapSoundId, volume, volume, 1, 0, 1f)
        }
    }
}
