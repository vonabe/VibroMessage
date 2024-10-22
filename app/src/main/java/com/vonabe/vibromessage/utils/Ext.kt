package com.vonabe.vibromessage.utils

import android.os.Build
import android.os.VibrationEffect
import androidx.annotation.RequiresApi

object Ext {

    @RequiresApi(Build.VERSION_CODES.O)
    fun String.commandToPattern(): VibrationEffect? {
        val commandList = this.split("\n")
        val data = LongArray(commandList.size + 2)
        data[0] = 0L
        for ((count, index) in (1..<commandList.size).withIndex()) {
            val it = commandList[count]
            data[index] = if (it.contains("s:")) it.split("s:").last().toLong() else it.split("p:").last().toLong()
        }
        data[data.size - 1] = 0L
        return VibrationEffect.createWaveform(data, -1)
    }

    fun String.commandToLong(): LongArray {
        val commandList = this.split("\n")
        val data = LongArray(commandList.size + 2)
        data[0] = 0L
        for ((count, index) in (1..<commandList.size).withIndex()) {
            val it = commandList[count]
            data[index] = if (it.contains("s:")) it.split("s:").last().toLong() else it.split("p:").last().toLong()
        }
        data[data.size - 1] = 0L
        return data
    }

    fun String.getDelay(): Long = commandToLong().sum()

}