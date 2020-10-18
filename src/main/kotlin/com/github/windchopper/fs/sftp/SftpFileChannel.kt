package com.github.windchopper.fs.sftp

import com.github.windchopper.fs.internal.jsch.JSchHelper
import com.jcraft.jsch.ChannelSftp
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel

class SftpFileChannel(private val helper: JSchHelper<ChannelSftp>, private val path: SftpPath): FileChannel() {

    override fun implCloseChannel() {
        TODO("Not yet implemented")
    }

    override fun read(dst: ByteBuffer?): Int {
        TODO("Not yet implemented")
    }

    override fun read(dsts: Array<out ByteBuffer>?, offset: Int, length: Int): Long {
        TODO("Not yet implemented")
    }

    override fun read(dst: ByteBuffer?, position: Long): Int {
        TODO("Not yet implemented")
    }

    override fun write(src: ByteBuffer?): Int {
        TODO("Not yet implemented")
    }

    override fun write(srcs: Array<out ByteBuffer>?, offset: Int, length: Int): Long {
        TODO("Not yet implemented")
    }

    override fun write(src: ByteBuffer?, position: Long): Int {
        TODO("Not yet implemented")
    }

    override fun position(): Long {
        TODO("Not yet implemented")
    }

    override fun position(newPosition: Long): FileChannel {
        TODO("Not yet implemented")
    }

    override fun size(): Long {
        TODO("Not yet implemented")
    }

    override fun truncate(size: Long): FileChannel {
        TODO("Not yet implemented")
    }

    override fun force(metaData: Boolean) {
        TODO("Not yet implemented")
    }

    override fun transferTo(position: Long, count: Long, target: WritableByteChannel?): Long {
        TODO("Not yet implemented")
    }

    override fun transferFrom(src: ReadableByteChannel?, position: Long, count: Long): Long {
        TODO("Not yet implemented")
    }

    override fun map(mode: MapMode?, position: Long, size: Long): MappedByteBuffer {
        TODO("Not yet implemented")
    }

    override fun lock(position: Long, size: Long, shared: Boolean): FileLock {
        TODO("Not yet implemented")
    }

    override fun tryLock(position: Long, size: Long, shared: Boolean): FileLock {
        TODO("Not yet implemented")
    }

}