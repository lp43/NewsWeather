package com.newsweather;

public class News {
	private String _title="";
	private String _link="";
	private String _desc="";
	private String _date="";
	private String _guid="";
	private String _category="";
	private String _comments="";
	private String _encoded="";
	
	public String getGuid() {
		return _guid;
	}
	public void setGuid(String _guid) {
		this._guid = _guid;
	}
	public String getCategory() {
		return _category;
	}
	public void setCategory(String _category) {
		this._category = _category;
	}
	public String getComments() {
		return _comments;
	}
	public void setComments(String _comments) {
		this._comments = _comments;
	}
	public String getEncoded() {
		return _encoded;
	}
	public void setEncoded(String _encoded) {
		this._encoded = _encoded;
	}
	
	public String getTitle() {
		return _title;
	}
	public void setTitle(String _title) {
		this._title = _title;
	}
	public String getLink() {
		return _link;
	}
	public void setLink(String _link) {
		this._link = _link;
	}
	public String getDesc() {
		return _desc;
	}
	public void setDesc(String _desc) {
		this._desc = _desc;
	}
	public String getDate() {
		return _date;
	}
	public void setDate(String _date) {
		this._date = _date;
	}
	
	
}
