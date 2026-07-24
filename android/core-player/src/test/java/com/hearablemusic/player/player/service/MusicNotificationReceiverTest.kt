package com.hearablemusic.player.player.service

import android.content.Intent
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MusicNotificationReceiverTest {

    @Test
    fun onReceive_nullIntent_doesNotThrow() {
        val receiver = MusicNotificationReceiver()
        val context = RuntimeEnvironment.getApplication()
        receiver.onReceive(context, null)
    }

    @Test
    fun onReceive_intentWithAction_doesNotThrow() {
        val receiver = MusicNotificationReceiver()
        val context = RuntimeEnvironment.getApplication()
        val intent = Intent("com.hmp.ACTION_PLAY")
        receiver.onReceive(context, intent)
    }

    @Test
    fun onReceive_intentWithoutAction_doesNotThrow() {
        val receiver = MusicNotificationReceiver()
        val context = RuntimeEnvironment.getApplication()
        val intent = Intent()
        receiver.onReceive(context, intent)
    }
}
