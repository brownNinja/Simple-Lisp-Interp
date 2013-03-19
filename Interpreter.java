import java.io.*;
import java.util.HashMap;

public class Interpreter{
	//create the necessary global variables
    private static Tokenizer tokens;
    private static HashMap<String,BinaryTNode<String>> Dlist = new HashMap<String,BinaryTNode<String>>(50);
    private static HashMap<String,String[]> DlistParam = new HashMap<String,String[]>(50);
    private static HashMap<String,Integer> PrimitiveMap = new HashMap<String,Integer>(20);
	
    //main function
    public static void main(final String[] args) throws FileNotFoundException{
	//for testing!
	//FileInputStream instream = new FileInputStream(args[0]);
	//InputStreamReader in = new InputStreamReader(instream);
	//end for testing!
	
    //creates the map
	CreatePrimitiveMap();
	//read in the input
	InputStreamReader in = new InputStreamReader(System.in);
	tokens = new Tokenizer(in);
	String tok1 = tokens.GetNext();
	while(!tok1.equals("!END")){
	tok1 = tokens.GoBack();
	BinaryTNode<String> result = Parse_S();
	System.out.println(Eval_Begin(result));
	tok1 = tokens.GetNext();
	}
	}
	
	
	//Parser Starts HERE
    //parse the s
	private static BinaryTNode<String> Parse_S(){
		BinaryTNode<String> result = Parse_E();
		return result;
	}
	
	//parses the E
	private static BinaryTNode<String> Parse_E(){
		String tok = tokens.GetNext();
		if(tok.equals("!END")){
			System.out.println("Error: Unfinished S-Expression");
			System.exit(0);
		}
		if(tok.equals("(")){
		    BinaryTNode<String> leftLeaf = new BinaryTNode<String>(tok,null,null);
			return new BinaryTNode<String>(" ",leftLeaf,Parse_X());
		}else{
			return new BinaryTNode<String>(tok,null,null);
		}
	}
	
	//parses the X
	private static BinaryTNode<String> Parse_X(){
		String tok = tokens.GetNext();
		if(tok.equals("!END")){
			System.out.println("Error: Unfinished S-Expression");
			System.exit(0);
		}
		
		if(tok.equals(")")){
			return new BinaryTNode<String>(tok,null,null);
		}else{
			tok = tokens.GoBack();
			return new BinaryTNode<String>(" ",Parse_E(),Parse_Y());
		}
	}
	
	private static BinaryTNode<String> Parse_Y(){
		String tok = tokens.GetNext();
		if(tok.equals("!END")){
			System.out.println("Error: Unfinished S-Expression");
			System.exit(0);
		}
		if(tok.equals(".")){
			String tempTok = tok;
			BinaryTNode<String> leftLeaf = Parse_E();
			tok = tokens.GetNext();
			if(tok.equals("!END")){
				System.out.println("Error: Unfinished S-Expression");
				System.exit(0);
			}
			if(!tok.equals(")")){
				System.out.println("Error: Expecting Right Paren");
				System.exit(0);
			}
			return new BinaryTNode<String>(tempTok,leftLeaf,new BinaryTNode<String>(tok,null,null));
		}else{
			tok = tokens.GoBack();
			BinaryTNode<String> leftLeaf = Parse_R();
			tok = tokens.GetNext();
			if(tok.equals("!END")){
				System.err.println("Error: Unfinished S-Expression");
				System.exit(0);
			}
			if(!tok.equals(")")){
				System.out.println("Error: Expecting Right Paren");
				System.exit(0);
			}
			return new BinaryTNode<String>(" ",leftLeaf,new BinaryTNode<String>(tok,null,null));
		}
	}
	
	//parses the R
	private static BinaryTNode<String> Parse_R(){
		String tok = tokens.GetNext();
		if(tok.equals("!END") || tok.equals(")")){
			tok = tokens.GoBack();
			return new BinaryTNode<String>(" ",null,null);
		}else{
			tok = tokens.GoBack();
			return new BinaryTNode<String>(" ",Parse_E(),Parse_R());
		}
	}
	//END PARSING
	
	//BEGIN EVALUATION
	private static String Eval_Begin(BinaryTNode<String> node){
		String check = node.getLeft().getRoot();
		String result = "";
		//checks for correct s-expressions
		if(check.equals("(")){
			check = node.getRight().getRoot();
			if(check.equals(")")){
				result = "NIL";
			}else if(node.getRight().getRoot().equals(" ")){
				BinaryTNode<String> newNode = getLeftmostNode(node.getRight());
				//calls eval_which which is the switchboard for all of the eval functions
				result = Eval_Which(newNode);
			}else{
				System.out.println("Error: Invalid S-Expression");
				System.exit(0);
			}
			return result;
		}else{
		return node.getRoot();
		}
	}
	
