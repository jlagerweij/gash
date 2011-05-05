package net.lagerwey.gash;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class TreeElement<E> {
    private String key;
    private E value;
    private List<TreeElement<E>> children;

    public TreeElement() {
        children = new ArrayList<TreeElement<E>>();
    }

    public TreeElement(String key) {
        this();
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public E getValue() {
        return value;
    }

    public void setValue(E value) {
        this.value = value;
    }

    public List<TreeElement<E>> getChildren() {
        return children;
    }

    public void setChildren(List<TreeElement<E>> children) {
        this.children = children;
    }
}
