package explorer;

import edu.ship.thb.swoogle.Results;
import edu.stanford.smi.protegex.owl.inference.reasoner.ProtegeReasoner;
import edu.stanford.smi.protegex.owl.model.OWLModel;

public abstract class Task {

	protected OWLModel model;
	protected ProtegeReasoner reasoner;
	protected Results results;
	
	public Task(OWLModel model, ProtegeReasoner reasoner, Results results)
	{
		this.model = model;
		this.reasoner = reasoner;
		this.results = results;
	}
	
	public abstract void run( );
}
