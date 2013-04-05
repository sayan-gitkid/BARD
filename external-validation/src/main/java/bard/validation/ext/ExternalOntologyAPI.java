package bard.validation.ext;

import java.util.List;

/**
 * Contract for External Ontology implementations
 * 
 * @author southern
 * 
 */
public abstract class ExternalOntologyAPI {

	/**
	 * given id is searched against the underlying external ontology
	 */
	public abstract ExternalItem findById(String id) throws ExternalOntologyException;

	/**
	 * given name is searched against the underlying external ontology
	 */
	public abstract ExternalItem findByName(String name) throws ExternalOntologyException;

	/**
	 * given term is searched against the underlying external ontology system
	 * the term is first run through the <b>queryGenerator</b> method so it is
	 * suitable for the given system
	 */
	public List<ExternalItem> findMatching(String term) throws ExternalOntologyException {
		return findMatching(term, -1);
	}

	public abstract List<ExternalItem> findMatching(String term, int limit) throws ExternalOntologyException;

	/**
	 * returns the URL of the external system where the user can search for
	 * terms.
	 */
	public abstract String getExternalURL(String id);

	/**
	 * formats a query term. E.g. adds % for SQL LIKE, trims the term etc.
	 */
	public abstract String queryGenerator(String term);

	public boolean validate(String name, String id) throws ExternalOntologyException {
		ExternalItem item = findByName(name);
		ExternalItem item2 = findById(id);
		return item.equals(item2);
	}
}