	//Evaluates the plus
	private static String Eval_Plus(BinaryTNode<String> node){
		String check = node.getLeft().getRoot();
		String result = "";
		String rightVal = "";
		if(!check.equals("PLUS")){
			System.out.println("Error: Expecting PLUS");
			System.exit(0);
		}
		//goes down left subtree
		String leftLeaf = node.getRight().getLeftmostLeaf();
		if(leftLeaf.equals("(")){
			result = Eval_Which(getLeftmostNode(node.getRight()).getRight());
			if(!getLeftmostNode(node.getRight()).getRightmostLeaf().equals(")")){
				System.out.println("Error: Mismatched Parenthesis");
				System.exit(0);
			}
		}else {//if there is not sub expression result is the atom
			result = leftLeaf;
		}
		//goes down right subtree
		String rightLeaf = node.getRight().getLeft().getRight().getLeftmostLeaf();
		if(rightLeaf.equals("(")){
			rightVal = Eval_Which(getLeftmostNode(node.getRight().getLeft().getRight()).getRight().getRight());
		}else{//if there is not sub expression result is the atom
			rightVal = rightLeaf;
		}
		if(!isInteger(result) || !isInteger(rightVal)){
			System.out.println("Error: PLUS requires 2 Integers");
			System.exit(0);
		}else{
			result = Integer.toString(Integer.parseInt(result) + Integer.parseInt(rightVal));
		}
		return result;
	}
	
	//Exactly the same as PLUS but uses a different operator at the end
	private static String Eval_Minus(BinaryTNode<String> node){
		String check = node.getLeft().getRoot();
		String result = "";
		String rightVal = "";
		if(!check.equals("MINUS")){
			System.out.println("Error: Expecting MINUS");
			System.exit(0);
		}
		String leftLeaf = node.getRight().getLeftmostLeaf();
		if(leftLeaf.equals("(")){
			result = Eval_Which(getLeftmostNode(node.getRight()).getRight());
			if(!getLeftmostNode(node.getRight()).getRightmostLeaf().equals(")")){
				System.out.println("Error: Mismatched Parenthesis");
				System.exit(0);
			}
		}else {
			result = leftLeaf;
		}
		String rightLeaf = node.getRight().getLeft().getRight().getLeftmostLeaf();
		if(rightLeaf.equals("(")){
			rightVal = Eval_Which(getLeftmostNode(node.getRight().getLeft().getRight()).getRight());
			if(!getLeftmostNode(node.getRight().getLeft().getRight()).getRightmostLeaf().equals(")")){
				System.out.println("Error: Mismatched Parenthesis");
				System.exit(0);
			}
		}else{
			rightVal = rightLeaf;
		}
		if(!isInteger(result) || !isInteger(rightVal)){
			System.out.println("Error: MINUS requires 2 Integers");
			System.exit(0);
		}else{
			result = Integer.toString(Integer.parseInt(result) - Integer.parseInt(rightVal));
		}
		return result;
	}
	
	private static String Eval_Times(BinaryTNode<String> node){
		String check = node.getLeft().getRoot();
		String result = "";
		String rightVal = "";
		if(!check.equals("TIMES")){
			System.out.println("Error: Expecting TIMES");
			System.exit(0);
		}
		String leftLeaf = node.getRight().getLeftmostLeaf();
		if(leftLeaf.equals("(")){
			result = Eval_Which(getLeftmostNode(node.getRight()).getRight());
			if(!getLeftmostNode(node.getRight()).getRightmostLeaf().equals(")")){
				System.out.println("Error: Mismatched Parenthesis");
				System.exit(0);
			}
		}else {
			result = leftLeaf;
		}
		String rightLeaf = node.getRight().getLeft().getRight().getLeftmostLeaf();
		if(rightLeaf.equals("(")){
			rightVal = Eval_Which(getLeftmostNode(node.getRight().getLeft().getRight()).getRight());
			if(!getLeftmostNode(node.getRight().getLeft().getRight()).getRightmostLeaf().equals(")")){
				System.out.println("Error: Mismatched Parenthesis");
				System.exit(0);
			}
		}else{
			rightVal = rightLeaf;
		}
		if(!isInteger(result) || !isInteger(rightVal)){
			System.out.println("Error: TIMES requires 2 Integers");
			System.exit(0);
		}else{
			result = Integer.toString(Integer.parseInt(result) * Integer.parseInt(rightVal));
		}
		return result;
	}
	
