package uk.co.ElmHoe.Bukkit.Utilities;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public abstract class StringUtility {
	
	public static String format(String message) {
		return message.replaceAll("(?i)&([a-z0-9])", "\u00A7$1");
	}
	
	public static boolean isNumeric(String str){
		  return str.matches("-?\\d+(\\.\\d+)?");
	}
	
	public static List<String> format(List<String> list){
		List<String> temp = new ArrayList<String>();
		for(String line : list){
			temp.add(StringUtility.format(line));
		}
		return temp;
	}
	
	public static String formatMoney(double money){
		DecimalFormat format = new DecimalFormat("$###,###.##");
		return format.format(money);
	}
	
	public static String formatNumber(int number){
		DecimalFormat format = new DecimalFormat("###,###");
		return format.format(number);
	}
	
	public static String trim(String string, int number){
		if(string.length() <= number){
			return string;
		}else{
			return string.substring(0, number);
		}
	}
	
	public static String toLowerCase(String string){
		return string.toLowerCase();
	}
	
	public static String shift(String string, int size){
		char array[] = string.toCharArray();
		char firstChar = array[0];
		for(int i = 0; i < array.length - 1; i++){
			array[i] = array[i + 1];
		}
		array[array.length - 1] = firstChar;
		if(size == -1){
			return build(array);
		}
		return build(array).substring(0, size);
	}
	
	public static String build(char array[]){
		String string = "";
		for(char chr : array){
			string += chr;
		}
		return string;
	}
}