package br.ufrpe;

import java.util.ArrayList;
import java.util.UUID;

import simple_soccer_lib.PlayerCommander;
import simple_soccer_lib.perception.FieldPerception;
import simple_soccer_lib.perception.MatchPerception;
import simple_soccer_lib.perception.PlayerPerception;
import simple_soccer_lib.utils.Vector2D;

public class Player extends Thread {
	private int LOOP_INTERVAL = 100;  //0.1s
	private double ERROR = 0.9;
	private double ERROR_RUN_BALL = 0.9;
	
	private PlayerCommander commander;
	private MatchPerception matchPerc;
	private PlayerPerception selfPerc;
	private FieldPerception  fieldPerc;
	
	private Vector2D initialPosition;
	private String uuid;
	
	private PlayerStatus status;
	private PlayerType type;
	
	public Player(PlayerCommander player, double x, double y, PlayerType type) {
		commander = player;
		
		initialPosition = new Vector2D(x, y);
		
		uuid = UUID.randomUUID().toString();
		
		status = PlayerStatus.Idle;
		this.type = type;
	}

	@Override
	public void run() {
		long nextIteration = System.currentTimeMillis() + LOOP_INTERVAL;
		boolean beforeGame = true;
		
		// 52,0 - -52,0

		while (true) {
			try {
				updatePerceptions();
				
				switch (matchPerc.getState()) {
					case MatchPerception.MatchState.FREE_KICK_LEFT:
					case MatchPerception.MatchState.FREE_KICK_RIGHT:
					case MatchPerception.MatchState.AFTER_GOAL_LEFT:
					case MatchPerception.MatchState.AFTER_GOAL_RIGHT:
					case MatchPerception.MatchState.BEFORE_KICK_OFF:
					case MatchPerception.MatchState.KICK_OFF_LEFT:
					case MatchPerception.MatchState.KICK_OFF_RIGHT:
						if (beforeGame) {
							//System.out.println(selfPerc.getTeam() + ", " + selfPerc.getUniformNumber() + ": Move: " + initialPosition);
							commander.doMoveBlocking(initialPosition.getX(), initialPosition.getY());
							beforeGame = false;
						}

						break;
					case MatchPerception.MatchState.PLAY_ON:
						HasTheBall whoHasTheBall = Memory.teamHasTheBall(fieldPerc, selfPerc.getTeam());
						
						//System.out.println(whoHasTheBall);
						
						if (type == PlayerType.FieldPlayer) {
							switch (status) {
								case Idle:
									if (whoHasTheBall == HasTheBall.YourTeam) {
										status = PlayerStatus.Attack;
									} else if (whoHasTheBall == HasTheBall.Opponent) {
										status = PlayerStatus.Steal;
									} else {
										status = PlayerStatus.Pursue;
									}
									break;
								case Attack:
									boolean kickToGoal = true;
									
									/*if (selfPerc.getState() == PlayerPerception.PlayerState.HAS_BALL) {
										ArrayList<PlayerPerception> myTeam = fieldPerc.getTeamPlayers(selfPerc.getSide());
										for (PlayerPerception player : myTeam) {
											if (player.getPosition() != null) {
												if (selfPerc.getSide() == 1) {
													if (player.getPosition().getX() > selfPerc.getPosition().getX() + 5) {
														System.out.println("Kick ball to someone");
														kickToPoint(player.getPosition().getX(), player.getPosition().getY());
														kickToGoal = false;
														break;
													}
												} else {
													if (player.getPosition().getX() < selfPerc.getPosition().getX() - 5) {
														System.out.println("Kick ball to someone");
														kickToPoint(player.getPosition().getX(), player.getPosition().getY());
														kickToGoal = false;
														break;
													}
												}
											}
										}
									}*/
									
									if (kickToGoal) {
										if (selfPerc.getSide() == 1)
											kickToPoint(52, 0);
										else
											kickToPoint(-52, 0);
									}
	
									runToBall(ERROR_RUN_BALL);
									
									if (whoHasTheBall == HasTheBall. Opponent) {
										status = PlayerStatus.Steal;
									} else if (whoHasTheBall == HasTheBall.NoOne) {
										status = PlayerStatus.Pursue;
									}
									
									break;
								case Pursue:
									//System.out.println("Pursue: " + Vector2D.distance(fieldPerc.getBall().getPosition(), selfPerc.getPosition()));
									if (Vector2D.distance(fieldPerc.getBall().getPosition(), selfPerc.getPosition()) > 25) {
										status = PlayerStatus.Defend;
									} else {
										runToBall(ERROR_RUN_BALL);
									}
									
									if (whoHasTheBall == HasTheBall.YourTeam) {
										status = PlayerStatus.Attack;
									} else if (whoHasTheBall == HasTheBall.Opponent) {
										status = PlayerStatus.Steal;
									}
									
									break;
								case Steal:
									//System.out.println("Steal: " + Vector2D.distance(fieldPerc.getBall().getPosition(), selfPerc.getPosition()));
									
									if (whoHasTheBall == HasTheBall.NoOne) {
										status = PlayerStatus.Pursue;
									} else if (whoHasTheBall == HasTheBall.YourTeam) {
										status = PlayerStatus.Attack;
									} else {
										if (Vector2D.distance(fieldPerc.getBall().getPosition(), selfPerc.getPosition()) > 35) {
											status = PlayerStatus.Defend;
										} else {
											runToBall(ERROR_RUN_BALL);
										}
									}
									
									break;
								case Defend:
									initialPosition.setX(initialPosition.getX() * selfPerc.getSide());
									
									runToPoint(initialPosition, ERROR);
									//System.out.println(selfPerc.getTeam() + ", " + selfPerc.getUniformNumber() + ": Move: " + initialPosition);
									
									if (whoHasTheBall == HasTheBall.NoOne) {
										status = PlayerStatus.Pursue;
									} else if (whoHasTheBall == HasTheBall.YourTeam) {
										status = PlayerStatus.Attack;
									} else if (Vector2D.distance(fieldPerc.getBall().getPosition(), selfPerc.getPosition()) < 35) {
										status = PlayerStatus.Pursue;
									}
									break;
								case Patrol:
									break;
							}

							//System.out.println(selfPerc.getTeam() + ", " + selfPerc.getUniformNumber() + ": " + status + " : " + type);

						} else {
							if (selfPerc.getPosition().distanceTo(fieldPerc.getBall().getPosition()) < 10) {
								runToBall(ERROR_RUN_BALL);
								
								if (selfPerc.getSide() == 1)
									kickToPoint(52, 0);
								else
									kickToPoint(-52, 0);
							} else {
								if (selfPerc.getSide() == 1)
									runToPoint(new Vector2D(-52, 0), ERROR);
								else
									runToPoint(new Vector2D(52, 0), ERROR);
								
								turnToBall();
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
	
	private void turnToPoint(double x, double y){
		Vector2D myPos = selfPerc.getPosition();
		Vector2D point = new Vector2D(x, y);
		Vector2D newDirection = point.sub(myPos);
		
		commander.doTurnToDirectionBlocking(newDirection);		
	}
		
	private void turnToBall() {
		Vector2D ballPos = fieldPerc.getBall().getPosition();
		Vector2D myPos = selfPerc.getPosition();
		
		Vector2D newDirection = ballPos.sub(myPos);
		
		commander.doTurnToDirectionBlocking(newDirection);		
	}
	
	private void runToBall(double margin) {
		runToPoint(fieldPerc.getBall().getPosition(), margin);
    }
	
	private void runToPoint(Vector2D position, double margin) {
		double velocityMax = 80d;
		double velocityMin = 10d;
		
		int countEquals = 0;
		double lastDistance = 0;
		
        if (matchPerc != null) {
            double i = velocityMax;
            while (selfPerc.getPosition().distanceTo(position) > margin) {
            	if (!isAlignToPoint(position, 25)) {
                    turnToPoint(position.getX(), position.getY());
                }
            	
                commander.doDashBlocking(i);
           
                i = position.distanceTo(selfPerc.getPosition()) * 40;
                if (i < velocityMin) i = velocityMin;
                else if (i > velocityMax) i = velocityMax;
            
                updatePerceptions();
                
                if (selfPerc.getPosition().distanceTo(fieldPerc.getBall().getPosition()) < 10)
                	break;
                
                /*System.out.println("antes: " + selfPerc.getPosition().distanceTo(position));
                System.out.println("depois: " + selfPerc.getPosition().distanceTo(position));
                
                if (Math.abs(selfPerc.getPosition().distanceTo(position) - lastDistance) < 1) {
                	countEquals++;
                	
                	if (countEquals > 5) {
                		System.out.println("Break run to point");
                		break;
                	}
                } else {
                	countEquals = 0;
                }*/
                
                lastDistance = selfPerc.getPosition().distanceTo(position);
            }
        }
	}
 
	private boolean isAlignToPoint(Vector2D point, double margin){
        double angle = point.sub(selfPerc.getPosition()).angleFrom(selfPerc.getDirection());
        return angle < margin && angle > margin*(-1);
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
