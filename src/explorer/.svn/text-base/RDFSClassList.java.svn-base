package explorer;

import java.util.HashSet;


import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.RDFSClass;

public class RDFSClassList extends HashSet<RDFSClass> {

	private static final long serialVersionUID = -2278711018824224188L;
	
	public void copyUniqueClasses(RDFSClassList source) {
        for(RDFSClass  C : source)
        {   
            if (!this.contains(C))
                this.add(C);    
        }
    }

}
