package swoogle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Stack;

import org.semanticweb.owl.model.OWLDataProperty;

import edu.stanford.smi.protegex.owl.model.*;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLComplementClass;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLDataRange;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLIndividual;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLNamedClass;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLObjectProperty;

public class OWLEquals {

	 Class classOrder[] = { 
			OWLOntology.class, 			OWLIndividual.class, 		OWLDataRange.class, 		
			OWLNamedClass.class,		OWLUnionClass.class,		OWLIntersectionClass.class,	
			OWLComplementClass.class,	OWLNAryLogicalClass.class,	OWLLogicalClass.class,		
			OWLDatatypeProperty.class,  OWLObjectProperty.class, 	OWLProperty.class,
			OWLMinCardinality.class,	OWLMaxCardinality.class,	OWLCardinality.class,
			OWLCardinalityBase.class,	OWLAllValuesFrom.class,		OWLSomeValuesFrom.class,
			OWLQuantifierRestriction.class,	OWLHasValue.class,		OWLExistentialRestriction.class,
			OWLRestriction.class,		OWLEnumeratedClass.class,	OWLAnonymousClass.class,
			OWLClass.class,						RDFList.class,
			RDFSClass.class,			RDFSNamedClass.class,		RDFUntypedResource.class,
			RDFProperty.class,			RDFResource.class, RDFIndividual.class
		};

	HashSet<Object> aclosed, bclosed;

	 
	public OWLEquals( )
	{
		aclosed = new HashSet<Object>( );
		bclosed = new HashSet<Object>( );
	}
	 
