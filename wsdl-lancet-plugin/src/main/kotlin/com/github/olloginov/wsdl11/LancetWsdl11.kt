package com.github.olloginov.wsdl11

import com.github.olloginov.FilterTree
import com.github.olloginov.Lancet
import com.github.olloginov.support.NamespaceContextMap
import com.github.olloginov.support.SmartNode
import org.w3c.dom.Document
import javax.xml.namespace.QName
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathFactory

class LancetWsdl11(
        private val document: Document
) : Lancet {
    private val documentElement = SmartNode(document.documentElement)
    private val xPath = XPathFactory.newInstance().newXPath()
    private val targetNamespace = documentElement.getAttributeOrEmpty("targetNamespace")

    init {
        xPath.namespaceContext = NamespaceContextMap(
                "wsdl", "http://schemas.xmlsoap.org/wsdl/")
    }


    private fun compile(expression: String): XPathExpression = xPath.compile(expression)

    private fun nmtokenToQName(localName: String): QName {
        if (targetNamespace.isEmpty()) {
            return QName(localName)
        }
        return QName(targetNamespace, localName)
    }

    override fun process(include: FilterTree, exclude: FilterTree) {
        val schema = Schema()


        fun readPortTypeOperationMessage(operationNode: SmartNode, tagName: String): QName {
            val messageNode = operationNode.evaluateNode(compile("./wsdl:$tagName"))
            return messageNode.fullQName(messageNode.getAttributeOrEmpty("message"))
        }

        fun readPortTypeOperations(portTypeNode: SmartNode): List<PortTypeOperation> = portTypeNode
                .evaluateNodes(compile("./wsdl:operation"))
                .map { n ->
                    PortTypeOperation(n,
                            inputMessage = readPortTypeOperationMessage(n, "input"),
                            outputMessage = readPortTypeOperationMessage(n, "output"))
                }

        for (node in documentElement.evaluateNodes(compile("/wsdl:definitions/wsdl:portType"))) {
            val portType = PortType(node,
                    name = nmtokenToQName(node.getAttributeOrEmpty("name")),
                    operations = readPortTypeOperations(node))
            schema.portTypes.add(portType)
        }

        for (node in documentElement.evaluateNodes(compile("/wsdl:definitions/wsdl:binding"))) {
            val binding = Binding(node,
                    name = nmtokenToQName(node.getAttributeOrEmpty("name")),
                    ports = readServicePorts(node))
            schema.services.add(service)
        }

        fun readServicePorts(serviceNode: SmartNode): List<ServicePort> = serviceNode
                .evaluateNodes(compile("./wsdl:port"))
                .map { it ->
                    ServicePort(it,
                            name = nmtokenToQName(it.getAttributeOrEmpty("name")),
                            binding = it.fullQName(it.getAttributeOrEmpty("binding")))
                }

        for (node in documentElement.evaluateNodes(compile("/wsdl:definitions/wsdl:service"))) {
            val service = Service(node,
                    name = nmtokenToQName(node.getAttributeOrEmpty("name")),
                    ports = readServicePorts(node))
            schema.services.add(service)
        }
    }

}