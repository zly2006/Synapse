package com.github.zly2006.synapse.kcp

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

object Constants {
    val annotationFqName = FqName("com.github.zly2006.synapse.Entity")
    val annotationClassId = ClassId.topLevel(annotationFqName)
    object DeclarationKey: GeneratedDeclarationKey() {
        override fun toString(): String {
            return "SynapseKcpPlugin"
        }
    }
}
