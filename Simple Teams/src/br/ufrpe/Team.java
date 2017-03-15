package br.ufrpe;

import simple_soccer_lib.AbstractTeam;
import simple_soccer_lib.PlayerCommander;

public class Team extends AbstractTeam {
	int team;

	public Team(int team) {
		super("BCC" + team, 6);
		
		this.team = team;
	}

	@Override
	protected void launchPlayer(int ag, PlayerCommander commander) {
		Player p = new Player(commander);
		p.start();
	}	
}
