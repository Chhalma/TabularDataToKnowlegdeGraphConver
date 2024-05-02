package coursework2.ontologyEmbeding;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import java.io.*;

public class MergeCW_Onto_Data {

    public static void main(String[] args) {
        // Load the first TTL file (ontology)
    	String cw_onto = "files/coursework2/pizza-restaurants-ontology.ttl";
    	String cw_data = "files/coursework2/solution/IN3067-INM713_coursework_data_pizza_500-taskRDFonly-data.ttl";
		String file_output = "files/coursework2/solution/RDFtriples_with_ontology1.ttl";
    	
    	Dataset dataset = RDFDataMgr.loadDataset(cw_onto, RDFLanguages.TURTLE);
        Model model = dataset.getDefaultModel();
       
        // Load cw_data.ttl 
        RDFDataMgr.read(model, cw_data);
        System.out.println("Data triples from cw_onto: " + model.size());

        // Write the model to an OWL file
        try {
        	OutputStream out = new FileOutputStream(file_output);
        	 RDFDataMgr.write(out, model, RDFFormat.RDFXML);	       
        	// RDFDataMgr.write(out, model, RDFLanguages.TURTLE);	       
          	
    	    out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

