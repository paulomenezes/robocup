package br.ufrpe;

import java.util.UUID;

import simple_soccer_lib.PlayerCommander;
import simple_soccer_lib.perception.FieldPerception;
import simple_soccer_lib.perception.MatchPerception;
import simple_soccer_lib.perception.PlayerPerception;
import simple_soccer_lib.utils.Vector2D;

public class Player extends Thread {
	private int LOOP_INTERVAL = 100;  //0.1s
	private double ERROR = 0.5;
	
	private PlayerCommander commander;
	private MatchPerception matchPerc;
	private PlayerPerception selfPerc;
	private FieldPerception  fieldPerc;
	
	private Vector2D initialPosition;
	private String uuid;
	
	/**
	 * MOVE:							M -25 0			-> X e Y do ponto
	 * KICK:							K 100 20		-> Intensidade e angulo relativo
	 * RUN:								R 100			-> Intensidade
	 * TURN:							T 90			-> Angulo
	 * TURN TO DIRECTION:               TD  0 1         -> Vetor de direcao X, Y
	 * TURN TO POINT: 					TTP	-10 5		-> X e Y do ponto
	 * TURN TO BALL: 					TTB
	 * RUN TO POINT:					RTP -10 5 3		-> X, Y do ponto e taxa de erro
	 * RUN TO BALL: 					RTB 3			-> Taxa de erro
	 * SHOW PLAYER INFO (ALL):			SP
	 * SHOW PLAYER POSITION:			SPPOS
	 * SHOW PLAYER DIRECTION:			SPDIR
	 * SHOW PLAYER TEAM NAME:			SPTEA
	 * SHOW PLAYER UNIFORM NUMBER:		SPUNI
	 * SHOW TIME:						STIME
	 * SHOW QNT. PLAYERS IN VIEW:		SQPLS
	 * SHOW PLAYER INFO:				SPLIN BTEAM 5	-> Nome do time e numero do jogador
	 * SHOW QNT. PLAYERS SPECIFC TEAM:	SQPSS BTEAM		-> Nome do time
	 * SHOW BALL POSITION:				SBPOS
	 **/
	
	/* Observações:
	 * - Posições bagunçadas do jogador e da bola (Ex: x = -400 e 0);
	 * - Leitura das percepções do monitor pode estar causando esse erro;
	 * - Turn 50 seguido de -50 não retorna a posicição
	 * - Run 100 é andar aproximadamente 1 unidade de distancia, run 50 é andar aproximadamente metade e assim por diante
	 * - Kick 100 é chutar aproximadamente 40 unidades de distancia, kick 50 é chutar aproximadamente metade e assim por diante.
	 */
	
	public Player(PlayerCommander player, double x, double y) {
		commander = player;
		
		initialPosition = new Vector2D(x, y);
		
		uuid = UUID.randomUUID().toString();
	}

