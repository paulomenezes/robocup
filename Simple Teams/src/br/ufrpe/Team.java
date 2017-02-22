package br.ufrpe;

import simple_soccer_lib.AbstractTeam;
import simple_soccer_lib.PlayerCommander;

public class Team extends AbstractTeam {
	int team;

	public Team(int team) {
		super("Time" + team, 2);
		
		this.team = team;
	}

	@Override
	protected void launchPlayer(int ag, PlayerCommander commander) {
		double targetX, targetY;
		
		if (ag == 0) {
			targetY = 34.0d / 2;
		} else {
			targetY = -34.0d / 2;
		}
		
		if (team == 2) {
			targetX = 52.5d / 2;
		} else {
			targetX = -52.5d / 2;
		}
		
		System.out.println("Player: " + ag + " Time: " + team + " X: " + targetX + " Y: " + targetY);

		Player p = new Player(commander, targetX, targetY);
		p.start();
	}	
}
