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
    private val wsdl = Wsdl()

    init {
        xPath.namespaceContext = NamespaceContextMap(
                "wsdl", "http://schemas.xmlsoap.org/wsdl/",
                "xs", "http://www.w3.org/2001/XMLSchema")
    }


    private fun compile(expression: String): XPathExpression = xPath.compile(expression)

    private fun nmtokenToQName(localName: String): QName {
        return nmtokenToQName(targetNamespace, localName)
    }

    private fun nmtokenToQName(targetNamespace: String, localName: String): QName {
        if (targetNamespace.isEmpty()) {
            return QName(localName)
        }
        return QName(targetNamespace, localName)
    }

    override fun compact(include: WsdlFilter) {
        readEmbeddedSchemas()
        readPortTypes()
        readServices()

        wsdl.portTypes.forEach { portType ->
            val deletableList = portType.operations.filter { include.needPortTypeOperation(portType.name, it.name) != WsdlFilterDecision.KEEP }
            for (deletable in deletableList) {
                deletePortTypeOperation(portType, deletable)
            }

            if (portType.operations.isEmpty()) {
                deletePortType(portType)
            }
        }

        compactMessages()

        wsdl.schemas.forEach { compactSchemaElements(it) }

        compactSchemaTypes()
    }

    private fun compactSchemaElements(schema: Schema) {
        val usedElements = wsdl.messages
                .flatMap { it.parts }
                .map { it.element }
                .toSet()

        schema.elements
                .filterNot { usedElements.contains(it.type) }
                .forEach {
                    schema.elements.remove(it)
                    it.node.remove()
                }
    }

    private fun compactMessages() {
        val usedMessages = wsdl.portTypes
                .flatMap { it.operations }
                .flatMap { it.faultMessage + it.inputMessage + it.outputMessage }
                .toSet()

        wsdl.messages
                .filterNot { usedMessages.contains(it.name) }
                .forEach { message ->
                    message.node.remove()
                    wsdl.messages.remove(message)
                }
    }

    private fun compactSchemaTypes() {
        class DependsOn {
        }

        val typeGraph: Map<QName, DependsOn> = mutableMapOf()
        wsdl.schemas
                .flatMap { it.types.values }
                .forEach {
                    typeGraph.get(it.type) ?: DependsOn()
                }
    }

    private fun readEmbeddedSchemas() {
        val referenceCollector = { node: SmartNode ->
            val selfType = listOf(node.getAttributeOrEmpty("type"))
                    .filter(String::isNotEmpty)
                    .map { node.fullQName(it) }

            val typeReferences = node.evaluateNodes(compile(".//*[@type]"))
                    .map { it.fullQName(it.getAttributeOrEmpty("type")) }

            val baseReferences = node.evaluateNodes(compile(".//*[@base]"))
                    .map { it.fullQName(it.getAttributeOrEmpty("base")) }

            (selfType + typeReferences + baseReferences).distinct()
        }

        for (schemaNode in documentElement.evaluateNodes(compile("/wsdl:definitions/wsdl:types/xs:schema"))) {
            val schemaNamespace = schemaNode.getAttributeOrEmpty("targetNamespace")

            val schemaTypes = schemaNode.evaluateNodes(compile("./xs:complexType")).map { schemaTypeNode ->
                SchemaType(schemaTypeNode,
                        nmtokenToQName(schemaNamespace, schemaTypeNode.getAttributeOrEmpty("name")),
                        referenceCollector(schemaTypeNode))
            }
                    .map { it.type to it }
                    .toMap().toMutableMap()

            val schemaElements = schemaNode.evaluateNodes(compile("./xs:element")).map { elementNode ->
                SchemaElement(elementNode,
                        nmtokenToQName(schemaNamespace, elementNode.getAttributeOrEmpty("name")),
                        referenceCollector(elementNode))
            }.toMutableList()

            wsdl.schemas.add(Schema(schemaNode,
                    schemaElements,
                    schemaTypes))
        }
    }

    private fun readServices() {
        fun readBindingOperations(bindingNode: SmartNode) = bindingNode
                .evaluateNodes(compile("./wsdl:operation"))
                .map { BindingOperation(it, name = it.getAttributeOrEmpty("name")) }
                .map { it.name to it }
                .toMap().toMutableMap()

        for (node in documentElement.evaluateNodes(compile("/wsdl:definitions/wsdl:binding"))) {
            val binding = Binding(node,
                    name = nmtokenToQName(node.getAttributeOrEmpty("name")),
                    type = node.fullQName(node.getAttributeOrEmpty("type")),
                    operations = readBindingOperations(node))
            wsdl.bindings[binding.name] = binding
        }

        fun readServicePorts(serviceNode: SmartNode) = serviceNode
                .evaluateNodes(compile("./wsdl:port"))
                .map { ServicePort(it, name = nmtokenToQName(it.getAttributeOrEmpty("name")), binding = it.fullQName(it.getAttributeOrEmpty("binding"))) }
                .toMutableList()

        for (node in documentElement.evaluateNodes(compile("/wsdl:definitions/wsdl:service"))) {
            val service = Service(node,
                    name = nmtokenToQName(node.getAttributeOrEmpty("name")),
                    ports = readServicePorts(node))
            wsdl.services.add(service)
        }
    }

    private fun readPortTypes() {
        fun readMessageParts(serviceNode: SmartNode) = serviceNode
                .evaluateNodes(compile("./wsdl:part"))
                .map { MessagePart(it, element = it.fullQName(it.getAttributeOrEmpty("element"))) }

        for (node in documentElement.evaluateNodes(compile("/wsdl:definitions/wsdl:message"))) {
            wsdl.messages.add(Message(node,
                    name = nmtokenToQName(node.getAttributeOrEmpty("name")),
                    parts = readMessageParts(node)))
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
                    operations = readPortTypeOperations(node).toMutableList())
            wsdl.portTypes.add(portType)
        }
    }

    private fun deletePortType(portType: PortType) {
        portType.operations.asSequence()
                .forEach { deletePortTypeOperation(portType, it) }
        portType.node.remove()

        wsdl.bindings.filterValues { it.type == portType.name }
                .map { it.value }
                .onEach { it.node.remove() }
                .onEach { wsdl.bindings.remove(it.name) }
                .onEach { binding ->
                    wsdl.services.forEach { service ->
                        service.ports
                                .filter { servicePort -> servicePort.binding == binding.name }
                                .onEach { servicePort ->
                                    servicePort.node.remove()
                                    service.ports.remove(servicePort)
                                }
                    }
                }
    }

    private fun deletePortTypeOperation(portType: PortType, portTypeOperation: PortTypeOperation) {
        portTypeOperation.node.remove()
        portType.operations.remove(portTypeOperation)

        wsdl.bindings
                .filterValues { it.type == portType.name }
                .forEach {
                    val bindingOperation = it.value.operations.remove(portTypeOperation.name)
                    if (bindingOperation != null) {
                        bindingOperation.node.remove()
                    }
                }
    }
}
