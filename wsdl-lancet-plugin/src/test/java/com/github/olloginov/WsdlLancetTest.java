package com.github.olloginov;

import lombok.val;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSSerializer;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

public class WsdlLancetTest {
    private void processFile(Map<String, String> ruleNamespaces, WsdlSlice wsdlSlice, String inputAsset, String expectedAsset) throws Exception {
        File target = File.createTempFile("wsdl-lancet-test", ".wsdl");
        File source = new File(WsdlLancetTest.class.getResource(inputAsset).getFile());

        LancetConfiguration configuration = new LancetConfiguration(source, target, ruleNamespaces, wsdlSlice);

        LancetWrapper lancet = new LancetWrapper(new SystemStreamLog());
        lancet.process(new LancetConfiguration[]{configuration});

        assertContentEquals(new File(WsdlLancetTest.class.getResource(expectedAsset).getFile()), target);
    }

    @Test
    public void processBiletix_oneOp() throws Exception {
        processFile(
                new TreeMap<String, String>() {{
                    put("tais", "http://www.tais.ru/");
                }},
                new WsdlSlice(Collections.singletonList(
                        new WsdlPortType("tais:TAISSoapPort", Collections.singletonList(
                                new WsdlPortTypeOperation("GetOptimalFares")
                        ))
                )),
                "/biletix-source.wsdl", "/biletix-target.wsdl");
    }

    @Test
    public void processBiletix_removeAll() throws Exception {
        processFile(
                new TreeMap<String, String>(),
                new WsdlSlice(),
                "/biletix-source.wsdl", "/biletix-target-00.wsdl");
    }

    @Test
    public void processBiletix2_singlePortTypeOperation() throws Exception {
        processFile(
                new TreeMap<String, String>() {{
                    put("tais", "http://www.tais.ru/");
                }},
                new WsdlSlice(Collections.singletonList(
                        new WsdlPortType("tais:TAISSoapPort", Collections.singletonList(
                                new WsdlPortTypeOperation("GetOptimalFares")
                        ))
                )),
                "/biletix2-source.wsdl", "/biletix2-target-00.wsdl");
    }

    @Test
    public void processBiletix2_wholePortType() throws Exception {
        processFile(
                new TreeMap<String, String>() {{
                    put("tais", "http://www.tais.ru/");
                }},
                new WsdlSlice(Collections.singletonList(
                        new WsdlPortType("tais:TAISSoapPort2", Collections.<WsdlPortTypeOperation>emptyList())
                )),
                "/biletix2-source.wsdl", "/biletix2-target-01.wsdl");
    }

    @Test
    public void processSmallStrings() throws Exception {
        processFile(
                new TreeMap<String, String>(),
                new WsdlSlice(),
                "/smallstrings-source.wsdl", "/smallstrings-target.wsdl");
    }

    @Test
    public void needOneEbaySvc() throws Exception {
        processFile(
                new TreeMap<String, String>(),
                new WsdlSlice(),
                "/ebaySvc-source.wsdl", "/ebaySvc-target-00.wsdl");
    }

    private void assertContentEquals(File expected, File actual) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        val domLS = (DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS");

        LSParser domParser = domLS.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
        Document actualDom = domParser.parseURI(actual.toURI().toASCIIString());
        Document expectedDom = domParser.parseURI(expected.toURI().toASCIIString());

        LSSerializer serializer = domLS.createLSSerializer();
        serializer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE); // Set this to true if the output needs to be beautified.
        serializer.getDomConfig().setParameter("xml-declaration", true); // Set this to true if the declaration is needed to be outputted.


        StringWriter actualDomText = new StringWriter();
        LSOutput output = domLS.createLSOutput();
        output.setEncoding("utf-8");
        output.setCharacterStream(actualDomText);
        serializer.write(actualDom, output);

        StringWriter exceptedDomText = new StringWriter();
        output = domLS.createLSOutput();
        output.setEncoding("utf-8");
        output.setCharacterStream(exceptedDomText);
        serializer.write(expectedDom, output);

        assertEquals(exceptedDomText.toString(), actualDomText.toString());
    }
}
