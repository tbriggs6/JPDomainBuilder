package explorer;

import java.util.Iterator;

import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLIntersectionClass;
import edu.stanford.smi.protegex.owl.model.OWLUnionClass;
import edu.stanford.smi.protegex.owl.model.RDFResource;


public class ResourcePair {

	OWLIntersectionClass original;
	OWLClass computed;
	boolean covered;
	
	
	public ResourcePair(OWLIntersectionClass original, OWLClass computed, boolean covered) {
		
		this.computed = computed;
		this.covered = covered;
		this.original = original;
	}


	public boolean isCovered( )
	{
		return covered;
	}
	
	public String toString( )
	{
		StringBuffer buff = new StringBuffer( );
		buff.append("original: ");
		Iterator oic = original.listOperands();
		while (oic.hasNext())
		{
			RDFResource C = (RDFResource) oic.next();
			buff.append(" ");
			buff.append(C.getLocalName());
		}
		
		buff.append("\ncomputed: ");
		if (computed instanceof OWLUnionClass) {
			Iterator ouc = ((OWLUnionClass) computed).listOperands();
			while (ouc.hasNext())
			{
				RDFResource C = (RDFResource) ouc.next();
				buff.append(" ");
				buff.append(C.getLocalName());
			}
		}
		else
		{
			buff.append(computed.getLocalName());
		}
		buff.append("\n");
		
		return buff.toString();
	}
}
