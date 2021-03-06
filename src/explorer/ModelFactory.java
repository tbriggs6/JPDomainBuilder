package explorer;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;


import edu.ship.thb.swoogle.SwoogleRepository;
import edu.ship.thb.swoogle.SwoogleRepositoryFactory;
import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.inference.pellet.ProtegePelletOWLAPIReasoner;
import edu.stanford.smi.protegex.owl.inference.protegeowl.ReasonerManager;
import edu.stanford.smi.protegex.owl.inference.reasoner.ProtegeReasoner;
import edu.stanford.smi.protegex.owl.inference.reasoner.exception.ProtegeReasonerException;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.jena.parser.ProtegeOWLParser;
import edu.stanford.smi.protegex.owl.model.NamespaceManager;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.repository.util.XMLBaseExtractor;

/**
 * Construct protege OWL Models from a number of sources, including from local files,
 * or from remote URI.  In the case of a model constructed from a remote URI the
 * SwoogleRepository will be used to improve run-time performance.
 * 
 * @see SwoogleRepository
 * @author tbriggs
 *
 */

public class ModelFactory {

	/**
	 * Loads an OWLModel from the given file name with the given base URI
	 * 
	 * @param fileName The name of the OWL file to load
	 * @param baseURIName The base name of the URI for the ontology
	 * @return protege model populated with the triples found in the ontology
	 * @throws OntologyLoadException 
	 * @throws FileNotFoundException 
	 * @throws URISyntaxException 
	 * @throws MalformedURLException 
	 */
	public static OWLModel loadFromFile(String fileName, String base) throws OntologyLoadException, FileNotFoundException, URISyntaxException, MalformedURLException {
		
		JenaOWLModel model = ProtegeOWL.createJenaOWLModel();
		ProtegeOWLParser parser = new ProtegeOWLParser(model);
		parser.setImporting(false);
		parser.run(new FileInputStream(fileName), base);
		
		return model;
	}
	
	
	/**
	 * Loads an OWLModel from the given file name with the given base URI
	 * 
	 * @param fileName The name of the OWL file to load
	 * @param baseURIName The base name of the URI for the ontology
	 * @return protege model populated with the triples found in the ontology
	 * @throws OntologyLoadException 
	 * @throws FileNotFoundException 
	 * @throws URISyntaxException 
	 * @throws MalformedURLException 
	 */
	public static OWLModel loadFromFile(String fileName) throws OntologyLoadException, FileNotFoundException, URISyntaxException, MalformedURLException {
		
		File inFile = new File(fileName);
		URI base = XMLBaseExtractor.getXMLBase(inFile.toURI().toString());
		
		JenaOWLModel model = ProtegeOWL.createJenaOWLModel();
		ProtegeOWLParser parser = new ProtegeOWLParser(model);
		parser.setImporting(false);
		parser.run(new FileInputStream(inFile), base.toString());
		
		return model;
	}
	

	/**
	 * Loads an OWLModel from the given base URI name.  Uses the {@link SwoogleRepository} to resolve the 
	 * URI to a URL input stream. 
	 * 
	 * @param baseURIName the name of the URI
	 * @return protege model populated with the triples found in the ontology
	 * @throws OntologyLoadException 
	 * @throws URISyntaxException
	 * @throws IOException 
	 */
	public static OWLModel loadFromURI(String baseURIName) throws OntologyLoadException,  URISyntaxException, IOException 
	{
		return loadFromURI( new URI(baseURIName));
	}

	/**
	 * Loads an OWLModel from the given base URI name.  Uses the {@link SwoogleRepository} to resolve the 
	 * URI to a URL input stream. 
	 * 
	 * @param baseURIName the name of the URI
	 * @return protege moel populated with the triples found in the ontology
	 * @throws OntologyLoadException 
	 * @throws URISyntaxException
	 * @throws IOException 
	 */
	public static OWLModel loadFromURI(URI uri) throws OntologyLoadException, URISyntaxException, IOException
	{
		SwoogleRepository repo = SwoogleRepositoryFactory.getSwoogleRepository();
		InputStream is = repo.getInputStream(uri);
		
		return loadModel(is, uri);
		
	}
	
	/**
	 * load a model from a given URI using Protege-OWL 3.4beta's new ProtegeOWLParser.  This parser
	 * is supposed to be considerable faster and more stable.  Currently this parser seems to ignore
	 * the protege repository structure and does not attempt to load imports into the ontology. 
	 * 
	 * This method attempts to resolve deep imports, i.e. transitively loading imports of imported resources.
	 * 
	 * This method attempts to resolve this by loading imported ontologies from the {@link SwoogleRepository}
	 *  
	 * @param ontologyURI  the URI to load from
	 * @return a populated OWLModel 
	 * @throws URISyntaxException  Invalid URI
	 * @throws OntologyLoadException An exception occurred during parsing 
	 */
	public static OWLModel loadModel(InputStream input, URI ontologyURI) throws URISyntaxException, OntologyLoadException
	{
		HashSet<String> loadedImports = new HashSet<String>( );

		
		SwoogleRepository repo = SwoogleRepositoryFactory.getSwoogleRepository();
		
		JenaOWLModel owlModel = ProtegeOWL.createJenaOWLModel();
		
		ProtegeOWLParser parser = new ProtegeOWLParser(owlModel);
		parser.setImporting(false);
		
		parser.run(input, ontologyURI.toString());
		parser.setImporting(false);
		
		boolean done = false;
		boolean error = false;
		
		while ((!error) && (!done)) {
		
			done = true;
			
			Set<String> imports = owlModel.getAllImports();
			for(String uristr : imports)
			{
				if (loadedImports.contains(uristr)) continue;
				
				System.out.printf("Loading import: %s\n", uristr);
			
				URI uri = new URI(uristr);
				
				try {
					parser.setImporting(true);
					parser.run(repo.getInputStream(uri), uristr);
					if (parser.getErrors().size() > 0) {
						System.err.println("Could not load import : " + uristr + " there were errors");
						throw new OntologyLoadException(null, "Error loading ontology for uri " + uri.toString());
					}
					loadedImports.add(uristr);
					done = false;
				}
				catch(Throwable E)
				{
					throw new OntologyLoadException(E, "Error loading ontology for uri " + uri.toString());
				}
			}
		}
	
		ProtegeOWLParser.doFinalPostProcessing(owlModel);
		return owlModel;
	}
	
	/**
	 * Creates a reasoner for the given model
	 * @param model  The Protege OWLModel to give to the reasoner
	 * @param classifyTaxonomy  Should the reasoner automatically classify the class taxonomy now
	 * @return The ProtegeReasoner for the given model
	 * @throws ProtegeReasonerException
	 */
	public static ProtegeReasoner createReasonerForModel(OWLModel model, boolean classifyTaxonomy) throws ProtegeReasonerException
	{
		// Get the reasoner manager instance
		ReasonerManager reasonerManager = ReasonerManager.getInstance();
		
		//Get an instance of the Protege Pellet reasoner
		ProtegeReasoner reasoner = reasonerManager.createProtegeReasoner(model, ProtegePelletOWLAPIReasoner.class);

		if (classifyTaxonomy) {
			reasoner.classifyTaxonomy();
		}
		
		return reasoner;
	}
}
