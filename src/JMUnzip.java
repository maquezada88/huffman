import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class JMUnzip implements Serializable {

  public static void main(String[] args) throws IOException {
    
    // Set up the option parser with the desired arguments.
       OptionParser parser = new OptionParser();
       
       OptionSpec<String> fileToDecompress = parser.accepts("Decompress", "File to decompress").withRequiredArg()
           .ofType(String.class).describedAs("File to decompress").required();
       
       OptionSpec<String> fileToCreate = parser.accepts("Create", "File to create").withRequiredArg()
           .ofType(String.class).describedAs("File to create").required();
       
       try {

         // Parse the command line arguments. This will raise an exception if the
         // arguments are not formatted correctly on the command line.
         OptionSet options = parser.parse(args);
         
         ObjectInputStream ois =
             new ObjectInputStream (
                 new FileInputStream (
                     options.valueOf (fileToDecompress)));
         HuffmanSave hs = (HuffmanSave) ois.readObject();
         System.out.println ("read Huffman Save object");
         for (Byte aByte : hs.getFrequencies().keySet())
         {
           System.out.println(aByte + ": " + hs.getFrequencies().get(aByte));
         }
         
         // Build Huffman tree.
         HuffTree hTree = HuffTree.buildTree (hs.getFrequencies());
         System.out.println ("built Huffman tree");
         
         // Create output file stream.
         FileOutputStream fos =
             new FileOutputStream (
                 options.valueOf (fileToCreate));
         
         Integer index = 0;
         while (index < hs.getEncoding().length())
         {
             index += decodeBitSequence (fos, hTree.root(), hs.getEncoding(), index);
         }
         fos.flush();
         fos.close();
       } catch (OptionException exception){
         System.out.println(exception.getMessage() + "\n");
         printHelpAndExit(parser);
       }
       catch (Throwable t)
       {
         System.out.println(t.getMessage() + "\n");        
       }
     }
  
     private static int decodeBitSequence (
         FileOutputStream fos, HuffBaseNode node, BitSequence bs, Integer index)
             throws IOException
     {
       if (node.isLeaf())
       {
         HuffLeafNode leaf = (HuffLeafNode) node;
         System.out.println("found an " + leaf.element()); 
         fos.write (leaf.element());
         return index;
       }
       else
       {
         HuffInternalNode internalNode = (HuffInternalNode) node;
         if (bs.getBit (index) == 0)
         {
           return decodeBitSequence (fos, internalNode.left(), bs, new Integer (index + 1));
         }
         else
         {
           return decodeBitSequence (fos, internalNode.right(), bs, new Integer (index + 1));
         }
       }
     }

  private static void buildBitList (
      Byte aByte, HuffBaseNode aNode, ArrayList<Integer> bs)
  {
    if (aNode.isLeaf())
    {
      HuffLeafNode leafNode = (HuffLeafNode) aNode;
      if (aByte.equals (leafNode.element())) return;
      bs.remove (bs.size() - 1);
    }
    else
    {
      HuffInternalNode internalNode = (HuffInternalNode) aNode;
      aNode = internalNode.left();
      if (aNode != null)
      {
        bs.add (0);
        buildBitList (aByte, aNode, bs);
      }
      else
      {
        bs.add (1);
        buildBitList (aByte, aNode, bs);
      }
    }
  }
  
  private static BitSequence makeBitSequence (ArrayList<Integer> bitList)
  {
    BitSequence bs = new BitSequence();
    for (Integer aBit : bitList) bs.appendBit (aBit);
    return bs;
  }
     
     private static void printHelpAndExit(OptionParser parser) throws IOException {
       parser.printHelpOn(System.out);
       // System.exit(0);
     }
}
