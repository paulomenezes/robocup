package br.ufrpe;

import java.util.ArrayList;
import java.util.List;

import simple_soccer_lib.perception.FieldPerception;
import simple_soccer_lib.perception.ObjectPerception;
import simple_soccer_lib.perception.PlayerPerception;
import simple_soccer_lib.utils.Vector2D;

public class Memory {
	public static boolean 	RECEIVER_ON = false;
	public static int 		RECEIVER_SIDE = -1;
	public static int 		RECEIVER_UNIFORM = -1;
	
	public static HasTheBall teamHasTheBall(FieldPerception fieldPerception, String team) {
		HasTheBall hasTheBall = HasTheBall.NoOne;
		
		ObjectPerception ball = fieldPerception.getBall();
		
		ArrayList<PlayerPerception> players = fieldPerception.getAllPlayers();
		for (PlayerPerception player : players) {
			if (player.getPosition() != null) {
				if (Vector2D.distance(player.getPosition(), ball.getPosition()) <= 5) {
					if (player.getTeam().equals(team)) {
						hasTheBall = HasTheBall.YourTeam;
						break;
					} else {
						hasTheBall = HasTheBall.Opponent;
					}
				}
			}
		}
		
		return hasTheBall;
	}
	
	public static boolean nearToBall(PlayerPerception player, FieldPerception field) {
		List<PlayerPerception> players = field.getTeamPlayers(player.getSide());
		
		ObjectPerception ball = field.getBall();
		
		double maxD = 999;
		int index = 0;
		
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i) != null && players.get(i).getPosition() != null && i > 3) {
				double d = ball.getPosition().distanceTo(players.get(i).getPosition());
				if (d < maxD) {
					maxD = d;
					index = players.get(i).getUniformNumber();
				}
			}
		}
		
		return index == player.getUniformNumber();
	}
	
	public static PlayerPerception otherAttacker(PlayerPerception player, FieldPerception field) {
		if (player.getUniformNumber() == 5)
			return field.getTeamPlayer(player.getSide(), 6);
		else if (player.getUniformNumber() == 6)
			return field.getTeamPlayer(player.getSide(), 5);
		
		return null;
	}
	
	public static boolean attackersCloser(PlayerPerception player, FieldPerception field) {
		PlayerPerception attack1 = field.getTeamPlayer(player.getSide(), 5);
		PlayerPerception attack2 = field.getTeamPlayer(player.getSide(), 6);
		
		if (attack1.getPosition().distanceTo(attack2.getPosition()) < 10) {
			return true;
		}
		
		return false;
	}
	
	public static double distanceFromEnemies(PlayerPerception player, FieldPerception field) {
		List<PlayerPerception> opponents = field.getTeamPlayers(player.getSide() == 1 ? -1 : 1);
		
		double maxD = 999;
		
		for (int i = 0; i < opponents.size(); i++) {
			if (opponents.get(i) != null && opponents.get(i).getPosition() != null) {
				double d = opponents.get(i).getPosition().distanceTo(player.getPosition());
				if (d < maxD) {
					maxD = d;
				}
			}
		}
		
		return maxD;
	}
}