package com.github.olloginov;

import lombok.RequiredArgsConstructor;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

@RequiredArgsConstructor
public class SmartNode {
    private final Node node;

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
}
