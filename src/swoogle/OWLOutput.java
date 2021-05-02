package swoogle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import org.semanticweb.owl.model.OWLDataProperty;

import edu.stanford.smi.protegex.owl.model.*;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLComplementClass;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLDataRange;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLIndividual;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLNamedClass;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLObjectProperty;

public class OWLOutput {

	static Class classOrder[] = { 
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
	
	@SuppressWarnings("unchecked")
	public static String owlToString( Object A)
	{
		
		// get these out of the way!
		if (A == null) return "";
		
		for (Class C : classOrder)
		{
			if (isSameClass(A,C))
			{
				try {
					Method m = OWLOutput.class.getMethod("owlToString", C);
					if (m == null) {
						System.err.println("Could not find class for " + C.getClass());
					}
					Object res;
					res = m.invoke(null, A);
					
					if (res == null) return "";
					
					if (res instanceof String)
					{
						return ((String) res);
					}
					else
					{
						return "<unknown type>";
					}
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	    
		return "";
	}

	public static boolean isSameClass(Object a, Class c) {
		return (c.isInstance(a));
	}
	
	public String owlToString(OWLOntology O)
	{
		StringBuffer buff = new StringBuffer( );
		buff.append("[ Ontology: " + O.getOntologyURI() );
		
		buff.append("]");
		return buff.toString();
	}
	
	private static String collectionToString(Collection C, String separator)
	{
		StringBuffer buff = new StringBuffer( );
		
		boolean first = true;
		for (Object O : C)
		{
			if (first) first = false;
			else buff.append(separator);
			
			if (O instanceof OWLNamedClass) {
				buff.append(((OWLNamedClass) O).getLocalName());
			}
			else {
				buff.append(owlToString(O));
			}
		}
		return buff.toString();
	}
	
	public static String owlToString(OWLIndividual I)
	{
		StringBuffer buff = new StringBuffer( );
		buff.append("[ Individual: " + I.getLocalName() + "\n");
		boolean first = true;
		for (Object O : I.getInferredTypes())
		{
			if (first) first = false;
			else buff.append(",\n");
			buff.append("     -> ");
			buff.append(owlToString(O));
		}
		buff.append(" ] ");
		
		return buff.toString();
	}
	
	public static String owlToString(OWLDataRange R)
	{
		StringBuffer buff = new StringBuffer( );
		buff.append("[ Datarange: " + R.getLocalName() + " \n");
		buff.append("    one of: ");
		buff.append(collectionToString(R.getOneOfValues(),","));
		buff.append("]");
		return buff.toString();
	}
	
	public static String owlToString(OWLNamedClass C)
	{
		StringBuffer buff = new StringBuffer( );
		buff.append("[ Named Class: " + C.getLocalName() );
		if (C.getEquivalentClasses().size() > 0) {
			buff.append("   \u2263 ");
			buff.append(collectionToString(C.getEquivalentClasses(),",\n     "));
			buff.append("\n");
		}
		if (C.getSuperclasses(true).size() > 0)
		{
			buff.append("   \u2291 ");
			buff.append(collectionToString(C.getSuperclasses(true),",\n       "));
			buff.append("\n");
		}
		buff.append("]");
		return buff.toString();
	}
	
	public static String owlToString(OWLUnionClass C)
	{
		StringBuffer buff = new StringBuffer( );
		buff.append("[ Union: " + C.getLocalName());
		buff.append(collectionToString(C.getOperands(),"\u222a"));
		buff.append("]\n");
		
		return buff.toString();
	}
	
	public static String owlToString(OWLIntersectionClass C)
	{
		StringBuffer buff = new StringBuffer( );
		buff.append("[ Intersection: " + C.getLocalName());
		buff.append(collectionToString(C.getOperands(),"\u2229"));
		buff.append("]\n");
		
		return buff.toString();
		
	}
	
	public static String owlToString(OWLComplementClass C)
	{
		return "[ Complement: \u223C " + owlToString(C.getComplement()) + " ] ";
	}
	
	public static String owlToString(OWLNAryLogicalClass C)
	{
		return "[ NAryLogicalClass: " + collectionToString(C.getOperands(), ",") + "]";
	}
	
	public static String owltoString(OWLLogicalClass C)
	{
		return "[ OWLLogicalClass: " + C.getBrowserText() + " ]";
	}
	
	public static String owlToString(OWLDatatypeProperty P)
	{
		StringBuffer buff = new StringBuffer( );
		
		buff.append("[ Datatype Prop: "  + P.getLocalName() + "\n");
		buff.append("    domains: " );
		buff.append(collectionToString(P.getDomains(false),","));
		buff.append("\n");
		buff.append("   ranges: ");
		buff.append(collectionToString(P.getRanges(false),","));
		buff.append("\n");
		buff.append("   same as: ");
		buff.append(collectionToString(P.getSameAs(),","));
		buff.append("]");
		return buff.toString();
	}
	
	public static String owlToString(OWLObjectProperty P)
	{
		StringBuffer buff = new StringBuffer( );
		
		buff.append("[ Object Prop: "  + P.getLocalName() + "   " );
		if (P.isTransitive()) buff.append(" trans ");
		if (P.isFunctional()) buff.append(" functional ");
		if (P.isSymmetric()) buff.append(" symmetric ");
		if (P.isInverseFunctional()) buff.append(" invfun ");
		buff.append("\n");
		
		buff.append("    domains: " );
		buff.append(collectionToString(P.getDomains(false),","));
		buff.append("\n");
		buff.append("   ranges: ");
		buff.append(collectionToString(P.getRanges(false),","));
		buff.append("\n");
		buff.append("   same as: ");
		buff.append(collectionToString(P.getSameAs(),","));
		buff.append("\n");
		buff.append("   inverse: ");
		buff.append(owlToString(P.getInverseProperty()));
		buff.append("]");
		return buff.toString();
	}

	public static String owlToString(OWLProperty P)
	{
		return "[ Property: " + P.getLocalName() + "]";
	}
	
	public static String owlToString(OWLMinCardinality C)
	{
		return "\u2265" + C.getCardinality();
	}
	
	public static String owlToString(OWLMaxCardinality C)
	{
		return "\u2264" + C.getCardinality();
	}
	
	public static String owlToString(OWLCardinality C)
	{
		return "=" + C.getCardinality();
	}

	public static String owlToString(OWLAllValuesFrom A)
	{
		return "[ all in: " + owlToString(A.getAllValuesFrom()) + " ] ";
	}
	
	public static String owlToString(OWLQuantifierRestriction R) 
	{
		return R.getFillerText();
	}
	
	public static String owlToString(OWLHasValue A)
	{
		return "\u2200" + A.getOnProperty().getLocalName() + "." + A.getFillerText();
	}
	
	public static String owlToString(OWLExistentialRestriction A)
	{
		return "\u2203" + A.getOnProperty().getLocalName() + "." + A.getFillerText();
	}
	
	public static String owlToString(OWLRestriction A)
	{
		return "[ restriction: " + A.getBrowserText() + "." + owlToString(A.getFillerProperty()) + " ]";
	}

	public static String owlToString(OWLEnumeratedClass A)
	{
		return "[ one of: " + collectionToString(A.getOneOf( ), ",") + " ]";
	}
	
	public static String owlToString(OWLAnonymousClass A)
	{
		return "[ anon id: " + A.getBrowserText() + " ]";
	}
	
	public static String owlToString(OWLClass C)
	{
		return "[ class: " + C.getBrowserText() + " ]";
	}
	
	public static String owlToString(OWLSomeValuesFrom S)
	{
		return "[ some: " + owlToString(S.getSomeValuesFrom()) + " ]";
	}
	
	public static String owlToString(RDFIndividual I)
	{
		StringBuffer buff = new StringBuffer( );
		buff.append("[ RDF Individual: " + I.getLocalName() + "\n");
		boolean first = true;
		for (Object O : I.getInferredTypes())
		{
			if (first) first = false;
			else buff.append(",\n");
			buff.append("     -> ");
			buff.append(owlToString(O));
		}
		buff.append(" ] ");
		
		return buff.toString();
	}
	
	public static String owlToString(RDFList L)
	{
		return collectionToString(L.getValues(), ",");
	}
	
	public static String owlToString(RDFSClass C)
	{
		return "[ RClass : " + C.getLocalName() + " ]";
	}

	public static String owlToString(RDFResource R)
	{
		return "[ Resource: " + R.getLocalName( ) + " ]";
	}
	
	public static String owlToString(RDFSNamedClass R)
	{
		return "[ RNamedClass : " + R.getLocalName() + " ]";
	}
	
	public static String owlToString(RDFUntypedResource R)
	{
		if (R == null) return "";
		return "[ RUntyped Resource: " + R.getLocalName( ) + " ] ";
	}
	
	public static String owlToString(RDFProperty P)
	{
		if (P == null) return "";
		return "[ RProperty: " + P.getLocalName() + " ] ";
	}
	
}
