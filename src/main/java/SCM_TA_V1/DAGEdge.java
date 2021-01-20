package main.java.SCM_TA_V1;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultEdge;

public class DAGEdge extends DefaultEdge implements EdgeFactory<Bug, DefaultEdge> {
	private final Class<? extends DefaultEdge> edgeClass=null;
	@Override
	public DefaultEdge createEdge(Bug arg0, Bug arg1) {
		// TODO Auto-generated method stub
		try {
            return edgeClass.getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            throw new RuntimeException("Edge factory failed", ex);
        }
	}


}
