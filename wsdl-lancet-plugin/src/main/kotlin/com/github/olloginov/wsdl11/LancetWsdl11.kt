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

        for (node in documentElement.evaluateNodes(compile("/wsdl:definitions/wsdl:portType"))) {
            val portType = PortType(node,
                    name = nmtokenToQName(node.getAttributeOrEmpty("name")))
            readPortTypeOperations(portType)
            schema.portTypes.add(portType)
        }

        for (node in documentElement.evaluateNodes(compile("/wsdl:definitions/wsdl:service"))) {
            val service = Service(node,
                    name = nmtokenToQName(node.getAttributeOrEmpty("name")))
            readServicePorts(service)
            schema.services.add(service)
        }
    }

    private fun readPortTypeOperations(portType: PortType) {
        TODO("start here next time")
        for (node in portType.node.evaluateNodes(compile("./wsdl:operation"))) {
            val port = PortTypeOperation(node,
                    inputMessage = node.fullQName(node.getAttributeOrEmpty("binding")),
                    outputMessage = node.fullQName(node.getAttributeOrEmpty("binding")))
            service.ports.add(port)
        }
    }

    private fun readServicePorts(service: Service) {
        for (node in service.node.evaluateNodes(compile("./wsdl:port"))) {
            val port = ServicePort(node,
                    name = nmtokenToQName(node.getAttributeOrEmpty("name")),
                    binding = node.fullQName(node.getAttributeOrEmpty("binding")))
            service.ports.add(port)
        }
    }
}