	private static String Eval_Quotient(BinaryTNode<String> node){
		String check = node.getLeft().getRoot();
		String result = "";
		String rightVal = "";
		if(!check.equals("QUOTIENT")){
			System.out.println("Error: Expecting QUOTIENT");
			System.exit(0);
		}
		String leftLeaf = node.getRight().getLeftmostLeaf();
		if(leftLeaf.equals("(")){
			result = Eval_Which(getLeftmostNode(node.getRight()).getRight());
			if(!getLeftmostNode(node.getRight()).getRightmostLeaf().equals(")")){
				System.out.println("Error: Mismatched Parenthesis");
				System.exit(0);
			}
		}else {
			result = leftLeaf;
		}
		String rightLeaf = node.getRight().getLeft().getRight().getLeftmostLeaf();
		if(rightLeaf.equals("(")){
			rightVal = Eval_Which(getLeftmostNode(node.getRight().getLeft().getRight()).getRight());
			if(!getLeftmostNode(node.getRight().getLeft().getRight()).getRightmostLeaf().equals(")")){
				System.out.println("Error: Mismatched Parenthesis");
				System.exit(0);
			}
		}else{
			rightVal = rightLeaf;
		}
		if(!isInteger(result) || !isInteger(rightVal)){
			System.out.println("Error: QUOTIENT requires 2 Integers");
			System.exit(0);
		}else{
			result = Integer.toString(Integer.parseInt(result) / Integer.parseInt(rightVal));
		}
		return result;
	}
	
	private static String Eval_Remainder(BinaryTNode<String> node){
		String check = node.getLeft().getRoot();
		String result = "";
		String rightVal = "";
		if(!check.equals("REMAINDER")){
			System.out.println("Error: Expecting REMAINDER");
			System.exit(0);
		}
		String leftLeaf = node.getRight().getLeftmostLeaf();
		if(leftLeaf.equals("(")){
			result = Eval_Which(getLeftmostNode(node.getRight()).getRight());
			if(!getLeftmostNode(node.getRight()).getRightmostLeaf().equals(")")){
				System.out.println("Error: Mismatched Parenthesis");
				System.exit(0);
			}
		}else {
			result = leftLeaf;
		}
		String rightLeaf = node.getRight().getLeft().getRight().getLeftmostLeaf();
		if(rightLeaf.equals("(")){
			rightVal = Eval_Which(getLeftmostNode(node.getRight().getLeft().getRight()).getRight());
			if(!getLeftmostNode(node.getRight().getLeft().getRight()).getRightmostLeaf().equals(")")){
				System.out.println("Error: Mismatched Parenthesis");
				System.exit(0);
			}
		}else{
			rightVal = rightLeaf;
		}
		if(!isInteger(result) || !isInteger(rightVal)){
			System.out.println("Error: REMAINDER requires 2 Integers");
			System.exit(0);
		}else{
			result = Integer.toString(Integer.parseInt(result) % Integer.parseInt(rightVal));
		}
		return result;
	}
	
	private static String Eval_Less(BinaryTNode<String> node){
		String check = node.getLeft().getRoot();
		String result = "";
		String rightVal = "";
		if(!check.equals("LESS")){
			System.out.println("Error: Expecting LESS");
			System.exit(0);
		}
		String leftLeaf = node.getRight().getLeftmostLeaf();
		if(leftLeaf.equals("(")){
			result = Eval_Which(getLeftmostNode(node.getRight()).getRight());
			if(!getLeftmostNode(node.getRight()).getRightmostLeaf().equals(")")){
				System.out.println("Error: Mismatched Parenthesis");
				System.exit(0);
			}
		}else {
			result = leftLeaf;
		}
		String rightLeaf = node.getRight().getLeft().getRight().getLeftmostLeaf();
		if(rightLeaf.equals("(")){
			rightVal = Eval_Which(getLeftmostNode(node.getRight().getLeft().getRight()).getRight());
			if(!getLeftmostNode(node.getRight().getLeft().getRight()).getRightmostLeaf().equals(")")){
				System.out.println("Error: Mismatched Parenthesis");
				System.exit(0);
			}
		}else{
			rightVal = rightLeaf;
		}
		if(!isInteger(result) || !isInteger(rightVal)){
			System.out.println("Error: LESS requires 2 Integers");
			System.exit(0);
		}else{
			if (Integer.parseInt(result) < Integer.parseInt(rightVal)){
				result = "T";
			}else{
				result = "NIL";
			}
		}
		return result;
	}
	