	@Override
	public void run() {
		long nextIteration = System.currentTimeMillis() + LOOP_INTERVAL;
		boolean beforeGame = true;

		while (true) {
			try {
				updatePerceptions();
				
				switch (matchPerc.getState()) {
					case MatchPerception.MatchState.BEFORE_KICK_OFF:
						if (beforeGame) {
							commander.doMoveBlocking(initialPosition.getX(), initialPosition.getY());
							beforeGame = false;
						}

						break;
					case MatchPerception.MatchState.KICK_OFF_LEFT:
						if (selfPerc.getSide() == 1) {
							if (selfPerc.getUniformNumber() == 2) {
								runToBall(ERROR);

								if (selfPerc.getState() == PlayerPerception.PlayerStatus.HAS_THE_BALL) {
									System.out.println("HAS_THE_BALL");
									PlayerPerception p = fieldPerc.getTeamPlayer(selfPerc.getTeam(), 1);
									
									kickToPoint(p.getPosition().getX(), p.getPosition().getY());
									Memory.kickOffFirstKick = uuid;
								}
							} else {
								turnToBall();
								commander.doDashBlocking(30.0d);
							}
						}
						break;
					case MatchPerception.MatchState.PLAY_ON:
						if (!Memory.isTheFirstKicker(uuid)) {
							runToBall(ERROR);
							if (selfPerc.getState() == PlayerPerception.PlayerStatus.HAS_THE_BALL) {
								PlayerPerception p = fieldPerc.getTeamPlayer(selfPerc.getTeam(), 1);;
								System.out.println("Has the ball, kick: " + p.getPosition());
								
								commander.doKick(100, selfPerc.getPosition().angleFrom(p.getPosition()));
								
								Memory.kickOffFirstKick = null;								
							}
						}
						break;
				}
				
				sleepUntil(nextIteration);
				nextIteration += LOOP_INTERVAL;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
		
	private void kickToPoint(double x, double y) {
        Vector2D myPos = selfPerc.getPosition();
        Vector2D point = new Vector2D(x, y);
        Vector2D newDirection = point.sub(myPos);
               
        commander.doTurnToDirectionBlocking(newDirection);
       
        double intensity = (newDirection.magnitude() * 100) / 40;
        if (intensity > 100){
            intensity = 100;
        }
        
        commander.doKickBlocking(intensity, 0);
    }
	
	private void turnToBall() {
		Vector2D ballPos = fieldPerc.getBall().getPosition();
		Vector2D myPos = selfPerc.getPosition();
		
		Vector2D newDirection = ballPos.sub(myPos);
		
		commander.doTurnToDirectionBlocking(newDirection);		
	}
	
	private void runToBall(double erro) {
		while (Math.abs(selfPerc.getPosition().getX() - fieldPerc.getBall().getPosition().getX()) > erro ||
				Math.abs(selfPerc.getPosition().getY() - fieldPerc.getBall().getPosition().getY()) > erro) {
			turnToBall();
			commander.doDashBlocking(100.0d);
			updatePerceptions();
		}
	}
	
	private void turnToPoint(double x, double y){
		Vector2D myPos = selfPerc.getDirection();
		Vector2D point = new Vector2D(x, y);
		Vector2D newDirection = point.sub(myPos);
		
		System.out.println(" => Point = " + point + " -- Player = " + myPos + " -- New Direction = " + newDirection);
		commander.doTurnToDirectionBlocking(newDirection);		
	}
	
	private void runToPoint(double x, double y, double erro) {
		Vector2D point = new Vector2D(x, y);
		while (Math.abs(selfPerc.getPosition().getX() - point.getX()) > erro ||
				Math.abs(selfPerc.getPosition().getY() - point.getY()) > erro) {
			turnToPoint(x, y);
			commander.doDashBlocking(100.0d);
			updatePerceptions();
		}
	}
	
	private boolean isAlignedToBall(double error) {
		Vector2D ballPos = fieldPerc.getBall().getPosition();
		Vector2D myPos = selfPerc.getPosition();
		
		if (ballPos == null || myPos == null) {
			return false;			
		}
		//System.out.println("Vetores: " + ballPos.sub(myPos) + "  " + selfPerc.getDirection());
		
		//double angle = Vector2D.angle(ballPos.sub(myPos), selfPerc.getDirection());
		double angle = selfPerc.getDirection().angleFrom(ballPos.sub(myPos));

		System.out.println(" => Angulo agente-bola: " + angle);
		
		return angle < error && angle > error; // 15 e -15
	}
		
	private void updatePerceptions() {
		PlayerPerception newSelf = commander.perceiveSelfBlocking();
		FieldPerception newField = commander.perceiveFieldBlocking();
		MatchPerception newMatch = commander.perceiveMatchBlocking();
		
		if (newSelf != null) {
			this.selfPerc = newSelf;
		}
		
		if (newField != null) {
			this.fieldPerc = newField;
		}
		
		if  (newMatch != null) {
			this.matchPerc = newMatch;
		}
	}

	private void sleepUntil(long timeMillis) {
		long diff = timeMillis - System.currentTimeMillis();
		if (diff < 0) {
//			System.out.println("System is too slow or this client is processing too much!)");
			//this.LOOP_INTERVAL = 30;
		} else { 
			try {
				Thread.sleep(diff);
			} catch (InterruptedException e) {
				//e.printStackTrace();
				System.out.println("Sleep interrupted, but going on...");
			}
		}
	}

}
