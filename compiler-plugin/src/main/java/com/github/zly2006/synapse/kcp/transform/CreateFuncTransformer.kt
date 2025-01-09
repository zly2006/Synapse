package com.github.zly2006.synapse.kcp.transform

import com.github.zly2006.synapse.kcp.Constants
import com.github.zly2006.synapse.kcp.DebugLogger
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.name.FqName

interface IrProvider {
    fun toIr(p1: IrPluginContext): IrStatement
}

internal class CreateFuncTransformer(
    private val pluginContext: IrPluginContext,
    private val debugLogger: DebugLogger,
) : IrElementTransformerVoidWithContext() {
    sealed class SqlExpr: IrProvider {
        data class BinaryExpr(val left: SqlExpr, val right: SqlExpr, val op: String) : SqlExpr() {
            override fun toIr(p1: IrPluginContext): IrStatement {
                TODO("Not yet implemented")
            }
        }
        data class Column(val table: String, val column: String) : SqlExpr() {
            override fun toIr(p1: IrPluginContext): IrStatement {
                TODO("Not yet implemented")
            }
        }

        data class Const(val value: Any?, val irConst: IrConst<*>) : SqlExpr() {
            override fun toIr(p: IrPluginContext): IrStatement {
                return irConst
            }
        }

        data class GreaterOrEqual(val left: SqlExpr, val right: SqlExpr) : SqlExpr() {
            override fun toIr(p1: IrPluginContext): IrStatement {
                TODO("Not yet implemented")
            }
        }
    }

    internal class SqlExprTranslator(val debugLogger: DebugLogger) : IrElementVisitor<SqlExpr?, Nothing?> {
        override fun visitElement(element: IrElement, data: Nothing?): SqlExpr? {
            debugLogger.error("Unexpected element ${element::class.simpleName}, $element")
            return null
        }

        override fun visitExpression(expression: IrExpression, data: Nothing?): SqlExpr? {
            return super.visitExpression(expression, data)
        }

        override fun visitCall(expression: IrCall, data: Nothing?): SqlExpr? {
            val signature = expression.symbol.signature
            return when (signature) {
                is IdSignature.CommonSignature -> {
                    val arguments = expression.valueArguments.map {
                        it?.accept(SqlExprTranslator(debugLogger), null)
                    }
                    if (arguments.any { it == null }) {
                        debugLogger.error("Null argument in call $expression")
                        return null
                    }
                    if (signature.description == "greaterOrEqual(kotlin.Int;kotlin.Int){}kotlin.Boolean") {
                        return SqlExpr.GreaterOrEqual(arguments[0]!!, arguments[1]!!)
                    }
                    else {
                        debugLogger.error("Unexpected call ${signature.description}")
                        null
                    }
                }

                else -> {
                    if (signature == null) {
                        if (expression.origin.toString() === "GET_PROPERTY") {
                            val receiver = expression.dispatchReceiver
                            val propertyName = expression.getValueArgument(0)?.accept(this, data)
                            if (receiver == null || propertyName == null) {
                                debugLogger.error("Null receiver or property name in call $expression")
                                return null
                            }
                            return SqlExpr.Column(receiver.toString(), propertyName.toString())
                        }
                    }
                    super.visitCall(expression, data)
                }
            }
        }

        override fun visitTypeOperator(expression: IrTypeOperatorCall, data: Nothing?): SqlExpr? {
            when (expression.operator) {
                IrTypeOperator.CAST,
                IrTypeOperator.IMPLICIT_CAST,
                IrTypeOperator.IMPLICIT_NOTNULL,
                IrTypeOperator.IMPLICIT_COERCION_TO_UNIT,
                IrTypeOperator.IMPLICIT_INTEGER_COERCION,
                    -> return expression.argument.accept(this, data)

                else -> debugLogger.error("Unexpected type operator ${expression.operator}")
            }
            return null
        }

        override fun visitFunctionExpression(expression: IrFunctionExpression, data: Nothing?): SqlExpr? {
            return expression.function.body?.accept(this, data)
        }

        override fun visitBody(body: IrBody, data: Nothing?): SqlExpr? {
            return body.statements.lastOrNull()?.accept(this, data)
        }

        override fun visitConst(expression: IrConst<*>, data: Nothing?): SqlExpr? {
            return SqlExpr.Const(expression.value, expression)
        }
    }


    override fun visitExpression(expression: IrExpression): IrExpression {
        val call = expression as? IrCall
        if (call is IrCall) {
            if (expression.symbol.owner.name != Constants.namePrintExpr) {
                return expression
            }
            val annotation =
                expression.symbol.owner.parentClassOrNull?.parentClassOrNull?.getAnnotation(Constants.annotationFqName)
            if (annotation == null) {
                return expression
            }
            val tableName = (annotation.getValueArgument(0) as? IrConst<String>)?.value ?: run {
                debugLogger.error("Entity annotation must have a value")
                return null.toIrConst(pluginContext.irBuiltIns.nothingType)
            }
            if (expression.getValueArgument(0) == null) {
                debugLogger.error("SQL expression must not be null")
                return null.toIrConst(pluginContext.irBuiltIns.nothingType)
            }
            val sqlExpr = expression.getValueArgument(0) as? IrFunctionExpression ?: run {
                debugLogger.error("SQL expression must be a lambda")
                return null.toIrConst(pluginContext.irBuiltIns.nothingType)
            }
            val funBody = sqlExpr.function.body ?: run {
                debugLogger.error("SQL expression must have a body")
                return null.toIrConst(pluginContext.irBuiltIns.nothingType)
            }

            val expr = funBody.accept(SqlExprTranslator(debugLogger), null)

            println(tableName)

            //Create the constructor call for _ExampleApiImpl()
            val newCall = call
            //                IrConstructorCallImpl(
            //                    0,
            //                    0,
            //                    type = implClassSymbol.defaultType,
            //                    symbol = newConstructor,
            //                    0,
            //                    0,
            //                    0,
            //                    null
            //                )

            //Set _ExampleApiImpl() as argument for create<ExampleApi>()
            call.putValueArgument(0, newCall)
            return super.visitExpression(call)
        }
        return super.visitExpression(expression)
    }

}


private val FqName?.packageName: String
    get() {
        return this.toString().substringBeforeLast(".")
    }