	@SuppressWarnings("unchecked")
	public  boolean owlEquals( Object A, Object B)
	{
		if (aclosed.contains(A)) return true;
		if (bclosed.contains(B)) return true;
		
		aclosed.add(A);
		bclosed.add(B);
		
		// get these out of the way!
		if ((A == null) || (B == null)) return false;
		if (A == B) return true;
		if (A.getClass() != B.getClass()) return false;
		
		for (Class C : classOrder)
		{
	
			if (isSameClass(A,B,C))
			{
				try {
					Method m = OWLEquals.class.getMethod("equals", C, C);
					if (m == null)
					{
						System.err.println("Did not find a valid equality check for class " + C.getCanonicalName());
						return false;
					}
					
					Object res;
					res = m.invoke(this, A, B);
					if (res == null) return false;
					
					if (res instanceof Boolean)
					{
						return ((Boolean)res).booleanValue();
					}
					else
					{
						return false;
					}
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
				
			}
		}
	    
		return false;
	}

	public  boolean isSameClass(Object a, Object b, Class c) {
		return a.getClass().equals(b.getClass()) && (c.isInstance(a)) && (c.isInstance(b));
	}
	
	
	public  boolean equalsCollection(Collection rA, Collection rB) {
		if ((rA == null) && (rB == null)) return true;
		else if ((rA != null) & (rB == null)) return false;
		else if ((rA == null) & (rB != null)) return false;
		
		if (rA.size() != rB.size()) return false;
		
		for (Object O : rA)
		{
			boolean found = false;
			for (Object P : rB)
			{
				if (owlEquals(O, P))
				{
					found = true;
					break;
				}
			}
			if (!found) return false;
		}
		
		return true;
	}
	
	public  boolean equals(OWLIndividual A, OWLIndividual B)
	{
		if (!A.getName().equals(B.getName())) return false;
		
		return equalsCollection(A.getInferredTypes(), B.getInferredTypes());
	}
	
	public  boolean equals(OWLDataRange A, OWLDataRange B)
	{
		return equalsCollection(A.getOneOfValues(), B.getOneOfValues());
	}
	
	@SuppressWarnings("unchecked")
	public  boolean equals(OWLNamedClass A, OWLNamedClass B)
	{
		if (A.isAnonymous() != B.isAnonymous()) return false;
		
		if (A.isDefinedClass() != B.isDefinedClass()) return false;
		
		if (! A.getName().equals(B.getName())) return false;
		
//		Collection rA = A.getRestrictions();
//		Collection rB = B.getRestrictions();
//		
//		return equalsCollection(rA, rB);
		
		return equalsCollection(A.getSuperclasses(false), B.getSuperclasses(false)) &&
			equalsCollection(A.getEquivalentClasses(), B.getEquivalentClasses());
	}

	public  boolean equals(OWLUnionClass A, OWLUnionClass B)
	{
		if (A.isAnonymous() != B.isAnonymous()) return false;
		
		if (!A.isAnonymous()) {
			if (A.getName() != B.getName()) return false;
		}
		
		Collection rA = A.getOperands();
		Collection rB = A.getOperands();
		
		return equalsCollection(rA, rB);
	}

	public  boolean equals(OWLIntersectionClass A, OWLIntersectionClass B)
	{
		if (A.isAnonymous() != B.isAnonymous()) return false;
		
		if (!A.isAnonymous()) {
			if (A.getName() != B.getName()) return false;
		}

		Collection rA = A.getOperands();
		Collection rB = A.getOperands();
		
		return equalsCollection(rA, rB);
	}
	
	public  boolean equals(OWLComplementClass A, OWLComplementClass B)
	{
		return owlEquals(A.getComplement() , B.getComplement() );
	}
	
	public  boolean equals(OWLNAryLogicalClass A, OWLNAryLogicalClass B)
	{
		Collection rA = A.getOperands();
		Collection rB = A.getOperands();
		
		return equalsCollection(rA, rB);
		
	}
	
	public  boolean equals(OWLLogicalClass A, OWLLogicalClass B)
	{
		return A.getName().equals(B.getName());
	}
	
	public  boolean equals(OWLDatatypeProperty A, OWLDatatypeProperty B)
	{
		return A.getName().equals(B.getName()) && 
			equalsCollection(A.getDomains(false), B.getDomains(false)) &&
			equalsCollection(A.getDifferentFrom(), B.getDifferentFrom()) &&
			equalsCollection(A.getSameAs(), A.getSameAs());
	}
	
	public  boolean equals(OWLObjectProperty A, OWLObjectProperty B)
	{
		return A.getName().equals(B.getName()) &&
			(A.isTransitive() == B.isTransitive()) &&
			(A.isSymmetric() == B.isSymmetric()) &&
			(A.isFunctional() == B.isFunctional()) &&
			equalsCollection(A.getSuperproperties(true),B.getSuperproperties(true)) &&
			equalsCollection(A.getDomains(false), B.getDomains(false)) &&
			equalsCollection(A.getRanges(false), B.getRanges(false)) &&
			equalsCollection(A.getDifferentFrom(), B.getDifferentFrom()) &&
			equalsCollection(A.getSameAs(), A.getSameAs());
	}
	
	public  boolean equals(OWLProperty A, OWLProperty B)
	{
		return A.getName().equals(B.getName()) && 
			(A.isFunctional() == B.isFunctional()) &&
			equalsCollection(A.getDomains(false), B.getDomains(false)) &&
			equalsCollection(A.getDifferentFrom(), B.getDifferentFrom()) &&
			equalsCollection(A.getSameAs(), A.getSameAs());
	}

	
	public  boolean equals(OWLMinCardinality A, OWLMinCardinality B)
	{
		return A.getCardinality() == B.getCardinality();
	}
	
	public  boolean equals(OWLMaxCardinality A, OWLMaxCardinality B)
	{
		return A.getCardinality() == B.getCardinality();
	}
	

	public  boolean equals(OWLCardinality A, OWLCardinality B)
	{
		return A.getCardinality() == B.getCardinality();
	}
	
	public  boolean equals(OWLCardinalityBase A, OWLCardinalityBase B)
	{
		return A.getCardinality() == B.getCardinality();
	}
	
	public  boolean equals(OWLAllValuesFrom A, OWLAllValuesFrom B)
	{
		return A.getName().equals(B.getName()) && 
			owlEquals(A.getAllValuesFrom(), B.getAllValuesFrom());
	}
	
	public  boolean equals(OWLSomeValuesFrom A, OWLSomeValuesFrom B)
	{
		return A.getName().equals(B.getName()) &&
			owlEquals(A.getSomeValuesFrom(), B.getSomeValuesFrom());
	}
	
	public  boolean equals(OWLQuantifierRestriction A, OWLQuantifierRestriction B)
	{
		return A.getName().equals(B.getName()) && equals(A.getFiller(), B.getFiller());
	}
	
	public  boolean equals(OWLHasValue A, OWLHasValue B)
	{
		
		if (A.isAnonymous() && B.isAnonymous()) {
			return owlEquals(A.getOnProperty(), B.getOnProperty()) &&
			owlEquals(A.getFillerProperty(), B.getFillerProperty());
		}
		return A.getName().equals(B.getName()) &&
			owlEquals(A.getOnProperty(), B.getOnProperty()) &&
			owlEquals(A.getFillerProperty(), B.getFillerProperty());
	}
	
	public  boolean equals(OWLExistentialRestriction A, OWLExistentialRestriction B)
	{
		return A.getName().equals(B.getName()) &&
			owlEquals(A.getOnProperty(), B.getOnProperty()) &&
			owlEquals(A.getFillerProperty(), B.getFillerProperty());
	}
	
	public  boolean equals(OWLRestriction A, OWLRestriction B)
	{
		return A.getName().equals(B.getName()) &&
		owlEquals(A.getOnProperty(), B.getOnProperty());
	}
	
	public  boolean equals(OWLEnumeratedClass A, OWLEnumeratedClass B)
	{
		return A.getName().equals(B.getName()) &&
			owlEquals(A.getOneOf(), B.getOneOf());
	}
	
	public  boolean equals(OWLAnonymousClass A, OWLAnonymousClass B)
	{
		return false;
	}
	
	public  boolean equals(OWLClass A, OWLClass B)
	{
		return A.getName().equals(B.getName());
	}
	
	public  boolean equals(RDFIndividual A, RDFIndividual B)
	{
		if (!A.getName().equals(B.getName())) return false;
		
		return equalsCollection(A.getInferredTypes(), B.getInferredTypes());
	}
	
	public  boolean equals(RDFList A, RDFList B)
	{
		return equalsCollection(A.getValues(), B.getValues());
	}
	
	public  boolean equals(RDFSClass A, RDFSClass B)
	{
		return A.getName().equals(B.getName());
	}
	
	public  boolean equals(RDFSDatatype A, RDFSDatatype B)
	{
		return A.getName().equals(B.getName());
	}
	
	public  boolean equals(RDFSNamedClass A, RDFSNamedClass B)
	{
		return A.getName().equals(B.getName());
	}
	
	public  boolean equals(RDFUntypedResource A, RDFUntypedResource B)
	{
		return A.getName().equals(B.getName());
	}
	
	public  boolean equals(RDFResource A, RDFResource B)
	{
		return A.getName().equals(B.getName());
	}
	
	public  boolean equals(RDFProperty A, RDFProperty B)
	{
		return A.getName().equals(B.getName());
	}
	
	public  boolean equals(OWLOntology A, OWLOntology B)
	{
		return true;
	}
}
