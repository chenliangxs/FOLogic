import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class homework{
   public static void main(String args[]) throws FileNotFoundException{
      File inputfile = new File("input.txt");       //read input informations
      Scanner in = new Scanner(inputfile);
      PrintWriter out = new PrintWriter("output.txt");  //output results after resolution
      int queryNum = Integer.parseInt(in.nextLine());
      String[] query = new String[queryNum];
      for(int i=0;i<query.length;i++){
         query[i] = in.nextLine().trim();
      }
      int inputNum = Integer.parseInt(in.nextLine());
      String[] input = new String[inputNum];
      for(int i=0;i<inputNum;i++){
         input[i] = in.nextLine().trim();
      }
      KB_2 test = new KB_2(input);                        //build up the knowledge base with input information
      for(int i=0;i<query.length;i++){
         out.println(test.askKB(query[i]));             //infer the results
      }

      in.close();
      out.close();
   }
}