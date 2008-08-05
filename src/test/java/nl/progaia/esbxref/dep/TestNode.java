package nl.progaia.esbxref.dep;

import static org.junit.Assert.*;
import nl.progaia.esbxref.artifact.QueueArtifact;

import org.junit.Test;

import com.sonicsw.deploy.IArtifact;

public class TestNode {

	@Test
	public void testNode() {
		IArtifact a = new QueueArtifact("artifactName");
		Node n = new Node(a);
		assertEquals(a.getArchivePath(), n.getPath());
	}

	@Test
	public void testGetName() {
		IArtifact a = new QueueArtifact("artifactName");
		Node n = new Node(a);
		assertEquals(a.getName(), n.getName());
	}

	@Test
	public void testGetPath() {
		IArtifact a = new QueueArtifact("artifactName");
		Node n = new Node(a);
		assertEquals(a.getArchivePath(), n.getPath());
	}

	@Test
	public void testAddIUse() {
		IArtifact a = new QueueArtifact("artifactName");
		IArtifact b = new QueueArtifact("dependantArtifact");
		
		Node na = new Node(a);
		Node nb = new Node(b);
		
		assertEquals(na.getIUse().size(), 0);
		na.addIUse(nb);
		
		assertEquals(na.getIUse().size(), 1);
		na.addIUse(nb);
		
		assertEquals(na.getIUse().size(), 1);
		assertTrue(na.getIUse().contains(nb));
	}

	@Test
	public void testAddUsedBy() {
		IArtifact a = new QueueArtifact("artifactName");
		IArtifact b = new QueueArtifact("dependingArtifact");
		
		Node na = new Node(a);
		Node nb = new Node(b);
		
		assertEquals(na.getUsedBy().size(), 0);
		na.addUsedBy(nb);
		
		assertEquals(na.getUsedBy().size(), 1);
		na.addUsedBy(nb);
		
		assertEquals(na.getUsedBy().size(), 1);
		assertTrue(na.getUsedBy().contains(nb));
	}

	@Test
	public void testUses() {
		IArtifact a = new QueueArtifact("artifactName");
		IArtifact b = new QueueArtifact("dependantArtifact");
		
		Node na = new Node(a);
		Node nb = new Node(b);
		
		na.addIUse(nb);
		assertTrue(na.uses(nb));
		assertFalse(nb.uses(na));
	}

	@Test
	public void testUsesIndirect() {
		IArtifact a = new QueueArtifact("artifactName");
		IArtifact b = new QueueArtifact("dependantArtifact");
		IArtifact c = new QueueArtifact("dependantArtifact2");
		
		Node na = new Node(a);
		Node nb = new Node(b);
		Node nc = new Node(c);
		
		na.addIUse(nb);
		nb.addIUse(nc);
		
		assertTrue(na.uses(nb));
		assertFalse(na.usesIndirect(nb));
		
		assertTrue(na.uses(nc));
		assertTrue(na.usesIndirect(nc));
	}

	@Test
	public void testUsedBy() {
		IArtifact a = new QueueArtifact("artifactName");
		IArtifact b = new QueueArtifact("dependantArtifact");
		
		Node na = new Node(a);
		Node nb = new Node(b);
		
		nb.addUsedBy(na);
		assertTrue(nb.usedBy(na));
		assertFalse(na.usedBy(nb));
	}

	@Test
	public void testUsedByIndirect() {
		IArtifact a = new QueueArtifact("artifactName");
		IArtifact b = new QueueArtifact("dependantArtifact");
		IArtifact c = new QueueArtifact("dependantArtifact2");
		
		Node na = new Node(a);
		Node nb = new Node(b);
		Node nc = new Node(c);
		
		nb.addUsedBy(na);
		nc.addUsedBy(nb);
		
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
		
		Node na = new Node(a);
		Node nb = new Node(b);
		Node nc = new Node(c);
		
		na.addIUse(nb);
		na.addIUse(nc);
		nb.addIUse(nc);
		
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
		Node n = new Node(a);
		assertEquals(n.toString(), a.getName());
	}

	@Test
	public void testCompareTo() {
		IArtifact a = new QueueArtifact("artifactName");
		IArtifact b = new QueueArtifact("dependantArtifact");
		
		Node na = new Node(a);
		Node nb = new Node(b);
		
		assertEquals(na.compareTo(nb), a.getArchivePath().compareTo(b.getArchivePath()));
	}

}
