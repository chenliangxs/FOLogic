import java.util.ArrayList;
import java.util.Stack;
import java.util.HashMap;

public class KB_2{
   private String[] input;                                             //input string[]
   public HashMap<String, Info> knowledge;                              //map of knowledge, single literals, and rules;
   private ArrayList<ArrayList<Cell>> rules;                          //store clauses;
   private ArrayList<ArrayList<Cell>> singleLiterals;                         //store single literals;
   
   public KB_2(String[] sIn){
      input = sIn;
      knowledge = new HashMap<String, Info>();
      rules = new ArrayList<ArrayList<Cell>>();
      singleLiterals = new ArrayList<ArrayList<Cell>>();
      bulidKB();
   }
   
   /**
    * ask KB with query String s;
    * @param s
    * @return
    */
   public boolean askKB(String s){
      int sign = 0;
      if(s.charAt(0)=='~'){
         sign = 1;
         s = "("+s+")";
      }
      String sKey = readKey(s);
      String[] args = readArgm(s);
      Cell cell = new Cell(sign,sKey,args);
      if(!knowledge.containsKey(sKey)){
         return false;
      }
      return !tellKB(cell);
   }
   
   /**
    * tellKB, add a cell(rule or literal) to the KB, do resolution;
    * @param c
    * @return
    */
   public boolean tellKB(Cell c){
      ArrayList<Cell> inRule = new ArrayList<Cell>();
      inRule.add(c);
      Stack<ArrayList<Cell>> newKB = new Stack<ArrayList<Cell>>();
      Stack<ArrayList<Cell>> resolvants = new Stack<ArrayList<Cell>>();
      Stack<ArrayList<Cell>> used = new Stack<ArrayList<Cell>>();
      for(ArrayList<Cell> clit:singleLiterals){
         newKB.push(clit);
      }
      for(ArrayList<Cell> cRule:rules){
         newKB.push(cRule);
      }
      newKB.push(inRule);
      resolvants.push(inRule);
      addRules(inRule);
      int circle = 0;
      boolean stop = false;
      while(!stop && !newKB.isEmpty()){
         circle++;
         if(circle==100){
            stop = true;
            return true;
         }
         ArrayList<Cell> resoluteArray = newKB.pop();
         used.push(resoluteArray);
         //System.out.println(printRule(resoluteArray));
         
         for(Cell resoluteCell:resoluteArray){         //search for negated clauses; for loop for all possible combinations;
            int sign = resoluteCell.getSign();
            String resKey = resoluteCell.getPredicate();
            if(sign==1){
               ArrayList<ArrayList<Cell>> toSearch = new ArrayList<ArrayList<Cell>>();
               toSearch.addAll(knowledge.get(resKey).getNeg());
               toSearch.addAll(knowledge.get(resKey).getSenNeg());
               
               for(ArrayList<Cell>resoluteSearched:toSearch){        //search every clause found from KB;
                  
                  ArrayList<Cell> rule_1 = new ArrayList<Cell>();    //copy the two clauses to two new sentences;
                  ArrayList<Cell> rule_2 = new ArrayList<Cell>();    // 1. resoluteArray, 2. resoluteSearched;
                  
                  Cell toUni_1 = new Cell();                         //find the two cells to be unified;
                  Cell toUni_2 = new Cell();

                  for(Cell c_1:resoluteArray){                       //copy two clauses;
                     int sign_in = c_1.getSign();
                     String predicate_in = c_1.getPredicate();
                     String[] argms_in = new String[c_1.argms.length];
                     for(int i=0;i<argms_in.length;i++){
                           argms_in[i] = c_1.argms[i];
                     }
                     Cell c_x = new Cell(sign_in, predicate_in, argms_in);    //make a new cell for rule_1;
                     
                     if(c_1.isEqual(resoluteCell)){
                        toUni_1 = c_x;                                     //find the cell 
                     }
                     
                        rule_1.add(c_x);                                       //build rule_1 equals to 1. resoluteArray;
                  }
                  
                  for(Cell c_2:resoluteSearched){                        //build rule_2 equals to 2. resoluteSearched
                     int sign_in = c_2.getSign();
                     String predicate_in = c_2.getPredicate();
                     String[] argms_in = new String[c_2.argms.length];
                     for(int i=0;i<argms_in.length;i++){
                        argms_in[i] = c_2.argms[i];                           
                     }
                     Cell c_y = new Cell(sign_in, predicate_in, argms_in);
                     if(c_2.getPredicate().equals(resoluteCell.getPredicate()) && (c_2.getSign()+resoluteCell.getSign()==1)){  //??may have multiply ones;
                        toUni_2 = c_y;
                     }
                     
                        rule_2.add(c_y);                                      
                     
                  }
                  
                //System.out.println(printRule(rule_1)+" rule_1BeforeSub");
                  //System.out.println(printRule(rule_2)+" rule_2BeforeSub");
                  
                  if(canUnify(toUni_1,toUni_2)){
                     HashMap<String,String> uniKey = unify(toUni_1,toUni_2);
                     if(uniKey.isEmpty()){
                        break;
                     }
                     else{
                        if(!substitute(rule_1, rule_2, uniKey)){
                           break;
                        }              
                     }
                  }
                  else{
                    break;
                  }
                  
                //System.out.println(printRule(rule_1)+" rule_1AfterSub");
                 // System.out.println(printRule(rule_2)+" rule_2AfterSub");
                  
                  ArrayList<Cell> resolvedRule = new ArrayList<Cell>();                 //Doing resolution
                  
                  for(Cell restElement: rule_1){                               //search in the new array rule_1;
                     boolean refu = false;
                     for(Cell check:rule_2){                                  //search in the new Array rule_2;
                        if(check.isRefu(restElement)){
                           refu = true;
                        }
                     }
                     if(!refu){
                        boolean theSame = false;
                        for(Cell check_2:resolvedRule){
                           if(check_2.isEqual(restElement)){
                              theSame = true;
                           }
                        }
                        if(!theSame){
                           resolvedRule.add(restElement);
                        }                       
                     }
                  }
                  for(Cell restInSearch:rule_2){
                     boolean notAdd = false;
                     for(Cell check:rule_1){
                        if(check.isRefu(restInSearch) || check.isEqual(restInSearch)){
                           notAdd = true;
                        }
                     }
                     if(!notAdd){
                        boolean theSame = false;
                        for(Cell check_2:resolvedRule){
                           if(check_2.isEqual(restInSearch)){
                              theSame = true;
                           }
                        }
                        if(!theSame){
                           resolvedRule.add(restInSearch);
                        }                       
                        //resolvedRule.add(restInSearch);
                     }
                  }
                  
                 // System.out.println(printRule(resolvedRule)+" resolvedRule");
                  
                  if(resolvedRule.size()==0){                                             //delete added wrong rules;
                     while(!resolvants.isEmpty()){
                        removeRules(resolvants.pop());
                     }
                     return false;                                                    //controdictory;
                  }
                  else{                                                                  //add the new resolved to KB;
                     if(isNewRule(resolvedRule)){
                        resolvants.push(resolvedRule);
                        newKB.push(resolvedRule);
                        addRules(resolvedRule);
                     }
                  }      
               }
            }
            else{
               ArrayList<ArrayList<Cell>> toSearch = new ArrayList<ArrayList<Cell>>();
               toSearch.addAll(knowledge.get(resKey).getPos());
               toSearch.addAll(knowledge.get(resKey).getSenPos());
             
               for(ArrayList<Cell>resoluteSearched:toSearch){  
               
                  ArrayList<Cell> rule_1 = new ArrayList<Cell>();
                  ArrayList<Cell> rule_2 = new ArrayList<Cell>();
;
                  
                  Cell toUni_1 = new Cell();
                  Cell toUni_2 = new Cell();
                  
                  for(Cell c_1:resoluteArray){                       //copy two clauses;
                     int sign_in = c_1.getSign();
                     String predicate_in = c_1.getPredicate();
                     String[] argms_in = new String[c_1.argms.length];
                     for(int i=0;i<argms_in.length;i++){
                           argms_in[i] = c_1.argms[i];
                     }
                     Cell c_x = new Cell(sign_in, predicate_in, argms_in);    //make a new cell for rule_1;
                     
                     if(c_1.isEqual(resoluteCell)){
                        toUni_1 = c_x;                                     //find the cell 
                     }
                     
                     rule_1.add(c_x);                                       //build rule_1 equals to 1. resoluteArray;
                  }
                  
                  for(Cell c_2:resoluteSearched){                        //build rule_2 equals to 2. resoluteSearched
                     int sign_in = c_2.getSign();
                     String predicate_in = c_2.getPredicate();
                     String[] argms_in = new String[c_2.argms.length];
                     for(int i=0;i<argms_in.length;i++){
                        argms_in[i] = c_2.argms[i];                           
                     }
                     Cell c_y = new Cell(sign_in, predicate_in, argms_in);
                     if(c_2.getPredicate().equals(resoluteCell.getPredicate()) && (c_2.getSign()+resoluteCell.getSign()==1)){  //??may have multiply ones;
                        toUni_2 = c_y;
                     }
                     rule_2.add(c_y);
                  }
                  
                  //System.out.println(printRule(rule_1)+" rule_1BeforeSub");
                  //System.out.println(printRule(rule_2)+" rule_2BeforeSub");
                  
                  if(canUnify(toUni_1,toUni_2)){
                     HashMap<String,String> uniKey = unify(toUni_1,toUni_2);
                     if(uniKey.isEmpty()){
                        break;
                     }
                     else{
                        if(!substitute(rule_1, rule_2, uniKey)){
                           break;
                        }              
                     }
                  }
                  else{
                    break;
                  }
                  
                 // System.out.println(printRule(rule_1)+" rule_1AfterSub");
                  //System.out.println(printRule(rule_2)+" rule_2AfterSub");
    
                  ArrayList<Cell> resolvedRule = new ArrayList<Cell>();                    //rest elements in resoluteArray
                 
                  for(Cell restElement: rule_1){
                     boolean refu = false;
                     for(Cell check:rule_2){
                        if(check.isRefu(restElement)){
                           refu = true;
                        }
                     }
                     if(!refu){
                        boolean theSame = false;
                        for(Cell check_2:resolvedRule){
                           if(check_2.isEqual(restElement)){
                              theSame = true;
                           }
                        }
                        if(!theSame){
                           resolvedRule.add(restElement);
                        }                       
                     }
                  }
                  
                  for(Cell restInSearch:rule_2){
                     boolean notAdd = false;
                     for(Cell check:rule_1){
                        if(check.isRefu(restInSearch) || check.isEqual(restInSearch)){
                           notAdd = true;
                        }
                     }
                     if(!notAdd){
                        boolean theSame = false;
                        for(Cell check_2:resolvedRule){
                           if(check_2.isEqual(restInSearch)){
                              theSame = true;
                           }
                        }
                        if(!theSame){
                           resolvedRule.add(restInSearch);
                        }                       
                        //resolvedRule.add(restInSearch);
                     }
                  }

                 // System.out.println(printRule(resolvedRule)+" resolvedRule");
                  
                  if(resolvedRule.size()==0){                                                //delete added wrong rules;
                     while(!resolvants.isEmpty()){
                        removeRules(resolvants.pop());
                     }
                     return false;                                                     //controdictory;
                  }
                  else{                                                                  //add the new resolved to KB;                   
                     if(isNewRule(resolvedRule)){
                        resolvants.push(resolvedRule);
                        newKB.push(resolvedRule);
                        addRules(resolvedRule);
                     }
                  }      
               }
            }
         } 
      }
      return true;
   }
    
