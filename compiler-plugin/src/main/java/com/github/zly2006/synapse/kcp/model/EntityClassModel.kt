package com.github.zly2006.synapse.kcp.model

import org.jetbrains.kotlin.ir.declarations.IrClass

data class EntityClassModel(
    val irClass: IrClass,
    val tableMame: String,
) {
    companion object
}
