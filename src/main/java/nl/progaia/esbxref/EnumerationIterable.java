package nl.progaia.esbxref;

import java.util.Enumeration;
import java.util.Iterator;

public class EnumerationIterable<E> implements Iterable<E> {
	private Enumeration<? extends E> delegate;
	
	public EnumerationIterable(Enumeration<? extends E> enumeration) {
		this.delegate = enumeration;
	}
	
	public Iterator<E> iterator() {
		return new EnumerationIterator<E>(delegate);
	}
}

class EnumerationIterator<E> implements Iterator<E> {
	private Enumeration<? extends E> delegate;
	
	public EnumerationIterator(Enumeration<? extends E> enumeration) {
		this.delegate = enumeration;
	}
	
	public boolean hasNext() {
		return delegate.hasMoreElements();
	}

	public E next() {
		return delegate.nextElement();
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}