	private static String Eval_Greater(BinaryTNode<String> node){
		String check = node.getLeft().getRoot();
		String result = "";
		String rightVal = "";
		if(!check.equals("GREATER")){
			System.out.println("Error: Expecting GREATER");
			System.exit(0);
		}
		String leftLeaf = node.getRight().getLeftmostLeaf();
		if(leftLeaf.equals("(")){
			result = Eval_Which(getLeftmostNode(node.getRight()).getRight());
			if(!getLeftmostNode(node.getRight()).getRightmostLeaf().equals(")")){
				System.out.println("Error: Mismatched Parenthesis");
				System.exit(0);
			}
		}else {
			result = leftLeaf;
		}
		String rightLeaf = node.getRight().getLeft().getRight().getLeftmostLeaf();
		if(rightLeaf.equals("(")){
			rightVal = Eval_Which(getLeftmostNode(node.getRight().getLeft().getRight()).getRight());
			if(!getLeftmostNode(node.getRight().getLeft().getRight()).getRightmostLeaf().equals(")")){
				System.out.println("Error: Mismatched Parenthesis");
				System.exit(0);
			}
		}else{
			rightVal = rightLeaf;
		}
		if(!isInteger(result) || !isInteger(rightVal)){
			System.out.println("Error: GREATER requires 2 Integers");
			System.exit(0);
		}else{
			if (Integer.parseInt(result) > Integer.parseInt(rightVal)){
				result = "T";
			}else{
				result = "NIL";
			}
		}
		return result;
	}
	
	private static String Eval_EQ(BinaryTNode<String> node){
		String check = node.getLeft().getRoot();
		String result = "";
		String rightVal = "";
		if(!check.equals("EQ")){
			System.out.println("Error: Expecting EQ");
			System.exit(0);
		}
		String leftLeaf = node.getRight().getLeftmostLeaf();
		if(leftLeaf.equals("(")){
			result = Eval_Which(getLeftmostNode(node.getRight()).getRight());
			if(!getLeftmostNode(node.getRight()).getRightmostLeaf().equals(")")){
				System.out.println("Error: Mismatched Parenthesis");
				System.exit(0);
			}
		}else {
			result = leftLeaf;
		}
		String rightLeaf = node.getRight().getLeft().getRight().getLeftmostLeaf();
		if(rightLeaf.equals("(")){
			rightVal = Eval_Which(getLeftmostNode(node.getRight().getLeft().getRight()).getRight());
			if(!getLeftmostNode(node.getRight().getLeft().getRight()).getRightmostLeaf().equals(")")){
				System.out.println("Error: Mismatched Parenthesis");
				System.exit(0);
			}
		}else{
			rightVal = rightLeaf;
		}
		if(!isInteger(result) || !isInteger(rightVal)){
			System.out.println("Error: EQ requires 2 Integers");
			System.exit(0);
		}else{
			if (Integer.parseInt(result) == Integer.parseInt(rightVal)){
				result = "T";
			}else{
				result = "NIL";
			}
		}
		return result;
	}
	
	private static String Eval_List(BinaryTNode<String> node){
		//checks what the value of the left branch of the current node is
		String check = node.getLeft().getRoot();
		//adds the leading parenthesis to the list
	    String list = "( ";
	    String result = "";
	    //ensures that a primative is not in the left branch which would mean
	    //that this subtree is not a list
		if(PrimitiveMap.containsKey(check)){
			System.out.println("Error: Expecting a Literal Atom");
			System.exit(0);
		}
		//if the left leaf is another ( then we are in a sublist
		//the paren before entering this procedure will be sanitized
		 if(node.getLeftmostLeaf().equals("(")){
			 result = Eval_Which(getLeftmostNode(node.getLeft()).getRight());
			 list = list.concat(result + " ");
		}
		//adds in the value of the left leaf of the subtree
		list = list.concat(check + " ");
		//descends to the next level of the tree
		check = node.getRight().getRoot();

		//if the list is in dot form then it will present here
		if(check.equals(".")){
			//checks to see what the value after the dot is
			String leftLeaf = node.getRight().getLeftmostLeaf();
			//if the value after the dot is a paren, then this is a sublist
			if (leftLeaf.equals("(")){
				//recursive call for the sublist
				result = Eval_Which(getLeftmostNode(node.getRight()).getRight());
				//ensures no mismatched parenthesis
				if(!getLeftmostNode(node.getRight()).getRightmostLeaf().equals(")")){
					System.out.println("Error: Mismatched Parenthesis");
					System.exit(0);
				}
				if(result.equals("NIL")){
					list = list.concat(" )");
				}else{
				list = list.concat(". " + result);
				}
			}else{
				if(leftLeaf.equals("NIL")){
					list = list.concat(" )");
				}else{
					if(leftLeaf.equals(")")){
						System.out.println("Error: no value after .");
						System.exit(0);
					}else{
					list = list.concat(". " + leftLeaf + " )");
					}
				}
			}
		}else if(check.equals(" ")){//if the list is not in dot notation
			boolean flag = false;
			BinaryTNode<String> tempNode = node.getRight();
			//get all of the literal atoms
			while(!flag){
				if(tempNode.getRight() != null){
					//if the list contains a sublist, then recursive call
					if(tempNode.getLeftmostLeaf().equals("(")){
						if((getLeftmostNode(tempNode).getRight().getRoot()).equals(")")){
							list = list.concat("NIL )");
							flag = true;
						}else{
							list = list.concat(Eval_Which(getLeftmostNode(tempNode).getRight()) + " )");
							flag = true;
						}
					}else{
						/*if(tempNode.getRight().getRoot().equals(")")){
							list = list.concat(" )");
							flag = true;
						}else{*/
					//otherwise simply concat the next literal on the end
					list = list.concat(tempNode.getLeftmostLeaf() + " ");
					tempNode = getLeftmostNode(tempNode).getRight();
					
					}
				}/*else if(tempNode.getRoot().equals(".")){
					System.out.println("Error: Unexpected Atom");
					System.exit(0);
				}*/else{
					list = list.concat(" )");
					flag = true;
				}
			}
		}else{
			System.out.println("Error: Unexpected Atom");
			System.exit(0);
		}
		return list;
	}
	
