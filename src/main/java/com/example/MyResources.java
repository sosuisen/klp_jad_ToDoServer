package com.example;

import java.time.LocalDate;
import java.util.ArrayList;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletContext;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("todos")
public class MyResources {
	private DAO dao;

	// @Contextアノテーションと、それに続くメソッドは、
	// このクラスのインスタンス化のときに呼び出されます。
	// web.xml に書かれた <context-param> で設定されたパラメータ情報を持つ
	// ServletContextオブジェクトが、
	// このメソッドの引数 context へ自動的に注入されます。
	@Context
	public void setServletContext(ServletContext context) {
		dao = new DAO("jdbc:sqlite:" + context.getInitParameter("dbPath"));
	}

	@GET
	@RolesAllowed({ "ADMIN", "USER" })
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<ToDo> getAllToDos() {
		return dao.getAll();
	}

	@GET
	@RolesAllowed({ "ADMIN", "USER" })
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public ToDo getTodo(@PathParam("id") Integer id) {
		ToDo todo = dao.get(id);
		if (todo == null) {
			throw new NotFoundException();
		}
		return todo;
	}

	@POST
	@RolesAllowed({ "ADMIN" })
	@Produces(MediaType.APPLICATION_JSON)
	public ToDo postToDo(@FormParam("title") String title) {
		if (title == null) {
			throw new BadRequestException();
		}
		var date = LocalDate.now().toString();
		return dao.create(title, date, false);
	}

	@PUT
	@RolesAllowed({ "ADMIN" })
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public ToDo putToDo(@PathParam("id") Integer id,
			@FormParam("title") String title,
			@FormParam("date") String date,
			@FormParam("completed") String completedParam) {
		var exists = true;
		if (title != null) {
			exists = dao.updateTitle(id, title);
		}
		if (exists && date != null) {
			exists = dao.updateDate(id, date);
		}
		if (exists && completedParam != null) {
			var completed = Boolean.parseBoolean(completedParam);
			exists = dao.updateCompleted(id, completed);
		}

		if (!exists) {
			throw new NotFoundException();
		}
		return dao.get(id);
	}

	@DELETE
	@RolesAllowed({ "ADMIN" })
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public ToDo deleteToDo(@PathParam("id") Integer id) {
		ToDo todo = dao.get(id);
		if (todo == null) {
			throw new NotFoundException();
		}
		dao.delete(id);
		return todo;
	}

	@DELETE
	@RolesAllowed({ "ADMIN" })
	public void deleteToDo() {
		dao.deleteAll();
	}
}