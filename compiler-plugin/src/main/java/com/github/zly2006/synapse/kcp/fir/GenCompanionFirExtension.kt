package com.github.zly2006.synapse.kcp.fir

import com.github.zly2006.synapse.kcp.Constants
import com.github.zly2006.synapse.kcp.DebugLogger
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirAnnotationCall
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.NestedClassGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.plugin.createCompanionObject
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

internal class GenCompanionFirExtension(
    session: FirSession,
    private val debugLogger: DebugLogger,
) : FirDeclarationGenerationExtension(session) {
    val predicate = DeclarationPredicate.create {
        annotated(setOf(Constants.annotationFqName)) or
                metaAnnotated(Constants.annotationFqName, includeItself = false)
    }

    override fun getNestedClassifiersNames(
        classSymbol: FirClassSymbol<*>,
        context: NestedClassGenerationContext,
    ): Set<Name> {
        val classes = FirAnnotationCall::class.java.classLoader.getResourceAsStream("org/jetbrains/kotlin/fir/resolve")
            ?.readBytes()?.decodeToString()
        // this is empty ???
        debugLogger.warn("classes= $classes")
        debugLogger.warn("getNestedClassifiersNames ${classSymbol.name}")
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
        if (!session.predicateBasedProvider.matches(predicate, owner)) return null
        if (name == SpecialNames.DEFAULT_NAME_FOR_COMPANION_OBJECT) {
            if (owner is FirRegularClassSymbol && owner.companionObjectSymbol == null) {
                createCompanionObject(owner)
            }
        }
        return super.generateNestedClassLikeDeclaration(owner, name, context)
    }

    fun createCompanionObject(owner: FirRegularClassSymbol) {
        debugLogger.warn("Creating companion object for ${owner.name}")
        createCompanionObject(owner, Constants.DeclarationKey)
    }
}

fun FirBasedSymbol<*>.entityClassAnnotation(session: FirSession): FirAnnotation? {
    return resolvedCompilerAnnotationsWithClassIds.getAnnotationByClassId(Constants.annotationClassId, session)
}
