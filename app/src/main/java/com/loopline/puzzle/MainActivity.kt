package com.loopline.puzzle

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.loopline.puzzle.ads.AdsManager
import com.loopline.puzzle.game.GameSession
import com.loopline.puzzle.game.ModeSession
import com.loopline.puzzle.notifications.ReminderScheduler
import com.loopline.puzzle.ui.navigation.LoopLineNavGraph
import com.loopline.puzzle.ui.theme.LoopLineTheme

class MainActivity : ComponentActivity() {

    // The system permission dialog itself only appears once per app per
    // Android's own rules - if the player denies it, this callback still
    // fires (with `granted = false`) and we just skip scheduling; we never
    // nag them with a second request.
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // Whether granted or not, this is the point to (re)check and
        // schedule - scheduleIfEnabled() itself checks the live permission
        // via ReminderStore/NotificationHelper before ever posting one.
        ReminderScheduler.scheduleIfEnabled(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Rebuilds any in-progress sessions from disk before anything else
        // reads their state, so "Continue" is accurate even on the very
        // first frame after a full app restart.
        GameSession.hydrate(applicationContext)
        ModeSession.hydrate(applicationContext)
        AdsManager.initialize(applicationContext)

        setupDailyReminders()

        setContent {
            LoopLineTheme {
                LoopLineNavGraph()
            }
        }
    }

    /** Android 13+ (API 33) needs POST_NOTIFICATIONS granted at runtime
     * before any notification can show at all; below that, notifications
     * are allowed by default and we can schedule immediately. Either way
     * this is cheap and idempotent (WorkManager's KEEP policy), so calling
     * it on every cold start is fine - it won't re-prompt or reschedule a
     * job that's already running. */
    private fun setupDailyReminders() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (granted) {
                ReminderScheduler.scheduleIfEnabled(applicationContext)
            } else {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            ReminderScheduler.scheduleIfEnabled(applicationContext)
        }
    }
}
