package com.newsweather;

import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class MyHandler extends DefaultHandler {
	
	private boolean in_item=false;
	private boolean in_link=false;
	private boolean in_date=false;
	private boolean in_title=false;
	private boolean in_desc=false;
	private List<News> li;
	private News news;
	private StringBuffer buf = new StringBuffer();
	
	//將轉換成List<News>的XML資料回傳
	public List<News> getParasedData(){
		return li;
	}
	
	@Override//覆寫characters
	public void characters(char[] ch, int start, int length)
			throws SAXException {
			if(this.in_item){
				//將char[]加入Stringuffer
				buf.append(ch, start, length);
			}
	}

	@Override//XML文件開始解析時呼叫此方法
	public void startDocument() throws SAXException {
		li = new ArrayList<News>();
	}
	
	@Override//XML文件結束解析時呼叫此方法
	public void endDocument() throws SAXException {
	}

	@Override//解析到Element開頭時的method
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if(localName.equals("item")){
			this.in_item=true;
			//當解析到的字為"item"時，馬上new一個新物件
			news = new News();
		}else if(localName.equals("title")){
			if(this.in_item){
				this.in_title=true;
			}
		}else if(localName.equals("link")){
			if(this.in_item){
				this.in_link=true;
			}
		}else if(localName.equals("description")){
			if(this.in_item){
				this.in_desc=true;
			}
		}else if(localName.equals("pubDate")){
			if(this.in_item){
				this.in_date=true;
			}
		}
	}
	
	@Override//結析到Element結尾時用的method
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if(localName.equals("item")){
			this.in_item=false;
			//當解析到item結尾時，將News物件(item內的資料)放進List中
			li.add(news);
		}else if(localName.equals("title")){
			if(this.in_item){
				//設定News物件的title
				news.setTitle(buf.toString().trim());
				buf.setLength(0);
				this.in_title=false;
			}
		}else if(localName.equals("link")){
			if(this.in_item){
				//設定News物件的link
				news.setLink(buf.toString().trim());
				buf.setLength(0);
				this.in_link=false;
			}
		}else if(localName.equals("description")){
			if(this.in_item){
				//設定News物件的description
				news.setDesc(buf.toString().trim());
				buf.setLength(0);
				this.in_desc=false;
			}
		}else if(localName.equals("pubDate")){
			if(this.in_item){
				//設定News物件的pubDate
				news.setDate(buf.toString().trim());
				buf.setLength(0);
				this.in_date=false;
			}
		}
	}

}
