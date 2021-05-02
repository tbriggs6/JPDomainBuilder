package swoogle;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import org.mindswap.pellet.jena.PelletReasoner;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import edu.ship.thb.swoogle.PropertyRelationship;
import edu.ship.thb.swoogle.ResultProperty;
import edu.ship.thb.swoogle.Results;
import edu.ship.thb.swoogle.WorkUnit;
import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protegex.owl.inference.reasoner.ProtegeReasoner;
import edu.stanford.smi.protegex.owl.inference.reasoner.exception.ProtegeReasonerException;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import explorer.BuildDisjunction;
import explorer.BuildVivified;
import explorer.BuilderBase;
import explorer.BuildNamed;
import explorer.GeneratorType;
import explorer.ModelFactory;
import explorer.SimpleCounts;

public class OneRunner {

	// 30 min * 60 min/sec * 1000 ms/sec
	final long TIMEOUT = 30 * 60 * 1000;
	
	private class Alarm extends TimerTask {

		private int idurl;
		public Alarm(int idurl)
		{
			this.idurl = idurl;
		}
		@Override
		public void run() {
			
			try {
				System.err.format("==============================================\n");
				System.err.format("==============================================\n");
				System.err.format("!!  ERROR : TIMEOUT OCCURRED - TERMINATING  !!\n");
				System.err.format("==============================================\n");
				System.err.format("==============================================\n");
				
				WorkUnit.error(idurl, -2, "timeout");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.exit(-1);
		}
		
	}
	
	public void runJob(WorkUnit wunit) 
	{
		int idurl = -1;
		String url = "";
	
		Timer watchDog = new Timer( );

		try {
			url = wunit.getUrl();
			idurl = wunit.getIdurl();
			if (url.contains("www.hackcraft.net")) throw new RuntimeException("hack craft...");
			
			// set a timer for this job...
			watchDog.schedule(new Alarm(idurl), TIMEOUT);
		
			OWLModel model;
			if (url.startsWith("file:"))
			{
				model = ModelFactory.loadFromFile(url.substring(5), url);
			}
			else
			{
				model = ModelFactory.loadFromURI(url);
			}
			
			Results results = new Results( );
			ProtegeReasoner reasoner = ModelFactory.createReasonerForModel(model, true);
			reasoner.classifyTaxonomy();
						
			SimpleCounts counter = new SimpleCounts(model, reasoner, results);
			counter.run();
			
			BuilderBase dis = new BuildDisjunction(GeneratorType.DISJUNCTION, model, reasoner, results);
			dis.run( );
			
			BuildNamed lcns = new BuildNamed(model, reasoner, results);
			lcns.run( );

			BuildVivified bvd = new BuildVivified(model, reasoner, results);
			bvd.run();
			
			LinkedList<ResultProperty> props = results.getProperties();
			
			for(ResultProperty P : props)
			{
				OWLClass disj = null, lcnsd = null, vivf = null;
				
				if (P.getDomainDisjObject() != null)
					disj = (OWLClass) P.getDomainDisjObject();
	
				if (P.getDomainLCNSObject() != null)
					lcnsd = (OWLClass) P.getDomainLCNSObject();
				
				if (P.getDomainVivfObject() != null)
					vivf = (OWLClass) P.getDomainVivfObject();
				
				if ((disj != null) && (lcnsd != null))
				{
					if (reasoner.isSubsumedBy(disj, lcnsd))
					{
						if (reasoner.isSubsumedBy(lcnsd, disj))
							P.setDomDisjLCNS(PropertyRelationship.DISJ_EQ_LCNS);
						else
							P.setDomDisjLCNS(PropertyRelationship.DISJ_SUBSUMEDBY_LCNS);
					}
					else
					{
						P.setDomDisjLCNS(PropertyRelationship.DISJ_SUBSUMES_LCNS);
					}
				}
				
				if ((disj != null) && (vivf != null))
				{
					if (reasoner.isSubsumedBy(disj, vivf))
					{
						if (reasoner.isSubsumedBy(vivf, disj))
							P.setDomDisjVivf(PropertyRelationship.DISJ_EQ_VIVF);
						else
							P.setDomDisjVivf(PropertyRelationship.DISJ_SUBSUMEDBY_VIVF);
					}
					else
						P.setDomDisjVivf(PropertyRelationship.DISJ_SUBSUMES_VIVF);
				}
				
				if ((vivf != null) && (lcnsd != null))
				{
					if (reasoner.isSubsumedBy(vivf, lcnsd))
					{
						if (reasoner.isSubsumedBy(vivf, lcnsd))
							P.setDomLCNSVivf(PropertyRelationship.VIVF_EQ_LCNS);
						else
							P.setDomLCNSVivf(PropertyRelationship.VIVF_SUBSUMEDBY_LCNS);
							
				}
				else
					P.setDomLCNSVivf(PropertyRelationship.VIVF_SUBSUMES_LCNS);
				}
				
				System.out.format("%s : %s |  %s %s %s, (%s) (%s) (%s)\n", 
						P.getProperty(), P.getDomainOrig(), 
						P.getDomDisjLCNS(), P.getDomDisjVivf(), P.getDomLCNSVivf(),
						P.getDomainDisj(), P.getDomainLCNS(), P.getDomainVivf());

			}
			
			
			WorkUnit.update(idurl, results);
		}
		catch(Throwable E)
		{
			E.printStackTrace();
			
			try {
				if ((E == null) || (E.getCause() == null))
					WorkUnit.error(idurl, -1, "format exception");
				else 
					WorkUnit.error(idurl, -1, E.getCause().toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

		watchDog.cancel();
	}
	
	public static Connection makeConnection( ) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
	{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			return  DriverManager.getConnection("jdbc:mysql://excalibur.deck.ship.edu/swoogle3meta",
					"tbriggs","jdk1.6.0");
	}
	
	public static String getURL(Connection conn, int id) throws SQLException
	{
		PreparedStatement pstmt = conn.prepareStatement("select url from owl_swd where idurl = ?");
		pstmt.setInt(1, id);
		ResultSet rset = pstmt.executeQuery( );
		rset.next();
		
		String url = rset.getString(1);
		
		rset.close();
		pstmt.close();
		
		return url;
	}
	
	
	public static void main(String args[]) throws IOException, OntologyLoadException, URISyntaxException, ProtegeReasonerException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
	{
		Connection connection = makeConnection( );
	
		int id = 5453712;
		String url = getURL(connection, id);
		
		OneRunner runner = new OneRunner( );
		WorkUnit unit = new WorkUnit(""+id, url);
		
		runner.runJob(unit);
		
	}
}
