package edu.stevens.cs522.chat.twoway.interfaces;

import edu.stevens.cs522.chat.twoway.managers.TypedCursor;

public interface IQueryListener<T> {

	public void handleResults (TypedCursor<T> results);
	
	public void closeResults();
	
}
