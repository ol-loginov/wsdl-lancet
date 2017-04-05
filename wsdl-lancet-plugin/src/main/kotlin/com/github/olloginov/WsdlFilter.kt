package com.github.olloginov

import javax.xml.namespace.QName


enum class WsdlFilterDecision {
    KEEP,
    SKIP,
    UNCLEAR;
}

interface WsdlFilter {
    fun needPortTypeOperation(portType: QName, operation: String): WsdlFilterDecision
}

class WsdlSlice
@JvmOverloads constructor(
        val portTypes: MutableList<WsdlPortType> = mutableListOf()
)


class WsdlPortType
@JvmOverloads constructor(
        var name: String = "",
        val operations: MutableList<WsdlPortTypeOperation> = mutableListOf()
)

class WsdlPortTypeOperation
@JvmOverloads constructor(
        var name: String = ""
) 
