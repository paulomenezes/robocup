package simple_soccer_lib.perception;

public class MatchPerception {
	/*
	 * Nome dos times
	 * Tempo da partida
	 * Lado de cada time
	 * Estado da partida
	 */
	private String teamAName;
	private String teamBName;
	
	private int teamASide;
	private int teamBSide;
	
	private int teamAScore;
	private int teamBScore;
	
	private int time;	
	private int state;

	public MatchPerception() {}

	public MatchPerception(String teamAName, String teamBName, int teamASide,
			int teamBSide, int teamAScore, int teamBScore, int time, int state) {
		this.teamAName = teamAName;
		this.teamBName = teamBName;
		this.teamASide = teamASide;
		this.teamBSide = teamBSide;
		this.teamAScore = teamAScore;
		this.teamBScore = teamBScore;
		this.time = time;
		this.state = state;
	}

	public String getTeamAName() {
		return teamAName;
	}
	
	public void setTeamAName(String TeamAName) {
		this.teamAName = TeamAName;
	}
	
	public String getTeamBName() {
		return teamBName;
	}
	
	public void setTeamBName(String TeamBName) {
		this.teamBName = TeamBName;
	}
	
	public int getTeamASide() {
		return teamASide;
	}
	
	public void setTeamASide(int teamASide) {
		this.teamASide = teamASide;
	}
	
	public int getTeamBSide() {
		return teamBSide;
	}
	
	public void setTeamBSide(int teamBSide) {
		this.teamBSide = teamBSide;
	}
	
	public int getTeamAScore() {
		return teamAScore;
	}

	public void setTeamAScore(int teamAScore) {
		this.teamAScore = teamAScore;
	}

	public int getTeamBScore() {
		return teamBScore;
	}

	public void setTeamBScore(int teamBScore) {
		this.teamBScore = teamBScore;
	}

	public int getTime() {
		return time;
	}
	
	public void setTime(int time) {
		this.time = time;
	}
	
	public int getState() {
		return state;
	}
	
	public void setState(int state) {
		this.state = state;
	}

	public void overwrite(MatchPerception matchPerception) {
		this.teamAName = matchPerception.getTeamAName();
		this.teamBName = matchPerception.getTeamBName();
		this.teamAScore = matchPerception.getTeamAScore();
		this.teamBScore = matchPerception.getTeamBScore();
		this.teamASide = matchPerception.getTeamASide();
		this.teamBSide = matchPerception.getTeamBSide();
		this.time = matchPerception.getTime();
		this.state = matchPerception.getState();
	}
	
	public MatchPerception clone(){
		return new MatchPerception(teamAName, teamBName, teamASide,
				teamBSide, teamAScore, teamBScore, time, state);
	}
	
	public class MatchState{
		 public static final int
		 NULL 				= 0,
		 BEFORE_KICK_OFF 	= 1,
		 TIME_OVER 			= 2,
		 PLAY_ON 			= 3,
		 KICK_OFF_LEFT 		= 4,
		 KICK_OFF_RIGHT 	= 5,
		 KICK_IN_LEFT 		= 6,
		 KICK_IN_RIGHT 		= 7,
		 FREE_KICK_LEFT 	= 8,
		 FREE_KICK_RIGHT 	= 9,
		 CORNER_KICK_LEFT 	= 10,
		 CORNER_KICK_RIGHT 	= 11,
		 GOAL_KICK_LEFT 	= 12,
		 GOAL_KICK_RIGHT 	= 13,
		 AFTER_GOAL_LEFT 	= 14,
		 AFTER_GOAL_RIGHT 	= 15,
		 DROP_BALL 			= 16,
		 OFFSIDE_LEFT 		= 17,
		 OFFSIDE_RIGHT 	= 18,
		 MAX 				= 19,
		 FREE_KICK_FAULT_LEFT = 34,
		 FREE_KICK_FAULT_RIGHT = 35;
	}
}
