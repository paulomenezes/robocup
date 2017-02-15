package test;

import simple_soccer_lib.AbstractTeam;
import simple_soccer_lib.PlayerCommander;

public class CommandTeam extends AbstractTeam {

	public CommandTeam() {
		super("COMM", 1);
	}

	@Override
	protected void launchPlayer(int ag, PlayerCommander commander) {
		System.out.println("Player lan�ado");
		CommandPlayer p = new CommandPlayer(commander);
		p.start();
	}

	
}