   /**
    * canUnify function, determine whether two literals can unify;
    */
   public boolean canUnify(Cell c1, Cell c2){
	      if(c1.isDiff(c2)){
	         return false;
	      }
	      if(c1.argms.length!=c2.argms.length){
	         return false;
	      }
	      boolean hasSubstitution = false;
	      for(int i=0;i<c1.argms.length;i++){
	         String s1 = c1.argms[i];
	         String s2 = c2.argms[i];
	         if(!compatible(s1,s2)){
	            return false;
	         }
	         if (Character.isLowerCase(s1.charAt(0)) && Character.isUpperCase(s2.charAt(0))) {
	        	 hasSubstitution = true;
	         } else if (Character.isUpperCase(s1.charAt(0)) && Character.isLowerCase(s2.charAt(0))) {
	        	 hasSubstitution = true;
	         } else if (Character.isUpperCase(s1.charAt(0)) && Character.isUpperCase(s2.charAt(0))) {
	        	 hasSubstitution = true;
	         }
	      }
	      return hasSubstitution;
	   }
   /**
    * two variable is compatible;
    */
   public boolean compatible(String s1, String s2){
      if(Character.isUpperCase(s1.charAt(0)) && Character.isUpperCase(s2.charAt(0))){
         if(!s1.equals(s2)){
            return false;
         }
      }
      if(Character.isLowerCase(s1.charAt(0)) && Character.isLowerCase(s2.charAt(0))){
         if(s1.charAt(0)!=s2.charAt(0)){
            if(s1.substring(1).equals(s2.substring(1))){
               return false;
            }
         }
      }
      return true;
   }
   /**
    * unify two literals;
    * return hashmap with substitution;
    */
   public HashMap<String,String> unify(Cell s_1, Cell s_2){
      HashMap<String, String> substitution = new HashMap<String,String>();
      for(int i=0;i<s_1.argms.length;i++){
         String arg1 = s_1.argms[i];
         String arg2 = s_2.argms[i];
         if(Character.isLowerCase(arg1.charAt(0)) && Character.isUpperCase(arg2.charAt(0))){
               substitution.put(arg1,arg2);
         }
         else if(Character.isUpperCase(arg1.charAt(0)) && Character.isLowerCase(arg2.charAt(0))){
               substitution.put(arg2, arg1);
         }
         else if(Character.isLowerCase(arg1.charAt(0)) && Character.isLowerCase(arg2.charAt(0))){
            if(substitution.containsKey(arg1)){
               if(compatible(arg2,substitution.get(arg1))){
                  substitution.put(arg2, substitution.get(arg1));
               }
               else{
                  return new HashMap<String,String>();
               }
            }
            else{
               if(substitution.containsKey(arg2)){
                  substitution.put(arg1, substitution.get(arg2));
               }
               else{
                  substitution.put(arg1, arg2);
               }
            }
         }
         else{
            if(!compatible(arg1,arg2)){
               return new HashMap<String, String>();
            }
            else{
               substitution.put(arg1,arg2);
            }
         }
      }
      return substitution;
   }
         
