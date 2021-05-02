package explorer;


import java.util.Collection;
import java.util.Iterator;

import edu.ship.thb.swoogle.Results;
import edu.stanford.smi.protegex.owl.inference.reasoner.ProtegeReasoner;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.OWLAnonymousClass;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.util.DLExpressivityChecker;



/**
 * Count basic statistics from the OWL model
 * 
 * @author tbriggs
 *
 */
public class SimpleCounts extends Task {

	public SimpleCounts(OWLModel model, ProtegeReasoner reasoner, Results results) {
		super(model, reasoner, results);
	}

	@Override
	public void run() {
		
		countSpecies();
		countNamedClasses();
		countAnonymousClasses( );
		countProperties( );
		countIndividuals();
	}
	
	/**
	 * Determine the OWL species of the model and save it.
	 */
	private void countSpecies( )
	{
		if (model instanceof JenaOWLModel) {
			JenaOWLModel jmodel = (JenaOWLModel) model;
			int species = jmodel.getOWLSpecies();
			results.setSpecies(species);
		}
		else
			results.setSpecies(-1);
		
			

		DLExpressivityChecker checker = new DLExpressivityChecker(model);
		checker.check();

		String name = checker.getDLName();
		results.setExpressivity(name);
	}
	
	
	
	/**
	 * Determine the number of named classes and save it to results
	 */
	@SuppressWarnings("unchecked")
	private void countNamedClasses() {
		int count = 0;
		
		Iterator named = model.listOWLNamedClasses();
		while (named.hasNext())
		{
			OWLNamedClass ONC = (OWLNamedClass) named.next();
			if (ONC.isSystem()) continue;
			
			count++;
			
		}
		
		results.setNumNamedClass(count);
	}

	/**
	 * Determine the number of anonymous classes and save it to the results
	 */
	@SuppressWarnings("unchecked")
	private void countAnonymousClasses() {
		int count = 0;
		
		Iterator named = model.listOWLAnonymousClasses();
		while (named.hasNext())
		{
			OWLAnonymousClass oac = (OWLAnonymousClass) named.next();
			if(oac.isSystem()) continue;
			
			count++;
		}
		
		results.setNumAnonymousClass(count);
	}

	@SuppressWarnings("unchecked")
	private void countIndividuals() {
		int count = 0;
		
		Collection<OWLIndividual> named = model.getOWLIndividuals();
		for (OWLIndividual ind : named)
		{
			if (ind.isSystem()) continue;
			count++;
		}
		
		results.setNumIndividuals(count);
	}

	
	
	@SuppressWarnings("unchecked")
	private void countProperties( ) {
		int countObj = 0;
		int countDt = 0;
		int countAnn = 0;
		int countOther = 0;
		int countDomains = 0;
		int countRanges = 0;
		int countNullDomains = 0;
		int countNullRanges = 0;
		
		Iterator rdfprops = model.listRDFProperties();
		
		while (rdfprops.hasNext())
		{
			RDFProperty rprop = (RDFProperty) rdfprops.next();
			
			if (rprop.isSystem()) continue;
			
			if (rprop instanceof OWLObjectProperty) {
				countObj++;
				
				if (rprop.isDomainDefined(true))
					countDomains++;
				else 
					countNullDomains++;
				
				if (rprop.isRangeDefined())
					countRanges++;
				else
					countNullRanges++;
				
			}
			else if (rprop instanceof OWLDatatypeProperty)
				countDt++;
			else if (rprop.isAnnotationProperty())
				countAnn++;
			else
				countOther++;
		}
		
		results.setNumObjectProperties(countObj);
		results.setNumDatatypeProperties(countDt);
		results.setNumAnnotationProperties(countAnn);
		results.setNumOtherProperties(countOther);
		results.setNumRangedProperty(countRanges);
		results.setNumDomainedProperty(countDomains);
		results.setNumNullDomains(countNullDomains);
		results.setNumNullRanges(countNullRanges);
	}
	
	
}
