package swoogle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;

import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.ship.thb.swoogle.ResultProperty;
import edu.ship.thb.swoogle.Results;
import edu.ship.thb.swoogle.WorkUnit;
import edu.ship.thb.swoogle.WorkUnitCount;
import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protegex.owl.inference.reasoner.ProtegeReasoner;
import edu.stanford.smi.protegex.owl.inference.reasoner.exception.ProtegeReasonerException;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLIntersectionClass;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNAryLogicalClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.OWLUnionClass;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.triplestore.Triple;
import edu.stanford.smi.protegex.owl.model.triplestore.TripleStore;
import explorer.BuildDisjunction;
import explorer.BuildNamed;
import explorer.BuildVivified;
import explorer.BuilderBase;
import explorer.GeneratorType;
import explorer.ModelFactory;

/**
 * Given an OWL ontology, find a new fact by constructing and applying a domain
 * / range constraint for each property and then comparing the triples found
 * before and after the application of the domain/range constraints.
 * 
 * @author tbriggs
 * 
 */
public class FindNewFacts {

	boolean debug = true;
	WorkUnitCount workUnit;
	GeneratorType type;

	public FindNewFacts(WorkUnitCount workUnit, GeneratorType type) {
		this.workUnit = workUnit;
		this.type = type;
	}

	/**
	 * Use the reasoner to compute the known axioms for the given work unit. For
	 * each property, compute its domain/range constraints. Use the reasoner to
	 * compute the known axioms post-processing; and then compare the result
	 * sets.
	 * 
	 * Return the number of facts added and lost.
	 * 
	 * @return an array, element 0 is the number of facts added, 1 is facts lost
	 * @throws Exception
	 */
	public int[] findNewFacts() throws Exception {
		OWLModel origModel = loadModel();

		ProtegeReasoner reasoner = ModelFactory.createReasonerForModel(
				origModel, true);
		applyReasoner(origModel, reasoner);

		List<Triple> originalTriples, postTriples;
		List<RDFResource> originalResources, postResources;

		// originalTriples = getAllTriples(model);
		originalResources = getAllResources(origModel);
		origModel = null;

		OWLModel procModel = loadModel();
		ProtegeReasoner reasoner2 = ModelFactory.createReasonerForModel(
				procModel, true);
		applyReasoner(procModel, reasoner2);

		Results results = new Results();
		BuilderBase bnd = getBuilder(procModel, reasoner, results);

		bnd.run();
		procModel = null;

		OWLModel modModel = loadModel();
		setConstraints(modModel, results);
		ProtegeReasoner reasoner3 = ModelFactory.createReasonerForModel(
				modModel, true);
		applyReasoner(modModel, reasoner3);

		//			
		// applyReasoner(model, reasoner);
		// postTriples = getAllTriples(model);
		postResources = getAllResources(modModel);
		modModel = null;

		// ((JenaOWLModel)model).save(new File("newfacts.owl").toURI());

		// compareTriples(originalTriples, postTriples);
		compareResources(originalResources, postResources);

		int numLost = originalResources.size();
		int numAdded = postResources.size();

		int res[] = { numAdded, numLost };

		// new resources:
//		for (RDFResource R : originalResources) {
//			System.out.println("lost: " + R.getClass() + "\t"
//					+ R.getBrowserText());
//		}
//
//		// new resources:
//		for (RDFResource R : postResources) {
//			System.out.println("added: " + R.getClass() + "\t"
//					+ R.getBrowserText());
//		}

		return res;
	}

	private void compareResources(List<RDFResource> originalResources,
			List<RDFResource> postResources) {
		LinkedList<RDFResource> toDeleteFromOriginal, toDeleteFromPost;

		toDeleteFromOriginal = new LinkedList<RDFResource>();
		toDeleteFromPost = new LinkedList<RDFResource>();

		// for each resource in the original list, find its match in post list
		for (RDFResource R : originalResources) {
		
			
			for (RDFResource S : postResources) {
				
				
				
				OWLEquals O = new OWLEquals( );
				if (O.owlEquals(R, S)) {
					if (R.getBrowserText().contains("hasKeyword")) {
						System.out.println("Removing: " + R.getBrowserText());
					}
					toDeleteFromOriginal.add(R);
					toDeleteFromPost.add(S);
				}
			}
		}
		
		for (RDFResource R : postResources) {

			
			for (RDFResource S : originalResources) {
				OWLEquals O = new OWLEquals( );
				if (O.owlEquals(R, S)) {
					toDeleteFromPost.add(R);
					toDeleteFromOriginal.add(S);
				}
			}
		}

		originalResources.removeAll(toDeleteFromOriginal);
		postResources.removeAll(toDeleteFromPost);
		
		if (debug) {
			try {
				File output = new File("output.txt");
				PrintWriter out = new PrintWriter(new FileWriter(output));
				
				
				// now, those left standing... they are the ones that we care
				// about....
				out.format("Original ones left (not in new): %d\n", originalResources.size());
				for (RDFResource R : originalResources) {
					out.format("original %s\n", OWLOutput.owlToString((Object) R));
				}
	
				out.format("After processing: %d\n", postResources.size());
				for (RDFResource R : postResources) {
					out.format("after   %s\n", OWLOutput.owlToString((Object) R));
				}
				
				out.close();
				
			}
			catch(IOException E)
			{
				E.printStackTrace();
			}
		}

	}

	@SuppressWarnings("unchecked")
	private List<RDFResource> getAllResources(OWLModel model) {
		LinkedList<RDFResource> resources = new LinkedList<RDFResource>();
		resources.addAll(model.getRDFResources());
		return resources;
	}

