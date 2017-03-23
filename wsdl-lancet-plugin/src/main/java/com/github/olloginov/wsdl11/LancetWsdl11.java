package com.github.olloginov.wsdl11;

import com.ebmwebsourcing.easycommons.xml.DefaultNamespaceContext;
import com.github.olloginov.Lancet;
import com.github.olloginov.SmartNode;
import com.github.olloginov.SmartNodeList;
import com.github.olloginov.WsdlRule;
import lombok.RequiredArgsConstructor;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;

@RequiredArgsConstructor
public class LancetWsdl11 implements Lancet {
    private final Document document;
    private final XPath xPath;

    public LancetWsdl11(Document document) {
        this.document = document;

        DefaultNamespaceContext namespaceContext = new DefaultNamespaceContext();
        namespaceContext.bindNamespace("wsdl", "http://schemas.xmlsoap.org/wsdl/");

        xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(namespaceContext);
    }

    public void applyInclude(WsdlRule rule) {
        try {
            applyInclude0(rule);
        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        }
    }

    public void applyInclude0(WsdlRule rule) throws XPathExpressionException {
        removeBindings(rule);
    }

    private void removeBindings(WsdlRule rule) throws XPathExpressionException {
        XPathExpression bindingPath = xPath.compile("/wsdl:definitions/wsdl:binding");

        SmartNodeList bindingNodes = new SmartNodeList((NodeList) bindingPath.evaluate(document, XPathConstants.NODESET));
        for (SmartNode bindingNode : bindingNodes) {
            String bindingName = bindingNode.getAttribute("name");
            if (rule.getBindingByName(bindingName) == null) {
                bindingNode.remove();
            } else {
                
            }
        }
    }
}
