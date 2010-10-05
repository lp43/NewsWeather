package com.camangi.rssreader;

import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class MyHandler extends DefaultHandler {
	
	private boolean itemswitch =false;
	final int in_item=1;
	final int in_title=2;
	final int in_link=3;
	final int in_guid=4;
	final int in_desc=5;
	final int in_encoded=6;
	final int in_date=7;
	final int in_category=8;
	final int in_comments=9;
	final int in_mainTitle = 10;
	final int in_author=11;
	final int in_img=12;
	final int in_url=13;
	final int big_title=14;
	private ArrayList<News> li;
	private News news;
	int currentcase = 0;
	final String tag = "tag";
	
	
	//將轉換成List<News>的XML資料回傳
	public List<News> getParasedData(){
		return li;
	}
	
	
	@Override//XML文件開始解析時呼叫此方法
	public void startDocument() throws SAXException {
		li = new ArrayList<News>();
		Log.i(tag,"startDocument");
	}
	

	@Override//解析到Element開頭時的method
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
//		Log.i(tag,"startElement");
		if(localName.equals("channel")){
			currentcase=0;
			return;
		}else if(localName.equals("item")){
			currentcase=in_item;
			itemswitch=true;
			news= new News();
			return;
		}else if(localName.equals("title")){
			if(RssReader.nameyouwanttoadd.equals("")){
				currentcase=big_title;
			}
			
			if(itemswitch)currentcase=in_title;
			return;
		}else if(localName.equals("link")){
			if(itemswitch)currentcase=in_link;
			return;
		}else if(localName.equals("description")){
			if(itemswitch)currentcase=in_desc;
			return;
		}else if(localName.equals("pubDate")){
			if(itemswitch)currentcase=in_date;
			return;
		}else if(localName.equals("author")){
			if(itemswitch)currentcase=in_author;
			return;
		}else if(localName.equals("guid")){
			if(itemswitch)currentcase=in_guid;
			return;
		}else if(localName.equals("image")){
			if(itemswitch)currentcase=in_img;
			return;
		}else if(localName.equals("url")){
			if(itemswitch)currentcase=in_url;
			return;
		}else{
			currentcase =0;
			return;
		}
		
		
	}
	
	
	@Override//覆寫characters
	public void characters(char[] ch, int start, int length)
			throws SAXException {
//		Log.i(tag,"get_Characters");
			String bufString = new String(ch,start,length);
			switch(currentcase){
			case in_title:
				news.setTitle(bufString);
				currentcase=0;
				break;
			case in_link:
				news.setLink(bufString);
				currentcase=0;
				break;
			case in_guid:
				news.setGuid(bufString);
				currentcase=0;
				break;
			case in_desc:
				news.setDesc(bufString);
				currentcase=0;
				break;
			case in_encoded:
				news.setEncoded(bufString);
				currentcase=0;
				break;
			case in_date:
				news.setDate(bufString);
				currentcase=0;
				break;
			case in_category:
				news.setCategory(bufString);
				currentcase=0;
				break;
			case in_comments:
				news.setComments(bufString);
				currentcase=0;
				break;
			case in_mainTitle:	
				currentcase=0;
				break;
			case in_author:
				currentcase=0;
				break;
			case in_img:
				currentcase=0;
				break;
			case in_url:
				currentcase=0;
				break;
			case big_title:
				if(RssReader.nameyouwanttoadd.equals("")){
					RssReader.nameyouwanttoadd=bufString;
				}
				
				currentcase=0;
				break;
			default:return;
			}
	}
	
	
	@Override//結析到Element結尾時用的method
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if(localName.equals("title")){
			
		}else if(localName.equals("item")){
				li.add(news);
		}
//		Log.i(tag,"endElement");
	}
	
	
	@Override//XML文件結束解析時呼叫此方法
	public void endDocument() throws SAXException {
		Log.i(tag, "endDocument");
	}

}
