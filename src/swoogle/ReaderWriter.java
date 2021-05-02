package swoogle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;

import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.jena.parser.ProtegeOWLParser;
import edu.stanford.smi.protegex.owl.repository.util.XMLBaseExtractor;

public class ReaderWriter {

	
	public static void main(String args[]) throws Exception
	{
		File inFile = new File("src/tests/test-genfacts.owl");
		
		URI base = XMLBaseExtractor.getXMLBase(inFile.toURI().toString());
		System.out.println("Base is: " + base.toString());
		
		JenaOWLModel model = ProtegeOWL.createJenaOWLModel();
		ProtegeOWLParser parser = new ProtegeOWLParser(model);
		parser.setImporting(false);
		parser.run(new FileInputStream("src/tests/test-genfacts.owl"), base.toString());
		
		// now we have a model...
		
		model.getOWLObjectProperty("hasPlace").addComment("hello!");
		model.save(new File("out.owl").toURI());
		
		
	}
	
	
}
