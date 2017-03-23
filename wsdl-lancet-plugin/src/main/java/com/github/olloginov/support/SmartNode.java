package com.github.olloginov.support;

import lombok.RequiredArgsConstructor;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.util.Iterator;

@RequiredArgsConstructor
public class SmartNode {
    private final Node node;

    public SmartNode() {
        this(null);
    }

    public SmartNode(Object node) {
        this((Node) node);
    }

    public String getAttribute(String name) {
        if (node == null) {
            return null;
        }
        Node namedItem = node.getAttributes().getNamedItem(name);
        if (namedItem == null) {
            return null;
        }
        return namedItem.getNodeValue();
    }

    public String getAttributeOrEmpty(String name) {
        if (node == null) {
            return "";
        }
        Node namedItem = node.getAttributes().getNamedItem(name);
        if (namedItem == null) {
            return "";
        }
        return namedItem.getNodeValue();
    }

    public QName getAttributeQName(String attributeName) {
        return uriQName(getAttributeOrEmpty(attributeName));
    }

    public QName uriQName(String colonName) {
        return XmlUtil.resolveQName(colonName, new NamespaceContext() {
            @Override
            public String getNamespaceURI(String s) {
                return new SmartNode(node.getOwnerDocument().getDocumentElement()).getAttributeOrEmpty("xmlns:" + s);
            }

            @Override
            public String getPrefix(String s) {
                throw new IllegalStateException("not implemented");
            }

            @Override
            public Iterator getPrefixes(String s) {
                throw new IllegalStateException("not implemented");
            }
        });
    }

    public void remove() {
        if (node == null) {
            return;
        }
        Node parent = node.getParentNode();
        if (parent != null) {
            try {
                parent.removeChild(node);
            } catch (DOMException e) {
                if (e.code != DOMException.NOT_FOUND_ERR) {
                    throw e;
                }
            }
        }
    }

    public SmartNodeList evaluateNodes(XPathExpression expression) throws XPathExpressionException {
        if (node == null) {
            return new SmartNodeList();
        }
        return new SmartNodeList(expression.evaluate(node, XPathConstants.NODESET));
    }

    public SmartNode evaluateNode(XPathExpression expression) throws XPathExpressionException {
        if (node == null) {
            return new SmartNode();
        }
        return new SmartNode(expression.evaluate(node, XPathConstants.NODE));
    }
}
