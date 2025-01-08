package com.github.zly2006.synapse

@SynapseMeta
@Target(AnnotationTarget.CLASS)
annotation class Entity(
    val tableName: String = "",
)
