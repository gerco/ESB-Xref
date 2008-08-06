package nl.progaia.esbxref.dep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import nl.progaia.esbxref.artifact.QueueArtifact;

import org.junit.Test;

import com.sonicsw.deploy.IArtifact;

public class TestDependencyGraph {

	@Test
	public void testAddArtifactIArtifact() {
		DependencyGraph graph = new DependencyGraph();
		
		IArtifact a = new QueueArtifact("someQueue");
		graph.addArtifact(a);
		
		INode n = graph.getNode(a.getArchivePath());
		
		assertNotNull(n);
		assertEquals(n.getPath(), a.getArchivePath());
	}

	@Test
	public void testAddArtifactIArtifactIArtifactArray() {
		DependencyGraph graph = new DependencyGraph();
		
		IArtifact a = new QueueArtifact("someQueue");
		IArtifact b = new QueueArtifact("someOtherQueue");
		IArtifact c = new QueueArtifact("anotherQueue");
		
		graph.addArtifact(a, new IArtifact[] {b, c});
		
		INode na = graph.getNode(a.getArchivePath());
		INode nb = graph.getNode(b.getArchivePath());
		INode nc = graph.getNode(c.getArchivePath());
		
		assertNotNull(na);
		assertEquals(na.getPath(), a.getArchivePath());
		assertNotNull(nb);
		assertEquals(nb.getPath(), b.getArchivePath());
		assertNotNull(nc);
		assertEquals(nc.getPath(), c.getArchivePath());
		
		assertTrue(na.uses(nb));
		assertTrue(na.uses(nc));
		assertTrue(nb.usedBy(na));
		assertTrue(nc.usedBy(na));
		assertFalse(nb.uses(na));
		assertFalse(nb.uses(nc));
		assertFalse(nc.uses(na));
		assertFalse(nc.uses(nb));
	}

	@Test
	public void testMergeNodes() {
		DependencyGraph graph = new DependencyGraph();
		
		IArtifact a = new QueueArtifact("artifacta");
		IArtifact b = new QueueArtifact("artifactb");
		IArtifact c = new QueueArtifact("artifactc");
		IArtifact d = new QueueArtifact("artifactd");
		IArtifact e = new QueueArtifact("artifacte");
		
		graph.addArtifact(a, new IArtifact[] {b, c});
		graph.addArtifact(d, new IArtifact[] {e});

		INode na = graph.getNode(a.getArchivePath());
		INode nb = graph.getNode(b.getArchivePath());
		INode nc = graph.getNode(c.getArchivePath());
		INode nd = graph.getNode(d.getArchivePath());
		INode ne = graph.getNode(e.getArchivePath());

		assertFalse(na.uses(ne));
		assertFalse(ne.usedBy(na));
		
		graph.mergeNodes(na, nd);
		
		assertTrue(na.uses(nb));
		assertTrue(na.uses(nc));
		assertTrue(na.uses(ne));
		assertTrue(ne.usedBy(na));
		assertNull(graph.getNode(nd.getPath()));
		
		for(INode n: graph.getAllNodes()) {
			if(n.uses(nd))
				System.out.println(n + " still uses " + nd);
			if(n.usedBy(nd))
				System.out.println(n + " still used by " + nd);
			
			assertFalse(n.uses(nd));
			assertFalse(n.usedBy(nd));
		}
	}
	
	@Test
	public void testGetAllNodes() {
		DependencyGraph graph = new DependencyGraph();
		
		IArtifact a = new QueueArtifact("someQueue");
		IArtifact b = new QueueArtifact("someOtherQueue");
		IArtifact c = new QueueArtifact("anotherQueue");
		
		graph.addArtifact(a, new IArtifact[] {b, c});
		
		INode na = graph.getNode(a.getArchivePath());
		INode nb = graph.getNode(b.getArchivePath());
		INode nc = graph.getNode(c.getArchivePath());
		
		Collection<INode> nodes = graph.getAllNodes();
		assertNotNull(nodes);
		assertEquals(3, nodes.size());
		assertTrue(nodes.contains(na));
		assertTrue(nodes.contains(nb));
		assertTrue(nodes.contains(nc));
	}

	@Test
	public void testSaveLoad() throws IOException, ClassNotFoundException {
		DependencyGraph graph = new DependencyGraph();
		
		IArtifact a = new QueueArtifact("someQueue");
		IArtifact b = new QueueArtifact("someOtherQueue");
		IArtifact c = new QueueArtifact("anotherQueue");
		
		graph.addArtifact(a, new IArtifact[] {b, c});
		
		File file = File.createTempFile("depgraphtest", ".xref");
		graph.save(file);
		
		graph = DependencyGraph.load(file);
		
		INode na = graph.getNode(a.getArchivePath());
		INode nb = graph.getNode(b.getArchivePath());
		INode nc = graph.getNode(c.getArchivePath());
		
		Collection<INode> nodes = graph.getAllNodes();
		assertNotNull(nodes);
		assertEquals(3, nodes.size());
		assertTrue(nodes.contains(na));
		assertTrue(nodes.contains(nb));
		assertTrue(nodes.contains(nc));
	}

	@Test
	public void testFindUnused() {
		DependencyGraph graph = new DependencyGraph();
		
		IArtifact a = new QueueArtifact("artifacta");
		IArtifact b = new QueueArtifact("artifactb");
		IArtifact c = new QueueArtifact("artifactc");
		IArtifact d = new QueueArtifact("artifactd");
		IArtifact e = new QueueArtifact("artifacte");
		
		graph.addArtifact(a, new IArtifact[] {b, c});
		graph.addArtifact(d, new IArtifact[] {e});
		
		List<INode> unusedNodes = graph.findUnused();
		assertEquals(2, unusedNodes.size());
		assertTrue(unusedNodes.contains(graph.getNode(a.getArchivePath())));
		assertTrue(unusedNodes.contains(graph.getNode(d.getArchivePath())));
	}

	@Test
	public void testFindAllUnused() {
		DependencyGraph graph = new DependencyGraph();
		
		IArtifact a = new QueueArtifact("artifacta");
		IArtifact b = new QueueArtifact("artifactb");
		IArtifact c = new QueueArtifact("artifactc");
		IArtifact d = new QueueArtifact("artifactd");
		IArtifact e = new QueueArtifact("artifacte");
		
		graph.addArtifact(a, new IArtifact[] {b, c});
		graph.addArtifact(d, new IArtifact[] {e});
		
		List<INode> unusedNodes = graph.findAllUnused();
		assertEquals(5, unusedNodes.size());
		
		graph.setTopLevel(b.getArchivePath(), true);
		graph.setTopLevel(d.getArchivePath(), true);
		unusedNodes = graph.findAllUnused();
		
		assertEquals(2, unusedNodes.size());
		assertTrue(unusedNodes.contains(graph.getNode(a.getArchivePath())));
		assertTrue(unusedNodes.contains(graph.getNode(c.getArchivePath())));
	}
}
