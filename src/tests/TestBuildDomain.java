package tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.LinkedList;

import swoogle.SwoogleRunner;

import edu.ship.thb.swoogle.ResultProperty;
import edu.ship.thb.swoogle.Results;
import edu.ship.thb.swoogle.WorkUnit;
import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protegex.owl.inference.reasoner.ProtegeReasoner;
import edu.stanford.smi.protegex.owl.inference.reasoner.exception.ProtegeReasonerException;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import explorer.BuildDisjunction;
import explorer.BuildNamed;
import explorer.BuildVivified;
import explorer.BuilderBase;
import explorer.GeneratorType;
import explorer.ModelFactory;
import explorer.SimpleCounts;
import junit.framework.TestCase;

public class TestBuildDomain extends TestCase {

	
	public void testCount( ) throws FileNotFoundException, OntologyLoadException, URISyntaxException, ProtegeReasonerException
	{
		try {
			
			SwoogleRunner runner = new SwoogleRunner( );
	
			WorkUnit unit = new WorkUnit("5575302","http://www.daml.org/people/tself/cougaar-ont.owl");
			runner.runJob(unit);
			
//			OWLModel model = ModelFactory.loadFromFile("src/tests/reasoner-test.owl","http://www.example.org");
//			Results results = new Results( );
//			ProtegeReasoner reasoner = ModelFactory.createReasonerForModel(model, true);
//			reasoner.classifyTaxonomy();
//			
//			
//			BuilderBase bnd = new BuildDisjunctionDomain(GeneratorType.DISJUNCTION, model,reasoner, results);
//			bnd.run( );
//			
//			bnd = new BuildNamedDomains(model, reasoner, results);
//			bnd.run( );
//			
//			
//			bnd = new BuildVivifiedDomain(model, reasoner,results);
//			bnd.run();
//			
//			LinkedList<ResultProperty> props = results.getProperties();
//			
//			for(ResultProperty P : props)
//			{
//		
//				System.out.format("%s : %s |  %s %s %s, (%s) (%s) (%s)\n", 
//						P.getProperty(), P.getDomainOrig(), P.getDomOrigDisj(), P.getDomOrigLCNS(), P.getDomOrigVivf(), 
//						P.getDomainDisj(), P.getDomainLCNS(), P.getDomainVivf());
//
//			}
//			
//			
//			System.out.println("--------------| XML |----------------------");
//			System.out.println(results.toXML());
//			
//			System.out.println("Sending....");
//			WorkUnit.update(333, results);
			
		}
		catch(Throwable E)
		{
			System.err.println(E.getClass().getName());
			throw new RuntimeException(E);
		}
		
	}
	
	
}
