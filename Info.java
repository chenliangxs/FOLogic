import java.util.ArrayList;

/**
 * info library to store the single cells and CNF
 * @author chenliangxs
 *
 */
public class Info{
   private ArrayList<ArrayList<Cell>> positive;  //positive cells
   private ArrayList<ArrayList<Cell>> negative;  //negative cells
   private ArrayList<ArrayList<Cell>> sentencePos; //positive rules
   private ArrayList<ArrayList<Cell>> sentenceNeg;  //negative rules
   
   public Info(){
      positive = new ArrayList<ArrayList<Cell>>();
      negative = new ArrayList<ArrayList<Cell>>();
      sentencePos = new ArrayList<ArrayList<Cell>>();
      sentenceNeg = new ArrayList<ArrayList<Cell>>();
   }
   public ArrayList<ArrayList<Cell>> getPos(){
      return positive;
   }
   public ArrayList<ArrayList<Cell>> getNeg(){
      return negative;
   }
   public ArrayList<ArrayList<Cell>> getSenPos(){
      return sentencePos;
   }
   public ArrayList<ArrayList<Cell>> getSenNeg(){
      return sentenceNeg;
   }
}