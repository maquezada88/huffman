import java.util.HashMap;
import java.util.PriorityQueue;

/** A Huffman coding tree */
class HuffTree implements Comparable {
  private HuffBaseNode root;

  /** Constructors */
  HuffTree(byte el, int wt) {
    root = new HuffLeafNode(el, wt);
  }

  HuffTree(HuffBaseNode l, HuffBaseNode r, int wt) {
    root = new HuffInternalNode(l, r, wt);
  }

  HuffBaseNode root() {
    return root;
  }

  int weight() // Weight of tree is weight of root
  {
    return root.weight();
  }

  public int compareTo(Object t) {
    HuffTree that = (HuffTree) t;
    if (root.weight() < that.weight())
      return -1;
    else if (root.weight() == that.weight())
      return 0;
    else
      return 1;
  }

  public static HuffTree buildTree (HashMap<Byte, Integer> freqTable)
  {
    PriorityQueue<HuffTree> heap = new PriorityQueue<HuffTree>();
    HuffTree tmp1, tmp2, tmp3 = null;
    // Build initial version of heap.
    for (Byte aByte : freqTable.keySet())
    {
      heap.add (new HuffTree (aByte, freqTable.get(aByte)));
    }
    // Rebuild heap using Huffman rules.
    while (heap.size() > 1) { // While two items left
      tmp1 = heap.remove();
      tmp2 = heap.remove();
      tmp3 = new HuffTree(tmp1.root(), tmp2.root(), tmp1.weight() + tmp2.weight());
      heap.add (tmp3); // Return new tree to heap
    }
    return tmp3; // Return the tree
  }
}
