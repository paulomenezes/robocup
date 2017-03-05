package br.ufrpe;

import java.util.ArrayList;

import simple_soccer_lib.perception.FieldPerception;
import simple_soccer_lib.perception.ObjectPerception;
import simple_soccer_lib.perception.PlayerPerception;
import simple_soccer_lib.utils.Vector2D;

public class Memory {
	public static PlayerPerception hasBall(FieldPerception fieldPerception) {
		PlayerPerception hasTheBall = null;
		
		ObjectPerception ball = fieldPerception.getBall();
		
		ArrayList<PlayerPerception> players = fieldPerception.getAllPlayers();
		for (PlayerPerception player : players) {
			if (player.getPosition() != null) {
				if (nearestTheBall(player, fieldPerception)) {
					if (Vector2D.distance(player.getPosition(), ball.getPosition()) <= 5) {
						hasTheBall = player;
						break;
					}
				}
			}
		}
		
		return hasTheBall;
	}
	
	public static boolean nearestTheBall(PlayerPerception perception, FieldPerception field) {
		ObjectPerception ball = field.getBall();
		
		double nearest = 999;
		int numberNearest = -1;
		
		ArrayList<PlayerPerception> players = field.getTeamPlayers(perception.getSide());
		for (PlayerPerception player : players) {
			if (player.getPosition() != null) {
				double distance = Vector2D.distance(player.getPosition(), ball.getPosition());
				if (distance <= nearest) {
					numberNearest = player.getUniformNumber();
					nearest = distance;
				}
			}
		}
			
		return perception.getUniformNumber() == numberNearest;
	}
}
