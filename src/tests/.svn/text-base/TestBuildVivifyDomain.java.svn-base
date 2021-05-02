package tests;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import edu.ship.thb.swoogle.Results;
import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protegex.owl.inference.reasoner.ProtegeReasoner;
import edu.stanford.smi.protegex.owl.inference.reasoner.exception.ProtegeReasonerException;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import explorer.BuildDisjunction;
import explorer.BuildVivified;
import explorer.BuilderBase;
import explorer.ModelFactory;
import explorer.SimpleCounts;
import junit.framework.TestCase;

public class TestBuildVivifyDomain extends TestCase {

	
	public void testCount( ) throws FileNotFoundException, OntologyLoadException, URISyntaxException, ProtegeReasonerException, MalformedURLException
	{
		OWLModel model = ModelFactory.loadFromFile("src/tests/test-vivify.owl","http://www.example.org");
	
		Results results = new Results( );
		ProtegeReasoner reasoner = ModelFactory.createReasonerForModel(model, true);
		reasoner.classifyTaxonomy();
		
		BuilderBase bnd = new BuildVivified(model,reasoner, results);
		bnd.run( );
		
		System.out.format("num domain more specific: %d\n", results.getNumComputedDomainMoreSpecific());
		System.out.format("num domain more general: %d\n", results.getNumComputedDomainMoreGeneral());
		System.out.format("num domain equal: %d\n", results.getNumComputedDomainEqual());
		System.out.format("num domain error: %d\n", results.getNumComputedDomainError());
		
	}
	
	
}
