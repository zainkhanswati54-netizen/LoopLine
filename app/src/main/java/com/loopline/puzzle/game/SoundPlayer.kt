package com.loopline.puzzle.game

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.loopline.puzzle.R

/**
 * Tiny wrapper around [SoundPool] for the game's short effects: a soft
 * "pop" every time the stroke connects a new tile, and a rewarding chime
 * the moment a level is solved. SoundPool (not MediaPlayer) is the right
 * tool for both - it preloads every clip into memory up front and plays
 * them with near-zero latency, which matters for the connect sound (it can
 * fire many times a second during a fast drag) and means the completion
 * chime is instant too instead of MediaPlayer's noticeable "prepare, then
 * play" delay right at the moment that most needs to feel snappy.
 * MediaPlayer only earns its keep for longer streamed audio (background
 * music, voiceover) - not for one-shot effects like these.
 *
 * One instance is meant to live for as long as a game screen is on
 * screen - create it with [SoundPlayer.create] in a `remember` block and
 * call [release] when leaving the screen (see GameScreen's
 * `DisposableEffect`). Both clips are loaded once, up front, so nothing
 * decodes on the audio-critical path later.
 */
class SoundPlayer private constructor(
    private val pool: SoundPool,
    private val connectSoundId: Int,
    private val successSoundId: Int
) {

    /** Played when the stroke extends to a new tile. */
    fun playConnect(volume: Float = 1f) {
        pool.play(connectSoundId, volume, volume, 1, 0, 1f)
    }

    /** Played once, the instant a level's final tile connects. */
    fun playSuccess(volume: Float = 1f) {
        pool.play(successSoundId, volume, volume, 1, 0, 1f)
    }

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
            val connectId = pool.load(context, R.raw.tile_connect, 1)
            val successId = pool.load(context, R.raw.level_complete, 1)
            return SoundPlayer(pool, connectId, successId)
        }
    }
}
