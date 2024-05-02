package coursework2.ontologyAlignment;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;

import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;

public class OntologyAlignment {
    
    OntModel model;
    Map<String, OntClass> uriToClsLabel1 = new HashMap<String, OntClass>();
    Map<String, OntClass> uriToClsLabel2 = new HashMap<String, OntClass>();
    Map<String, OntProperty> uriToProLabel1 = new HashMap<String, OntProperty>();
    Map<String, OntProperty> uriToProLabel2 = new HashMap<String, OntProperty>();
    private static final double SIMILARITY_THRESHOLD = 0.75; // Adjust this threshold as needed

    
    public void loadOntologyFromURL(String sourceURL) {
        
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        model.read(sourceURL, "RDF/XML");

        
        System.out.println("Number of classes: " + model.listNamedClasses().toList().size());
        
        
    }
    
    public void loadOntologyFromLocalFile(String onto_file) throws FileNotFoundException {
        
        InputStream input_file = new FileInputStream(onto_file);
        
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        model.read(input_file, "RDF/XML");

        
        System.out.println("Number of classes: " + model.listNamedClasses().toList().size());
        
        
    }
    
    
    public Set<String> getRDFSLabelsForClass(OntClass cls) {
        
        final NodeIterator labels = cls.listPropertyValues(RDFS.label);
        
        Set<String> labels_set =  new HashSet<String>();
        
        while( labels.hasNext() ) {
            final RDFNode labelNode = labels.next();
            final Literal label = labelNode.asLiteral();
            //label.getLanguage(; In case we want to filter by language
            labels_set.add(label.getString());
        }
        
        return labels_set;
        
    }
    
 public Set<String> getRDFSLabelsForProperty(OntProperty pro) {
        
        final NodeIterator labels = pro.listPropertyValues(RDFS.label);
        
        Set<String> labels_set =  new HashSet<String>();
        
        while( labels.hasNext() ) {
            final RDFNode labelNode = labels.next();
            final Literal label = labelNode.asLiteral();
            //label.getLanguage(; In case we want to filter by language
            labels_set.add(label.getString());
        }
        
        return labels_set;
        
    }
 
   public void iterateOverLabels() {
        
        for (Iterator<? extends OntClass> i = model.listClasses(); i.hasNext(); ) {
            OntClass c = i.next();
            if (!c.isAnon()) {  //To filter complex classes. One could listNamedClasses too
                System.out.println(c.getURI());
                System.out.println("\t" + c.getLocalName());  //Access to name in URI (it can be a non informative ID)
                System.out.println("\t" + getRDFSLabelsForClass(c)); //Access to rdfs:label
                
            }
        }
    }

    public boolean lexicalSimilarityCheck(String key1,String key2) {

        JaroWinklerSimilarity jwSim = new JaroWinklerSimilarity();
        LevenshteinDistance levenshteinDist = new LevenshteinDistance();

                double jwSimilarity = jwSim.apply(key1, key2);
                double levenshteinDistance = levenshteinDist.apply(key1, key2);

                // Check if the similarity exceeds the threshold
                if (jwSimilarity >= SIMILARITY_THRESHOLD || (1 - levenshteinDistance / Math.max(key1.length(), key2.length())) >= SIMILARITY_THRESHOLD) {
                   return true;  
                   }else 
                	   return false;
            }
     

