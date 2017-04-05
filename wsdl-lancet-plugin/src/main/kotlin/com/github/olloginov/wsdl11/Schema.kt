package com.github.olloginov.wsdl11

import com.github.olloginov.support.SmartNode
import javax.xml.namespace.QName


internal class Schema(
        val services: MutableList<Service> = mutableListOf(),
        val portTypes: MutableList<PortType> = mutableListOf(),
        val bindings: MutableList<Binding> = mutableListOf(),
        val messages: MutableList<Message> = mutableListOf()
)

internal class NMToken {}

internal open class RefCountable {
    private var refs = 0

    fun addRef() {
        refs += 1
    }

    fun removeRef() {
        refs -= 1
    }
}

internal open class NodeHolder(val node: SmartNode) : RefCountable()

internal class Service(node: SmartNode,
                       val name: QName,
                       val ports: List<ServicePort> = emptyList()
) : NodeHolder(node)

internal class ServicePort(node: SmartNode,
                           val name: QName,
                           val binding: QName
) : NodeHolder(node)

internal class Binding(node: SmartNode,
                       val name: QName,
                       val type: QName,
                       val operations: List<BindingOperation>
) : NodeHolder(node)

internal class BindingOperation(node: SmartNode,
                                val name: String
) : NodeHolder(node)

internal class PortType(node: SmartNode,
                        val name: QName,
                        var operations: List<PortTypeOperation>
) : NodeHolder(node) {
    fun removeOperations(filter: (PortTypeOperation) -> Boolean): List<PortTypeOperation> {
        val deletable = this.operations.filter(filter)

        val operations = this.operations.toMutableList()
        operations.removeAll(deletable)

        this.operations = operations

        return deletable
    }
}

internal class PortTypeOperation(node: SmartNode,
                                 val name: String,
                                 val inputMessage: QName,
                                 val outputMessage: QName,
                                 val faultMessage: List<QName> = emptyList()
) : NodeHolder(node)

internal class Message(node: SmartNode,
                       val name: QName,
                       val parts: List<MessagePart>
) : NodeHolder(node)

internal class MessagePart(node: SmartNode,
                           val element: QName
) : NodeHolder(node)
