package com.github.olloginov.support;

import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class SmartNodeList extends ArrayList<SmartNode> {
    public SmartNodeList() {
        this(null);
    }

    public SmartNodeList(Object nodeList) {
        this((NodeList) nodeList);
    }

    public SmartNodeList(NodeList nodeList) {
        for (int i = 0; nodeList != null && i < nodeList.getLength(); ++i) {
            add(new SmartNode(nodeList.item(i)));
        }
    }
}
