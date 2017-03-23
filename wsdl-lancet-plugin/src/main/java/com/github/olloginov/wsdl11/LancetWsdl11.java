package com.github.olloginov.wsdl11;

import com.github.olloginov.FilterName;
import com.github.olloginov.FilterPortType;
import com.github.olloginov.FilterTree;
import com.github.olloginov.Lancet;
import com.github.olloginov.support.*;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.xpath.*;
import java.util.Stack;

@RequiredArgsConstructor
public class LancetWsdl11 implements Lancet {
    private final Document document;
    private final XPath xPath;

    private Stack<PostRule> postRules = new Stack<>();

    public LancetWsdl11(Document document) {
        this.document = document;

        NamespaceContextMap namespaceContext = new NamespaceContextMap(
                "wsdl", "http://schemas.xmlsoap.org/wsdl/");

        xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(namespaceContext);
    }

    private void reset() {
        postRules.clear();
    }

    @Override
    public void process(FilterTree include, FilterTree exclude) {
        try {
            processWsdl(include, exclude);

            while (!postRules.isEmpty()) {
                postRules.pop().run();
            }
        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        } finally {
            reset();
        }
    }

    private void processWsdl(FilterTree include, FilterTree exclude) throws XPathExpressionException {
        visitPortTypes(include.getPortTypes(), exclude.getPortTypes());
    }

    private void visitPortTypes(FilterPortType[] includeArray, FilterPortType[] excludeArray) throws XPathExpressionException {
        val include = FilterPortType.array(includeArray);
        val exclude = FilterPortType.array(excludeArray);
        for (SmartNode node : new SmartNodeList(compile("/wsdl:definitions/wsdl:portType").evaluate(document, XPathConstants.NODESET))) {
            DecisionMaker decision = new DecisionMaker(Decision.INCLUDE);
            String portTypeName = tnName(node.getAttribute("name"));

            FilterPortType excludePortType = exclude.getPortType(portTypeName);
            if (excludePortType != null && !excludePortType.hasOperations()) {
                decision.exclude();
            }

            FilterPortType includePortType = include.getPortType(portTypeName);
            if (includePortType != null && !includePortType.hasOperations()) {
                decision.include();
            }

            Decision defaultOperationDecision;
            switch (decision.getDecision()) {
                case EXCLUDE:
                    node.remove();
                    defaultOperationDecision = Decision.EXCLUDE;
                    break;

                default:
                    if (includePortType != null) {
                        defaultOperationDecision = Decision.EXCLUDE;
                    } else if (excludePortType != null) {
                        defaultOperationDecision = Decision.INCLUDE;
                    } else {
                        defaultOperationDecision = Decision.DEFAULT;
                    }
                    break;
            }

            visitPortTypeOperations(node, includePortType, excludePortType, defaultOperationDecision);
        }
    }

    private void visitPortTypeOperations(SmartNode portTypeNode, FilterPortType portTypeInclude, FilterPortType portTypeExclude, Decision defaultDecision) throws XPathExpressionException {
        val include = FilterName.array(portTypeInclude == null ? null : portTypeInclude.getOperations());
        val exclude = FilterName.array(portTypeExclude == null ? null : portTypeExclude.getOperations());

        for (SmartNode node : portTypeNode.evaluateNodes(compile("./wsdl:operation"))) {
            DecisionMaker decision = new DecisionMaker(defaultDecision);
            String name = tnName(node.getAttribute("name"));

            if (exclude.contains(name)) {
                decision.exclude();
            }

            if (include.contains(name)) {
                decision.include();
            }

            switch (decision.getDecision()) {
                case EXCLUDE:
                    node.remove();

                    final String inputMessage = node.evaluateNode(compile("./wsdl:input")).getAttributeQName("message").toString();
                    final String outputMessage = node.evaluateNode(compile("./wsdl:output")).getAttributeQName("message").toString();

                    postRules.push(new PostRule() {
                        @Override
                        public void run() throws XPathExpressionException {
                            visitMessages(null, new FilterName[]{
                                    new FilterName(inputMessage),
                                    new FilterName(outputMessage)
                            });
                        }
                    });
                    break;
            }
        }
    }

    /**
     * resolve target namespace name
     */
    private String tnName(String localName) {
        String targetNamespace = new SmartNode(document.getDocumentElement()).getAttribute("targetNamespace");
        if (targetNamespace == null || targetNamespace.isEmpty()) {
            return new QName(localName).toString();
        }
        return new QName(targetNamespace, localName).toString();
    }

    private void visitMessages(FilterName[] includeNames, FilterName[] excludeNames) throws XPathExpressionException {
        val include = FilterName.array(includeNames);
        val exclude = FilterName.array(excludeNames);

        for (SmartNode node : new SmartNodeList(compile("/wsdl:definitions/wsdl:message").evaluate(document, XPathConstants.NODESET))) {
            DecisionMaker decision = new DecisionMaker(Decision.DEFAULT);
            String name = tnName(node.getAttribute("name"));

            if (exclude.contains(name)) {
                decision.exclude();
            }

            if (include.contains(name)) {
                decision.include();
            }

            switch (decision.getDecision()) {
                case EXCLUDE:
                    node.remove();
                    continue;
            }
        }
    }

    private XPathExpression compile(String expression) throws XPathExpressionException {
        return xPath.compile(expression);
    }

    interface PostRule {
        void run() throws XPathExpressionException;
    }
}
