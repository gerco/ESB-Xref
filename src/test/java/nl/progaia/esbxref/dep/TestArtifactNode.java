package nl.progaia.esbxref.dep;

import static org.junit.Assert.*;
import nl.progaia.esbxref.artifact.QueueArtifact;

import org.junit.Test;

import com.sonicsw.deploy.IArtifact;

public class TestArtifactNode {

	@Test
	public void testNode() {
		IArtifact a = new QueueArtifact("artifactName");
		INode n = new ArtifactNode(a);
		assertEquals(a.getArchivePath(), n.getPath());
	}

	@Test
	public void testGetName() {
		IArtifact a = new QueueArtifact("artifactName");
		INode n = new ArtifactNode(a);
		assertEquals(a.getName(), n.getName());
	}

	@Test
	public void testGetPath() {
		IArtifact a = new QueueArtifact("artifactName");
		INode n = new ArtifactNode(a);
		assertEquals(a.getArchivePath(), n.getPath());
	}

	@Test
	public void testAddOutgoing() {
		IArtifact a = new QueueArtifact("artifactName");
		IArtifact b = new QueueArtifact("dependantArtifact");
		
		INode na = new ArtifactNode(a);
		ArtifactNode nb = new ArtifactNode(b);
		
		assertEquals(na.getOutgoing().size(), 0);
		na.addOutgoing(new Link(na, nb, false));
		
		assertEquals(na.getOutgoing().size(), 1);
		na.addOutgoing(new Link(na, nb, false));
		
		assertEquals(na.getOutgoing().size(), 1);
		assertTrue(na.uses(nb));
	}

	@Test
	public void testAddIncoming() {
		IArtifact a = new QueueArtifact("artifactName");
		IArtifact b = new QueueArtifact("dependingArtifact");
		
		INode na = new ArtifactNode(a);
		ArtifactNode nb = new ArtifactNode(b);
		
		assertEquals(na.getIncoming().size(), 0);
		na.addIncoming(new Link(nb, na, false));
		
		assertEquals(na.getIncoming().size(), 1);
		na.addIncoming(new Link(nb, na, false));
		
		assertEquals(na.getIncoming().size(), 1);
		assertTrue(na.usedBy(nb));
	}

	@Test
	public void testUses() {
		IArtifact a = new QueueArtifact("artifactName");
		IArtifact b = new QueueArtifact("dependantArtifact");
		
		INode na = new ArtifactNode(a);
		ArtifactNode nb = new ArtifactNode(b);
		
		na.addOutgoing(new Link(na, nb, false));
		assertTrue(na.uses(nb));
		assertFalse(nb.uses(na));
	}

	@Test
	public void testUsesIndirect() {
		IArtifact a = new QueueArtifact("artifactName");
		IArtifact b = new QueueArtifact("dependantArtifact");
		IArtifact c = new QueueArtifact("dependantArtifact2");
		
		INode na = new ArtifactNode(a);
		ArtifactNode nb = new ArtifactNode(b);
		ArtifactNode nc = new ArtifactNode(c);
		
		na.addOutgoing(new Link(na, nb, false));
		nb.addOutgoing(new Link(nb, nc, false));
		
		assertTrue(na.uses(nb));
		assertFalse(na.usesIndirect(nb));
		
		assertTrue(na.uses(nc));
		assertTrue(na.usesIndirect(nc));
	}

	@Test
	public void testUsedBy() {
		IArtifact a = new QueueArtifact("artifactName");
		IArtifact b = new QueueArtifact("dependantArtifact");
		
		ArtifactNode na = new ArtifactNode(a);
		INode nb = new ArtifactNode(b);
		
		nb.addIncoming(new Link(na, nb, false));
		assertTrue(nb.usedBy(na));
		assertFalse(na.usedBy(nb));
	}

	@Test
	public void testUsedByIndirect() {
		IArtifact a = new QueueArtifact("artifactName");
		IArtifact b = new QueueArtifact("dependantArtifact");
		IArtifact c = new QueueArtifact("dependantArtifact2");
		
		ArtifactNode na = new ArtifactNode(a);
		ArtifactNode nb = new ArtifactNode(b);
		INode nc = new ArtifactNode(c);
		
		nb.addIncoming(new Link(na, nb, false));
		nc.addIncoming(new Link(nb, nc, false));
		
		assertTrue(nb.usedBy(na));
		assertTrue(nc.usedBy(na));
		
		assertTrue(nc.usedByIndirect(na));
		assertFalse(nc.usedByIndirect(nb));
	}

	@Test
	public void testCompressLinks() {
		IArtifact a = new QueueArtifact("artifactName");
		IArtifact b = new QueueArtifact("dependantArtifact");
		IArtifact c = new QueueArtifact("dependantArtifact2");
		
		INode na = new ArtifactNode(a);
		ArtifactNode nb = new ArtifactNode(b);
		ArtifactNode nc = new ArtifactNode(c);
		
		na.addOutgoing(new Link(na, nb, false));
		na.addOutgoing(new Link(na, nc, false));
		nb.addOutgoing(new Link(nb, nc, false));
		
		assertTrue(na.uses(nb));
		assertTrue(na.uses(nc));
		assertTrue(na.usesIndirect(nc)); // To ensure there the link between a and c is indirect
		assertFalse(nb.usesIndirect(nc)); // To ensure that the link between b and c is direct
		
		// Remove all obsolete direct links
		na.compressLinks();
		
		assertTrue(na.uses(nb));
		assertTrue(na.uses(nc));
		assertTrue(na.usesIndirect(nc)); // To ensure there the link between a and c is indirect
		assertFalse(nb.usesIndirect(nc)); // To ensure that the link between b and c is direct
	}

	@Test
	public void testToString() {
		IArtifact a = new QueueArtifact("artifactName");
		INode n = new ArtifactNode(a);
		assertEquals(n.toString(), a.getName());
	}

	@Test
	public void testCompareTo() {
		IArtifact a = new QueueArtifact("artifactName");
		IArtifact b = new QueueArtifact("dependantArtifact");
		
		ArtifactNode na = new ArtifactNode(a);
		ArtifactNode nb = new ArtifactNode(b);
		
		assertEquals(na.compareTo(nb), a.getArchivePath().compareTo(b.getArchivePath()));
	}

}
