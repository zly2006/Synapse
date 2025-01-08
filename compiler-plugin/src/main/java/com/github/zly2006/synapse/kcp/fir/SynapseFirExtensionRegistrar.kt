package com.github.zly2006.synapse.kcp.fir

import com.github.zly2006.synapse.kcp.Constants.hasMetaAnnotation
import com.github.zly2006.synapse.kcp.DebugLogger
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirExtensionSessionComponent

internal class SynapseFirExtensionRegistrar(
    private val debugLogger: DebugLogger,
) : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +FirExtensionSessionComponent.Factory {
            object : FirExtensionSessionComponent(it) {
                override fun FirDeclarationPredicateRegistrar.registerPredicates() {
                    register(hasMetaAnnotation)
                }
            }
        }
        +FirDeclarationGenerationExtension.Factory {
            GenCompanionFirExtension(it, debugLogger)
        }
    }
}

