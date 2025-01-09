package com.github.zly2006.synapse.kcp.transform

import com.github.zly2006.synapse.kcp.Constants
import com.github.zly2006.synapse.kcp.DebugLogger
import com.github.zly2006.synapse.kcp.exception.SynapseInternalException
import com.github.zly2006.synapse.kcp.model.EntityClassModel
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.findDeclaration

internal class EntityClassTransformer(
    private val pluginContext: IrPluginContext,
    private val debugLogger: DebugLogger,
) : IrElementTransformerVoidWithContext() {
    override fun visitClassNew(declaration: IrClass): IrStatement {
        val entity = declaration.annotations.firstOrNull {
            it.type.classFqName == Constants.annotationFqName
        }?.let {
            @Suppress("UNCHECKED_CAST")
            val tableName = it.getValueArgument(0) as? IrConst<String>
                ?: throw IllegalStateException("Entity annotation must have a value")
            debugLogger.warn("Found Entity annotation on class ${declaration.name}, tableName: ${tableName.value}")
            EntityClassModel(declaration, tableName.value)
        }
        if (entity != null) {
            val companionObject = declaration.companionObject()
                ?: throw SynapseInternalException("Entity class must have a companion object, this is a compiler bug")

            listOf(Constants.nameHello, Constants.namePrintExpr).map { name ->
                companionObject.findDeclaration<IrSimpleFunction> { it.name == name }
                    ?: throw SynapseInternalException("Companion object must have function $name")
            }.map { funcDeclaration ->
                val startOffset = funcDeclaration.startOffset.takeIf { it >= 0 } ?: companionObject.startOffset
                val endOffset = funcDeclaration.endOffset.takeIf { it >= 0 } ?: companionObject.endOffset
                funcDeclaration.isExternal = false
                funcDeclaration.body =
                    DeclarationIrBuilder(pluginContext, funcDeclaration.symbol, startOffset, endOffset).irBlockBody(
                        startOffset,
                        endOffset
                    ) {
                        +irReturn(irString("Hello, table name is ${entity.tableName}"))
                    }
            }
        }
        return super.visitClassNew(declaration)
    }
}
