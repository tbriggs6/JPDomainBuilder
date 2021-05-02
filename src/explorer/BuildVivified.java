package explorer;

/**
 * BuildVivifiedDomain - 
 * 	Process domains in a given ontology and compute and save vivified domain entries.
 * A Vivified domain is a disjunction of classes with some degree of simplification. 
 * When the absorption criteria is met, a portion of the disjunction is replaced by 
 * its super-class.  This process is repeated until the domain is fully vivified.
 *
 *	The absorption criteria is triggered when some percentage of sub-classes of a 
 * common super-class are present in the disjunction list; and this percentage is 
 * greater than or equal to the beta parameter.
 * 
 * @author Tom Briggs
 */
import java.util.Collection;
import java.util.LinkedList;

import edu.ship.thb.swoogle.Results;
import edu.stanford.smi.protegex.owl.inference.reasoner.ProtegeReasoner;
import edu.stanford.smi.protegex.owl.inference.reasoner.exception.ProtegeReasonerException;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.OWLUnionClass;
import edu.stanford.smi.protegex.owl.model.RDFSClass;

public class BuildVivified extends BuildDisjunction {

	/**
	 * beta controls the absorption criteria.  0 <= beta <= 1.
	 * A value of 0 is equivalent to the disjunction (though more expensive to compute),
	 * while a value of 1 is equivalent to the least-common named subsumer.
	 */
	public static final double beta = 0.6;

	
	/**
	 * AbsoprtionEntry -
	 * 	A helper class to store a record of an absorption.  An absorption record tracks
	 * the current, absorbed super-class of a member of the domain disjunction.  This record
	 * is used to create the final summary of the domain for a given property.
	 * 
	 * @author Tom Briggs
	 */
	private class AbsorptionEntry {
		OWLNamedClass dclass;
		OWLNamedClass sclass;
		boolean used;
	}
	
	/**
	 * AbsorptionList - 
	 * 	A list of absortion entries.
	 * @author Tom Briggs
	 *
	 */
	@SuppressWarnings("serial")
	private class AbsorptionList extends LinkedList<AbsorptionEntry> { };
	
	/**
	 * ClassList -
	 * 	A list of OWL named classes.
	 * @author Tom Briggs
	 *
	 */
	@SuppressWarnings("serial")
	private class ClassList extends LinkedList<OWLNamedClass> { };
	
	public BuildVivified(OWLModel model, ProtegeReasoner reasoner, Results results) {
		super(GeneratorType.VIVIFICATION, model, reasoner, results);
	}
	
	
	/**
	 * DomainPair -
	 * 	Build a domain for the given OWL object property.  The domain built will be 
	 * vivified based on the structure of the given super.model.  The computed domain
	 * will be returned as a {@link DomainPair}, giving the original domain and the 
	 * computed domain.  
	 * 
	 * @param OWLObjectProperty prop
	 * @return DomainPair
	 * @throws NotUsuablePropertyException 
	 */
	@Override
	protected DomainPair buildDomain(OWLObjectProperty prop) throws NotUsuablePropertyException 
	{
		
		if (! isUsuableProperty(prop))
			throw new NotUsuablePropertyException(prop);
		
		// compute the disjunction domain of the given property
		//TODO verify that the domains created here are compatible and don't create copies in the OWL model
		DomainPair disjunctDomain = super.buildDomain(prop);
		
		try {
			
			// convert the disjunction to a class list
			ClassList list = buildResourcePairList(disjunctDomain);
			
			// compute the default absorption list for the class list
			AbsorptionList alist = buildMap(list);
			
			// the next super class to operate on
			OWLNamedClass nextSuper;
			
			// while there are more super classes, get the next super class from the absorption list
			while( (nextSuper = getNextSuperClass(alist)) != null)
			{
				// mark the current super class as used - to avoid infinite looping
				setSuperClassUsed(alist, nextSuper);
				
				// if the super-class meets the absorption criteria, perform absorption
				if (isAbsorptionCriteria(alist, nextSuper))
				{
					// this super class meets the absorption criteria, so replace all subsumed entries with it
					replaceClassList(list, nextSuper);
					replaceAbsorptionList(alist, nextSuper);
					
					addAbsorptionEntry(alist, nextSuper);
					list.add(nextSuper);
				}
			}
			
			// the disjunction list has been subsumed at this point
			// display the some debugging information
			System.out.format("Computed domain of %s: (", prop.getLocalName());
			for(OWLNamedClass C : list)
			{
				System.out.format("%s:%s ", C.getNamespacePrefix(),C.getName());
			}
			System.out.println(")");
		
			disjunctDomain.computed = buildUnionClass(list);
		}
		catch(ProtegeReasonerException E)
		{
			// something bad happened, so....
			disjunctDomain.computed = null;
		}
		
		return disjunctDomain;
	}
	
