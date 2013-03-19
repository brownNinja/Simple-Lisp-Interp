


@SuppressWarnings("hiding")
public class BinaryTNode<String>{
	private String root = null;
	private BinaryTNode<String> left, right;
	
	public BinaryTNode(String initRoot, BinaryTNode<String> initialLeft, BinaryTNode<String> initialRight)
	   {
		  root = initRoot;
	      left = initialLeft;
	      right = initialRight;
	   }
	 
	public String getRoot(){
		return root;
	}
	
	 public BinaryTNode<String> getLeft()
	   {
	      return left;                                        
	   }
	 
	 public String getLeftmostLeaf()
	   {
	      if (left == null)
	         return root;
	      else
	         return left.getLeftmostLeaf();
	   }
	 
	 public BinaryTNode<String> getRight()
	   {
	      return right;                                               
	   } 
	 
	  public String getRightmostLeaf()
	   {
	      if (right == null)
	         return root;
	      else
	         return right.getRightmostLeaf();
	   } 
	  
	  public void setRoot(String newRoot)
	   {                    
	      root = newRoot;
	   }
	  
	  public void setLeft(BinaryTNode<String> newLeft)
	   {                    
	      left = newLeft;
	   }
	  
	  public void setRight(BinaryTNode<String> newRight)
	   {                    
	      right = newRight;
	   }
	  //used to test the correctness of the parser
	  public void Print()
	   {
		  System.out.println(root);
	      if (left != null)
	         left.Print();
	      if (right != null)
	         right.Print();
	   }  
	  
	  
}