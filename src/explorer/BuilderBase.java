package explorer;
import java.util.Collection;
import java.util.Iterator;

import org.semanticweb.owl.model.OWLPropertyRange;
import org.semanticweb.owl.model.OWLQuantifiedRestriction;


import edu.ship.thb.swoogle.PropertyRelationship;
import edu.ship.thb.swoogle.ResultProperty;
import edu.ship.thb.swoogle.Results;
import edu.stanford.smi.protegex.owl.inference.reasoner.ProtegeReasoner;
import edu.stanford.smi.protegex.owl.inference.reasoner.exception.ProtegeReasonerException;
import edu.stanford.smi.protegex.owl.model.OWLAllValuesFrom;
import edu.stanford.smi.protegex.owl.model.OWLCardinality;
import edu.stanford.smi.protegex.owl.model.OWLCardinalityBase;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLExistentialRestriction;
import edu.stanford.smi.protegex.owl.model.OWLHasValue;
import edu.stanford.smi.protegex.owl.model.OWLIntersectionClass;
import edu.stanford.smi.protegex.owl.model.OWLMaxCardinality;
import edu.stanford.smi.protegex.owl.model.OWLMinCardinality;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.OWLRestriction;
import edu.stanford.smi.protegex.owl.model.OWLSomeValuesFrom;
import edu.stanford.smi.protegex.owl.model.OWLUnionClass;
import edu.stanford.smi.protegex.owl.model.RDFObject;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;


public abstract class BuilderBase extends Task {

	GeneratorType type;
	
	public BuilderBase(GeneratorType type, OWLModel model, ProtegeReasoner reasoner, Results results) {
		super(model, reasoner, results);
		
		this.type = type;
	}

	@Override
	public void run() {
		
		Iterator<?> rdfprops = model.listRDFProperties();
		
		while (rdfprops.hasNext())
		{
			RDFProperty rprop = (RDFProperty) rdfprops.next();
			
			if (rprop.isSystem()) continue;
			
			if (rprop.isAnonymous()) continue;
			
			if (rprop.isAnnotationProperty()) continue;
			
			if (rprop instanceof OWLObjectProperty) {

				ResultProperty propResult = results.getProperty(rprop.getBrowserText());

				// extract information out of original domain / range
				RDFSClass origDomain = rprop.getDomain(false);
				RDFResource origRange = rprop.getRange(false);
				
				propResult.setOWLProperty(rprop);
				
				if (origDomain != null)
					propResult.setDomainOrig(origDomain.getName());
				else
					propResult.setDomainOrig(null);
				
				if (origRange != null)
					propResult.setRangeOrig(origRange.getName());
				else
					propResult.setRangeOrig(null);
				
				
				try {
					storeDomainResults(rprop, propResult);
					storeRangeResults(rprop, propResult);
				} catch (NotUsuablePropertyException e) {
					System.err.println("cannot compute domain/range for : " + e.getProperty( ));
				}
				
			}

		}
	
	}

	private void storeRangeResults(RDFProperty rprop, ResultProperty propResult) throws NotUsuablePropertyException {
		
		RangePair pair = buildRange( (OWLObjectProperty) rprop);
		
		PropertyRelationship rel = computeProperyRelationship(rprop, pair);
		

		switch(type)
		{
		case DISJUNCTION:
			propResult.setRngOrigDisj(rel);
			propResult.setRangeDisj(unionClassToString(pair.computed));
			propResult.setRangeDisj(pair.computed);
			break;
		case LCNS:
			propResult.setRngOrigLCNS(rel);
			propResult.setRangeLCNS(unionClassToString(pair.computed));
			propResult.setRangeLCNS(pair.computed);
			break;
		case VIVIFICATION:
			propResult.setRngOrigVivf(rel);
			propResult.setRangeVivf(unionClassToString(pair.computed));
			propResult.setRangeVivf(pair.computed);
			break;
		}
	}
	
	
	private void storeDomainResults(RDFProperty rprop, ResultProperty propResult) throws NotUsuablePropertyException {
	
		DomainPair pair = buildDomain( (OWLObjectProperty) rprop);
		
		PropertyRelationship rel = computeProperyRelationship(rprop, pair);
		

		switch(type)
		{
		case DISJUNCTION:
			propResult.setDomOrigDisj(rel);
			propResult.setDomainDisj(unionClassToString(pair.computed));
			propResult.setDomainDisj(pair.computed);
			break;
		case LCNS:
			propResult.setDomOrigLCNS(rel);
			propResult.setDomainLCNS(unionClassToString(pair.computed));
			propResult.setDomainLCNS(pair.computed);
			break;
		case VIVIFICATION:
			propResult.setDomOrigVivf(rel);
			propResult.setDomainVivf(unionClassToString(pair.computed));
			propResult.setDomainVivf(pair.computed);
			break;
		}
	}

