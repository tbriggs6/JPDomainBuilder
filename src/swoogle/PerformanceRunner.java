package swoogle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import org.mindswap.pellet.jena.PelletReasoner;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.ship.thb.swoogle.PropertyRelationship;
import edu.ship.thb.swoogle.ResultProperty;
import edu.ship.thb.swoogle.Results;
import edu.ship.thb.swoogle.WorkUnit;
import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protegex.owl.inference.reasoner.ProtegeReasoner;
import edu.stanford.smi.protegex.owl.inference.reasoner.exception.ProtegeReasonerException;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNAryLogicalClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import explorer.BuildDisjunction;
import explorer.BuildVivified;
import explorer.BuilderBase;
import explorer.BuildNamed;
import explorer.GeneratorType;
import explorer.ModelFactory;
import explorer.SimpleCounts;

public class PerformanceRunner {

	GeneratorType type;

	/**
	 * Based on SwoogleRunner - this will run the given work unit to determine
	 * how long it takes to build and reason with the given work unit.
	 * 
	 * @param wunit
	 * @return
	 */
	public long runJob(GeneratorType type, WorkUnit wunit) {
		int idurl = -1;
		String url = "";

		this.type = type;
		// Timer watchDog = new Timer();

		long start = System.currentTimeMillis();

		try {
			url = wunit.getUrl();
			idurl = wunit.getIdurl();
			if (url.contains("www.hackcraft.net"))
				throw new RuntimeException("hack craft...");

			// set a timer for this job...
			// watchDog.schedule(new Alarm(idurl), TIMEOUT);

			OWLModel model;
			if (url.startsWith("file:")) {
				model = ModelFactory.loadFromFile(url.substring(5), url);
			} else {
				model = ModelFactory.loadFromURI(url);
			}

			start = System.currentTimeMillis();

			Results results = new Results();

			ProtegeReasoner reasoner = ModelFactory.createReasonerForModel(
					model, true);
			
			applyReasoner(model, reasoner);

			BuilderBase builder = null;

			if (type != null) {
				switch (type) {
				case DISJUNCTION:
					builder = new BuildDisjunction(type, model, reasoner,
							results);
					break;
				case LCNS:
					builder = new BuildNamed(model, reasoner, results);
					break;
				case VIVIFICATION:
					builder = new BuildVivified(model, reasoner, results);
				}

				builder.run();
				
				setConstraints(model, results);
			}
			
			
			applyReasoner(model, reasoner);
			
		} catch (Throwable E) {
			E.printStackTrace();
			System.exit(10);
		}

		long end = System.currentTimeMillis();

		System.out.println("Time: " + (end - start) + " ms");

		return (end - start);
	}

	private void applyReasoner(OWLModel model, ProtegeReasoner reasoner)
			throws ProtegeReasonerException {

		reasoner.setAutoSynchronizationEnabled(true);
		reasoner.classifyTaxonomy();
		reasoner.computeInferredHierarchy();
		reasoner.computeInferredIndividualTypes();
		reasoner.computeInconsistentConcepts();

	}

	private boolean isThing(OWLModel model, OWLClass C) {
		if (C == null)
			return true;
		if (C.equalsStructurally(model.getOWLThingClass()))
			return true;

		if (C instanceof RDFList) {

			RDFList list = (RDFList) C;
			ExtendedIterator I = list.iterator();
			while (I.hasNext()) {
				Object O = I.next();
				if (O instanceof RDFResource) {
					if (!((RDFResource) O).equalsStructurally(model
							.getOWLThingClass()))
						return false;
				}
			}
			return true;
		}

		if (C instanceof OWLNAryLogicalClass) {
			OWLNAryLogicalClass list = (OWLNAryLogicalClass) C;
			Iterator<RDFSClass> iterator = list.getOperands().iterator();
			while (iterator.hasNext()) {
				RDFSClass O = iterator.next();
				if (!O.equalsStructurally(model.getOWLThingClass()))
					return false;
			}
			return true;
		}

		return false;
	}

	public void setConstraints(OWLModel model, Results results) {
		LinkedList<ResultProperty> props = results.getProperties();
		for (ResultProperty P : props) {
			OWLClass domain;
			OWLClass range;

			switch (type) {
			case LCNS:
				domain = (OWLClass) P.getDomainLCNSObject();
				range = (OWLClass) P.getRangeLCNSObject();
				break;
			case VIVIFICATION:
				domain = (OWLClass) P.getDomainVivfObject();
				range = (OWLClass) P.getRangeVivfObject();
				break;
			case DISJUNCTION:
				domain = (OWLClass) P.getDomainDisjObject();
				range = (OWLClass) P.getRangeDisjObject();
				break;
			default:
				domain = null;
				range = null;
				break;
			}

			OWLObjectProperty property = model.getOWLObjectProperty(P
					.getProperty());
			if (!isThing(model, domain)) {
				property.setDomain(domain);
			}

			if (!isThing(model, range)) {
				property.setRange(range);
			}

		}
	}

	public static void main(String args[]) {

		PerformanceRunner runner = new PerformanceRunner();

		GeneratorType type = null;

//		if (args[0].equals("none"))
//			type = null;
//		else if (args[0].equals("disj"))
//			type = GeneratorType.DISJUNCTION;
//		else if (args[0].equals("vivf"))
//			type = GeneratorType.VIVIFICATION;
//		else if (args[0].equals("lcns"))
//			type = GeneratorType.LCNS;
//
//		WorkUnit unit = new WorkUnit(args[1], args[2]);
		WorkUnit unit = new WorkUnit(""+2913284, "http://www.fruitfly.org/~cjm/obol/export/plant_anatomy.owl");
		type = GeneratorType.LCNS;
		runner.runJob(type, unit);
	}
}
