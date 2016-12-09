package com.yaraslav;

public class ObjectItem {
	
	long objectId;
	String title;
	
	public String getTitle() {
		return title;
	}
	
	public long getObjectId() {
		return objectId;
	}

	public ObjectItem(String _title) {
		this(_title, 0);
	}
	
	public ObjectItem(String _title, long _objectId) {
		title = _title;
		objectId = _objectId;
	}
	
	@Override
	public String toString() {
		return title;
	}
}
