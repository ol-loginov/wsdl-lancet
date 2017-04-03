package com.github.olloginov.wsdl11

import com.github.olloginov.support.SmartNode
import javax.xml.namespace.QName


internal class Schema(
        val services: MutableList<Service> = mutableListOf(),
        val portTypes: MutableList<PortType> = mutableListOf()
)

internal class NMToken {}

internal open class RefCountable {
    private var refs = 0

    fun addRef() {
        refs += 1
    }

    fun removeRef() {
        refs -= 1

        if (refs < 0) {
            throw IllegalStateException("cannot go below zero!")
        }
    }
}

internal open class NodeHolder(val node: SmartNode) : RefCountable() {
    init {
        addRef()
    }
}

internal class Service(
        node: SmartNode,
        val name: QName,
        val ports: List<ServicePort> = emptyList()
) : NodeHolder(node)

internal class ServicePort(
        node: SmartNode,
        val name: QName,
        val binding: QName
) : NodeHolder(node)

internal class Binding(node: SmartNode) : NodeHolder(node)

internal class BindingOperation(node: SmartNode) : NodeHolder(node)

internal class PortType(
        node: SmartNode,
        val name: QName,
        val operations: List<PortTypeOperation> = emptyList()
) : NodeHolder(node)

internal class PortTypeOperation(
        node: SmartNode,
        val inputMessage: QName,
        val outputMessage: QName,
        val faultMessage: List<QName> = emptyList()
) : NodeHolder(node)