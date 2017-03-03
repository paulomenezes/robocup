package br.ufrpe;

import simple_soccer_lib.AbstractTeam;
import simple_soccer_lib.PlayerCommander;

public class Team extends AbstractTeam {
	int team;

	public Team(int team) {
		super("Time" + team, 5);
		
		this.team = team;
	}

	@Override
	protected void launchPlayer(int ag, PlayerCommander commander) {
		double targetX, targetY;

		double[] XValues = new double[] { -50, -30, -30, -10, -10 };
		double[] YValues = new double[] { 0, -20, 20, -15, 15 };
		
		targetX = XValues[ag];
		targetY = YValues[ag];
		
		/*if (team == 1) {
			targetX *= -1;
		}*/
		
		// -50, 0 : -30, -20 : -30, 20 : -11, -15 : -11, 15
		
		System.out.println("Player: " + ag + " Time: " + team + " X: " + targetX + " Y: " + targetY);

		Player p = new Player(commander, targetX, targetY);
		p.start();
	}	
}
