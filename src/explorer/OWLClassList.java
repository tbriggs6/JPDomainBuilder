package explorer;

import java.util.HashSet;


import edu.stanford.smi.protegex.owl.model.OWLClass;

public class OWLClassList extends HashSet<OWLClass> {

	private static final long serialVersionUID = -2278711018824224188L;
	
	public void copyUniqueClasses(OWLClassList source) {
        for(OWLClass  C : source)
        {   
            if (!this.contains(C))
                this.add(C);    
        }
    }

}
