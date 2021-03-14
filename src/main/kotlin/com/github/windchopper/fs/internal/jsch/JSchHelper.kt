package com.github.windchopper.fs.internal.jsch

import com.github.windchopper.fs.internal.rethrow
import com.github.windchopper.fs.internal.takeAway
import com.jcraft.jsch.*
import kotlinx.coroutines.*
import java.io.IOException
import java.time.Duration
import kotlin.reflect.KClass

class JSchHelper<C: Channel>(private val type: Type<C>, private val channelInactivityDuration: Duration, private val session: Session) {

    class Type<C: Channel> internal constructor(val code: String, val type: KClass<C>) {

        companion object {

            val SESSION = Type("session", Channel::class)
            val SHELL = Type("shell", ChannelShell::class)
            val EXEC = Type("exec", ChannelExec::class)
            val X11 = Type("x11", Channel::class)
            val AGENT_FORWARDING = Type("auth-agent@openssh.com", Channel::class)
            val DIRECT_TCPIP = Type("direct-tcpip", ChannelDirectTCPIP::class)
            val FORWARDED_TCPIP = Type("forwarded-tcpip", ChannelForwardedTCPIP::class)
            val SFTP = Type("sftp", ChannelSftp::class)
            val SUBSYSTEM = Type("subsystem", ChannelSubsystem::class)

        }

    }

    private val disconnectionJobThreadLocal = ThreadLocal<Job>()
    private val channelThreadLocal = ThreadLocal<C>()

    @Suppress("UNCHECKED_CAST") fun connect(): C {
        disconnectionJobThreadLocal.takeAway()
            ?.cancel()

        return channelThreadLocal.get()
            ?.let {
                if (it.isConnected) it else null
            }
            ?:session.openChannel(type.code)
                .let {
                    channelThreadLocal.set(it as C)
                    it.connect()
                    it
                }
    }

    fun connected(): Boolean {
        return channelThreadLocal.get()?.isConnected?:false
    }

    fun disconnect(immediately: Boolean = false) {
        val channel = channelThreadLocal.get()?:return

        if (immediately) channel.disconnect() else with (GlobalScope) {
            disconnectionJobThreadLocal.set(async {
                delay(channelInactivityDuration.toMillis())

                if (isActive) {
                    channel.disconnect()
                }
            })
        }
    }

    fun <T> performConnected(action: (channel: C) -> T): T {
        return rethrow(::IOException) {
            val channel = connect()

            try {
                action(channel)
            } finally {
                disconnect()
            }
        }
    }

}