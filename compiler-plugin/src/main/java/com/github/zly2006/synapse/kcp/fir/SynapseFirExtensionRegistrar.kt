package com.github.zly2006.synapse.kcp.fir

import com.github.zly2006.synapse.kcp.DebugLogger
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

internal class SynapseFirExtensionRegistrar(
    private val debugLogger: DebugLogger,
) : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +FirDeclarationGenerationExtension.Factory {
            GenCompanionFirExtension(it, debugLogger)
        }
    }
}