	private static String Eval_Quote(BinaryTNode<String> node){
		String check = node.getLeft().getRoot();
		if(!check.equals("QUOTE")){
			System.out.println("Error: Expecting QUOTE");
			System.exit(0);
		}
		return Eval_List(getLeftmostNode(node.getRight()).getRight());
		
	}
	//maps the defun and gets the parameters
	private static String Map_Defun(BinaryTNode<String> node){
		String check = node.getLeft().getRoot();
		if(!check.equals("DEFUN")){
			System.out.println("Error: Expecting DEFUN");
			System.exit(0);
		}
		if(PrimitiveMap.containsKey(node.getRight().getLeftmostLeaf())){
			System.out.println("Error: Function name cannot be a primitive");
			System.exit(0);
		}
		if(Dlist.containsKey(node.getRight().getLeftmostLeaf())){
			System.out.println("Error: Function already defined");
			System.exit(0);
		}
		BinaryTNode<String> Paramget = getLeftmostNode(getLeftmostNode(node.getRight()).getRight());
		if(!Paramget.getLeft().getRoot().equals("(")){
			System.out.println("Error: No Parameters Given");
			System.exit(0);
		}
		//puts the function name into the map
		String defunName = node.getRight().getLeftmostLeaf();
		Dlist.put(defunName, node);
		boolean flag = true;
		//finds the parameters and builds the parameters map
		String[] paramArray = new String[20];
		BinaryTNode<String> Temp = getLeftmostNode(Paramget.getRight());
		int pos = 0;
		while(flag){
			if(Temp.getRight().getRight() == null){
				paramArray[pos] = Temp.getLeftmostLeaf();
				flag = false;
			}else{
			paramArray[pos] = Temp.getLeftmostLeaf();
			pos++;
			Temp = getLeftmostNode(Temp.getRight());
			}
			
		}
		//adds the parameters into the map
		DlistParam.put(defunName, paramArray);
		return "";
	}
	//Evaluates the NULL expression
	private static String Eval_Null(BinaryTNode<String> node){
		String check = node.getLeft().getRoot();
		String result = "";
		if(!check.equals("NULL")){
			System.out.println("Error: Expecting NULL");
			System.exit(0);
		}
		//looks down left subtree
		String leftLeaf = node.getRight().getLeftmostLeaf();
		if(leftLeaf.equals("(")){
			if(getLeftmostNode(node.getRight()).getRight().getRoot().equals(")")){
				result = "NIL";
			}else{
			result = Eval_Which(getLeftmostNode(node.getRight()).getRight());
			if(!getLeftmostNode(node.getRight()).getRightmostLeaf().equals(")")){
				System.out.println("Error: Mismatched Parenthesis");
				System.exit(0);
			}
			}
		}else{
			result = leftLeaf;
		}
		
		if(result.equals("NIL")){
			return "T";
		}else{
			return "NIL";
		}
	}
	//Evaluates the Int expression
	private static String Eval_Int(BinaryTNode<String> node){
		String check = node.getLeft().getRoot();
		String result = "";
		if(!check.equals("INT")){
			System.out.println("Error: Expecting INT");
			System.exit(0);
		}
		//looks down left subtree
		String leftLeaf = node.getRight().getLeftmostLeaf();
		if(leftLeaf.equals("(")){
			if(getLeftmostNode(node.getRight()).getRight().getRoot().equals(")")){
				result = "NIL";
			}else{
			result = Eval_Which(getLeftmostNode(node.getRight()).getRight());
			if(!getLeftmostNode(node.getRight()).getRightmostLeaf().equals(")")){
				System.out.println("Error: Mismatched Parenthesis");
				System.exit(0);
			}
			}
		}else {
			result = leftLeaf;
		}
		if(isInteger(result)){
			return "T";
		}else{
			return "NIL";
		}
	}
	