	private PropertyRelationship computeProperyRelationship(RDFProperty rprop, ResourcePair pair) {
		PropertyRelationship rel;
		try {
			
			if (!rprop.isDomainDefined())
			{
				if (!pair.covered)
				{
					rel = PropertyRelationship.ORIG_NULL_NOT_COVERED;
				}
				else if ((pair.computed == null) || (pair.computed.equalsStructurally(model.getOWLThingClass())))
				{
					rel = PropertyRelationship.ORIG_NULL_GEN_NULL;
				}
				else if (pair.computed instanceof OWLClass) {
					
					OWLClass oc = (OWLClass) pair.computed;
					if (reasoner.isSubsumedBy(model.getOWLThingClass(), oc))
					{
						rel = PropertyRelationship.ORIG_NULL_GEN_NULL;
					}
					else
					{
						rel = PropertyRelationship.ORIG_NULL_GEN_NOT;
					}
				}
				else {
					rel = PropertyRelationship.UNKNOWN;
				}
			}
			else 
			{
				if (!pair.isCovered())
				{
					rel = PropertyRelationship.ORIG_NOT_NOT_COVERED;
				}
				else if ((pair.computed == null) || (pair.computed.equalsStructurally(model.getOWLThingClass())))
				{
					rel = PropertyRelationship.ORIG_NOT_GEN_NULL;
				}
				else if (pair.computed instanceof OWLClass) 
				{
					OWLClass oc = (OWLClass) pair.computed;
				
					if (reasoner.isSubsumedBy(model.getOWLThingClass(),oc))
					{
						rel = PropertyRelationship.ORIG_NOT_GEN_NULL;
					}
					else if (reasoner.isSubsumedBy(oc, pair.original))
					{
						if (reasoner.isSubsumedBy(pair.original, oc))
							rel = PropertyRelationship.GEN_EQUIV_ORIG;
						else
							rel = PropertyRelationship.GEN_SUBSETEQ_ORIG;
					}
					else
					{
						if (reasoner.isSubsumedBy(pair.original, oc))
							rel = PropertyRelationship.ORIG_SUBSETEQ_GEN;
						else
							rel = PropertyRelationship.UNKNOWN;
					}
				}
				else {
					rel = PropertyRelationship.UNKNOWN;
				}
			}
		} catch (ProtegeReasonerException e) {
			rel = PropertyRelationship.ERROR;
		}
		return rel;
	}
	
	@SuppressWarnings("unchecked")
	abstract protected DomainPair buildDomain( OWLObjectProperty prop) throws NotUsuablePropertyException;

	@SuppressWarnings("unchecked")
	abstract protected RangePair buildRange(OWLObjectProperty prop) throws NotUsuablePropertyException;
	
	
	OWLIntersectionClass buildIntersectionClass(Collection<OWLClass> list)
	{

		OWLIntersectionClass oic = model.createOWLIntersectionClass();
		for (OWLClass C : list)
		{
			oic.addOperand(C);
		}
        
		return oic;
	}
	
	OWLUnionClass buildUnionClass(Collection<OWLClass> list)
	{
		OWLUnionClass ouc = model.createOWLUnionClass();
		for (OWLClass C : list)
		{
//			OWLClass inModel = model.getOWLNamedClass(C.getName());
//			ouc.addOperand(inModel);
			ouc.addOperand(C);
		}

		return ouc;
	}
	
	
	
