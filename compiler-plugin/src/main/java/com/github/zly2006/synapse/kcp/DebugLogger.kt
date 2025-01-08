package com.github.zly2006.synapse.kcp

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

internal data class DebugLogger(val debug: Boolean, val messageCollector: MessageCollector) {
    fun log(message: String) {
        if (debug) {
            messageCollector.report(CompilerMessageSeverity.INFO, message)
        }
    }

    fun warn(message: String) {
        if (debug) {
            messageCollector.report(CompilerMessageSeverity.WARNING, message)
        }
    }

    fun error(message: String) {
        if (debug) {
            messageCollector.report(CompilerMessageSeverity.ERROR, message)
        }
    }
}
