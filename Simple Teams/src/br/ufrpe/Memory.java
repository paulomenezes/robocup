package br.ufrpe;

public class Memory {
	public static String kickOffFirstKick = null;
	
	public static boolean isTheFirstKicker(String uuid) {
		if (kickOffFirstKick == null)
			return false;
		else 
			return kickOffFirstKick.equals(uuid);
	}
}