    /**
     * Enumerate all named classes, build a list of classes that are defined with
     * the given restriction. 
     * 
     * TODO Evaluate whether anonymous classes should be included in this output 
     * 
     * @param restr the restriction to search for
     * @return the list of classes with the given restriction
     * @see {@link OWLClassList}
     */
    protected OWLClassList findClassesWithRestriction(OWLRestriction restr) {
        
        // create the owl class list
        OWLClassList list = new OWLClassList( );
        
        // iterate over the list of named classes
        Iterator<?> J = model.listOWLNamedClasses();
        
        while (J.hasNext())   {
            
            // find a candidate class C
            OWLNamedClass candidate = (OWLNamedClass) J.next();
            
            // iterate over the candidates restrictions
            Iterator<?> K = candidate.getRestrictions().iterator();
            while (K.hasNext()) {
                
                Object candidateRestr = K.next();
                
                // compare the candidate restriction to the given restriction
                if ((candidateRestr == restr) || restr.equalsStructurally((RDFObject) candidateRestr))
                    // only put the list in once
                    //TODO the uniqueness constraint may not need to be here
                    if (!list.contains(candidate))
                        list.add(candidate);
            }
        }
        
        // return the list of classes
        return list;
    }
    
   
    /**
     * Enumerate all named classes, build a list of classes that are defined with
     * the given restriction. 
     * 
     * TODO Evaluate whether anonymous classes should be included in this output 
     * 
     * @param restr the restriction to search for
     * @return the list of classes with the given restriction
     * @throws BuilderException 
     * @see {@link OWLClassList}
     */
    
	protected OWLClass findClassesRefedbyRestriction(OWLRestriction restr) throws BuilderException 
    {
		Object rsrc;
		
    	if (restr instanceof OWLCardinality)
    	{
    		OWLCardinality oc = (OWLCardinality) restr;
    		
    		if (oc.isQualified())
    		{
    			RDFSClass c = oc.getQualifier();
    			rsrc = c;
    		}
    		else
    			rsrc = null;
    	}
    	else if (restr instanceof OWLMinCardinality)
    	{
    		OWLMinCardinality oc = (OWLMinCardinality) restr;
    		
    		if (oc.isQualified())
    		{
    			RDFSClass c = oc.getQualifier();
    			rsrc = c;
    		}
    		else
    			rsrc = null;
    	}
    	else if (restr instanceof OWLMaxCardinality)
    	{
    		OWLMaxCardinality oc = (OWLMaxCardinality) restr;
    		
    		if (oc.isQualified())
    		{
    			RDFSClass c = oc.getQualifier();
    			rsrc = c;
    		}
    		else
    			rsrc = null;
    	}
    	else if (restr instanceof OWLHasValue)
    	{
    		return null;
    	}
    	else if (restr instanceof OWLSomeValuesFrom)
    	{
    		OWLSomeValuesFrom osv = (OWLSomeValuesFrom) restr;
    		rsrc = osv.getFiller();
    	}
    	else if (restr instanceof OWLAllValuesFrom)
    	{
    		OWLAllValuesFrom avf = (OWLAllValuesFrom) restr;
    		rsrc = avf.getAllValuesFrom();
    	}
    	else
    	{
    		throw new BuilderException("Error: unknown restricton type: " + restr);
    	}
    	
    	if (rsrc instanceof OWLClass)
    		return (OWLClass) rsrc;
    	else
    		return null;
    }
    
    
    protected String unionClassToString(OWLClass union)
    {
    	if (union == null) return "owl:Thing";
    	
    	StringBuffer buff = new StringBuffer( );
    	
    	if (union instanceof OWLUnionClass)
    	{
    		OWLUnionClass ouc = (OWLUnionClass) union;
    		Iterator I = ouc.listOperands();
    		while (I.hasNext()) {
    			OWLClass C = (OWLClass) I.next();
    			if (buff.length() > 0) buff.append(",");
    			buff.append(C.getLocalName());
    		}
    	}
    	else {
    		buff.append(union.getLocalName());
    	}
    	
    	return buff.toString();
    }

	public boolean isUsuableProperty(OWLObjectProperty prop) {
		for (Object O : prop.getDomains(false))
		{
			if (!(O instanceof OWLClass)) 
				return false;
		}
		
		for (Object O : prop.getRanges(false))
		{
			if (!(O instanceof OWLClass)) 
				return false;
		}
		
		return true;
	}
}
