package explorer;
import java.util.Collection;
import java.util.Iterator;


import edu.ship.thb.swoogle.Results;
import edu.stanford.smi.protegex.owl.inference.reasoner.ProtegeReasoner;
import edu.stanford.smi.protegex.owl.inference.reasoner.exception.ProtegeReasonerException;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLIntersectionClass;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.OWLRestriction;
import edu.stanford.smi.protegex.owl.model.OWLUnionClass;
import edu.stanford.smi.protegex.owl.model.RDFObject;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
/**
 * BuildNamed - Uses Protege's reasoner interface to build a least common subsumer
 * named subsumer of a concept.
 * @author Tom Briggs
 * 
 */

public class BuildNamed extends BuildDisjunction {

  /**
   * Constructor for the build named method. Adapts the BuildDisjunction base 
   * constructor which provides a number of important common supporting routines.
   * 
   * @param model - The Protege OWLModel to operate on
   * @param reasoner - The ProtegeReasoner for the given model
   * @param results - The results collection to hold reasoning results
   * @see {@link BuildDisjunction}
   */
	public BuildNamed(OWLModel model, ProtegeReasoner reasoner, Results results) {
		super(GeneratorType.LCNS, model, reasoner, results);
	}


	/**
	 * Builds a domain for the given property by computing the least common named subsumer (LCNS).
	 * 
	 * @param prop - The property whose domain to build
	 * @throws NotUsuablePropertyException - If the property is not an OWL class description
	 */
	@SuppressWarnings("unchecked")
	protected DomainPair buildDomain( OWLObjectProperty prop) throws NotUsuablePropertyException {
	
	  // Build the disjunction of all classes that are defined in terms of role restrictions
	  // on this class.
		DomainPair disjunctDomain = super.buildDomain(prop);
	    
		// Create an empty "Thing" class class
		OWLNamedClass namedDomain = model.getOWLThingClass();
		
		// for each class in the model
		Iterator classes = model.listOWLNamedClasses();
		while (classes.hasNext())
		{
			try {
			  // get the next class from the list
				OWLNamedClass onc = (OWLNamedClass) classes.next();
				
				// determine the subsumption relationship between the current 
				// class (onc) and the current property description
				if (reasoner.isSubsumedBy(disjunctDomain.computed, onc))
				{
				  // if the current class is more specific than the computed domain
				  // and the current class is less specific than the named domain
				  // then set the namedDomain to the current class
					if (reasoner.isSubsumedBy(onc, namedDomain)) 
						namedDomain = onc;
				}
			}
			catch(ProtegeReasonerException E)
			{
				;
			}
		}
		
		// set the computed domain to the named / computed domain
		disjunctDomain.computed = namedDomain;
	
		return disjunctDomain;
	}

	
	/**
   * Builds a range for the given property by computing the least common named subsumer (LCNS).
   * 
   * @param prop - The property whose domain to build
   * @throws NotUsuablePropertyException - If the property is not an OWL class description
   */
	@SuppressWarnings("unchecked")
	protected RangePair buildRange( OWLObjectProperty prop) throws NotUsuablePropertyException {
	
		RangePair disjunctRange = super.buildRange(prop);
	    
		OWLNamedClass namedRange = model.getOWLThingClass();
		
		Iterator classes = model.listOWLNamedClasses();
		while (classes.hasNext())
		{
			try {
				OWLNamedClass onc = (OWLNamedClass) classes.next();
				if (reasoner.isSubsumedBy(disjunctRange.computed, onc))
				{
					if (reasoner.isSubsumedBy(onc, namedRange))
						namedRange = onc;
				}
			}
			catch(ProtegeReasonerException E)
			{
				;
			}
		}
		
	
		disjunctRange.computed = namedRange;
	
		return disjunctRange;
	}
	
}
