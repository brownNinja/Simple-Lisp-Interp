import java.io.InputStreamReader;
import java.io.IOException;

public class Tokenizer{
	
	private static InputStreamReader in;
	private static String[] TokenList = new String[800];
	private static int outputPos = 0;
	private static int inputpos = 0;
	//Constructor
	public Tokenizer(InputStreamReader f){
		in = f;
		BuildTokenArray();
	}
	
	public String GetNext(){
		String nextToken;
		if(outputPos < inputpos){
		nextToken = TokenList[outputPos];
		outputPos++;
		}else{
			nextToken = "!END";
		}
		return nextToken;
	}
	public static int SkipToken(){
		if(outputPos <= inputpos-1){
			outputPos++;
			return 0;
		}else
		{
			return -1;
		}
	}
	public String GoBack(){
		String prevToken = "";
		if(outputPos > 0){
			outputPos--;
			prevToken = TokenList[outputPos];
		}
		return prevToken;
	}
	
private static void BuildTokenArray(){
		
		boolean white = true;
		boolean paren = true;
		int data = 0;
		char c;
		String token = "";
		try{
	    data = in.read();
		while(data != -1){
			c = Character.toUpperCase((char)data);
			//checks to see if the token is a literal atom
			if(Character.isLetter(c)){
				if(!paren && !white){
					System.out.println("Whitespace or Paren must precede Literal Atom.");
					System.exit(0);
				}
				if (!token.equals("") ){
					System.out.println("Invalid Literal Atom Token.");
					System.exit(0);
				}
				while(c != ')' && c != '(' && !isWhitespace(c)){
					if(!isAtom(c)){
						System.out.println("Unexpected Character.");
						System.exit(0);
						}
					if(c == '.' || c == '+' || c == '-'){
						System.out.println("Invalid Literal Atom Token.");
						System.exit(0);
					}
					token = token.concat(Character.toString(c));
					data = in.read();
					c = Character.toUpperCase((char)data);
				}
				TokenList[inputpos]= token;
				inputpos++;
				token = "";
				white = false;
				paren = false;
			}
			//checks to see if the token is numeric atom
			if(Character.isDigit(c) || c == '+' || c == '-'){
				if(!paren && !white){
					System.out.println("Whitespace or Paren must precede Numeric Atom.");
					System.exit(0);
				}
				if (!token.equals("") ){
					System.out.println("Invalid Numeric Atom Token.");
					System.exit(0);
				}
				token = token.concat(Character.toString(c));
				data = in.read();
				c = Character.toUpperCase((char)data);
				
				while(c != ')' && c != '(' && !isWhitespace(c)){
					if(!isAtom(c)){
						System.out.println("Unexpected Character.");
						System.exit(0);
						}
					if(c == '.' || c == '+' || c == '-' || Character.isLetter(c)){
						System.out.println("Invalid Numeric Atom Token.");
						System.exit(0);
					}
					token = token.concat(Character.toString(c));
					data = in.read();
					c = Character.toUpperCase((char)data);
				}
				TokenList[inputpos]= token;
				inputpos++;
				token = "";
				white = false;
				paren = false;
			}
			if(c == '(' || c == ')'){
				token = token.concat(Character.toString(c));
				TokenList[inputpos]= token;
				inputpos++;
				token = "";
				white = false;
				paren = true;
			}else if (c == '.'){
				if(!paren && !white){
					System.out.println("Whitespace or Paren must precede Dot Token.");
					System.exit(0);
				}
				token = token.concat(Character.toString(c));
				TokenList[inputpos]= token;
				inputpos++;
				token = "";
				white = false;
				paren = false;
			}else if(isWhitespace(c)){
				white = true;
				paren = false;
			}else{//if it is not an atom then error
				if(!isAtom(c)){
				System.out.println("Unexpected Character.");
				System.exit(0);
				}
			}
			data = in.read();
			}
			
		
		//TODO for the error make sure to check for the end of file token!
		}catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error: File not found.");
			System.exit(0);
}
}

private static boolean isWhitespace(char c){
	if((c == ' ') ||(c == '\n') || (c == '\r') || (c == '\t')){
		return true;
	}else{
		return false;
	}
}
private static boolean isAtom(char c){
	if(Character.isLetter(c) || Character.isDigit(c) || isWhitespace(c) || c == '.' || c == '+' || c == '-' || c == '(' || c == ')'){
		return true;
	}
	else{
		return false;
	}
	
}

	
}