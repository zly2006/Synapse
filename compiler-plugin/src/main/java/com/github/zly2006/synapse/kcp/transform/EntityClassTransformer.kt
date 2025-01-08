package com.github.zly2006.synapse.kcp.transform

import com.github.zly2006.synapse.kcp.DebugLogger
import com.github.zly2006.synapse.kcp.model.EntityClassModel
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.companionObject

internal class EntityClassTransformer(
    private val pluginContext: IrPluginContext,
    private val debugLogger: DebugLogger,
) : IrElementTransformerVoidWithContext() {
    override fun visitClassNew(declaration: IrClass): IrStatement {
        val entity = declaration.annotations.firstOrNull {
            it.type.classFqName?.asString() == "com.github.zly2006.synapse.Entity"
        }?.let {
            @Suppress("UNCHECKED_CAST")
            val tableName = it.getValueArgument(0) as? IrConst ?: throw IllegalStateException("Entity annotation must have a value")
            debugLogger.warn("Found Entity annotation on class ${declaration.name}, tableName: ${tableName.value}")
            EntityClassModel(declaration, tableName.value as String)
        }
        if (entity != null) {
            requireNotNull(declaration.companionObject())
        }
        return super.visitClassNew(declaration)
    }
}
