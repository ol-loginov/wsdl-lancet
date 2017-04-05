package com.github.olloginov.wsdl11

import com.github.olloginov.Lancet
import com.github.olloginov.WsdlFilter
import com.github.olloginov.WsdlFilterDecision
import com.github.olloginov.support.NamespaceContextMap
import com.github.olloginov.support.SmartNode
import org.w3c.dom.Document
import javax.xml.namespace.QName
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathFactory

class LancetWsdl11(
        document: Document
) : Lancet {
    private val documentElement = SmartNode(document.documentElement)
    private val xPath = XPathFactory.newInstance().newXPath()
    private val targetNamespace = documentElement.getAttributeOrEmpty("targetNamespace")
    private val schema = Schema()

    init {
        xPath.namespaceContext = NamespaceContextMap(
                "wsdl", "http://schemas.xmlsoap.org/wsdl/")
    }

    private val unusedMessages = mutableSetOf<QName>()

    private fun compile(expression: String): XPathExpression = xPath.compile(expression)

    private fun nmtokenToQName(localName: String): QName {
        if (targetNamespace.isEmpty()) {
            return QName(localName)
        }
        return QName(targetNamespace, localName)
    }

    override fun process(include: WsdlFilter) {
        fun readMessageParts(serviceNode: SmartNode): List<MessagePart> = serviceNode
                .evaluateNodes(compile("./wsdl:part"))
                .map { MessagePart(it, element = it.fullQName(it.getAttributeOrEmpty("element"))) }

        for (node in documentElement.evaluateNodes(compile("/wsdl:definitions/wsdl:message"))) {
            val message = Message(node,
                    name = nmtokenToQName(node.getAttributeOrEmpty("name")),
                    parts = readMessageParts(node))
            schema.messages.add(message)
            unusedMessages += message.name
        }

        fun readPortTypeOperationMessage(operationNode: SmartNode, tagName: String): QName {
            val messageNode = operationNode.evaluateNode(compile("./wsdl:$tagName"))
            return messageNode.fullQName(messageNode.getAttributeOrEmpty("message"))
        }

        fun readPortTypeOperations(portTypeNode: SmartNode): List<PortTypeOperation> = portTypeNode
                .evaluateNodes(compile("./wsdl:operation"))
                .map { PortTypeOperation(it, name = it.getAttributeOrEmpty("name"), inputMessage = readPortTypeOperationMessage(it, "input"), outputMessage = readPortTypeOperationMessage(it, "output")) }

        for (node in documentElement.evaluateNodes(compile("/wsdl:definitions/wsdl:portType"))) {
            val portType = PortType(node,
                    name = nmtokenToQName(node.getAttributeOrEmpty("name")),
                    operations = readPortTypeOperations(node))
            schema.portTypes.add(portType)
        }

        fun readBindingOperations(bindingNode: SmartNode): List<BindingOperation> = bindingNode
                .evaluateNodes(compile("./wsdl:operation"))
                .map { BindingOperation(it, name = it.getAttributeOrEmpty("name")) }

        for (node in documentElement.evaluateNodes(compile("/wsdl:definitions/wsdl:binding"))) {
            val binding = Binding(node,
                    name = nmtokenToQName(node.getAttributeOrEmpty("name")),
                    type = node.fullQName(node.getAttributeOrEmpty("type")),
                    operations = readBindingOperations(node))
            schema.bindings.add(binding)
        }

        fun readServicePorts(serviceNode: SmartNode): List<ServicePort> = serviceNode
                .evaluateNodes(compile("./wsdl:port"))
                .map { ServicePort(it, name = nmtokenToQName(it.getAttributeOrEmpty("name")), binding = it.fullQName(it.getAttributeOrEmpty("binding"))) }

        for (node in documentElement.evaluateNodes(compile("/wsdl:definitions/wsdl:service"))) {
            val service = Service(node,
                    name = nmtokenToQName(node.getAttributeOrEmpty("name")),
                    ports = readServicePorts(node))
            schema.services.add(service)
        }


        // теперь маркируем кандидатов. елемент остается, если
        // 1) его нет в excludes
        // или
        // 2) он есть в списке includes

        schema.portTypes.forEach { portType ->
            val deletables = portType.removeOperations { include.needPortTypeOperation(portType.name, it.name) != WsdlFilterDecision.KEEP }
            for (deletable in deletables) {
                deletePortTypeOperation(portType, deletable)
            }

            if (portType.operations.isEmpty()) {
                deletePortType(portType)
            }
        }
    }

    private fun deletePortType(portType: PortType) {
        for (deletable in portType.removeOperations { true }) {
            deletePortTypeOperation(portType, deletable)
        }
        portType.node.remove()
    }

    private fun deletePortTypeOperation(portType: PortType, portTypeOperation: PortTypeOperation) {
        portTypeOperation.node.remove()
    }
}