	private List<Triple> getAllTriples(OWLModel model) {
		List<Triple> originalTriples;
		originalTriples = new LinkedList<Triple>();
		Iterator<Triple> iterator = model.getTripleStoreModel()
				.getActiveTripleStore().listTriples();
		while (iterator.hasNext()) {
			Triple T = iterator.next();
			originalTriples.add(T);
		}
		return originalTriples;
	}

	private BuilderBase getBuilder(OWLModel model, ProtegeReasoner reasoner,
			Results results) {
		BuilderBase bnd = null;

		switch (type) {
		case DISJUNCTION:
			bnd = new BuildDisjunction(type, model, reasoner, results);
			break;
		case LCNS:
			bnd = new BuildNamed(model, reasoner, results);
			break;
		case VIVIFICATION:
			bnd = new BuildVivified(model, reasoner, results);
			break;
		}
		return bnd;
	}

	private void applyReasoner(OWLModel model, ProtegeReasoner reasoner)
			throws ProtegeReasonerException {

		reasoner.setAutoSynchronizationEnabled(true);
		reasoner.classifyTaxonomy();
		reasoner.computeInferredHierarchy();
		reasoner.computeInferredIndividualTypes();
		reasoner.computeInconsistentConcepts();

	}

	private OWLModel loadModel() throws OntologyLoadException,
			FileNotFoundException, URISyntaxException, MalformedURLException,
			IOException {
		String url = workUnit.getUrl();
		OWLModel model;

		if (url.startsWith("file:")) {
			model = ModelFactory.loadFromFile(url.substring(5), url);
		} else {
			model = ModelFactory.loadFromURI(url);
		}
		return model;
	}

	@SuppressWarnings("unchecked")
	private String unionToString(OWLClass C) {
		StringBuffer buff = new StringBuffer();
		if (C instanceof OWLUnionClass) {
			OWLUnionClass ouc = (OWLUnionClass) C;
			Iterator I = ouc.listOperands();
			int count = 0;
			buff.append("(");
			while (I.hasNext()) {
				if (count++ > 0)
					buff.append(" U ");
				buff.append(unionToString((OWLClass) I.next()));
			}
			buff.append(")");

		} else if (C instanceof OWLIntersectionClass) {
			OWLIntersectionClass oic = (OWLIntersectionClass) C;
			Iterator I = oic.listOperands();
			int count = 0;
			buff.append("(");
			while (I.hasNext()) {
				if (count++ > 0)
					buff.append(" ^ ");
				buff.append(unionToString((OWLClass) I.next()));
			}
			buff.append(")");
		} else
			buff.append(C.getName());

		return buff.toString();
	}

	private void compareTriples(List<Triple> original, List<Triple> after) {

		if (debug)
			System.out.format("Size original: %d after: %d\n", original.size(),
					after.size());

		// decimate triples in sets
		// wipe out new in old
		LinkedList<Triple> toDeleteOriginal = new LinkedList<Triple>();
		LinkedList<Triple> toDeleteAfter = new LinkedList<Triple>();

		for (Triple T : after) {
			if (original.contains(T)) {
				toDeleteOriginal.add(T);
				toDeleteAfter.add(T);
			}
		}

		original.removeAll(toDeleteOriginal);
		after.removeAll(toDeleteAfter);

		// rinse, wash and repeat
		toDeleteOriginal = new LinkedList<Triple>();
		toDeleteAfter = new LinkedList<Triple>();

		for (Triple T : original) {
			if (after.contains(T)) {
				toDeleteOriginal.add(T);
				toDeleteAfter.add(T);
			}
		}

		original.removeAll(toDeleteOriginal);
		after.removeAll(toDeleteAfter);

		if (debug) {
			// now, those left standing... they are the ones that we care
			// about....
			System.out.format("Original ones left (not in new): %d\n", original
					.size());
			for (Triple T : original) {
				System.out.format("original %s\n", T);
			}

			System.out.format("After processing: %d\n", after.size());
			for (Triple T : after) {
				System.out.format("after %s\n", T);
			}
		}
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

				if (debug)
					System.out.format("Setting %s domain to %s\n", property,
							unionToString(domain));
			}

			if (!isThing(model, range)) {
				property.setRange(range);
				if (debug)
					System.out.format("Setting %s range to %s\n", property,
							unionToString(range));
			}

		}

		try {
			((JenaOWLModel) model).save(new File("newfacts.owl").toURI());
		} catch (Exception e) {
			;
		}
	}


	
	public static void main(String args[]) throws IOException {

		GeneratorType use_type = GeneratorType.VIVIFICATION;
		boolean getNextWorkunit = false;
		
		int idurl = -1;
			try {
				WorkUnitCount unit;
				
				if (!getNextWorkunit) {
//					idurl = 1985812;
//					unit = WorkUnitCount.getID(idurl);
					idurl = -1;
					File test = new File("test-ff1.owl");
					unit = new WorkUnitCount(""+idurl,test.toURI().toString());
				}
				else
					unit = WorkUnitCount.getNext();
				
				if (unit == null) {
					System.err.println("No work units found");
					System.exit(10);
				}
				idurl = unit.getIdurl();
				
				FindNewFacts finder = new FindNewFacts(unit, use_type);
				int res[] = finder.findNewFacts();

				System.out.format("Results: %d added, %d lost\n", res[0],res[1]);
				WorkUnitCount.update(idurl,res[0],res[1]);
				System.exit(1);
				
			} catch (Exception E) {
				E.printStackTrace();
				WorkUnitCount.error(idurl, 11, E.getClass().toString());
				System.exit(11);
			}
		
	}

	
}
