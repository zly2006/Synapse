package com.github.zly2006.synapse.kcp.transform

import com.github.zly2006.synapse.kcp.DebugLogger
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal class ExampleIrGenerationExtension(private val debugLogger: DebugLogger) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.transform(ElementTransformer(pluginContext, debugLogger), null)
        moduleFragment.transform(EntityClassTransformer(pluginContext, debugLogger), null)
    }
}