   /**
    * substitute rules with a hashmap;
    */
   public boolean substitute(ArrayList<Cell> rule_1, ArrayList<Cell> rule_2, HashMap<String, String> theta){
      for(Cell c_1:rule_1){
         for(int i=0;i<c_1.argms.length;i++){
            if(theta.containsKey(c_1.argms[i])){
               if(compatible(c_1.argms[i],theta.get(c_1.argms[i]))){
               c_1.argms[i] = theta.get(c_1.argms[i]);
               }
               else{
                  return false;
               }
               //System.out.println(c_1.argms[i]+" rule1");
            }
         }
      }
      for(Cell c_2:rule_2){
         for(int i=0;i<c_2.argms.length;i++){
            if(theta.containsKey(c_2.argms[i])){
               if(compatible(c_2.argms[i],theta.get(c_2.argms[i]))){
                  c_2.argms[i] = theta.get(c_2.argms[i]);
               }
               else{
                  return false;
               }
               //System.out.println(c_2.argms[i]+" rule2");
            }
         }
      }
      return true;
   }
   
   /**
    * is a new rule;
    */
   public boolean isNewRule(ArrayList<Cell> newRule){
      Cell lookCell = newRule.get(0);
      String searchKey = lookCell.getPredicate();
      int searchSign = lookCell.getSign();
      int searchSize = newRule.size();
      if((searchSign==1) && (searchSize==1)){
         for(ArrayList<Cell> oldRule:knowledge.get(searchKey).getPos()){
            if(oldRule.size()==newRule.size()){
               if(sameRule(newRule,oldRule)){
                  return false;
               }
            }
         }
         return true;
      }
      else if((searchSign==0) && (searchSize==1)){
         for(ArrayList<Cell> oldRule:knowledge.get(searchKey).getNeg()){
            if(oldRule.size()==newRule.size()){
               if(sameRule(newRule,oldRule)){
                  return false;
               }
            }
         }
         return true;
      }
      else if((searchSign==1) && (searchSize>1)){
         for(ArrayList<Cell> oldRule:knowledge.get(searchKey).getSenPos()){   //search every rules in KB, if there is a same on return false;
            if(oldRule.size()==newRule.size()){
               if(sameRule(newRule,oldRule)){
                  return false;
               }
            }              
         }
         return true;                                    // no same rule;
      }
      else if(searchSign==0 && searchSize>1){
         for(ArrayList<Cell> oldRule:knowledge.get(searchKey).getSenNeg()){
            if(oldRule.size()==newRule.size()){
               if(sameRule(newRule,oldRule)){
                  return false;
               }
            }              
         }
         return true;
      }
      return true;
   }
   /**
    * check whether two rules are the same;
    * @param r1
    * @param r2
    * @return
    */
   public boolean sameRule(ArrayList<Cell> r1, ArrayList<Cell> r2){
      if(r1.size()!=r2.size()){
         return false;
      }
      for(Cell check_1:r1){
         boolean same = false;
         for(Cell check_2:r2){
            if(check_1.isEqual(check_2)){
               same = true;
            }
         }
         if(same==false){
            return false;
         }
      }
      for(Cell check_3:r2){
         boolean same2 = false;
         for(Cell check_4:r1){
            if(check_3.isEqual(check_4)){
               same2 = true;
            }
         }
         if(same2==false){
            return false;
         }
      }
      return true;
   }
   /**
    * add rule to knowledge;
    */
   public void addRules(ArrayList<Cell> rule){
      int xsize = 1;
      if(rule.size()>1){
         xsize = 0;
      }
      for(Cell literal:rule){
         int sign = literal.getSign();
         String addKey = literal.getPredicate();
         if(xsize==1 && sign==1){
            if(!(knowledge.get(addKey).getPos().contains(rule))){
               knowledge.get(addKey).getPos().add(rule);
            }
         }
         else if(xsize==1 && sign==0){
            if(!(knowledge.get(addKey).getNeg().contains(rule))){
               knowledge.get(addKey).getNeg().add(rule);
            }
         }
         else if(xsize==0 && sign==1){
            if(!(knowledge.get(addKey).getSenPos().contains(rule))){
               knowledge.get(addKey).getSenPos().add(rule);
            }
         }
         else{
            if(!(knowledge.get(addKey).getSenNeg().contains(rule))){
               knowledge.get(addKey).getSenNeg().add(rule);
            }
         }
      }
   }
   