	//Evaluates the ATom
	private static String Eval_Atom(BinaryTNode<String> node){
		String check = node.getLeft().getRoot();
		String result = "";
		if(!check.equals("ATOM")){
			System.out.println("Error: Expecting ATOM");
			System.exit(0);
		}
		//look down the left subtree
		String leftLeaf = node.getRight().getLeftmostLeaf();
		if(leftLeaf.equals("(")){
			if(getLeftmostNode(node.getRight()).getRight().getRoot().equals(")")){
				result = "NIL";
			}else{
			result = Eval_Which(getLeftmostNode(node.getRight()).getRight());
			if(!getLeftmostNode(node.getRight()).getRightmostLeaf().equals(")")){
				System.out.println("Error: Mismatched Parenthesis");
				System.exit(0);
			}
			}
		}else {
			result = leftLeaf;
		}
		if(result.charAt(0) == '('){
			return "NIL";
		}else{
			return "T";
		}
	}
	
	//evaluates CAR
	public static String Eval_Car(BinaryTNode<String> node){
		String check = node.getLeft().getRoot();
		String result = "";
		String carValue = "";
		if(!check.equals("CAR")){
			System.out.println("Error: Expecting CAR");
			System.exit(0);
		}
		String leftLeaf = node.getRight().getLeftmostLeaf();
		//ensures that the list is there and requires a quote
		if(leftLeaf.equals("(")){
			if(getLeftmostNode(node.getRight()).getRight().getRoot().equals(")")){
				result = "NIL";
			}else if(!getLeftmostNode(node.getRight()).getRight().getLeftmostLeaf().equals("QUOTE")){
				System.out.println("Error: CAR requires a QUOTE");
				System.exit(0);
			}else{
			result = Eval_Which(getLeftmostNode(node.getRight()).getRight());
			if(!getLeftmostNode(node.getRight()).getRightmostLeaf().equals(")")){
				System.out.println("Error: Mismatched Parenthesis");
				System.exit(0);
			}
			}
		}else {
			System.out.println("Error: Unexpected Atom");
			System.exit(0);
		}
		boolean flag = true;
		int pos = 0;
		int startPos = 0;
		//takes the string from the list expression and takes the first atom
		while(flag){
			if(result.charAt(pos) == '(' || result.charAt(pos) == ' '){
				pos++;
			}else{
				flag = false;
				startPos = pos;
				while(result.charAt(pos)!= ' '){
					pos++;
				}
				carValue = result.substring(startPos, pos);
			}
		}
		if(result.charAt(pos+1) == ')'){
			System.out.println("Error: CAR operates on lists");
			System.exit(0);
		}
		return carValue;
	}
	//same as the CAR expression except takes the rest of the list string
	public static String Eval_Cdr(BinaryTNode<String> node){
		String check = node.getLeft().getRoot();
		String result = "";
		String cdrValue = "";
		if(!check.equals("CDR")){
			System.out.println("Error: Expecting CDR");
			System.exit(0);
		}
		String leftLeaf = node.getRight().getLeftmostLeaf();
		if(leftLeaf.equals("(")){
			if(getLeftmostNode(node.getRight()).getRight().getRoot().equals(")")){
				result = "NIL";
			}else if(!getLeftmostNode(node.getRight()).getRight().getLeftmostLeaf().equals("QUOTE")){
				System.out.println("Error: CDR requires a QUOTE");
				System.exit(0);
			}else{
			result = Eval_Which(getLeftmostNode(node.getRight()).getRight());
			if(!getLeftmostNode(node.getRight()).getRightmostLeaf().equals(")")){
				System.out.println("Error: Mismatched Parenthesis");
				System.exit(0);
			}
			}
		}else {
			System.out.println("Error: Unexpected Atom");
			System.exit(0);
		}
		boolean flag = true;
		int pos = 0;
		//takes the end of the list string as result
		while(flag){
			if(result.charAt(pos) == '(' || result.charAt(pos) == ' '){
				pos++;
			}else{
				flag = false;
				while(result.charAt(pos)!= ' '){
					pos++;
				}
				if(result.charAt(pos + 1) == '.' || result.charAt(pos + 2) == '.' || result.charAt(pos + 3) == '.'){
					pos = pos+2;
				}
				//gets rid of the excess characters
				cdrValue = "( " + result.substring(pos);
				String nil = "NIL";
				cdrValue = cdrValue.replace("(   )", nil);
				cdrValue = cdrValue.replace("NIL   .", "");
				cdrValue = cdrValue.replace("(  NIL  )   .", "");
				
			}
		}
		return cdrValue;
	}
	//evaluates the cons
	public static String Eval_Cons(BinaryTNode<String> node){
		String check = node.getLeft().getRoot();
		String leftValue = "";
		String rightVal = "";
		if(!check.equals("CONS")){
			System.out.println("Error: Expecting CONS");
			System.exit(0);
		}
		//looks down left subtree
		String leftLeaf = node.getRight().getLeftmostLeaf();
		if(leftLeaf.equals("(")){
			leftValue = Eval_Which(getLeftmostNode(node.getRight()).getRight());
			if(!getLeftmostNode(node.getRight()).getRightmostLeaf().equals(")")){
				System.out.println("Error: Mismatched Parenthesis");
				System.exit(0);
			}
		}else{
				leftValue = leftLeaf;
		}
		String rightLeaf = node.getRight().getLeft().getRight().getLeftmostLeaf();
		if(rightLeaf.equals("(")){
			rightVal = Eval_Which(getLeftmostNode(node.getRight().getLeft().getRight()).getRight());
			if(!getLeftmostNode(node.getRight().getLeft().getRight()).getRightmostLeaf().equals(")")){
				System.out.println("Error: Mismatched Parenthesis");
				System.exit(0);
			}
		}else{
			System.out.println("Error: CONS must take a list as the second argument");
			System.exit(0);
		}
		if(leftValue.charAt(0) == '('){
			System.out.println("Error: CONS must take an Atom as the first argument");
			System.exit(0);
		}
		//returns the correct expression
		return "( " + leftValue + " . " + rightVal + " )";
	}
	
