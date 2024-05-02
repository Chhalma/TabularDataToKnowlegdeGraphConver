package coursework2.ontologyEmbeding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.LineSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;

public class Owl2VecEmbedding {
	
	
	public Owl2VecEmbedding () throws NumberFormatException, IOException {
		
		
		
	}
	
	
	public void loadPrecomputedEmbeddings() throws IOException {
		
		//https://deeplearning4j.konduit.ai/language-processing/word2vec
		
		
		Word2Vec w2vModel = WordVectorSerializer.readWord2VecModel("files/coursework2/solution/embedding/cw_java_config1.embeddings");
		
		//Word2Vec w2vModel = WordVectorSerializer.readWord2VecModel("files/coursework2/solution/embedding/cw.embeddings");
		//Word2Vec w2vModel = WordVectorSerializer.readWordVectors(new File("files_lab9/pizza.embeddings.txt"));

		//Attempts to read python generated vectors
		//Word2Vec w2vModel = WordVectorSerializer.readWordVectors(new File("files_lab9/pizza.embeddings.txt"));
		//Word2Vec w2vModel = WordVectorSerializer.readWord2VecModel(new File("files_lab9/pizza.embeddings.bin"));
		//InputStream word2vecmodelFile = new FileInputStream("files_lab9/pizza.embeddings.bin");
		//Word2Vec w2vModel = WordVectorSerializer.readBinaryModel(word2vecmodelFile, false, true);
		//Word2Vec w2vModel = WordVectorSerializer.readWord2Vec(word2vecmodelFile, false);
				
				
		Map<String, Double> similarityMap1 = new HashMap<>();
		Map<String, Double> similarityMap2 = new HashMap<>();


        // Define entity pairs for 3 similar pairs.......
		
        String[] similarPairs = {
            "http://www.semanticweb.org/city/in3067-inm713/2024/restaurants#BlueCheese",
            "http://www.semanticweb.org/city/in3067-inm713/2024/restaurants#Cheddar",
            
            "http://www.semanticweb.org/city/in3067-inm713/2024/restaurants#Pepperoni",
            "http://www.semanticweb.org/city/in3067-inm713/2024/restaurants#Sausage",
            
            "http://www.semanticweb.org/city/in3067-inm713/2024/restaurants#GreekRestaurant",
            "http://www.semanticweb.org/city/in3067-inm713/2024/restaurants#IndianRestaurant"
        };

      
        // Calculate and store similarity values for 3 similar pairs.....
        for (int i = 0; i < similarPairs.length; i += 2) {
            String entity1 = similarPairs[i];
            String entity2 = similarPairs[i + 1];
            double similarity = w2vModel.similarity(entity1, entity2);
            similarityMap1.put(entity1 + " <-> " + entity2, similarity);
        }
        //Print entity and similarity....
        for (Map.Entry<String, Double> entry : similarityMap1.entrySet()) {
            System.out.println(entry.getKey() + " => " + entry.getValue());
        }

        //Not similar pairs....
        String[] diSimilarPairs = {
        		"http://www.semanticweb.org/city/in3067-inm713/2024/restaurants#Tomato",
        		"http://www.semanticweb.org/city/in3067-inm713/2024/restaurants#City",
        		 

        		"http://www.semanticweb.org/city/in3067-inm713/2024/restaurants#Beef",
        		"http://www.semanticweb.org/city/in3067-inm713/2024/restaurants#VegetarianPizza",
        		
        		"http://www.semanticweb.org/city/in3067-inm713/2024/restaurants#AmericanRestaurant",
        		"http://www.semanticweb.org/city/in3067-inm713/2024/restaurants#Pepperoni",
           
        };
        
        // Calculate and store similarity values
        for (int i = 0; i < diSimilarPairs.length; i += 2) {
            String entity1 = diSimilarPairs[i];
            String entity2 = diSimilarPairs[i + 1];
            double similarity = w2vModel.similarity(entity1, entity2);
            similarityMap2.put(entity1 + " <-> " + entity2, similarity);
        }
        for (Map.Entry<String, Double> entry : similarityMap2.entrySet()) {
            System.out.println(entry.getKey() + " => " + entry.getValue());
        }
	}
	
	
	
	public void createEmbeddings(String file_sentences) throws IOException {
		//Reused from https://deeplearning4j.konduit.ai/language-processing/word2vec#loading-data
		
		
		//Load sentences
		SentenceIterator iter = new LineSentenceIterator(
				new File(file_sentences));
		iter.setPreProcessor(new SentencePreProcessor() {
		    @Override
		    public String preProcess(String sentence) {
		        //return sentence.toLowerCase();
		        return sentence;//.toLowerCase();
		    }
		});
		
		// Split on white spaces in the line to get words
		TokenizerFactory t = new DefaultTokenizerFactory();
		//t.setTokenPreProcessor(new CommonPreprocessor());
		
		
		//Building mode;Configuration 1...
		Word2Vec w2vModel = new Word2Vec.Builder()
		        .minWordFrequency(3)
		        .epochs(3)
		        .iterations(5)
		        .layerSize(100)
		        .seed(42)
		        .negativeSample(5)
		        .windowSize(5)
		        .iterate(iter)
		        .tokenizerFactory(t)
		        .build();
		
		// Building mode; Configuration 2.....
	/*	Word2Vec w2vModel = new Word2Vec.Builder()
		        .minWordFrequency(1)   // Adjust the minimum word frequency to filter out infrequent words
		        .epochs(5)            // Increase the number of training epochs
		        .iterations(10)         // Set the number of iterations per epoch
		        .layerSize(300)        // Increase the size of the hidden layer
		        .seed(100)             // Change the random seed for reproducibility
		        .negativeSample(30)    // Adjust the number of negative samples for training
		        .windowSize(10)        // Increase the window size for context
		        .iterate(iter)
		        .tokenizerFactory(t)
		        .build(); 

		*/

		//Fitting model
		w2vModel.fit();
		
		
		
		//Evaluate model
		System.out.println(w2vModel.wordsNearest("pizza", 10));
		System.out.println(w2vModel.similarity("pizza", "meat"));
		
		
		//Vector for a given word
		w2vModel.getWordVector("pizza");
		
		
		//Save vectors
	//	WordVectorSerializer.writeWord2VecModel(w2vModel, "files/coursework2/solution/embedding/cw_java_config1.embeddings");
	//	WordVectorSerializer.writeWordVectors(w2vModel, "files/coursework2/solution/embedding/cw_java_config1.embeddings.txt");
		
		//Save fo rconfiguration 2...
		WordVectorSerializer.writeWord2VecModel(w2vModel, "files/coursework2/solution/embedding/cw_java_config2.embeddings");
		WordVectorSerializer.writeWordVectors(w2vModel, "files/coursework2/solution/embedding/cw_java_config2.embeddings.txt");
		
	}
	
	
	
	//Gives an error to load the embeddings from python. Instead use the generated document and compute word embeddings.
	//It will need some work. 
	
	
	public static void main(String[] args) {

		try {
			Owl2VecEmbedding  owl2Vec = new Owl2VecEmbedding ();
			
		//	owl2Vec.createEmbeddings("files/coursework2/solution/embedding/document_sentences.txt");
			
			//From the step above
			owl2Vec.loadPrecomputedEmbeddings();
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