    public void computeAndSaveEquivalences(OntModel model1, OntModel model2, String outputFile) throws FileNotFoundException {
    	
    	OntModel equivalenceModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

    	 for (Iterator<? extends OntClass> i = model1.listClasses(); i.hasNext(); ) {
             OntClass parentClass = i.next();
             if (!parentClass.isAnon()) {  
            	  uriToClsLabel1.put(parentClass.getLocalName(), parentClass);
            	//  System.out.println("Parent Class " +parentClass.getLocalName());
                  
            	 //To filter complex classes. One could listNamedClasses too
            	 for (Iterator<? extends OntClass> p = parentClass.listSubClasses(); p.hasNext(); ) {
            	        OntClass subClass = p.next();
            	        if (!subClass.isAnon()) {  
                      	  uriToClsLabel1.put(subClass.getLocalName(), subClass);
                      //	  System.out.println(subClass.getLocalName());
                      
            	        }
            	 }
             }
         }
    	 for (Iterator<? extends OntClass> i = model2.listClasses(); i.hasNext(); ) {
    		 OntClass parentClass = i.next();
             if (!parentClass.isAnon()) {  
            	  uriToClsLabel2.put(parentClass.getLocalName(), parentClass);
            	 //To filter complex classes. One could listNamedClasses too
            	 for (Iterator<? extends OntClass> p = parentClass.listSubClasses(); p.hasNext(); ) {
            	        OntClass subClass = p.next();
            	        if (!subClass.isAnon()) {  
                      	  uriToClsLabel2.put(subClass.getLocalName(), subClass);
                        }
            	 }
             }
         }
    	 
    	 for (String key : uriToClsLabel1.keySet()) {
    	        if (uriToClsLabel2.containsKey(key)) {
    	            OntClass cls1 = uriToClsLabel1.get(key);
    	            OntClass cls2 = uriToClsLabel2.get(key);
    	            
    	            equivalenceModel.add(cls1, OWL.equivalentClass, cls2);
    	            
    	           } else {
    	        	for(String key2 :uriToClsLabel2.keySet()) {
    	        		
    	        		if(lexicalSimilarityCheck(key,key2)) {
    	        			
    	        			if(key2.contains("Topping")) {
    	        				if(key2.equals(key+"Topping")) {
    	        					OntClass cls1 = uriToClsLabel1.get(key);
    	    	    	            OntClass cls2 = uriToClsLabel2.get(key2);
    	    	    	           equivalenceModel.add(cls1, OWL.equivalentClass, cls2);
    	        				}
    	        			}
    	        			else if (key2.contains("Pizza") && key.contains("Pizza"))  {
    	        				int index1 = key.indexOf("Pizza");
    	        				int index2 = key2.indexOf("pizza");
    	        				String str1,str2;
    	        				if(index1==0&&key.length()>5) {
    	        					 str1  = key.substring(5, key.length());
    	        				}
    	        				else {
    	        					str1 = key.substring(0,key.length()-5);
    	        				}
    	        				if(index2==0&&key2.length()>5) {
   	        					 str2  = key2.substring(5, key2.length());
   	        				}
   	        				else {
   	        					str2 = key2.substring(0,key2.length()-5);
   	        				}
    	        				if(str1 != null && str2 != null)
    	        				if(lexicalSimilarityCheck(str1,str2)) {
    	        					OntClass cls1 = uriToClsLabel1.get(key);
    	    	    	            OntClass cls2 = uriToClsLabel2.get(key2);
    	    	    	           equivalenceModel.add(cls1, OWL.equivalentClass, cls2);
    	        				}
    	        				
    	        			} 
    	        			
    	        		}
        	        }
    	        }
    	       }
    	 for (Iterator<? extends OntProperty> i = model1.listAllOntProperties(); i.hasNext(); ) {
             OntProperty parentProp = i.next();
             if (!parentProp.isAnon()) {  
           	  uriToProLabel1.put(parentProp.getLocalName(), parentProp);
           	 //To filter complex classes. One could listNamedClasses too
           	 for (Iterator<? extends OntProperty> p = parentProp.listSubProperties(); p.hasNext(); ) {
           	        OntProperty subProperty = p.next();
           	        if (!subProperty.isAnon()) {  
                     	  uriToProLabel1.put(subProperty.getLocalName(), subProperty);
                     
           	        }
           	 }
            }

            
         }
    	 for (Iterator<? extends OntProperty> i = model2.listAllOntProperties(); i.hasNext(); ) {
    		 OntProperty parentProp = i.next();
             if (!parentProp.isAnon()) {  
           	  uriToProLabel2.put(parentProp.getLocalName(), parentProp);
           	 //To filter complex classes. One could listNamedClasses too
           	 for (Iterator<? extends OntProperty> p = parentProp.listSubProperties(); p.hasNext(); ) {
           	        OntProperty subProperty = p.next();
           	        if (!subProperty.isAnon()) {  
                     	  uriToProLabel2.put(subProperty.getLocalName(), subProperty);
                     
           	        }
           	 }
            }
         }
    	 for (String key : uriToProLabel1.keySet()) {
    	        if (uriToProLabel2.containsKey(key)) {
    	            OntProperty prop1 = uriToProLabel1.get(key);
    	            OntProperty prop2 = uriToProLabel2.get(key);
    	            
    	            // Do something with the values, for example:
    	            equivalenceModel.add(prop1, OWL.equivalentProperty, prop2);
    	            
    	            // Here you can perform further operations with the values if needed.
    	        }
    	 }
    	 try (OutputStream outputStream = new FileOutputStream(outputFile)) {
    	     equivalenceModel.write(outputStream, "TTL");
    	     System.out.println("Equivalences saved to " + outputFile);
    	 } catch (IOException e) {
    	     e.printStackTrace();
    	 }
    
    	}
    // Method to compute and save equivalences between entities of two ontologies
    public static void main(String[] args) {
        		    	
	   			OntologyAlignment access = new OntologyAlignment();

	   					String onto_file1 = "files/coursework2/pizza-restaurants-ontology.owl";
		                String onto_file2 = "files/coursework2/pizza.owl";
        		        String outputFile = "files/coursework2/solution/pizza-cw-equivalences.ttl";
        		        String refFile = "files/coursework2/reference-mappings-pizza.ttl";

        		        try {
        		            access.loadOntologyFromLocalFile(onto_file1);
        		            OntModel model1 = access.model;
        		           
        		            access.loadOntologyFromLocalFile(onto_file2);
        		            OntModel model2 = access.model;

        		            access.computeAndSaveEquivalences(model1, model2, outputFile);
        		            access.computePrecisionAndRecall(outputFile,refFile);

        		        } catch (FileNotFoundException e) {
        		            e.printStackTrace();
        		        } catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
        		    }

