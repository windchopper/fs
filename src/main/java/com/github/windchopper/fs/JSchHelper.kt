package com.github.windchopper.fs

import com.github.windchopper.fs.sftp.takeAway
import com.jcraft.jsch.*
import kotlinx.coroutines.*
import java.time.Duration
import kotlin.reflect.KClass

class JSchHelper<C: Channel>(val type: Type<C>, val channelInactivityDuration: Duration, val session: Session) {

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

    val disconnectionJobThreadLocal = ThreadLocal<Job>()
    val channelThreadLocal = ThreadLocal<C>()

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
                } as C
    }

    fun disconnect() {
        val channel = channelThreadLocal.get()

        with (GlobalScope) {
            disconnectionJobThreadLocal.set(async {
                delay(channelInactivityDuration.toMillis())

                if (isActive) {
                    channel.disconnect()
                }
            })
        }
    }

}