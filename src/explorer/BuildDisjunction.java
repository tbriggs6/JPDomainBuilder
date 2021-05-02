package explorer;

import java.util.Collection;
import java.util.Iterator;

import edu.ship.thb.swoogle.Results;
import edu.stanford.smi.protegex.owl.inference.reasoner.ProtegeReasoner;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.OWLRestriction;
import edu.stanford.smi.protegex.owl.model.RDFResource;

public class BuildDisjunction extends BuilderBase {

	
	public BuildDisjunction(GeneratorType type, OWLModel model, ProtegeReasoner reasoner,
			Results results) {
		super(type, model, reasoner, results);
		// TODO Auto-generated constructor stub
	}

	
	@SuppressWarnings("unchecked")
	protected DomainPair buildDomain( OWLObjectProperty prop) throws NotUsuablePropertyException {
	
		boolean covered = false;
		
		if (! isUsuableProperty(prop))
			throw new NotUsuablePropertyException(prop);
			
		Collection<OWLClass> origDomainList = prop.getDomains(false);
		if (origDomainList.size() == 0) {
			origDomainList = new OWLClassList( );
			origDomainList.add(model.getOWLThingClass());
		}
		
		OWLClassList classesWithRestr = new OWLClassList( );
		 
        /*
         * Iterate over the model's restrictions on this property.  For
         * each of these properties, we will find those classes that are
         * (indirect) subclasses of this restriction. 
         */
		Iterator<?> I = model.getOWLRestrictionsOnProperty(prop).iterator();
	    while(I.hasNext())
	    {
	        /*
	         * For each restriction, find those classes that are subclasses
	         * on the property.  Apparently we have to enumerate each of the
	         * classes in the model and use (class) .getRestriction( );
	         */
	            OWLRestriction restr = (OWLRestriction) I.next();
	            if (restr.isSystem()) continue;
	            
	            classesWithRestr.addAll(findClassesWithRestriction(restr));
	            
	            covered = true;
	     }
	    
	    if (classesWithRestr.size() == 0)
	    	classesWithRestr.add(model.getOWLThingClass());
	    
	    /*
	     * build the domain pair - original and new domain list.
	     */
	    return new DomainPair(buildIntersectionClass(origDomainList), buildUnionClass(classesWithRestr), covered);
	}
	
	protected RangePair buildRange( OWLObjectProperty prop) throws NotUsuablePropertyException {
		
		if (! isUsuableProperty(prop))
			throw new NotUsuablePropertyException(prop);
			
		
		boolean covered = false;
		
		Collection<OWLClass> origRangeList = prop.getRanges(false);
		
		if (origRangeList.size() == 0) {
			origRangeList = new OWLClassList( );
			origRangeList.add(model.getOWLThingClass());
		}
		
		OWLClassList classesWithRestr = new OWLClassList( );
		
        /*
         * Iterate over the model's restrictions on this property.  For
         * each of these properties, we will find those classes that are
         * (indirect) subclasses of this restriction. 
         */
		Iterator<?> I = model.getOWLRestrictionsOnProperty(prop).iterator();
	    while(I.hasNext())
	    {
	        /*
	         * For each restriction, find those classes that are subclasses
	         * on the property.  Apparently we have to enumerate each of the
	         * classes in the model and use (class) .getRestriction( );
	         */
	            OWLRestriction restr = (OWLRestriction) I.next();
	            if (restr.isSystem()) continue;
	            
	            try {
					OWLClass rsrc = findClassesRefedbyRestriction(restr);
					if (rsrc == null) continue;
					classesWithRestr.add(rsrc);
				} catch (BuilderException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            
	            covered = true;
	     }
	    
	    if (classesWithRestr.size() == 0)
	    	classesWithRestr.add(model.getOWLThingClass());
	    
	    /*
	     * build the domain pair - original and new domain list.
	     */
	    return new RangePair(buildIntersectionClass(origRangeList), buildUnionClass(classesWithRestr), covered);
	}
	
	
	
}
