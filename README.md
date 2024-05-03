<h1>Semantic Web Technologies and Knowledge Graphs</h1>
<h2>Course work Project</h2>
<p><b>Overview:</b>
In this project, we aim to create a Knowledge Graph from tabular data, SPARQL and Reasoning, Ontology Alignment and Ontology Embedding.
</p>
<ul>
  <li><p><b>TaskRDF:</b> Created Knowledge graph from a CSV file for Restaurant Pizza Domain. The main task was performed in the Coursework2.java class. The created RDF file was stored in files/coursework2/solution/ 
  as IN3067-INM713_coursework_data_pizza_500-taskRDFonly-data.ttl.Then we did Reasoning using OWLMicroReasonar. </p>
  <p>We also computed  IN3067-INM713_coursework_data_pizza_500-taskRDF-only-data-google.ttl.  and IN3067-INM713_coursework_data_pizza_500-taskRDF-only-data-wiki.ttl. reusing Google KG vocabulary and Wiki KG vocabulary.</p>
</li>
  <li><p><b>reasoningSPARQL:</b> We check our knowledge graph with fine SPARQL queries and stored results in CSV files. </p>
  </li>
  <li><p><b>ontologyEmbedding:</b>Computed equivalence class between pizza.owl and our ontology which is pizza-restaurants-ontology.owl. We also computed precision and recall against a given reference.</p>
  <p>After that performed 
    reasoning using pizza.owl, pizza-restaurants-ontology.owl, computed equivalence pizza-cw-equivalences.ttl and generated data IN3067-INM713_coursework_data_pizza_500-taskRDFonly-data.ttl. which is cw_pizza_equivalance_data_reasoning.ttl </p>
    <p>Finally, we used pizza vocabulary to retrieve data from cw_pizza_equivalance_data_reasoning.ttl and stored in a CSV file. </p>
  </li>
  <li>
  <p><b>ontologyEmbedding:</b>We created a .embeddings file and a .txt file for our knowledge graph using the document_sentences.txt file, which was generated from Python. We used two settings in Word2Vec configuration and 
      computed similarity between 6 pairs of entities. 3 pairs similar entities and 3 pairs less similar for both settings .
    </p>  </li>
</ul>
  <h2>Dependencies</h2>
    <ul><li>Java 8 or higher</li>
    <li>Maven</li>
      <li>Libraries: Main dependency is Apache Jena 3.17.0 </li>
    </ul>
