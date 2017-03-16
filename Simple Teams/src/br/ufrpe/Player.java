package br.ufrpe;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
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
	
	private Vector2D lastBallPosition;
	
	public Player(PlayerCommander player) {
		commander = player;
		
		uuid = UUID.randomUUID().toString();
		
		status = PlayerStatus.Idle;
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
							double[] XValues = new double[] { -50, -30, -30, -25, -10, -10 };
							double[] YValues = new double[] { 0, -20, 20, 0, -15, 15 };

							PlayerType[] tipos = new PlayerType[] { PlayerType.GoalKeeper, PlayerType.Left, PlayerType.Right, PlayerType.Defense, PlayerType.Attack, PlayerType.Attack };
							type = tipos[selfPerc.getUniformNumber() - 1];
							
							initialPosition = new Vector2D(XValues[selfPerc.getUniformNumber() - 1], YValues[selfPerc.getUniformNumber() - 1]);
							commander.doMoveBlocking(XValues[selfPerc.getUniformNumber() - 1], YValues[selfPerc.getUniformNumber() - 1]);
							
							beforeGame = false;
						}

						break;
					case MatchPerception.MatchState.PLAY_ON:
						HasTheBall whoHasTheBall = Memory.teamHasTheBall(fieldPerc, selfPerc.getTeam());
						
						//System.out.println(whoHasTheBall);
						
						if (type == PlayerType.Attack) {
							if (Memory.RECEIVER_ON && selfPerc.getSide() == Memory.RECEIVER_SIDE) {
								if (Memory.RECEIVER_UNIFORM == selfPerc.getUniformNumber()) {
									runToBall(ERROR);
								} else {
									double x, y = 0;
									if (selfPerc.getSide() == 1) {
										x = 10;
									} else { 
										x = -10;
									}
									
									if (selfPerc.getUniformNumber() == 5) {
										y = -10;
									} else {
										y = 10;
									}
	
									//System.out.println("Jogador: " + selfPerc.getUniformNumber() + " moveu para frente da bola: " + x + ", " + y);
									runToPoint(new Vector2D(fieldPerc.getBall().getPosition().getX() + x, fieldPerc.getBall().getPosition().getY() + y), ERROR);
								}
								
								Memory.RECEIVER_ON = false;
							} else {
								if (Memory.nearToBall(selfPerc, fieldPerc)) {
									runToBall(ERROR);
									
									if ((selfPerc.getSide() == 1 && fieldPerc.getBall().getPosition().getX() >= 30) ||
										(selfPerc.getSide() == -1 && fieldPerc.getBall().getPosition().getX() <= -30)) {
										if (selfPerc.getPosition().distanceTo(fieldPerc.getBall().getPosition()) <= 1) {
											//System.out.println("Jogador: " + selfPerc.getUniformNumber() + " chutou para gol");
											if (selfPerc.getSide() == 1)
												kickToPoint(52, 0, 100);
											else
												kickToPoint(-52, 0, 100);
										}
									} else {
										if (Memory.distanceFromEnemies(selfPerc, fieldPerc) > 5) {
											//System.out.println("Jogador: " + selfPerc.getUniformNumber() + " saiu com a bola");
											if (selfPerc.getPosition().distanceTo(fieldPerc.getBall().getPosition()) <= 1) {
												if (selfPerc.getSide() == 1)
													kickToPoint(52, 0, 10);
												else
													kickToPoint(-52, 0, 10);
											}
											
											runToBall(ERROR);
										} else {
											List<PlayerPerception> players = fieldPerc.getTeamPlayers(selfPerc.getSide());
											
											double maxD = 999;
											int index = -1;
											
											for (int i = 0; i < players.size(); i++) {
												if (players.get(i) != null && players.get(i).getPosition() != null && 
													players.get(i).getUniformNumber() != selfPerc.getUniformNumber() &&
													((selfPerc.getSide() == 1 && players.get(i).getPosition().getX() > selfPerc.getPosition().getX()) || 
													 (selfPerc.getSide() == -1 && players.get(i).getPosition().getX() < selfPerc.getPosition().getX()))) {
													double d = players.get(i).getPosition().distanceTo(selfPerc.getPosition());
													if (d < maxD) {
														maxD = d;
														index = i;
													}
												}
											}

											if (index > -1) {
												//System.out.println("Receiver para " + players.get(index).getUniformNumber());
												if (!Memory.RECEIVER_ON) {
													Memory.RECEIVER_ON = true;
													Memory.RECEIVER_SIDE = selfPerc.getSide();
													Memory.RECEIVER_UNIFORM = players.get(index).getUniformNumber();
												}
												//System.out.println("Kick proximo a frente - de " + selfPerc.getUniformNumber() + " para " + players.get(index).getUniformNumber());
												kickToPoint(players.get(index).getPosition());
											} else {
												if (selfPerc.getSide() == 1)
													kickToPoint(52, 0, 100);
												else
													kickToPoint(-52, 0, 100);
											}
											
											//System.out.println("Jogador: " + selfPerc.getUniformNumber() + " perto da bola");
											//if (selfPerc.getPosition().distanceTo(fieldPerc.getBall().getPosition()) <= 3 || Memory.attackersCloser(selfPerc, fieldPerc)) {
												//System.out.println("Jogador: " + selfPerc.getUniformNumber() + " tocou");
												
												/*Memory.RECEIVER_ON = true;
												Memory.RECEIVER_SIDE = selfPerc.getSide();
												Memory.RECEIVER_UNIFORM = Memory.otherAttacker(selfPerc, fieldPerc).getSide();*/
												
												/*if (selfPerc.getPosition().getY() < 0) {
													if (selfPerc.getPosition().distanceTo(fieldPerc.getTeamPlayer(selfPerc.getSide(), 1).getPosition()) < 
															selfPerc.getPosition().distanceTo(Memory.otherAttacker(selfPerc, fieldPerc).getPosition())) {
														kickToPoint(fieldPerc.getTeamPlayer(selfPerc.getSide(), 1).getPosition());
													} else {
														kickToPoint(Memory.otherAttacker(selfPerc, fieldPerc).getPosition());
													}
												} else {
													if (selfPerc.getPosition().distanceTo(fieldPerc.getTeamPlayer(selfPerc.getSide(), 2).getPosition()) < 
															selfPerc.getPosition().distanceTo(Memory.otherAttacker(selfPerc, fieldPerc).getPosition())) {
														kickToPoint(fieldPerc.getTeamPlayer(selfPerc.getSide(), 2).getPosition());
													} else {
														kickToPoint(Memory.otherAttacker(selfPerc, fieldPerc).getPosition());
													}
												}*/
											//}
										}
									}
								} else {
									double x, y = 0;
									if (selfPerc.getSide() == 1) {
										x = 10;
									} else { 
										x = -10;
									}
									
									if (selfPerc.getUniformNumber() == 5) {
										y = -10;
									} else {
										y = 10;
									}
									
									if (fieldPerc.getBall().getPosition().getX() + x > 52 ||
											fieldPerc.getBall().getPosition().getX() + x < -52)
										x = 0;

									if (fieldPerc.getBall().getPosition().getY() + y > 33 ||
											fieldPerc.getBall().getPosition().getY() + y < -33)
										y = 0;
									
									//System.out.println("Jogador: " + selfPerc.getUniformNumber() + " moveu para frente da bola: " + x + ", " + y);
									runToPoint(new Vector2D(fieldPerc.getBall().getPosition().getX() + x, fieldPerc.getBall().getPosition().getY() + y), ERROR);
									
									/*runToPoint(new Vector2D(fieldPerc.getBall().getPosition().getX(), selfPerc.getPosition().getY()), ERROR);
									
									if (Memory.attackersCloser(selfPerc, fieldPerc)) {
										System.out.println("Jogador: " + selfPerc.getUniformNumber() + " muito perto, se afastar");
										
										Vector2D v = new Vector2D();
										v.setX(selfPerc.getPosition().getX());
										v.setY(initialPosition.getY());
										runToPoint(v, ERROR);
									}*/
								}
							}
						} else if (type == PlayerType.Left || type == PlayerType.Right) {
							Rectangle rectLeft1 = new Rectangle(-52, -34, 104, 20);
							Rectangle rectRight1 = new Rectangle(-52, 13, 104, 20);

							//Rectangle rectLeft2 = new Rectangle(-52, -34, 104, 20);
							//Rectangle rectRight2 = new Rectangle(-52, 13, 104, 20);
							
							//System.out.println(type + " - " + selfPerc.getTeam());
							
							if ((type == PlayerType.Left && 
								 rectLeft1.contains(fieldPerc.getBall().getPosition().getX(), fieldPerc.getBall().getPosition().getY())) ||
								(type == PlayerType.Right && 
								 rectRight1.contains(fieldPerc.getBall().getPosition().getX(), fieldPerc.getBall().getPosition().getY()))) {
								
								//System.out.println("Bola dentro da área do lateral");
								
								if (fieldPerc.getBall().getPosition().distanceTo(selfPerc.getPosition()) > 1 && 
										fieldPerc.getBall().getPosition().distanceTo(selfPerc.getPosition()) < 5) {
									//System.out.println("Corre para bola");
									runToBall(ERROR);
								} else if (fieldPerc.getBall().getPosition().distanceTo(selfPerc.getPosition()) <= 1) {
									if ((selfPerc.getSide() == 1 && fieldPerc.getBall().getPosition().getX() >= 30) ||
										(selfPerc.getSide() == -1 && fieldPerc.getBall().getPosition().getX() <= -30)) {
										//System.out.println("Jogador: " + selfPerc.getUniformNumber() + " chutou para gol");
										if (selfPerc.getSide() == 1)
											kickToPoint(52, 0, 100);
										else
											kickToPoint(-52, 0, 100);
									} else {
										//System.out.println("Toca para alguém");
										kickToNear();
									}
								} else {
									runToPoint(new Vector2D(fieldPerc.getBall().getPosition().getX(), selfPerc.getPosition().getY()), ERROR);
									/*if (whoHasTheBall == HasTheBall.YourTeam) {
										runToPoint(new Vector2D(fieldPerc.getBall().getPosition().getX(), initialPosition.getY()), ERROR);
									} else if (whoHasTheBall == HasTheBall.Opponent) {
										if (selfPerc.getPosition().distanceTo(fieldPerc.getBall().getPosition()) < 5) {
											runToBall(ERROR);
										} else {
											if (type == PlayerType.Left) {
												runToPoint(fieldPerc.getTeamPlayer(selfPerc.getSide() == 1 ? -1 : 1, 1).getPosition(), ERROR);
											} else if (type == PlayerType.Right) {
												runToPoint(fieldPerc.getTeamPlayer(selfPerc.getSide() == 1 ? -1 : 1, 2).getPosition(), ERROR);
											}
										}
									} else {
										returnToInitialPosition();
									}*/
								}
							} else {
								//System.out.println("Fora da área, go home");
								returnToInitialPosition();
							}
						} else if (type == PlayerType.Defense) {
							if ((selfPerc.getSide() == 1 && fieldPerc.getBall().getPosition().getX() > 0) ||
								(selfPerc.getSide() == -1 && fieldPerc.getBall().getPosition().getX() < 0)){
								returnToInitialPosition();
							} else {
								if (fieldPerc.getBall().getPosition().distanceTo(selfPerc.getPosition()) < 15) {
									runToBall(ERROR);
								} else {
									returnToInitialPosition();
								}
								
								if (fieldPerc.getBall().getPosition().distanceTo(selfPerc.getPosition()) <= 1) {
									kickToNear();
									//returnToInitialPosition();
								}
							}
							
							/*if (selfPerc.getSide() == 1) {
								if (fieldPerc.getBall().getPosition().getX() < 0) {
									runToBall(ERROR);
								} else {
									returnToInitialPosition();
								}
							} else {
								if (fieldPerc.getBall().getPosition().getX() > 0) {
									runToBall(ERROR);
								} else {
									returnToInitialPosition();
								}
							}*/
						} else if (type == PlayerType.GoalKeeper) {
							if (selfPerc.getPosition().distanceTo(fieldPerc.getBall().getPosition()) < 10) {
								if (lastBallPosition != null) {
									Vector2D velocity = fieldPerc.getBall().getPosition().sub(lastBallPosition);
									Vector2D futurePosition = fieldPerc.getBall().getPosition().sum(velocity);
									//System.out.println("Go to future position: " + futurePosition);
									runToPoint(futurePosition, ERROR);
								} else {
									runToBall(ERROR_RUN_BALL);
								}
							
								if (selfPerc.getPosition().distanceTo(fieldPerc.getBall().getPosition()) <= 1) {
									if (selfPerc.getSide() == 1)
										kickToPoint(52, 0);
									else
										kickToPoint(-52, 0);
								}
							} else {
								if (selfPerc.getSide() == 1)
									runToPoint(new Vector2D(-52, 0), ERROR);
								else
									runToPoint(new Vector2D(52, 0), ERROR);
								
								turnToBall();
							}
							
							lastBallPosition = fieldPerc.getBall().getPosition();
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
	
	private void kickToNear() {
		List<PlayerPerception> players = fieldPerc.getTeamPlayers(selfPerc.getSide());
		
		double maxD = 999;
		int index = 0;
		
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i) != null && players.get(i).getPosition() != null && 
				players.get(i).getUniformNumber() != selfPerc.getUniformNumber() && i > 1) {
				double d = players.get(i).getPosition().distanceTo(selfPerc.getPosition());
				if (d < maxD) {
					maxD = d;
					index = i;
				}
			}
		}
		
		//System.out.println("Kick to near - de " + selfPerc.getUniformNumber() + " para " + players.get(index).getUniformNumber());
		kickToPoint(players.get(index).getPosition());
	}
	
	private void returnToInitialPosition() {
		if (selfPerc.getSide() == 1) {
			runToPoint(initialPosition, ERROR);
		} else {
			Vector2D v = new Vector2D();
			v.setX(initialPosition.getX() * -1);
			v.setY(initialPosition.getY());
			runToPoint(v, ERROR);
		}
	}
	
	private void kickToPoint(Vector2D pos) {
	    kickToPoint(pos.getX(), pos.getY());
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
	
	private void kickToPoint(double x, double y, double intensity) {
	    Vector2D myPos = selfPerc.getPosition();
	    Vector2D point = new Vector2D(x, y);
	    Vector2D newDirection = point.sub(myPos);
	           
	    commander.doTurnToDirectionBlocking(newDirection);
	   
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
