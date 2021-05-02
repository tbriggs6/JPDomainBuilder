package explorer;

import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;

public class NotUsuablePropertyException extends Exception {

	private OWLObjectProperty prop;

	public NotUsuablePropertyException(OWLObjectProperty prop)
	{
		this.prop = prop;	
	}

	public String getProperty() {
		return prop.getBrowserText();
	}
}
