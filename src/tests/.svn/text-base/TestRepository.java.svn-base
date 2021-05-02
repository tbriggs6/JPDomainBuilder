package tests;

import junit.framework.TestCase;
import edu.ship.thb.swoogle.SwoogleRepository;
import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protege.model.framestore.NarrowFrameStore;
import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.jena.parser.ProtegeOWLParser;
import edu.stanford.smi.protegex.owl.model.NamespaceManager;
import edu.stanford.smi.protegex.owl.model.triplestore.TripleStore;
import edu.stanford.smi.protegex.owl.repository.Repository;
import edu.stanford.smi.protegex.owl.repository.RepositoryManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;


public class TestRepository extends TestCase {

	public void testContains() {

		SwoogleRepository sr = getSR();
		try {
			if (! sr.contains(getURI()))
					fail("Error: this ontology is not in the database.");
		} catch (URISyntaxException e) {
			fail("A system exception occurred.");
		}
	}
	
	

	public void testGetOntologies() {
	
		SwoogleRepository sr = getSR( );
		
		Collection<URI> allURI = sr.getOntologies();
		if (allURI.size() < 1000)
			fail("there should be more than 1000 uri's");
		
	}

	public void testGetOntologyLocationDescription() throws URISyntaxException {
		SwoogleRepository sr = getSR();
		String descr = sr.getOntologyLocationDescription(getURI());
		
		System.out.println(descr);
	}



	private URI getURI() throws URISyntaxException {
		//return new URI("http://archive.astro.umd.edu/ont/Instrument.owl");
//		return new URI("http://www.mindswap.org/~tw7/work/spire/keywords.owl");
		return new URI("http://clipper.ship.edu/~tbriggs/test.owl");
	}



	private SwoogleRepository getSR() {
		return edu.ship.thb.swoogle.SwoogleRepositoryFactory.getSwoogleRepository();
	}

	public void testGetOutputStream() {
		SwoogleRepository sr = getSR();
		
		try {
			sr.getOutputStream(getURI());
			fail("Error: not an output repository");
		}
		
		catch(Exception E)
		{
			
		}
	}

	public void testGetRepositoryDescription() {
		SwoogleRepository sr = getSR();
		
		String descr = sr.getRepositoryDescription();
	}

	public void testGetRepositoryDescriptor() {
		SwoogleRepository sr = getSR();
		
		String descr = sr.getRepositoryDescriptor();
	}

	public void testHasOutputStreamURI() throws URISyntaxException {
		URI uri = getURI();
		
		SwoogleRepository sr = getSR( );
		
		if (sr.hasOutputStream(uri))
			fail("read only repository.");
	}

	public void testIsSystem() {
		SwoogleRepository sr = getSR( );
		
		if (sr.isSystem())
			fail("not a system repository");
	}

	public void testIsWritable() throws URISyntaxException {
		SwoogleRepository sr = getSR();
		if (sr.isWritable(getURI()))
			fail("read only repository");
	
	}
	

//	public void testGetInputStreamURI()  {
//		
//		// get a url
//		SwoogleRepository sr = getSR( );
//		
//		try {
//			InputStream is = sr.getInputStream(getURI());
//			if (sr.getLastRequestedIS() != is)
//				fail("did not update input stream");
//			if (! sr.getLastRequestedURI().equals(getURI()))
//				fail("did not update last uri");
//		}
//		catch(Exception E)
//		{
//			fail("Could not load ontology.");
//		}
//	}

	public void testGetInputStreamURI2()  {
		
		// get a url
		SwoogleRepository sr = getSR( );
		
		try {
			InputStream is = sr.getInputStream(getURI());
			InputStream is2 = sr.getInputStream(getURI());
			
			if (is != is2)
				fail("the local cache did not work.");
		}
		catch(Exception E)
		{
			fail("Could not load ontology.");
		}
	}
	
	public void testGetInputStreamURI3()  {
		
		// get a url
		SwoogleRepository sr = getSR( );
		
		try {
			
			URI uri = getURI();
			if (! sr.contains(uri))
			{
				fail("did not find uri");
			}
			
			if (! sr.getLastRequestedURI().equals(uri))
				fail("did not update last uri");
			
			int oldReq = sr.getNumRequests();
			int oldHit = sr.getNumHits();
			
			InputStream is = sr.getInputStream(getURI());
			
			int newReq = sr.getNumRequests();
			int newHit = sr.getNumHits( );
			
			if ((oldReq - newReq) != -1) 
				fail("requests don't match " + (oldReq - newReq));
			if ((oldHit - newHit) != -1)
				fail("hits don't match " + (oldHit - newHit));
			
		}
		catch(Exception E)
		{
			fail("Could not load ontology.");
		}
	}
	
	public void testGetInputStreamURI4()  {
		
		// get a url
		SwoogleRepository sr = getSR( );
		
		try {
			URI uri1 = getURI( );
			URI uri2 = new URI("http://www.mindswap.org/~tw7/work/spire/keywords.owl");

			InputStream is1, is2,is3;
			
			if (!sr.contains(uri1))
				fail("did not find " + uri1);
			
			is1 = sr.getInputStream(uri1);
			
			if (!sr.contains(uri2))
				fail("did not find " + uri2);
			
			is2 = sr.getInputStream(uri2);
			
			is3 = sr.getInputStream(uri1);
			
			if (is1 == is2)
				fail("did not update cache.");
			
			if (is1 == is3)
				fail("did not update cache");
			
			if (is2 == is3)
				fail("did not update cache");
			
			if (sr.getNumHits() < 2)
				fail("num hits didn't match " + sr.getNumHits());
			
			if (sr.getNumRequests() < 2)
				fail("num requests didn't match " + sr.getNumRequests());
		}
		catch(Exception E)
		{
			fail("Could not load ontology.");
		}
	}
	
	
	public void testRepositoryManger( ) throws URISyntaxException, OntologyLoadException, IOException
	{
		LinkedList<String> loadedImports = new LinkedList<String>( );
		URI ontologyURI = getURI( );
		
		JenaOWLModel owlModel = ProtegeOWL.createJenaOWLModel();
		
		ProtegeOWLParser parser = new ProtegeOWLParser(owlModel);
		parser.setImporting(false);
		
		parser.run(getSR().getInputStream(ontologyURI), ontologyURI.toString());
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
					parser.run(getSR().getInputStream(uri), uristr);
					done = false;
					imports.add(uristr);
				}
				catch(Throwable E)
				{
					System.err.println("Error loading import");
					error = true;
				}
			}
		}
	
		ProtegeOWLParser.doFinalPostProcessing(owlModel);
	}
}
