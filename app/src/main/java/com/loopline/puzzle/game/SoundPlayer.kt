package com.loopline.puzzle.game

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.loopline.puzzle.R

/**
 * Tiny wrapper around [SoundPool] for the one short effect the game needs:
 * a soft "pop" every time the stroke connects a new tile (and a quieter
 * version when backing up). SoundPool (not MediaPlayer) is the right tool
 * here since it preloads the clip into memory and plays it with near-zero
 * latency, which matters for something that can fire many times a second
 * during a fast drag.
 *
 * One instance is meant to live for as long as a game screen is on
 * screen - create it with [SoundPlayer.create] in a `remember` block and
 * call [release] when leaving the screen (see GameScreen's
 * `DisposableEffect`).
 */
class SoundPlayer private constructor(private val pool: SoundPool, private val connectSoundId: Int) {

    /** Played when the stroke extends to a new tile. */
    fun playConnect(volume: Float = 1f) {
        pool.play(connectSoundId, volume, volume, 1, 0, 1f)
    }

    /** A quieter version for retracting the stroke, so it still reads as
     * connected to the drag but doesn't compete with the forward sound. */
    fun playRetract() = playConnect(volume = 0.5f)

    fun release() {
        pool.release()
    }

    companion object {
        fun create(context: Context): SoundPlayer {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val pool = SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(attributes)
                .build()
            val soundId = pool.load(context, R.raw.tile_connect, 1)
            return SoundPlayer(pool, soundId)
        }
    }
}
