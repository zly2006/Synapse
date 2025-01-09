package com.github.zly2006.synapse.kcp

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate.BuilderContext.metaAnnotated
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object Constants {
    val annotationFqName = FqName("com.github.zly2006.synapse.Entity")
    val annotationClassId = ClassId.topLevel(annotationFqName)
    val metaAnnotationFqName = FqName("com.github.zly2006.synapse.SynapseMeta")
    val metaAnnotationClassId = ClassId.topLevel(metaAnnotationFqName)
    val nameHello = Name.identifier("hello")
    val namePrintExpr = Name.identifier("printExpr")

    internal val hasMetaAnnotation = DeclarationPredicate.create {
        metaAnnotated(metaAnnotationFqName, includeItself = false)
    }
    object DeclarationKey: GeneratedDeclarationKey() {
        override fun toString(): String {
            return "SynapseKcpPlugin"
        }
    }
}
