package com.github.olloginov.wsdl11

import com.github.olloginov.support.SmartNode
import javax.xml.namespace.QName


internal class Wsdl(
        val services: MutableList<Service> = mutableListOf(),
        val portTypes: MutableList<PortType> = mutableListOf(),
        val bindings: MutableMap<QName, Binding> = mutableMapOf(),
        val messages: MutableList<Message> = mutableListOf(),
        val schemas: MutableList<Schema> = mutableListOf()
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


internal class Schema(node: SmartNode,
                      val elements: MutableList<SchemaElement>,
                      val types: MutableList<SchemaType>,
                      val referenceCount: MutableMap<QName, Int>
) : NodeHolder(node)

internal class SchemaElement(node: SmartNode,
                             val type: QName,
                             val references: List<QName>
) : NodeHolder(node)

internal class SchemaType(node: SmartNode,
                          val type: QName,
                          val references: List<QName>
) : NodeHolder(node)

internal class Service(node: SmartNode,
                       val name: QName,
                       val ports: MutableList<ServicePort> = mutableListOf()
) : NodeHolder(node)

internal class ServicePort(node: SmartNode,
                           val name: QName,
                           val binding: QName
) : NodeHolder(node)

internal class Binding(node: SmartNode,
                       val name: QName,
                       val type: QName,
                       val operations: MutableMap<String, BindingOperation>
) : NodeHolder(node)

internal class BindingOperation(node: SmartNode,
                                val name: String
) : NodeHolder(node)

internal class PortType(node: SmartNode,
                        val name: QName,
                        val operations: MutableList<PortTypeOperation>
) : NodeHolder(node)

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
