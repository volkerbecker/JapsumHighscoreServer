package de.theamazingsax.myhighscoreserver;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;

@SuppressWarnings("serial")
public class MyHighscoreServerServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");
		String game = req.getParameter("game");
		String name = req.getParameter("name");
		int points=0;
		
		String pointsStr = req.getParameter("points");
		if(pointsStr!=null) {
			points=Integer.parseInt(pointsStr);
		}
		
		String newuser = req.getParameter("newuser");
		if(newuser!=null) {
			tryAddUser(resp,game,newuser);
			return;
		}
		
		String getuserpoints = req.getParameter("getuserpoints");
		
		if(getuserpoints != null) {
			getpoints(resp,game,getuserpoints);
			return;
		}
		
		
		String maxStr = req.getParameter("max");
		int max=10;
		if(maxStr!=null) {
			max= Integer.parseInt(maxStr);
		}
		
		if(points>0 && name !=null) {
			addHighscore(resp,game,name,points);
			return;
		}
		returnHighscores(resp,game,max);
		
	}
	
	
	private void getpoints(HttpServletResponse resp, String game,String name) throws IOException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key gameKey = KeyFactory.createKey("game", game);
		Query query = new Query("highscore",gameKey);
		Query.FilterPredicate filter = new Query.FilterPredicate("name",Query.FilterOperator.EQUAL,name);
		query.setFilter(filter);
		List<Entity> eintrag =  datastore.prepare(query).asList(FetchOptions.Builder.withLimit(10));
		if(eintrag.size()!=1) {
			if(eintrag.size()==0) {
				resp.getWriter().println("no such user");
			} else {
				throw new IllegalArgumentException();
			}
		} else {
			Entity entity=eintrag.get(0);
			long points=(long)entity.getProperty("points");
			resp.getWriter().println(points);
		}
	}


	private void tryAddUser(HttpServletResponse resp,String game, String newuser) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key gameKey = KeyFactory.createKey("game",game);
		Query query = new Query("highscore",gameKey);
		Query.FilterPredicate filter = new Query.FilterPredicate("name",Query.FilterOperator.EQUAL,newuser);
		query.setFilter(filter);
		List<Entity> istVorhanden =  datastore.prepare(query).asList(FetchOptions.Builder.withLimit(10)); 
		try {
			if (istVorhanden.size() == 0) {
				addNewHighscore(game,newuser,0);
				resp.getWriter().println("true");
			} else {
				resp.getWriter().println("false");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addNewHighscore(String game,String name,int points) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key gameKey = KeyFactory.createKey("game", game);
		Entity highscore=new Entity("highscore",gameKey);
		highscore.setProperty("name", name);
		highscore.setProperty("points", points);
		datastore.put(highscore);
	}
	
	private void addHighscore(HttpServletResponse resp,String game, String name, int points) throws IOException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key gameKey = KeyFactory.createKey("game", game);
		Query query = new Query("highscore",gameKey);
		Query.FilterPredicate filter = new Query.FilterPredicate("name",Query.FilterOperator.EQUAL,name);
		query.setFilter(filter);
		List<Entity> eintrag =  datastore.prepare(query).asList(FetchOptions.Builder.withLimit(10));
		if(eintrag.size()!=1) {
			resp.getWriter().println("username not found");
		}
		Entity entity=eintrag.get(0);
		long oldpoints=(long)entity.getProperty("points");
		entity.setProperty("points",oldpoints+points);
		datastore.put(entity);
		resp.getWriter().println("done");
	}
	
	private void returnHighscores(HttpServletResponse resp,String game,int max) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key gameKey = KeyFactory.createKey("game",game);
		Query query = new Query("highscore",gameKey);
		query.addSort("points",Query.SortDirection.DESCENDING);
		List<Entity> highscores = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(max));
		
		for(Entity e: highscores) {
			try {
				resp.getWriter().println(e.getProperty("name")+","+e.getProperty("points"));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
	}
}
