package br.ufrpe;

import java.net.UnknownHostException;

public class Game {

	public static void main(String[] args) throws UnknownHostException {
		Team team1 = new Team(1);
		team1.launchTeamAndServer();

		//Team team2 = new Team(2);
		//team2.launchTeam();
	}
}
