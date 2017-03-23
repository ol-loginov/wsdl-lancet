package com.github.olloginov.support;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;

public class XmlUtil {
    public static QName resolveQName(String name, NamespaceContext namespaceContext) {
        int colonIndex = name.indexOf(':');
        if (colonIndex == -1) {
            return new QName(namespaceContext.getNamespaceURI(DEFAULT_NS_PREFIX), name);
//            namespaceUri = new SmartNode(node.getOwnerDocument().getDocumentElement()).getAttributeOrEmpty("xmlns");
        } else {
            String namespace = name.substring(0, colonIndex);
            String localName = name.substring(colonIndex + 1);
            return new QName(namespaceContext.getNamespaceURI(namespace), localName, namespace);
//            namespaceUri = new SmartNode(node.getOwnerDocument().getDocumentElement()).getAttributeOrEmpty("xmlns:" + namespace);
        }
    }
}
