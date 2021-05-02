package tests;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import explorer.ModelFactory;
import junit.framework.TestCase;

public class TestModelFactory extends TestCase {

	
	public void testLoadFromFile( )
	{
			try {
				OWLModel model = ModelFactory.loadFromFile("src/tests/pizza.owl","http://www.example.org");
			} catch (FileNotFoundException e) {
				fail("file should have been located.");
			} catch (OntologyLoadException e) {
				fail("pizza.owl should parse correctly");
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public void testLoadFromFile2( )
	{
			try {
				OWLModel model = ModelFactory.loadFromFile("src/tests/reasoner.owl","http://www.example.org");
			} catch (FileNotFoundException e) {
				fail("file should have been located.");
			} catch (OntologyLoadException e) {
				fail("pizza.owl should parse correctly");
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public void testLoadFromURI( )
	{
		try {
			OWLModel model = ModelFactory.loadFromURI("http://www.co-ode.org/ontologies/pizza/2005/05/16/pizza.owl");
		} catch (OntologyLoadException e) {
			fail("pizza.owl should parse correctly from a URI");
		} catch (URISyntaxException e) {
			fail("pizza.owl as URI could not be found.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			fail("error could not load ontology");
		}
	}
	
}