   /**
    * remove a rule;
    * @param ruleToRemove
    */
   public void removeRules(ArrayList<Cell> ruleToRemove){
      int rule = 0;
      if(ruleToRemove.size()>1){
         rule = 1;
      }
      for(Cell literalToRemove:ruleToRemove){
         String reMoveKey = literalToRemove.getPredicate();
         int sign = literalToRemove.getSign();
         if(rule==0 && sign==1){
            knowledge.get(reMoveKey).getPos().remove(ruleToRemove);
         }
         else if(rule==0 && sign==0){
            knowledge.get(reMoveKey).getNeg().remove(ruleToRemove);
         }
         else if(rule==1 && sign==1){
            knowledge.get(reMoveKey).getSenPos().remove(ruleToRemove);
         }
         else{
            knowledge.get(reMoveKey).getSenNeg().remove(ruleToRemove);
         }
      }
   }
   
   
   /**
    * build up a KB
    */
   private void bulidKB(){
      int j=0;
      for(String s:input){
         j++;                                                             //process input string;
         String cnfForm = toCNF(s);
         String[] trans = cnfForm.split("&");
         for(int i=0;i<trans.length;i++){
            trans[i] = readLiteral(trans[i]);
         }
         for(String t:trans){
            
            String[] st = t.split("\\|");
            for(int i=0;i<st.length;i++){
               st[i] = readLiteral(st[i]);
            }
            if(st.length==1){
               int sign = 1;
               if(st[0].charAt(0)=='('){
                  sign = 0;
               }
               String predicate = readKey(st[0]);
               String[] arg = readArgm(st[0]);
               for(int i=0;i<arg.length;i++){
                  if(Character.isLowerCase(arg[i].charAt(0))){
                     arg[i] = arg[i]+j;
                  }
               }
               Cell cell = new Cell(sign,predicate,arg);
               ArrayList<Cell> readSingleLit = new ArrayList<Cell>();
               readSingleLit.add(cell);
               singleLiterals.add(readSingleLit);
            }
            else{
               ArrayList<Cell> readRule = new ArrayList<Cell>();
               for(String stt:st){
                  int sign = 1;
                  if(stt.charAt(0)=='('){
                     sign = 0;
                  }
                  String predicate = readKey(stt);
                  String[] arg = readArgm(stt);
                  for(int i=0;i<arg.length;i++){
                     if(Character.isLowerCase(arg[i].charAt(0))){
                        arg[i] = arg[i]+j;
                     }
                  }
                  Cell cell = new Cell(sign,predicate,arg);
                  readRule.add(cell);
               }
               rules.add(readRule);
            }
         }
      }
      for(ArrayList<Cell> strLit:singleLiterals){
         int sign = strLit.get(0).getSign();
         String kbKey = strLit.get(0).getPredicate();
         if(knowledge.containsKey(kbKey)){
            if(sign==1){
               knowledge.get(kbKey).getPos().add(strLit);
            }
            else{
               knowledge.get(kbKey).getNeg().add(strLit);
            }
         }
         else{
            Info info = new Info();
            if(sign==1){
               info.getPos().add(strLit);
            }
            else{
               info.getNeg().add(strLit);
            }
            knowledge.put(kbKey, info);
         }
      }
      for(ArrayList<Cell> s_rule:rules){
         for(Cell s_subRule:s_rule){
            int sign = s_subRule.getSign();
            String kbKey = s_subRule.getPredicate();
            if(knowledge.containsKey(kbKey)){
               if(sign==1){
                  knowledge.get(kbKey).getSenPos().add(s_rule);
               }
               else{
                  knowledge.get(kbKey).getSenNeg().add(s_rule);
               }
            }
            else{
               Info info = new Info();
               if(sign==1){
                  info.getSenPos().add(s_rule);
               }
               else{
                  info.getSenNeg().add(s_rule);
               }
               knowledge.put(kbKey,info);
            }
         }
      }
   }
   public String readLiteral(String s){
      String res = "";
      Stack<Character> stack_1 = new Stack<Character>();
      Stack<Character> stack_2 = new Stack<Character>();
      Stack<Character> stack_out = new Stack<Character>();
      int m=0;
      for(int i=0;i<s.length();i++){
         char ch_1 = s.charAt(i);
         char ch_2 = s.charAt(s.length()-i-1);
         stack_1.push(ch_1);
         stack_2.push(ch_2);
         if(ch_1=='('){
            m++;
         }
         else if(ch_1==')'){
            m--;
         }
      }
      int n=0;
      boolean stop = false;
      if(m>=0){
         while(!stop && !stack_1.isEmpty()){
            char check = stack_1.pop();
            if(check==')'){
               n++;
            }
            else if(check=='('){
               n--;
            }
            stack_out.push(check);
            if((stack_1.isEmpty()) || (n==0 && stack_1.peek()=='(')){
               stop = true;
            }
         }
         
         while(!stack_out.isEmpty()){
            res = res + stack_out.pop();
         }
      }
      else{
         while(!stop && !stack_2.isEmpty()){
            char check = stack_2.pop();
            if(check==')'){
               n++;
            }
            else if(check=='('){
               n--;
            }
            stack_out.push(check);
            if((stack_2.isEmpty()) || (n==0 && stack_2.peek()==')')){
               stop = true;
            }
         }
         while(!stack_out.isEmpty()){
            res = stack_out.pop()+res;
         }
      }
      return res;
   }
   
