package com.github.olloginov;

import com.github.olloginov.wsdl11.LancetWsdl11;
import org.apache.maven.plugin.logging.Log;
import org.ow2.easywsdl.wsdl.api.WSDLException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;

public class WsdlLancet {
    private final Log log;
    private final DOMImplementationLS domLS;

    public WsdlLancet(Log log) {
        this.log = log;
        try {
            domLS = (DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS");
        } catch (Exception e) {
            throw new IllegalStateException("cannot work without DOMImplementationRegistry", e);
        }

        if (domLS == null) {
            throw new IllegalStateException("cannot work without DOMImplementationLS");
        }
    }

    public void process(WsdlSetup[] files) throws IOException, WSDLException, URISyntaxException {
        if (files == null || files.length == 0) {
            return;
        }

        for (WsdlSetup setup : files) {
            process(setup);
        }
    }

    private static <T> T requireArgument(T element, String nullMessage) {
        if (element == null) {
            throw new IllegalArgumentException(nullMessage);
        }
        return element;
    }

    private void process(WsdlSetup setup) throws IOException, WSDLException, URISyntaxException {
        File source = requireArgument(setup.getSource(), "parameter 'source' is not set");
        if (!source.exists()) {
            throw new IllegalArgumentException("source file '" + setup.getSource() + "' is not set");
        }

        File target = requireArgument(setup.getTarget(), "parameter 'target' is not set");
        if (!target.getParentFile().exists() && !target.getParentFile().mkdirs()) {
            throw new IllegalStateException("cannot create folder " + setup.getTarget().getParent());
        }

        log.info("parse " + source);
        LSParser domParser = domLS.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
        Document dom = domParser.parseURI(source.toURI().toASCIIString());

        Lancet lancet = null;
        QName rootElement = qName(dom.getDocumentElement());
        if (rootElement.equals(QName.valueOf("{http://schemas.xmlsoap.org/wsdl/}definitions"))) {
            lancet = new LancetWsdl11(dom);
        }

        if (lancet == null) {
            throw new IllegalStateException("no lancet for " + rootElement);
        }

        log.info("process " + source);
        lancet.applyInclude(setup.getInclude());

        log.info("write " + target);
        saveWsdl(dom, target);
    }

    private static QName qName(Node element) {
        return new QName(element.getNamespaceURI(), element.getLocalName());
    }

    private void saveWsdl(Document document, File target) throws WSDLException, IOException {
        LSSerializer serializer = domLS.createLSSerializer();
        serializer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE); // Set this to true if the output needs to be beautified.
        serializer.getDomConfig().setParameter("xml-declaration", true); // Set this to true if the declaration is needed to be outputted.

        LSOutput output = domLS.createLSOutput();
        output.setEncoding("utf-8");

        try (OutputStreamWriter fileOutputStream = new OutputStreamWriter(new FileOutputStream(target), "utf-8")) {
            output.setCharacterStream(fileOutputStream);
            serializer.write(document, output);
            fileOutputStream.flush();
        }
    }
}
