package tests;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;

import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protegex.owl.inference.pellet.ProtegePelletOWLAPIReasoner;
import edu.stanford.smi.protegex.owl.inference.protegeowl.ReasonerManager;
import edu.stanford.smi.protegex.owl.inference.reasoner.ProtegeReasoner;
import edu.stanford.smi.protegex.owl.inference.reasoner.exception.ProtegeReasonerException;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import explorer.ModelFactory;
import junit.framework.TestCase;

public class TestReasoning extends TestCase {

	public void testSimpleTest( ) throws FileNotFoundException, OntologyLoadException, URISyntaxException, ProtegeReasonerException, MalformedURLException
	{
		OWLModel model = ModelFactory.loadFromFile("src/tests/reasoner-test.owl","http://www.example.org");

		OWLClass likedThings = model.getOWLNamedClass("LikedThings");
		assertNotNull(likedThings);
		
		// Get the reasoner manager instance
		ReasonerManager reasonerManager = ReasonerManager.getInstance();
		assertNotNull(reasonerManager);
		
		//Get an instance of the Protege Pellet reasoner
		ProtegeReasoner reasoner = reasonerManager.createProtegeReasoner(model, ProtegePelletOWLAPIReasoner.class);
		reasoner.classifyTaxonomy();
		reasoner.computeInferredIndividualTypes();
				
		Collection<OWLIndividual> individuals = reasoner.getIndividualsBelongingToClass(likedThings);
		assertNotNull(individuals);
		assert(individuals.size() > 0);
		
		for(OWLIndividual indiv : individuals)
		{
			System.out.println(indiv);
		}
	}
	
	
}