	//evaluates the condition using the EvalSubCond operation
	private static String Eval_Cond(BinaryTNode<String> node){
		boolean flag = true;
		String result = " ";
		//separates out the first condition
		BinaryTNode<String> leftCond = getLeftmostNode(node.getRight());
		result = EvalSubCond(leftCond.getRight());
		if(!result.equals("!F")){
			return result;
		}
		//ensures that if there is only one condition and it fails then error
		if(node.getRight().getLeft().getRight().getRight() == null){
			System.out.println("Error: All conditions failed");
			System.exit(0);
		}
		//sets the rightTree at the beginning of all of the next conditions
		BinaryTNode <String> rightTree = node.getRight().getLeft().getRight();
		//evaluates the first right condition
		result = EvalSubCond(rightTree.getLeft().getRight());
		if(!result.equals("!F")){
			return result;		
			}
		//loops until the last condition is reached or one returns true;
		while(flag && rightTree.getRight().getRight() != null){
			rightTree = rightTree.getRight();
			result = EvalSubCond(rightTree.getLeft().getRight());
			if(!result.equals("!F")){
				flag = false;		
				}
		}
		if(result.equals("!F")){
			System.out.println("Error: All conditions failed");
			System.exit(0);
		}
		return result;
	}
	
	//subprocedure to evaluate all of the sub conditions
	private static String EvalSubCond(BinaryTNode<String> node){
		BinaryTNode<String> Cond = getLeftmostNode(node);
		
		String condValue = "";
		String eval = "";
		String result = " ";
		
		String leftLeaf = Cond.getLeftmostLeaf();
		//evaluate the condition if there is a sub expression
		if(leftLeaf.equals("(")){
			condValue = Eval_Which(Cond.getRight());
			if(!Cond.getRightmostLeaf().equals(")")){
				System.out.println("Error: Mismatched Parenthesis");
				System.exit(0);
			}
		}else{//otherwise just take the value
				condValue = leftLeaf;
		}
		
		//evaluate the return for the condition
		BinaryTNode<String> Value = getLeftmostNode(node.getRight());
		leftLeaf = Value.getLeftmostLeaf();
		//evaluate the return if there is a sub expression
		if(leftLeaf.equals("(")){
			eval = Eval_Which(Value.getRight());
			if(!Value.getRightmostLeaf().equals(")")){
				System.out.println("Error: Mismatched Parenthesis");
				System.exit(0);
			}
		}else{//otherwise just take the value
				eval = leftLeaf;
		}
		//check to make sure the condition evaluates to true or false
		//if the condition is false then return the false character
		if(condValue.equals("NIL")){
			result =  "!F";
		}else if(condValue.equals("T")){
			result = eval;
		}else{
			System.out.println("Error: Condition did not evaluate to Nil or T");
			System.exit(0);
		}
		return result;
	}
	
	private static String Eval_Defun(BinaryTNode<String> node){
		//the local alist map
		HashMap<String,String> Alist = new HashMap<String,String>(80);
		String dName = node.getLeft().getRoot();
		BinaryTNode<String> Defun = Dlist.get(dName);
		String[] defunParam = DlistParam.get(dName);
		
		//get the parameters
		BinaryTNode<String> Paramget = getLeftmostNode(node.getRight());
		if(!Paramget.getLeft().getRoot().equals("(")){
			System.out.println("Error: No Parameters Given");
			System.exit(0);
		}
		boolean flag = true;
		//finds the parameters and builds the Alist map
		String[] paramArray = new String[20];
		BinaryTNode<String> Temp = getLeftmostNode(Paramget.getRight());
		int pos = 0;
		//finds the amount of parameters for both strings and ensures that they are equal
		while(flag){

			if(Temp.getRight().getRight() == null){
				paramArray[pos] = Temp.getLeftmostLeaf();
				pos++;
				flag = false;
			}else{
			paramArray[pos] = Temp.getLeftmostLeaf();
			pos++;
			Temp = getLeftmostNode(Temp.getRight());
			}
		}
		int length = 0;
		while(defunParam[length] != null){
			length++;
		}
		
		if(length != pos){
			System.out.println("Error: Wrong amount of parameters");
			System.exit(0);
		}
		
		for(int i = 0; i < length; i++){
			Alist.put(defunParam[i],paramArray[i]);
		}
		//replaces all of the parameters with the alist
		BinaryTNode<String> result = Replace(getLeftmostNode(Defun.getRight().getLeft().getRight().getRight()).getRight(),Alist);
		return Eval_Which(result);
		
	}
	
