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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TreeElement that = (TreeElement) o;

        if (key != null ? !key.equals(that.key) : that.key != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }
}
