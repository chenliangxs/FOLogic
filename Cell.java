/**
 * Cell used to store an individual node info, predicate, arguments, sign
 * @author chenliangxs
 *
 */
public class Cell{
   public int sign;
   public String predicate;
   public String[] argms;
   public Cell(){
      sign = 1;
      predicate = "";
      argms = new String[1];
   }
   public Cell(int sign, String predicate, String[] argms){
      this.sign = sign;
      this.predicate = predicate;
      this.argms = argms;
   }
   
   public int getSign(){
      return sign;
   }
   
   public String getPredicate(){
      return predicate;
   }
   /**
    * check if two cell is equal
    * @param c2
    * @return
    */
   public boolean isEqual(Cell c2){
      if(sign!=c2.sign || !predicate.equals(c2.predicate)){
         return false;
      }
      String[] c2_argms = c2.argms;
      if(argms.length!=c2_argms.length){
         return false;
      }
      else{
         for(int i=0;i<argms.length;i++){
            if(!argms[i].equals(c2_argms[i])){
               return false;
            }
         }
      }
      return true;
   }
   /**
    * check if two cells are refu
    * @param c2
    * @return
    */
   public boolean isRefu(Cell c2){
      String[] c2_argms = c2.argms;
      if(argms.length!=c2_argms.length){
         return false;
      }
      else{
         for(int i=0;i<argms.length;i++){
            if(!argms[i].equals(c2_argms[i])){
               return false;
            }
         }
      }
      if(!predicate.equals(c2.predicate)){
         return false;
      }
      if((sign+c2.sign)==1){
         return true;
      }
      else{
         return false;
      }
   }
   /**
    * check if two cells are diff
    * @param c2
    * @return
    */
   public boolean isDiff(Cell c2){
      if(!predicate.equals(c2.predicate)){
         return true;
      }
      return false;
   }
}