	//the switchboard to all of the eval operations
	private static String Eval_Which(BinaryTNode<String> node){
		String prim = node.getLeft().getRoot();
		String result = "";
		if(PrimitiveMap.containsKey(prim)){
		switch(PrimitiveMap.get(prim)){
		case 1:
			result = Eval_Plus(node);
			break;
		case 2:
			result = Eval_Minus(node);
			break;
		case 3:
			result = Eval_Times(node);
			break;
		case 4:
			result = Eval_Quotient(node);
			break;
		case 5:
			result = Eval_Remainder(node);
			break;
		case 6:
			result = Eval_Less(node);
			break;
		case 7:
			result = Eval_Greater(node);
			break;
		case 8:
			result = Eval_EQ(node);
			break;
		case 9:
			result = Eval_Car(node);
			break;
		case 10:
			result = Eval_Cdr(node);
			break;
		case 11:
			result = Eval_Cons(node);
			break;
		case 12:
			result = Eval_Atom(node);
			break;
		case 13:
			result = Eval_Int(node);
			break;
		case 14:
			result = Eval_Cond(node);
			break;
		case 15:
			result = Eval_Quote(node);
			break;
		case 16:
			result = Map_Defun(node);
			break;
		case 17:
			result = Eval_Null(node);
			break;
		default:
			result = " ";
		}
		return result;
		}else if(Dlist.containsKey(prim)){
			return Eval_Defun(node);
		}else{
			return Eval_List(node);
		}
	}
	//helper operation that allows the left most node to be retrieved
	private static BinaryTNode<String> getLeftmostNode(BinaryTNode<String> node){
		 if(!node.getLeft().getRoot().equals(" "))
			 return node;
		 else
			 return getLeftmostNode(node.getLeft());
	 }
	//helper operation to determine if it is an integer
	private static boolean isInteger(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch (NumberFormatException nFE) {
			return false;
		}
	}
	
	//creates a map of the primitives for the switchboard and error checking
	private static void CreatePrimitiveMap(){
		int pos = 1;
		PrimitiveMap.put("PLUS", pos);
		pos++;
		PrimitiveMap.put("MINUS", pos);
		pos++;
		PrimitiveMap.put("TIMES", pos);
		pos++;
		PrimitiveMap.put("QUOTIENT", pos);
		pos++;
		PrimitiveMap.put("REMAINDER", pos);
		pos++;
		PrimitiveMap.put("LESS", pos);
		pos++;
		PrimitiveMap.put("GREATER", pos);
		pos++;
		PrimitiveMap.put("EQ", pos);
		pos++;
		PrimitiveMap.put("CAR", pos);
		pos++;
		PrimitiveMap.put("CDR", pos);
		pos++;
		PrimitiveMap.put("CONS", pos);
		pos++;
		PrimitiveMap.put("ATOM", pos);
		pos++;
		PrimitiveMap.put("INT", pos);
		pos++;
		PrimitiveMap.put("COND", pos);
		pos++;
		PrimitiveMap.put("QUOTE", pos);
		pos++;
		PrimitiveMap.put("DEFUN", pos);
		pos++;
		PrimitiveMap.put("NULL", pos);
		pos++;
	}
	
	//replaces the parameters using the alist
	private static BinaryTNode<String> Replace(BinaryTNode<String> node, HashMap<String,String> Alist){
		if(Alist.containsKey(node.getLeft().getRoot())){
			node.getLeft().setRoot(Alist.get(node.getLeft().getRoot()));
		}else{
			if((node.getLeft().getRoot().equals(" ") || node.getLeft().getRoot().equals(".")) && node.getLeft() != null){
				Replace(node.getLeft(), Alist);
			}
			if((node.getRight().getRoot().equals(" ") || node.getRight().getRoot().equals(".") || node.getRight().getRoot().equals(")"))){
				if(node.getRight().getRoot().equals(")")){
					Replace(node.getLeft(),Alist);
				}else if((node.getRight().getRight() == null)){
				}else{
				Replace(node.getRight(), Alist);
				}
			}
		}
		return node;
	}
	
	}