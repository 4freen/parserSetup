package parserSetup;

import java.io.StringReader;
import java.util.List;

import edu.smu.tspell.wordnet.NounSynset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.CoreTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.smu.tspell.wordnet.NounSynset;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;


class parserApp {
	
	
	public static String[] month = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    private final static String PCG_MODEL = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";        
    
    /* invertible: Store enough information about the original form of the token and the whitespace 
     * around it that a list of tokens can be faithfully converted back to the original String. 
     * Valid only if the LexedTokenFactory is an instance of CoreLabelTokenFactory. 
     * The keys used in it are: TextAnnotation for the tokenized form, 
     * OriginalTextAnnotation for the original string, BeforeAnnotation and 
     * AfterAnnotation for the whitespace before and after a token, 
     * and perhaps CharacterOffsetBeginAnnotation and CharacterOffsetEndAnnotation to record 
     * token begin/after end character offsets, if they were specified to be recorded in TokenFactory construction. 
     * (Like the String class, begin and end are done so end - begin gives the token length.) Default is false.
     */

    private final TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "invertible=true");

    //PCG_MODEL for building the tree
    private final LexicalizedParser parser = LexicalizedParser.loadModel(PCG_MODEL);

    //Stanford Parser function
    public Tree parse(String str) {                
        List<CoreLabel> tokens = tokenize(str);
        Tree tree = parser.apply(tokens);
        return tree;
    }
    //Stanford Parser function
    private List<CoreLabel> tokenize(String str) {
        Tokenizer<CoreLabel> tokenizer =
        	tokenizerFactory.getTokenizer(
                new StringReader(str));    
        return tokenizer.tokenize();
    }

    @SuppressWarnings("null")
	public static void main(String[] args) { 
    	
    	//WordNet api
    	NounSynset nounSynset; //synonyms
			
		WordNetDatabase database = WordNetDatabase.getFileInstance(); 

        String str = "What is the water pollution in Bangalore in 31/12/2015?";
        String time;
        String[] timestamp;
        String location = null;
        String[] condition = new String[50];
        int keywords = 0;
        int syear = 0, smonth = 0, sday = 0;
        parserApp parser = new parserApp();
        
        //Parse tree is created
        Tree tree = parser.parse(str);  

        List<Tree> leaves = tree.getLeaves();
        // Print words and POS(part of speech) Tags
        // POS tags explained here - https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
        // Condition - NN (noun, singular or mass)
        // Location - NNP (proper noun, singular)
        // Time - CD (cardinal number)
        
        // Printing the tags and figuring out the time parameter
        for (Tree leaf : leaves) {
            Tree parent = leaf.parent(tree);
            System.out.print(leaf.label().value() + "/" + parent.label().value() + " ");
            
           // CD tag enables us to look for either day or year of the event
            if(parent.label().value().equalsIgnoreCase("CD")) {
            	time = leaf.label().value().toString();
            	
            	// find out if year is specified
            	if(time.length() == 4)
            		syear = Integer.parseInt(time);
            	
            	//find out if day is specified
            	else if(time.length() == 2) {
            		sday = Integer.parseInt(time);
            		if(sday <= 1 && sday >= 31)
            			sday = 0;
            		
            	}
            	//else if specified in DD/MM/YYYY format
            	else {
            		timestamp = time.split("/");
            		sday = Integer.parseInt(timestamp[0]);
            		smonth = Integer.parseInt(timestamp[1]);
            		syear = Integer.parseInt(timestamp[2]);
            		

            	}
            }
            
          //the freetext keywords string
			if(parent.label().value().equals("NN")) {
				condition[keywords] = leaf.label().value();
				keywords++;
			}
            
        }
        
        //TEST point for checking if day, month, year is correctly defined
        System.out.println(sday+" "+smonth+" "+syear);
        
        //Wordnet API 
        for(Tree leaf : leaves) {
            Tree parent = leaf.parent(tree);
    		Synset[] synsets = database.getSynsets(leaf.label().value(), SynsetType.NOUN); 
    			for (int i = 0; i < synsets.length; i++) { 
    				nounSynset = (NounSynset)(synsets[i]); 
    				System.err.println(nounSynset.getWordForms()[0] + 
    						": " + nounSynset.getDefinition());
    				
    				//check if month is given in words
    				if(parent.label().value().equals("NNP")) {
    					if(smonth == 0)
    					{
    						for(int m = 0; m < 12; m++) {
    							if(leaf.label().value().equalsIgnoreCase(month[m])) {
    								smonth = m+1;
    							}		
    						}
    					}
    					
    					//the other proper noun in the scenario is Location
    					else {
    						location = leaf.label().value().toString();
    					}
    						
    				}
    			}
        }
        
        //TEST point to check if condition, location and time have been correctly set
        System.out.println("Loc: "+ location);
        System.out.println("Time: "+ sday + " " + smonth + " " + syear);
        System.out.print("Condition: ");
        for(int k = 0; k<keywords; k++)
        	System.out.print(condition[k]+ " ");
        System.out.println();
        
        
    }
}