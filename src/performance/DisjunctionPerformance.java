package performance;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Vector;

import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.inference.pellet.ProtegePelletOWLAPIReasoner;
import edu.stanford.smi.protegex.owl.inference.protegeowl.ReasonerManager;
import edu.stanford.smi.protegex.owl.inference.reasoner.ProtegeReasoner;
import edu.stanford.smi.protegex.owl.inference.reasoner.exception.ProtegeReasonerException;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.OWLUnionClass;
import edu.stanford.smi.protegex.owl.model.RDFSClass;

public class DisjunctionPerformance {

	static int next_sym = 0;
	private int maxdisjunction;
	private int nclasses, nproperties,nindiv;
	private JenaOWLModel owlModel;
	
	double subclass = 0.25;
	double disjclass = 0.30;
	double propstmt = 0.1;
	
	
	
	public DisjunctionPerformance(int nclasses, int nprop, int nindiv, int maxdisjunction) throws OntologyLoadException
	{
		
		this.nclasses = nclasses;
		this.maxdisjunction = maxdisjunction;
		this.nproperties = nprop;
		this.nindiv = nindiv;
		
		owlModel = ProtegeOWL.createJenaOWLModel();
		
		constructModel( );
		
	}
	

	private void constructModel( )
	{
		Vector<String> classes = new Vector<String>( );
		Vector<String> properties = new Vector<String>( );
		Vector<String> individuals = new Vector<String>( );
		
		for (int i = 0; i < nclasses; i++)
		{
			String sym = gensym("SYM" );
			classes.add(sym);
			OWLNamedClass symclass = owlModel.createOWLNamedClass(sym);
			
			for (int j = 0; j < (classes.size() * subclass); j++) {
				
				if (Math.random() < subclass)
				{
					int n = (int)(Math.random() * classes.size());
					String sname = classes.get(n);
					if (sname.equals(sym)) continue;
					
					OWLNamedClass onc = owlModel.getOWLNamedClass(sname);
					symclass.addSuperclass(onc);
				}
			}
		}
		
		for (int i = 0; i < nproperties; i++)
		{
			String prop = gensym("PROP" );
			properties.add(prop);
			OWLObjectProperty oop = owlModel.createOWLObjectProperty(prop);
			
			int count = 0;
			OWLUnionClass ouc = owlModel.createOWLUnionClass();
			for (int j = 0; j < maxdisjunction; j++)
			{
				if (Math.random() < disjclass)
				{
					int n = (int)(Math.random() * classes.size());
					String sname = classes.get(n);
					OWLNamedClass onc = owlModel.getOWLNamedClass(sname);
					if (onc == null) continue;
					
					count++;
					ouc.addOperand(onc);
				}
			}
		
			if (count > 0)
				oop.setDomain(ouc);
		}
		
		for (int i = 0; i < nindiv; i++)
		{
			String indiv = gensym("I");
			individuals.add(indiv);
			
			OWLIndividual oindiv = owlModel.getOWLThingClass().createOWLIndividual(indiv);
			
			if (i < 10) continue;
			
			for (int j= 0; j < nproperties; j++)
			{
				if (Math.random() < propstmt) { 
					// pick a property
					int pidx = (int) (Math.random() * properties.size());
					String pname = properties.get(pidx);
					OWLObjectProperty oop = owlModel.getOWLObjectProperty(pname);
					if (oop == null) continue;
					
					// pick an individual
					int iidx = (int) (Math.random() * individuals.size());
					String iname = individuals.get(iidx);
					OWLIndividual obj = owlModel.getOWLIndividual(iname);

					if (obj == null) continue;
					if (obj.equals(oindiv)) continue;
					
					oindiv.addPropertyValue(oop, obj);
				}
			}
			
		}
	}
	
	public long reason( ) throws ProtegeReasonerException
	{
		// Get the reasoner manager instance
		ReasonerManager reasonerManager = ReasonerManager.getInstance();
		
		//Get an instance of the Protege Pellet reasoner
		ProtegeReasoner reasoner = reasonerManager.createProtegeReasoner(owlModel, ProtegePelletOWLAPIReasoner.class);

		long start = System.currentTimeMillis();
		
		reasoner.classifyTaxonomy();
		reasoner.computeInferredIndividualTypes();
		
		long end = System.currentTimeMillis();
		
		return end-start;
	}
	
	static String gensym(String prefix )
	{
		return prefix + next_sym++;
	}
	
	public void write( ) throws URISyntaxException, Exception
	{
		File outf= new File("out.owl");
		owlModel.save(outf.toURI());
		
	}
	public static void main(String args[]) throws URISyntaxException, Exception
	{
		try {
			PrintWriter out = new PrintWriter(new FileOutputStream("results.txt"));
			for (int classes = 20; classes < 230; classes+=10) {
				
				DisjunctionPerformance DP = new DisjunctionPerformance( classes,classes/2, classes*2,(int) Math.sqrt(classes));
				DP.write();
				long time = DP.reason();
				
				out.format( "%d,%d,%d,%d\n", classes,classes/2,classes*2,time);
				System.out.format("reason: %d,%d,%d,%d\n", classes,classes/2,classes*2,time);
				out.flush();
			}
			out.close();
		} catch (Throwable E) {
			
			// TODO Auto-generated catch block
			E.printStackTrace();
		}
	}
}
