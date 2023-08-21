
public class HuffLeafNode implements HuffBaseNode {
  private byte element; // Element for this node
  private int weight; // Weight for this node

  /** Constructor */
  HuffLeafNode(byte el, int wt) {
    element = el;
    weight = wt;
  }

  /** @return The element value */
  byte element() {
    return element;
  }

  /** @return The weight */
  public int weight() {
    return weight;
  }

  /** Return true */
  public boolean isLeaf() {
    return true;
  }
}

