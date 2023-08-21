import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class JMZip {

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static void main(String[] args) throws IOException {

    // Set up the option parser with the desired arguments.
    OptionParser parser = new OptionParser();

    OptionSpec<String> fileToCompress = parser.accepts("Compress", "File Name").withRequiredArg()
        .ofType(String.class).describedAs("File to compress").required();

    OptionSpec<String> fileToCreate = parser.accepts("Create", "File Name").withRequiredArg()
        .ofType(String.class).describedAs("File to create").required();

    try {
      // Parse the command line arguments. This will raise an exception if the
      // arguments are not formatted correctly on the command line.
      OptionSet options = parser.parse(args);

      // create inputstream for file
      FileInputStream fis = new FileInputStream(options.valueOf(fileToCompress));
      
      // Construct frequency table.
      HashMap<Byte, Integer> freqTable = new HashMap<Byte, Integer>();
      int content;
      while ((content = fis.read()) != -1)
      {
        System.out.println("next byte");
        if (freqTable.containsKey ((byte) content))
        {
          int count = freqTable.get ((byte) content) + 1;
          freqTable.remove((byte) content);
          freqTable.put ((byte) content, count);
          System.out.println("freq table, " + content + "= " + count);
        }
        else
        {
          freqTable.put ((byte) content, 1);
        }
      }
      
      // Build Huffman tree.
      HuffTree hTree = HuffTree.buildTree (freqTable);
      
      // Iterate through each byte in the input file and use our
      // newly constructed Huffman tree to encode the BitSequence.
      BitSequence bs = new BitSequence();
      fis.close();
      fis = new FileInputStream (options.valueOf (fileToCompress));
      while ((content = fis.read()) != -1)
      {
        ArrayList<Integer> bitList = new ArrayList<Integer>();
        buildBitList ((byte) content, hTree.root(), bitList);
        bs.appendBits (makeBitSequence (bitList));
      }
      fis.close();
      
      System.out.println(bs);

      // Construct our save object and write it to output file.
      HuffmanSave hs = new HuffmanSave (bs, freqTable);
      ObjectOutputStream oos =
          new ObjectOutputStream (
              new FileOutputStream (options.valueOf (fileToCreate)));
      oos.writeObject (hs);
      oos.flush();
      oos.close();

    } catch (OptionException exception) {
      System.out.println(exception.getMessage() + "\n");
      printHelpAndExit(parser);
    } catch (IOException exception) {
      System.out.println(exception.getMessage() + "\n");
      printHelpAndExit(parser);
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
        aNode = internalNode.right();
        if (aNode != null)
        {
          bs.add (1);
          buildBitList (aByte, aNode, bs);
        }
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
    System.exit(0);
  }
}
