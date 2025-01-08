package com.github.zly2006.synapse.kcp

import com.github.zly2006.synapse.kcp.fir.SynapseFirExtensionRegistrar
import com.github.zly2006.synapse.kcp.transform.ExampleIrGenerationExtension
import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

@Suppress("unused")
@AutoService(CompilerPluginRegistrar::class)
class CommonComponentRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        if (configuration[KEY_ENABLED] == false) {
            return
        }

        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        val logging = true
        IrGenerationExtension.registerExtension(
            ExampleIrGenerationExtension(DebugLogger(logging, messageCollector))
        )

        FirExtensionRegistrarAdapter.registerExtension(SynapseFirExtensionRegistrar(DebugLogger(logging, messageCollector)))
    }
}
