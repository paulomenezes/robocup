package br.ufrpe;

import simple_soccer_lib.AbstractTeam;
import simple_soccer_lib.PlayerCommander;
import simple_soccer_lib.perception.PlayerPerception;
import simple_soccer_lib.utils.Vector2D;

public class Team extends AbstractTeam {
	int team;
	
	public PlayerPerception controller, lastController; 
	public PlayerPerception receptor;
	public PlayerPerception nearest;
	public PlayerPerception support;
	
	public TeamState teamState = TeamState.Defending;

	public Team(int team) {
		super("Time" + team, 5);
		
		this.team = team;
	}

	@Override
	protected void launchPlayer(int ag, PlayerCommander commander) {
		double[] XDefense = new double[] { -50, -30, -30, -10, -10 };
		double[] YDefense = new double[] { 0, -20, 20, -15, 15 };

		double[] XAttack = new double[] { -50, -28, -10, 30, 30 };
		double[] YAttack = new double[] { 0, 0, 20, -20, 20 };
		
		double targetX = XDefense[ag];
		double targetY = YDefense[ag];
		
		Player p = new Player(commander, new Vector2D(targetX, targetY), new Vector2D(XAttack[ag], YAttack[ag]), 
				this, ag == 0 ? PlayerType.GoalKeeper : PlayerType.FieldPlayer);
		p.start();
	}
	
	public enum TeamState {
		Attacking,
		Defending
	}
}
