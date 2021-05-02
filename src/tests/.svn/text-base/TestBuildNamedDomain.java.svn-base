package tests;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import edu.ship.thb.swoogle.Results;
import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protegex.owl.inference.reasoner.ProtegeReasoner;
import edu.stanford.smi.protegex.owl.inference.reasoner.exception.ProtegeReasonerException;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import explorer.BuilderBase;
import explorer.BuildNamed;
import explorer.ModelFactory;
import explorer.SimpleCounts;
import junit.framework.TestCase;

public class TestBuildNamedDomain extends TestCase {

	
	public void testCount( ) throws FileNotFoundException, OntologyLoadException, URISyntaxException, ProtegeReasonerException, MalformedURLException
	{
		OWLModel model = ModelFactory.loadFromFile("src/tests/reasoner-test.owl","http://www.example.org");
		Results results = new Results( );
		ProtegeReasoner reasoner = ModelFactory.createReasonerForModel(model, true);
		reasoner.classifyTaxonomy();
		
		BuilderBase bnd = new BuildNamed(model,reasoner, results);
		bnd.run( );
		
		
		assertEquals(results.getNumComputedDomainEqual(),1);
		assertEquals(results.getNumComputedDomainMoreSpecific(), 1);
		assertEquals(results.getNumComputedDomainMoreGeneral( ), 0);
		assertEquals(results.getNumComputedDomainError(), 0);
		
	}
	
	public void testOne( ) throws FileNotFoundException, OntologyLoadException, URISyntaxException, ProtegeReasonerException, MalformedURLException
	{
		OWLModel model = ModelFactory.loadFromFile("src/tests/test-nameddomain.owl","http://www.example.org");
		Results results = new Results( );
		ProtegeReasoner reasoner = ModelFactory.createReasonerForModel(model, true);
		reasoner.classifyTaxonomy();
		
		BuilderBase bnd = new BuildNamed(model,reasoner, results);
		bnd.run( );
		
		
		assertEquals(results.getNumComputedDomainEqual(),1);
		assertEquals(results.getNumComputedDomainMoreSpecific(), 0);
		assertEquals(results.getNumComputedDomainMoreGeneral( ), 0);
		assertEquals(results.getNumComputedDomainError(), 0);
		
	}
	
	public void testTwo( ) throws FileNotFoundException, OntologyLoadException, URISyntaxException, ProtegeReasonerException, MalformedURLException
	{
		OWLModel model = ModelFactory.loadFromFile("src/tests/test-nameddomain2.owl","http://www.example.org");
		Results results = new Results( );
		ProtegeReasoner reasoner = ModelFactory.createReasonerForModel(model, true);
		reasoner.classifyTaxonomy();
		
		BuilderBase bnd = new BuildNamed(model,reasoner, results);
		bnd.run( );
		
		assertEquals(results.getNumComputedDomainEqual(),0);
		assertEquals(results.getNumComputedDomainMoreSpecific(), 1);
		assertEquals(results.getNumComputedDomainMoreGeneral( ), 0);
		assertEquals(results.getNumComputedDomainError(), 0);
		
	}

	public void testThree( ) throws FileNotFoundException, OntologyLoadException, URISyntaxException, ProtegeReasonerException, MalformedURLException
	{
		OWLModel model = ModelFactory.loadFromFile("src/tests/test-nameddomain3.owl","http://www.example.org");
		Results results = new Results( );
		ProtegeReasoner reasoner = ModelFactory.createReasonerForModel(model, true);
		reasoner.classifyTaxonomy();
		
		BuilderBase bnd = new BuildNamed(model,reasoner, results);
		bnd.run( );
		
		
		assertEquals(results.getNumComputedDomainEqual(),1);
		assertEquals(results.getNumComputedDomainMoreSpecific(), 0);
		assertEquals(results.getNumComputedDomainMoreGeneral( ), 0);
		assertEquals(results.getNumComputedDomainError(), 0);
		
		
	}
	
}
