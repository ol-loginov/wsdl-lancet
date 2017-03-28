package com.github.olloginov.support;

import lombok.Getter;
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
    @Getter
    private final Node delegate;

    public SmartNode() {
        this(null);
    }

    public SmartNode(Object node) {
        this((Node) node);
    }

    public String getAttributeOrEmpty(String name) {
        if (delegate == null) {
            return "";
        }
        Node namedItem = delegate.getAttributes().getNamedItem(name);
        if (namedItem == null) {
            return "";
        }
        return namedItem.getNodeValue();
    }

    public String getAttributeOrElse(String name, String noneValue) {
        if (delegate == null) {
            return noneValue;
        }
        Node namedItem = delegate.getAttributes().getNamedItem(name);
        if (namedItem == null) {
            return noneValue;
        }
        return namedItem.getNodeValue();
    }

    public QName fullQName(String colonName) {
        return XmlUtil.fullQName(colonName, new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                return delegate.lookupNamespaceURI(prefix);
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
        if (delegate == null) {
            return;
        }
        Node parent = delegate.getParentNode();
        if (parent != null) {
            try {
                parent.removeChild(delegate);
            } catch (DOMException e) {
                if (e.code != DOMException.NOT_FOUND_ERR) {
                    throw e;
                }
            }
        }
    }

    public SmartNodeList evaluateNodes(XPathExpression expression) throws XPathExpressionException {
        if (delegate == null) {
            return new SmartNodeList();
        }
        return new SmartNodeList(expression.evaluate(delegate, XPathConstants.NODESET));
    }

    public SmartNode evaluateNode(XPathExpression expression) throws XPathExpressionException {
        if (delegate == null) {
            return new SmartNode();
        }
        return new SmartNode(expression.evaluate(delegate, XPathConstants.NODE));
    }

    public boolean exists() {
        return delegate != null;
    }

    public SmartNode parent() {
        return new SmartNode(delegate.getParentNode());
    }
}
