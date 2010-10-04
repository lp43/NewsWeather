package com.camangi.rssreader;

import android.app.Activity;
import android.content.Context;

public class Transfer{
	
	public static final int for_channel=1;
	public static final int what_do_you_want=2;
	public static final int hide=3;
	public static final int rename=4;
	public static final int delete=6;
	public static final int back=7;
	public static final int rename_for=8;
	public static final int ok=9;
	public static final int cancel=10;
	public static final int error=11;
	public static final int enter_completely_to_rename=12;
	public static final int are_you_sure_delete=13;
	public static final int attention=14;
	public static final int add_channel=15;
	public static final int author=16;
	public static final int copyright=17;
	public static final int about=18;
	public static final int report_problem=19;
	public static final int loading=20;
	public static final int least_one_channel=21;
	public static final int reset=22;
	public static final int loading_completely_to_action=23;
	public static final int parse_error=24;
	public static final int cant_parse=25;
	public static final int error_reason=26;
	public static final int click_to_wifi=27;
	public static final int cant_detect_internet=28;
//	public static final int for_channel=1;
//	public static final int for_channel=1;
//	public static final int for_channel=1;
	public static String transfer(Context context,int variable){
		String RtoString="";
		
		switch (variable){
		case for_channel:
			RtoString=(String) context.getResources().getText(R.string.for_channel);
			break;
			
		case what_do_you_want:
			RtoString = (String) context.getResources().getText(R.string.what_do_you_want);
			break;
			
		case hide:
			RtoString=(String) context.getResources().getText(R.string.hide);
			break;
			
		case rename:
			RtoString= (String) context.getResources().getText(R.string.rename);
			break;
			
		case delete:
			RtoString=(String) context.getResources().getText(R.string.delete);
			break;
			
		case back:
			RtoString= (String) context.getResources().getText(R.string.back);
			break;
			
		case rename_for:
			RtoString= (String) context.getResources().getText(R.string.rename_for);
			break;	
			
		case ok:
			RtoString= (String) context.getResources().getText(R.string.ok);
			break;	
			
		case error:
			RtoString= (String) context.getResources().getText(R.string.error);
			break;	
			
		case enter_completely_to_rename:
			RtoString= (String) context.getResources().getText(R.string.enter_completely_to_rename);
			break;	
			
		case are_you_sure_delete:
			RtoString= (String) context.getResources().getText(R.string.are_you_sure_delete);
			break;
			
		case attention:
			RtoString= (String) context.getResources().getText(R.string.attention);
			break;
			
		case add_channel:
			RtoString= (String) context.getResources().getText(R.string.add_channel);
			break;
			
		case author:
			RtoString= (String) context.getResources().getText(R.string.author);
			break;
			
		case copyright:
			RtoString= (String) context.getResources().getText(R.string.copyright);
			break;
			
		case about:
			RtoString= (String) context.getResources().getText(R.string.about);
			break;
			
		case report_problem:
			RtoString= (String) context.getResources().getText(R.string.report_problem);
			break;
			
		case loading:
			RtoString= (String) context.getResources().getText(R.string.loading);
			break;
			
		case least_one_channel:
			RtoString= (String) context.getResources().getText(R.string.least_one_channel);
			break;
			
		case reset:
			RtoString= (String) context.getResources().getText(R.string.reset);
			break;
			
		case loading_completely_to_action:
			RtoString= (String) context.getResources().getText(R.string.loading_completely_to_action);
			break;
		
		case parse_error:
			RtoString= (String) context.getResources().getText(R.string.parse_error);
			break;
			
		case cant_parse:
			RtoString= (String) context.getResources().getText(R.string.cant_parse);
			break;
			
		case error_reason:
			RtoString= (String) context.getResources().getText(error_reason);
			break;
			
		case click_to_wifi:
			RtoString= (String) context.getResources().getText(R.string.click_to_wifi);
			break;
			
		case cant_detect_internet:
			RtoString= (String) context.getResources().getText(R.string.cant_detect_internet);
			break;
			
//		case enter_completely_to_rename:
//			RtoString= (String) context.getResources().getText(R.string.enter_completely_to_rename);
//			break;
			
//		case enter_completely_to_rename:
//			RtoString= (String) context.getResources().getText(R.string.enter_completely_to_rename);
//			break;
		
//		case enter_completely_to_rename:
//			RtoString= (String) context.getResources().getText(R.string.enter_completely_to_rename);
//			break;
		}

		
		
		return RtoString;
	}
		
}
