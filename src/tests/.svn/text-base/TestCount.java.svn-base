package tests;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import edu.ship.thb.swoogle.Results;
import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protegex.owl.inference.reasoner.ProtegeReasoner;
import edu.stanford.smi.protegex.owl.inference.reasoner.exception.ProtegeReasonerException;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import explorer.ModelFactory;
import explorer.SimpleCounts;
import junit.framework.TestCase;

public class TestCount extends TestCase {

	
	public void testCount( ) throws FileNotFoundException, OntologyLoadException, URISyntaxException, ProtegeReasonerException, MalformedURLException
	{
		OWLModel model = ModelFactory.loadFromFile("src/tests/reasoner-test.owl","http://www.example.org");
		Results results = new Results( );
		ProtegeReasoner reasoner = ModelFactory.createReasonerForModel(model, true);
		
		SimpleCounts counter = new SimpleCounts(model,reasoner, results);
		counter.run( );
		
		System.out.format("Expressivity: (%s)\n", results.getExpressivity());
		System.out.format("OWL Level: %d\n", results.getSpecies());
		System.out.format("Named classes: %d\n", results.getNumNamedClass());
		System.out.format("Anonymous classes: %d\n", results.getNumAnonymousClass());
		System.out.format("Object properties: %d\n", results.getNumObjectProperty());
		System.out.format("Datatype properties: %d\n", results.getNumDatatypeProperty());
		System.out.format("Annotation properties: %d\n", results.getNumAnnotationProperty());
		System.out.format("Other properties: %d\n", results.getNumOtherProperty());
		System.out.format("Props w/ domains: %d\n", results.getNumDomains());
		System.out.format("Props w/ ranges: %d\n", results.getNumRanges());
		System.out.format("Individuals: %d\n", results.getNumIndividuals());
	}
	
	
}
