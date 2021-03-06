package com.cybercom.librarytest.rest;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.spi.BadRequestException;

import com.cybercom.librarytest.LocalEntityManagerFactory;
import com.cybercom.librarytest.model.Author;
import com.cybercom.librarytest.model.Authors;
import java.net.URI;
import java.util.Collection;

/**
 * EJB implementing the REST interface for authors in the library.
 * @author Lennart Moraeus
 */
@Path("/authors")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class AuthorRestService {

	@Context
	private UriInfo uriInfo;

	/**
	 * Adds a new Author.
	 * @throws BadRequestException - if the specified author is null.
	 */
	@POST
	public Response createAuthor(Author author) {
		if (author == null || doesAuthorExistInDatabase(author.getId())) {
			throw new BadRequestException("Author was null.");
		}
		EntityManager em = 
				LocalEntityManagerFactory.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		em.persist(author);
		tx.commit();
		em.close();
		URI authorUri = uriInfo.getAbsolutePathBuilder()
				.path(String.valueOf(author.getId())).build();
		return Response.created(authorUri).build();
	}

	/**
	 * Updates the info about a author.
	 * @throws BadRequestException - if the specified author is null.
	 */  
	@PUT
	public Response updateAuthor(Author author) {
		if (author == null) {
			throw new BadRequestException("Author was null.");
		}
		if (!doesAuthorExistInDatabase(author.getId())) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		EntityManager em = 
				LocalEntityManagerFactory.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		em.merge(author);
		tx.commit();
		em.close();
		return Response.ok().build();
	}

	/**
	 * Returns all authors in the database.
	 */
	@GET
	public Response getAllAuthors() {
		EntityManager em = 
				LocalEntityManagerFactory.createEntityManager();

		// TODO: We can't use JPA 2.0 createNamedQuery call like this. Why? 
		//TypedQuery<Author> query = em.createNamedQuery(Author.FIND_ALL, Author.class);
		//Authors authors = new Authors(query.getResultList());
		
		// Instead we are stuck with this workaround.
		Query query = em.createNamedQuery(Author.FIND_ALL);
		Authors authors = new Authors((Collection<? extends Author>)(query.getResultList()));
		Response r = Response.ok(authors).build();
		return r;
	}
	
	/**
	 * Gets the author with the specified id.
	 * @throws NotFoundException - if no author exists with the given id.
	 */
	@GET
	@Path("{id}")
	public Response getAuthor(@PathParam("id") Long id) {
		EntityManager em = 
				LocalEntityManagerFactory.createEntityManager();
		Author author = em.find(Author.class, id);
		em.close();
		if (author == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		return Response.ok(author).build();
	}

	/**
	 * Deletes the author with the specified id.
	 * @throws NotFoundException - if no author exists with the given id. 
	 */
	@DELETE
	@Path("{id}")
	public Response deleteAuthor(@PathParam("id") Long id) {
		EntityManager em = 
				LocalEntityManagerFactory.createEntityManager();
		Author author = em.find(Author.class, id);
		if (author == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		em.remove(author);
		tx.commit();
		return Response.noContent().build();
	}
	
	private boolean doesAuthorExistInDatabase(Long id) {
		if (id == null) {
			return false;
		}
		EntityManager em = LocalEntityManagerFactory.createEntityManager();
		boolean exists = em.find(Author.class, id) != null;
		em.close();
		return exists;
	}
}