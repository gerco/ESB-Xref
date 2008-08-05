package nl.progaia.esbxref.dep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
		
		Node n = graph.getNode(a.getArchivePath());
		
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
		
		Node na = graph.getNode(a.getArchivePath());
		Node nb = graph.getNode(b.getArchivePath());
		Node nc = graph.getNode(c.getArchivePath());
		
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
	public void testGetAllNodes() {
		DependencyGraph graph = new DependencyGraph();
		
		IArtifact a = new QueueArtifact("someQueue");
		IArtifact b = new QueueArtifact("someOtherQueue");
		IArtifact c = new QueueArtifact("anotherQueue");
		
		graph.addArtifact(a, new IArtifact[] {b, c});
		
		Node na = graph.getNode(a.getArchivePath());
		Node nb = graph.getNode(b.getArchivePath());
		Node nc = graph.getNode(c.getArchivePath());
		
		Collection<Node> nodes = graph.getAllNodes();
		assertNotNull(nodes);
		assertTrue(nodes.size() == 3);
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
		
		Node na = graph.getNode(a.getArchivePath());
		Node nb = graph.getNode(b.getArchivePath());
		Node nc = graph.getNode(c.getArchivePath());
		
		Collection<Node> nodes = graph.getAllNodes();
		assertNotNull(nodes);
		assertTrue(nodes.size() == 3);
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
		
		List<Node> unusedNodes = graph.findUnused();
		assertEquals(unusedNodes.size(), 2);
		assertTrue(unusedNodes.contains(graph.getNode(a.getArchivePath())));
		assertTrue(unusedNodes.contains(graph.getNode(d.getArchivePath())));
	}

	@Test
	public void testFindAllUnused() {
		fail("Not yet implemented");
	}

	@Test
	public void testFindAllUnusedListOfNode() {
		fail("Not yet implemented");
	}
	
}
