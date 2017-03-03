package simple_soccer_lib.utils;

public enum EMatchState {
	NULL,
	BEFORE_KICK_OFF,
	TIME_OVER,
	PLAY_ON,
	KICK_OFF_LEFT,
	KICK_OFF_RIGHT,
	KICK_IN_LEFT,
	KICK_IN_RIGHT,
	FREE_KICK_LEFT,
	FREE_KICK_RIGHT,
	CORNER_KICK_LEFT,
	CORNER_KICK_RIGHT,
	GOAL_KICK_LEFT,
	GOAL_KICK_RIGHT,
	AFTER_GOAL_LEFT,
	AFTER_GOAL_RIGHT,
	DROP_BALL,
	OFF_SIDE_LEFT,
	OFF_SIDE_RIGHT,
	MAX;
	
	private static final int qntState = 20;

	public static EMatchState getStateFromCode(int code){
		if(qntState-1 >= code){
			return EMatchState.values()[code];
		}
		return null;
	}
}
