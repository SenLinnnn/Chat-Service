package edu.stevens.cs522.chat.twoway.interfaces;

import java.util.List;

public interface ISimpleQueryListener<T> {

	public void handleResults(List<T> results);
	
}