	protected RangePair buildRange(OWLObjectProperty prop) throws NotUsuablePropertyException 
	{
		
		if (! isUsuableProperty(prop))
			throw new NotUsuablePropertyException(prop);
		
		// compute the disjunction domain of the given property
		//TODO verify that the domains created here are compatible and don't create copies in the OWL model
		RangePair disjunctRange = super.buildRange(prop);
		
		try {
			
			// convert the disjunction to a class list
			ClassList list = buildResourcePairList(disjunctRange);
			
			// compute the default absorption list for the class list
			AbsorptionList alist = buildMap(list);
			
			// the next super class to operate on
			OWLNamedClass nextSuper;
			
			// while there are more super classes, get the next super class from the absorption list
			while( (nextSuper = getNextSuperClass(alist)) != null)
			{
				// mark the current super class as used - to avoid infinite looping
				setSuperClassUsed(alist, nextSuper);
				
				// if the super-class meets the absorption criteria, perform absorption
				if (isAbsorptionCriteria(alist, nextSuper))
				{
					// this super class meets the absorption criteria, so replace all subsumed entries with it
					replaceClassList(list, nextSuper);
					replaceAbsorptionList(alist, nextSuper);
					
					addAbsorptionEntry(alist, nextSuper);
					list.add(nextSuper);
				}
			}
			
			// the disjunction list has been subsumed at this point
			// display the some debugging information
			System.out.format("Computed range of %s: (", prop.getLocalName());
			for(OWLNamedClass C : list)
			{
				System.out.format("%s ", C.getName());
			}
			System.out.println(")");
		
			disjunctRange.computed = buildUnionClass(list);
		}
		catch(ProtegeReasonerException E)
		{
			// something bad happened, so....
			disjunctRange.computed = null;
		}
		
		return disjunctRange;
	}
	
	
	
	OWLUnionClass buildUnionClass(ClassList list)
	{
		OWLUnionClass ouc = model.createOWLUnionClass();
		for (OWLClass C : list)
		{
			OWLClass inModel = model.getOWLNamedClass(C.getName());
			ouc.addOperand(inModel);
		}

		return ouc;
	}
	
	
	
	private OWLNamedClass getNextSuperClass(AbsorptionList list)
	{
		for (AbsorptionEntry entry : list)
		{
			if (entry.used == false) return entry.sclass;
		}
		
		return null;
	}
	
	private void setSuperClassUsed(AbsorptionList list, OWLNamedClass sclass)
	{
		for (AbsorptionEntry entry : list)
		{
			if (entry.sclass.getName().equals(sclass.getName())) 
				entry.used = true;
		}
	}
	

	private ClassList buildResourcePairList(ResourcePair pair)
	{
		ClassList list = new ClassList( );
		
		if (pair.computed instanceof OWLUnionClass)
		{
			OWLUnionClass OUC = (OWLUnionClass) pair.computed;
			Collection<RDFSClass> ops = OUC.getOperands();
			for (RDFSClass C : ops)
			{
				if (C instanceof OWLNamedClass)
					list.add((OWLNamedClass) C);
			}
		}
		else if (pair.computed instanceof OWLNamedClass)
		{
			list.add((OWLNamedClass) pair.computed);
			
		}
		
		return list;
	}
	
	
	@SuppressWarnings("unchecked")
	private AbsorptionList buildMap(ClassList disjunction)
	{
		AbsorptionList list = new AbsorptionList( );
		// for each class in the disjunction
		for(OWLNamedClass C : disjunction) 
	
			addAbsorptionEntry(list, C);
		
		return list;
	}


	private void addAbsorptionEntry(AbsorptionList list, OWLNamedClass C) {
		Collection supers = C.getSuperclasses(false);
		
		for(Object O : supers) 
		{
			if (!(O instanceof OWLNamedClass)) continue;
			
			OWLNamedClass S = (OWLNamedClass) O;
			if (S.isAnonymous()) continue;
			if (S.isSystem()) continue;
			
			AbsorptionEntry E = new AbsorptionEntry( );
			E.dclass = C;
			E.sclass = S;
			E.used = false;
			
			list.add(E);
		}
	}
	
	private void replaceClassList(ClassList L, OWLNamedClass S) throws ProtegeReasonerException
	{
		ClassList L2 = new ClassList( );
		
		for (OWLNamedClass C : L)
		{
			if (reasoner.isSubsumedBy(C, S))
				L2.add(C);
		}
		
		L.removeAll(L2);
	}
	
	private void replaceAbsorptionList(AbsorptionList A, OWLNamedClass S) throws ProtegeReasonerException 
	{
		AbsorptionList L2 = new AbsorptionList( );
		
		for (AbsorptionEntry E : A)
		{
			// note - it is intentional that we scan super class, not child class
			if (reasoner.isSubsumedBy(E.sclass, S))
			{
				L2.add(E);
			}
		}
		
		A.removeAll(L2);
	}
	
	private boolean isAbsorptionCriteria(AbsorptionList A, OWLNamedClass S) throws ProtegeReasonerException
	{
		int m = getNumAbsorptionClasses(A,S);
		int n = getNumSubClasses(S);
		float c = (float) m / (float) n;
		
//		System.err.format("absorption: %s : %d %d %f %f\n", S.getName(), m,n, c, beta);
		return ( c >= beta );
	}
	
	private int getNumAbsorptionClasses(AbsorptionList A, OWLNamedClass S) throws ProtegeReasonerException
	{
		int count = 0;
		
		for (AbsorptionEntry E : A)
		{
			if (reasoner.isSubsumedBy(S, E.sclass))
				count++;
		
		}

		return count;
	}
	
	
	@SuppressWarnings("unchecked")
	private int getNumSubClasses(OWLClass C)
	{
		int count = 0;
		Collection subs = C.getSubclasses(false);
		for(Object O : subs)
		{
			if (! (O instanceof OWLNamedClass)) continue;
			
			OWLNamedClass S = (OWLNamedClass) O;
			
			if (S.isAnonymous()) continue;
			if (S.isSystem()) continue;
			
			count++;
		}
		
		return count;
	}
	
	@SuppressWarnings("unchecked")
	private int getNumSuperClasses(OWLClass C)
	{
		int count = 0;
		Collection subs = C.getSuperclasses(false);
		
		for(Object O : subs)
		{
			if (! (O instanceof OWLNamedClass)) continue;
			
			OWLNamedClass S = (OWLNamedClass) O;
			
			if (S.isAnonymous()) continue;
			if (S.isSystem()) continue;
			
			count++;
		}
		
		return count;
	}
}
