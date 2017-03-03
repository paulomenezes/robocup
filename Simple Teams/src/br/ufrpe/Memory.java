package br.ufrpe;

import java.util.ArrayList;

import simple_soccer_lib.perception.FieldPerception;
import simple_soccer_lib.perception.ObjectPerception;
import simple_soccer_lib.perception.PlayerPerception;
import simple_soccer_lib.utils.Vector2D;

public class Memory {
	public static HasTheBall teamHasTheBall(FieldPerception fieldPerception, String team) {
		HasTheBall hasTheBall = HasTheBall.NoOne;
		
		ObjectPerception ball = fieldPerception.getBall();
		
		ArrayList<PlayerPerception> players = fieldPerception.getAllPlayers();
		for (PlayerPerception player : players) {
			if (player.getState() == PlayerPerception.PlayerState.HAS_BALL) {
				if (player.getTeam().equals(team)) {
					hasTheBall = HasTheBall.YourTeam;
				} else {
					hasTheBall = HasTheBall.NoOne;
				}
				
				if (hasTheBall == HasTheBall.YourTeam) break;
			}
		}
		
		if (hasTheBall == HasTheBall.NoOne && ball != null) {
			for (PlayerPerception player : players) {
				if (player.getPosition() != null) {
					if (Vector2D.distance(player.getPosition(), ball.getPosition()) <= 5) {
						if (player.getTeam().equals(team)) {
							hasTheBall = HasTheBall.YourTeam;
						} else {
							hasTheBall = HasTheBall.NoOne;
						}
						
						if (hasTheBall == HasTheBall.YourTeam) break;
					}
				}
			}
		}
		
		return hasTheBall;
	}
}