    public void computePrecisionAndRecall(String alignmentFile, String referenceFile) throws IOException {
        // Load datasets
		Dataset alignmentDataset = RDFDataMgr.loadDataset(alignmentFile, RDFLanguages.TURTLE);
		Dataset referenceDataset = RDFDataMgr.loadDataset(referenceFile, RDFLanguages.TURTLE);
		
		// Get default models
		Model alignmentModel = alignmentDataset.getDefaultModel();
		Model referenceModel = referenceDataset.getDefaultModel();
		
		// Counters for true positives (TP), false positives (FP), and false negatives (FN)
		int tp = 0;
		int fp = 0;
		int fn = 0;

		// Iterate over mappings in the alignment model
		StmtIterator alignmentIterator = alignmentModel.listStatements();
		while (alignmentIterator.hasNext()) {
		    Statement stmt = alignmentIterator.nextStatement();
		    // Check if the mapping exists in the reference model
		    if (containsStatement(referenceModel, stmt)) {
		        tp++; // True positive
		    } else {
		        fp++; // False positive
		    }
		}

		// Iterate over mappings in the reference model to find false negatives
		StmtIterator referenceIterator = referenceModel.listStatements();
		while (referenceIterator.hasNext()) {
		    Statement stmt = referenceIterator.nextStatement();
		    // Check if the mapping exists in the alignment model
		    if (!containsStatement(alignmentModel, stmt)) {
		        fn++; // False negative
		    }
		}

		// Compute precision and recall
		double precision = (double) tp / (tp + fp);
		double recall = (double) tp / (tp + fn);

		// Output results
		System.out.println("True Positives (TP): " + tp);
		System.out.println("False Positives (FP): " + fp);
		System.out.println("False Negatives (FN): " + fn);
		System.out.println("Precision: " + precision);
		System.out.println("Recall: " + recall);
    }

    // Method to check if a statement exists in a model
    private boolean containsStatement(Model model, Statement stmt) {
        Resource subject = stmt.getSubject();
        Property predicate = stmt.getPredicate();
        RDFNode object = stmt.getObject();
        return model.contains(subject, predicate, object);
    }
    
}
