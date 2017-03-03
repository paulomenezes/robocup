package simple_soccer_lib.perception;

import java.util.ArrayList;

/**
 * Guarda apenas os objetos móveis do campo: jogadores e bola.
 *
 */
public class FieldPerception {
	private int time; //tempo!
	
	private ArrayList<PlayerPerception> playersA;
	private ArrayList<PlayerPerception> playersB;
	private ObjectPerception ball;

	public FieldPerception() {
		this.playersA = new ArrayList<PlayerPerception>();
		this.playersB = new ArrayList<PlayerPerception>();
		this.ball = new ObjectPerception();
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public ArrayList<PlayerPerception> getAllPlayers() {
		ArrayList<PlayerPerception> all = new ArrayList<>();
		all.addAll(playersA);
		all.addAll(playersB);
		return all;
	}
	
	public ObjectPerception getBall() {
		return ball;
	}

	public void setBall(ObjectPerception ball) {
		this.ball = ball;
	}

	//ATENCAO: este metodo faz "this" compartilhar objetos com "other"
	public void overwrite(FieldPerception other) {
		this.time = other.time;
		this.ball = other.ball;
		this.playersA.clear();
		this.playersA.addAll(other.playersA);
		this.playersB.clear();
		this.playersB.addAll(other.playersB);
	}
	
	//metodos get diversos...
	
	//metodo para retonar os players por time...
	
	public void setAllPlayersA(ArrayList<PlayerPerception> playersA){
		this.playersA.clear();
		this.playersA.addAll(playersA);
	}
	
	public void setAllPlayersB(ArrayList<PlayerPerception> playersB){
		this.playersB.clear();
		this.playersB.addAll(playersB);
	}
	
	public ArrayList<PlayerPerception> getTeamPlayers(int side){
		if(side == 1)
			return playersA;
		else if (side == -1)
			return playersB;
		else 
			return null;
	}
	
	public PlayerPerception getTeamPlayerA(int uniformNumber){
		try{
			return playersA.get(uniformNumber-1);
		}catch (Exception e) {e.printStackTrace();/*caso ocorra erro de index */}		
		return null;
	}
	
	public PlayerPerception getTeamPlayerB(int uniformNumber){
		try{
			return playersB.get(uniformNumber-1);
		}catch (Exception e) {e.printStackTrace();/*caso ocorra erro de index */}		
		return null;
	}
	
	public PlayerPerception getTeamPlayer(int side, int uniformNumber){
		try{
			if(side == 1)
				return playersA.get(uniformNumber-1);	
			else if(side == -1)
				return playersB.get(uniformNumber-1);	
		}catch (Exception e) {e.printStackTrace();/*caso ocorra erro de index */}		
		return null;
	}
	
	/**
	 * @deprecated Use {@link #getTeamPlayer(int, int)} for best efficiency.
	 * */
	@Deprecated
	public PlayerPerception getTeamPlayer(String teamName, int uniformNumber) {
		ArrayList<PlayerPerception> all = getAllPlayers();
		for (PlayerPerception player : all) {
			if (player.getTeam() != null && player.getTeam().equals(teamName)
					&& player.getUniformNumber() == uniformNumber) {
				return player;
			}
		}
		return null;
	}

	/**
	 * @Deprecated Use {@link #getTeamPlayers(int)} for best efficiency.
	 * */
	@Deprecated
	public ArrayList<PlayerPerception> getTeamPlayers(String teamName) {
		ArrayList<PlayerPerception> lpp = new ArrayList<PlayerPerception>();
		ArrayList<PlayerPerception> all = getAllPlayers();
		for (PlayerPerception playerPerception : all) {
			if (playerPerception.getTeam() != null && playerPerception.getTeam().equals(teamName)) {
				lpp.add(playerPerception);
			}
		}
		return lpp;
	}
	
//	public FieldPerception copy() {
//	}
	
}