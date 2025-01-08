package com.github.zly2006.synapse

@Target(AnnotationTarget.CLASS)
annotation class Entity(
    val tableName: String = ""
)
