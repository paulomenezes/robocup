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
		double[] XValues = new double[] { -50, -30, -30, -10, -10 };
		double[] YValues = new double[] { 0, -20, 20, -15, 15 };
		
		double targetX = XValues[ag];
		double targetY = YValues[ag];
		
		Player p = new Player(commander, targetX, targetY, ag == 0 ? PlayerType.GoalKeeper : PlayerType.FieldPlayer);
		p.start();
	}	
}
