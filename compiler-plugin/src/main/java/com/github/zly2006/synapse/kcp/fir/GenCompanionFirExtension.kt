package com.github.zly2006.synapse.kcp.fir

import com.github.zly2006.synapse.kcp.Constants
import com.github.zly2006.synapse.kcp.DebugLogger
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.utils.isCompanion
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.NestedClassGenerationContext
import org.jetbrains.kotlin.fir.plugin.createCompanionObject
import org.jetbrains.kotlin.fir.plugin.createConeType
import org.jetbrains.kotlin.fir.plugin.createDefaultPrivateConstructor
import org.jetbrains.kotlin.fir.plugin.createMemberFunction
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

private val companionToClassCache = mutableMapOf<FirRegularClassSymbol, FirRegularClassSymbol>()

internal class GenCompanionFirExtension(
    session: FirSession,
    private val debugLogger: DebugLogger,
) : FirDeclarationGenerationExtension(session) {
    override fun getNestedClassifiersNames(
        classSymbol: FirClassSymbol<*>,
        context: NestedClassGenerationContext,
    ): Set<Name> {
        val annotation = classSymbol.entityClassAnnotation(session)
        if (annotation != null) {
            return setOf(
                SpecialNames.DEFAULT_NAME_FOR_COMPANION_OBJECT
            )
        }
        return emptySet()
    }

    override fun generateNestedClassLikeDeclaration(
        owner: FirClassSymbol<*>,
        name: Name,
        context: NestedClassGenerationContext,
    ): FirClassLikeSymbol<*>? {
        if (owner.entityClassAnnotation(session) == null) return null
        if (name == SpecialNames.DEFAULT_NAME_FOR_COMPANION_OBJECT) {
            if (owner is FirRegularClassSymbol && owner.companionObjectSymbol == null) {
                val companionObject = createCompanionObject(owner)
                companionToClassCache[companionObject.symbol] = owner
                return companionObject.symbol
            }
        }
        return super.generateNestedClassLikeDeclaration(owner, name, context)
    }

    fun createCompanionObject(owner: FirRegularClassSymbol): FirRegularClass {
        debugLogger.warn("Creating companion object for ${owner.name}")
        return createCompanionObject(owner, Constants.DeclarationKey)
    }

    private fun nullIfCreated(owner: FirClassSymbol<*>, name: Name): Name? {
        return name.takeIf { !owner.declarationSymbols.any { it is FirCallableSymbol && it.name == name } }
    }

    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext): Set<Name> {
        return setOfNotNull(
            SpecialNames.INIT,
            nullIfCreated(classSymbol, Constants.nameHello),
            nullIfCreated(classSymbol, Constants.namePrintExpr),
        )
    }

    override fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol> {
        val owner = context.owner

        val result = mutableListOf<FirConstructorSymbol>()
        result += createDefaultPrivateConstructor(owner, Constants.DeclarationKey).symbol

        return result
    }

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?,
    ): List<FirNamedFunctionSymbol> {
        val owner = context?.owner ?: return emptyList()
        if (!owner.isCompanion) {
            return emptyList()
        }

        when (callableId.callableName) {
            Constants.nameHello -> {
                val function = createMemberFunction(
                    owner,
                    Constants.DeclarationKey,
                    callableId.callableName,
                    returnType = session.builtinTypes.stringType.type
                ) {
                }
                return listOf(
                    function.symbol
                )
            }

            Constants.namePrintExpr -> {
                val function = createMemberFunction(
                    owner,
                    Constants.DeclarationKey,
                    callableId.callableName,
                    returnType = session.builtinTypes.stringType.type
                ) {
                    valueParameter(
                        Name.identifier("expr"),
                        StandardNames.getFunctionClassId(1).createConeType(
                            session,
                            arrayOf(companionToClassCache[owner]!!.defaultType(), session.builtinTypes.unitType.type)
                        ), false, true, false, false, Constants.DeclarationKey
                    )
                }
                return listOf(
                    function.symbol
                )
            }

            else -> return super.generateFunctions(callableId, context)
        }
    }
}

private fun FirBasedSymbol<*>.entityClassAnnotation(session: FirSession): FirAnnotation? {
    return resolvedCompilerAnnotationsWithClassIds.getAnnotationByClassId(Constants.annotationClassId, session)
}
