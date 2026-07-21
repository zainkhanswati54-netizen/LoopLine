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
    private val successSoundId: Int,
    private val wrongMoveSoundId: Int,
    private val resetSoundId: Int
) {
    // Bug fix: SoundPool.load() is asynchronous - it returns a sound ID
    // immediately, but the clip isn't actually decoded and ready until the
    // pool's load-complete callback fires some milliseconds later. Calling
    // play() on an ID that hasn't finished loading yet is a silent no-op
    // (no crash, no sound, no error) - which made the very first tile
    // connect of a freshly opened level frequently play no pop at all,
    // since that tap could easily land before loading finished. Every
    // play*() method below now checks this set first.
    private val loadedSoundIds = mutableSetOf<Int>()

    init {
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) loadedSoundIds += sampleId
        }
    }

    private fun playIfLoaded(soundId: Int, volume: Float) {
        if (soundId in loadedSoundIds) {
            pool.play(soundId, volume, volume, 1, 0, 1f)
        }
    }

    /** Played when the stroke extends to a new tile. */
    fun playConnect(volume: Float = 1f) {
        playIfLoaded(connectSoundId, volume)
    }

    /** Played once, the instant a level's final tile connects - the
     * "level up" beat that plays alongside the tile shrink/pop-out just
     * before the next level loads. Backed by res/raw/level_up.mp3 - swap
     * that file's contents (same filename) if a different clip is wanted
     * later; no code change needed. */
    fun playSuccess(volume: Float = 1f) {
        playIfLoaded(successSoundId, volume)
    }

    /** Played the instant the player drags onto a non-adjacent/invalid
     * tile - paired with the red stroke flash and grid shake. */
    fun playWrongMove(volume: Float = 1f) {
        playIfLoaded(wrongMoveSoundId, volume)
    }

    /** Played when the Restart button is tapped - a distinct "tearing up
     * this attempt" cue rather than the generic UI button tap. */
    fun playReset(volume: Float = 1f) {
        playIfLoaded(resetSoundId, volume)
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
            val successId = pool.load(context, R.raw.level_up, 1)
            val wrongMoveId = pool.load(context, R.raw.wrong_move, 1)
            val resetId = pool.load(context, R.raw.reset_tap, 1)
            return SoundPlayer(pool, connectId, successId, wrongMoveId, resetId)
        }
    }
}