   /**
    * read the predicate;
    * @param s
    * @return
    */
   public String readKey(String s){
      String res = "";
      int i=0;
      if(s.charAt(0)=='('){
         i=2;
      }
      Stack<Character> pre = new Stack<Character>();
      boolean stop = false;
      while(!stop && i<s.length()){
         char ch_sPre = s.charAt(i);
         if(ch_sPre=='('){
            stop = true;
         }
         else{
            pre.push(ch_sPre);
         }
         i++;
      }
      while(!pre.isEmpty()){
         res = pre.pop()+res;
      }
      return res;
   }
   public String[] readArgm(String s){
      Stack<Character> stack_1 = new Stack<Character>();
      ArrayList<String> res = new ArrayList<String>();
      for(int i=0;i<s.length();i++){
         char ch = s.charAt(i);
         if(ch==','||ch==')'){
            String arg = "";
            while(stack_1.peek()!='('){
               arg = stack_1.pop()+arg;
            }
            if(arg.length()!=0){
               res.add(arg);
            }
         }
         else{
            stack_1.push(ch);
         }
      }
      return res.toArray(new String[res.size()]);
   }
   
   /**
    * convert input string to CNF;
    * @param s
    * @return
    */
   public String toCNF(String s){
      String s_1 = convertHorn(s);
      String s_2 = moveNegation(s_1);
      String s_3 = distribute(s_2);
      return s_3;
   }
   public String convertHorn(String s){
      String result = "";
      Stack<Character> stack_in = new Stack<Character>();
      int i=s.length()-1;
      while(i>=0){
         char ch_in = s.charAt(i);
         if(ch_in=='&'||ch_in=='|'||ch_in==')'||ch_in=='('||ch_in=='~'||ch_in=='='||ch_in=='>'){
            while(!stack_in.isEmpty() && stack_in.peek()==' '){
               stack_in.pop();
            }
            stack_in.push(ch_in);
            i--;
         }
         else if(ch_in==' '){
            if(stack_in.peek()=='&'||stack_in.peek()=='|'||stack_in.peek()=='('||stack_in.peek()==')'||stack_in.peek()=='~'||stack_in.peek()=='='||stack_in.peek()=='>'){
               i--;
            }
            else{
               stack_in.push(ch_in);
               i--;
            }
         }
         else{
            stack_in.push(ch_in);
            i--;
         }
      }
      Stack<Character> stack_out = new Stack<Character>();
      while(!stack_in.isEmpty()){
         char ch_h = stack_in.pop();
         if(ch_h!='='){
            stack_out.push(ch_h);
         }
         else{
            stack_in.pop();
            Stack<Character> temp = new Stack<Character>();
            int n = 0;
            boolean stop = false;
            while(!stop){
               char ch_hp = stack_out.pop();
               temp.push(ch_hp);
               if(ch_hp==')'){
                  n++;
               }
               if(ch_hp=='('){
                  n--;
               }
               if(n==0&&(stack_out.peek()=='(')){
                  stop = true;
               }
            }
            stack_out.push('(');
            stack_out.push('~');
            while(!temp.isEmpty()){
               stack_out.push(temp.pop());
            }
            stack_out.push(')');
            stack_out.push('|');
         }
      }
      while(!stack_out.isEmpty()){
         result = stack_out.pop()+result;
      }
      return result;
   }
   public String moveNegation(String s){
      String result = "";
      Stack<Character> stack_input = new Stack<Character>();
      Stack<Character> stack_result = new Stack<Character>();
      Stack<Character> stack_3 = new Stack<Character>();
      int i=s.length()-1;
      while(i>=0){
         char ch_in = s.charAt(i);
         if(ch_in=='&'||ch_in=='|'||ch_in==')'||ch_in=='('||ch_in=='~'){
            while(!stack_input.isEmpty() && stack_input.peek()==' '){
               stack_input.pop();
            }
            stack_input.push(ch_in);
            i--;
         }
         else if(ch_in==' '){
            if(stack_input.peek()=='&'||stack_input.peek()=='|'||stack_input.peek()=='('||stack_input.peek()==')'||stack_input.peek()=='~'){
               i--;
            }
            else{
               stack_input.push(ch_in);
               i--;
            }
         }
         else{
            stack_input.push(ch_in);
            i--;
         }
      }
      while(!stack_input.isEmpty()){
         char ch = stack_input.pop();
         if(ch!='~'){
            stack_result.push(ch);
         }
         else if(ch=='~'){
            if(stack_input.peek()!='('){
               stack_result.push(ch);
            }
            else{
               stack_result.pop();
               char ch_sub = stack_input.pop();
               stack_3.push(ch_sub);
               int inCount=1;
               while(inCount>0){
                  char chx = stack_input.pop();
                  stack_3.push(chx);
                  if(chx==')'){
                     inCount--;
                  }
                  if(chx=='('){
                     inCount++;
                  }
               }
               stack_input.pop();
               Stack<Character> temp = new Stack<Character>();
               while(!stack_3.isEmpty()){
                     char ch_3 = stack_3.peek();
                     char chx = stack_3.pop();
                
                     if(ch_3==')'){
                        stack_input.push(chx);
                        inCount++;
                     }
                     else if(ch_3=='('){
                        if(inCount>1){
                           stack_input.push(chx);
                        }
                        inCount--;
                        if(inCount==0){
                           int n=0;
                           boolean stop=false;
                           while(!stop){
                              char chn = stack_input.pop();
                              if(chn=='('){
                                 n++;
                              }
                              else if(chn==')'){
                                 n--;
                              }
                              temp.push(chn);
                              if(n==0&&(stack_input.peek()=='&'||stack_input.peek()=='|')){
                                 stack_input.push(')');
                                 stop=true;
                                 while(!temp.isEmpty()){
                                    stack_input.push(temp.pop());
                                 }
                                 stack_input.push('~');
                                 stack_input.push('(');
                                 stack_input.push('(');
                              }
                           }
                        }
                     }
                     else if(ch_3!='~'&&ch_3!='|'&&ch_3!='&'){
                        stack_input.push(chx);
                     }
                     else if(ch_3=='~'||ch_3=='|'||ch_3=='&'){
                        if(inCount!=1){
                           stack_input.push(chx);
                        }
                        else{
                           if(ch_3=='~'){
                              stack_3.pop();
                              int n = 1;
                              while(n>0){
                                 char chn = stack_input.pop();
                                 if(chn=='('){
                                    n++;
                                 }
                                 else if(chn==')'){
                                    n--;
                                 }
                                 if(n>0){
                                    temp.push(chn);
                                 }
                              }
                              while(!temp.isEmpty()){
                                 stack_input.push(temp.pop());
                              }
                           }
                           else if(ch_3=='|'||ch_3=='&'){
                              int n=0;
                              boolean stop = false;
                              while(!stop){
                                 char chn = stack_input.pop();
                                 if(chn=='('){
                                    n++;
                                 }
                                 else if(chn==')'){
                                    n--;
                                 }
                                 temp.push(chn);
                                 if(n==0 &&(stack_input.peek()==')'||stack_input.peek()=='&'||stack_input.peek()=='|')){
                                    stop = true;
                            
                                 }
                              }
                              stack_input.push(')');
                              while(!temp.isEmpty()){
                                 stack_input.push(temp.pop());
                              }
                              stack_input.push('~');
                              stack_input.push('(');
                              if(ch_3=='|'){
                                 stack_input.push('&');
                              }
                              else if(ch_3=='&'){
                                 stack_input.push('|');
                              }
                           }
                        }
                     }               
               }
               inCount=0;
            }
         }
      }
      while(!stack_result.isEmpty()){
         result = stack_result.pop()+result;
      }
      return result;
   }
   public String distribute(String s){
      String result = "";
      Stack<Character> stack_in = new Stack<Character>();
      int i=s.length()-1;
      while(i>=0){
         char ch_in = s.charAt(i);
         if(ch_in=='&'||ch_in=='|'||ch_in==')'||ch_in=='('||ch_in=='~'){
            while(!stack_in.isEmpty() && stack_in.peek()==' '){
               stack_in.pop();
            }
            stack_in.push(ch_in);
            i--;
         }
         else if(ch_in==' '){
            if(stack_in.peek()=='&'||stack_in.peek()=='|'||stack_in.peek()=='('||stack_in.peek()==')'||stack_in.peek()=='~'){
               i--;
            }
            else{
               stack_in.push(ch_in);
               i--;
            }
         }
         else{
            stack_in.push(ch_in);
            i--;
         }
      }
      Stack<Character> stack_out = new Stack<Character>();
      while(!stack_in.isEmpty()){
         char ch_pop = stack_in.pop();
         if(ch_pop!='&'){
            stack_out.push(ch_pop);
         }
         else if(ch_pop=='&'){
            int n=0;
            int m=0;
            boolean stop_1 = false;
            boolean stop_2 = false;
            Stack<Character> temp_1 = new Stack<Character>();
            Stack<Character> temp_2 = new Stack<Character>();
            Stack<Character> temp_3 = new Stack<Character>();
            while(!stop_1){
               
               char ch_outPP = stack_out.pop();
               temp_1.push(ch_outPP);
               if(ch_outPP==')'){
                  n++;
               }
               if(ch_outPP=='('){
                  n--;
               }
               if(n==0 && (stack_out.peek()=='(' || stack_out.peek()=='|' || stack_out.peek()=='&')){
                  stop_1=true;
               }
            }
            while(!stop_2){
               
               char ch_inPP = stack_in.pop();
               temp_2.push(ch_inPP);
               if(ch_inPP=='('){
                  m++;
               }
               if(ch_inPP==')'){
                  m--;
               }
               if(m==0&&(stack_in.peek()==')' || stack_in.peek()=='&'|| stack_in.peek()=='|' )){
                  stop_2=true;
               }
            }
            stack_out.pop();
            stack_in.pop();
            
           
            if(!stack_out.isEmpty() && stack_out.peek()=='|'){
               stack_out.pop();
               int x = 0;
               boolean b_out = false;
               while(!b_out){
                  char ch_3Out = stack_out.pop();
                  temp_3.push(ch_3Out);
                  if(ch_3Out==')'){
                     x++;
                  }
                  if(ch_3Out=='('){
                     x--;
                  }
                  if(x==0&&(stack_out.peek()=='(' || stack_out.peek()=='&' || stack_out.peek()=='|')){
                     b_out=true;
                  }
               }
               Stack<Character> transfer = new Stack<Character>();
               Stack<Character> transfer_2 = new Stack<Character>();
               while(!temp_3.isEmpty()){
                  char p = temp_3.pop();
                  transfer.push(p);
                  transfer_2.push(p);
               }
               stack_in.push(')');
               while(!temp_2.isEmpty()){
                  stack_in.push(temp_2.pop());
               }
               stack_in.push('|');
               while(!transfer.isEmpty()){
                  stack_in.push(transfer.pop());
               }
               stack_in.push('(');
               stack_in.push('&');
               stack_in.push(')');
               while(!temp_1.isEmpty()){
                  transfer.push(temp_1.pop());
               }
               while(!transfer.isEmpty()){
                  stack_in.push(transfer.pop());
               }
               stack_in.push('|');
               while(!transfer_2.isEmpty()){
                  stack_in.push(transfer_2.pop());
               }
               stack_in.push('(');
            }
            else if(!stack_in.isEmpty() && stack_in.peek()=='|'){
               stack_in.pop();
               int y=0;
               boolean b_in = false;
               while(!b_in){
                  char ch_3In = stack_in.pop();
                  temp_3.push(ch_3In);
                  if(ch_3In=='('){
                     y++;
                  }
                  if(ch_3In==')'){
                     y=y-1;
                  }
                  if(y==0&&(stack_in.peek()==')' || stack_in.peek()=='|' || stack_in.peek()=='&' )){
                     b_in = true;
                  }
               }
               Stack<Character> transfer = new Stack<Character>();
               Stack<Character> transfer_2 = new Stack<Character>();
               while(!temp_3.isEmpty()){
                  transfer.push(temp_3.pop());
               }
               while(!transfer.isEmpty()){
                  char p = transfer.pop();
                  transfer_2.push(p);
                  temp_3.push(p);
               }
               stack_in.push(')');
               while(!temp_3.isEmpty()){
                  stack_in.push(temp_3.pop());
               }
               stack_in.push('|');
               while(!temp_2.isEmpty()){
                  stack_in.push(temp_2.pop());
               }
               stack_in.push('(');
               stack_in.push('&');
               stack_in.push(')');
               while(!transfer_2.isEmpty()){
                  stack_in.push(transfer_2.pop());
               }
               stack_in.push('|');
               while(!temp_1.isEmpty()){
                  transfer.push(temp_1.pop());
               }
               while(!transfer.isEmpty()){
                  stack_in.push(transfer.pop());
               }
               stack_in.push('(');
            }
            else if(stack_out.isEmpty()&& stack_in.isEmpty()){
               stack_out.push('(');
               while(!temp_1.isEmpty()){
                  stack_out.push(temp_1.pop());
               }
               stack_out.push('&');
               stack_in.push(')');
               while(!temp_2.isEmpty()){
                  stack_in.push(temp_2.pop());
               }
            }
            else{
               Stack<Character> transfer = new Stack<Character>();
               while(!temp_2.isEmpty()){
                  transfer.push(temp_2.pop());
               }
               stack_out.push('(');
               while(!temp_1.isEmpty()){
                  stack_out.push(temp_1.pop());
               }
               stack_out.push('&');
               while(!transfer.isEmpty()){
                  stack_out.push(transfer.pop());
               }
               stack_out.push(')');
            }
         }
      }
      while(!stack_out.isEmpty()){
         result = stack_out.pop()+result;
      }
      return result;
   }
   public String printRule(ArrayList<Cell> r){
      String res="";
      for(Cell c:r){
         res = res+c.getSign()+c.getPredicate();
         String ar = "%";
         for(String s:c.argms){
            ar=ar+","+s;
         }
         res = res+ar+"|";
      }
      res = res+"$ ";
      return res;
   }
}