package com.github.olloginov;

import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class SmartNodeList extends ArrayList<SmartNode> {
    public SmartNodeList(NodeList nodeList) {
        for (int i = 0; i < nodeList.getLength(); ++i) {
            add(new SmartNode(nodeList.item(i)));
        }
    }
}
