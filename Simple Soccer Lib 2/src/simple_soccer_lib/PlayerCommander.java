package simple_soccer_lib;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import simple_soccer_lib.comm.KrisletCommunicator;
import simple_soccer_lib.perception.FieldPerception;
import simple_soccer_lib.perception.MatchPerception;
import simple_soccer_lib.perception.PlayerPerception;
import simple_soccer_lib.positions_monitor.MonitorCommunicator;
import simple_soccer_lib.utils.Vector2D;

/**
 * Esta classe serve para facilitar a comunica��o com o servidor, para controlar um 
 * jogador. 
 * 
 * Ela oferece percep��es do jogo (vindas do simulador) na forma de objetos de alto 
 * n�vel e oferece m�todos de alto n�vel para enviar as a��es de um jogador.
 *  
 * As percep��es funcionam na forma de "consumo". Depois de lida uma vez, s� haver�
 * uma nova percep��o dispon�vel quando o servidor enviar nova mensagem. Se houver
 * uma nova leitura no intervalo, ser� retornando null.
 * 
 */
public class PlayerCommander extends Thread {
	private static final int WAIT_TIME = 5;
	private static final int SIMULATOR_CYCLE = 100;

	private KrisletCommunicator communicator;
	private MonitorCommunicator perceiver;
	
	private String teamName;
	private int uniformNumber;
	private char fieldSide; //no futuro: 1) incluir no player perception 
	                        //    ou     2) abstrair (para enviar/receber comandos como se atacasse para a direita)
	
	private PlayerPerception self;  //as informacoes sobre o pr�prio jogador comandado
	private boolean selfConsumed;   //MANTER apenas se for retornar a c�pia
	private FieldPerception field;  //as informacoes sobre os objetos m�veis do campo: bola e outros jogadores
	private boolean fieldConsumed;
	private MatchPerception match; // as informacoes sobre a partida: nome dos times, placar, lado do campo, tempo e estado do jogo
	private boolean matchConsumed;
		
	private Vector2D viewDirection; //dire��o absoluta da vis�o, necess�ria para algumas a��es de alto n�vel
	
	private long nextActionTime;
		
	/**
	 * Recebe o endere�o do servidor e a posi��o inicial do jogador, antes do kick-off.
	 */
	public PlayerCommander(String teamName, String host, int port) throws UnknownHostException {
		InetAddress address = InetAddress.getByName(host);
		
		this.teamName = teamName;
		this.communicator = new KrisletCommunicator(address, port, teamName); // InetAddress lan�a exce��o
		this.perceiver = new MonitorCommunicator(address);
		
		this.self = new PlayerPerception();
		this.field = new FieldPerception();
		this.match = new MatchPerception();
		this.selfConsumed = this.fieldConsumed = this.matchConsumed = true;
		this.start();
	}
	
	public String getTeamName() {
		return this.teamName;
	}
	
	public void run() {
		try {
			communicator.connect(); //conecta com o servidor, no modo jogador
			uniformNumber = communicator.getInitialUniformNumber();
			fieldSide = communicator.getInitialSide();
			
			perceiver.connect();    //conecta com o servidor, no modo monitor
			
			nextActionTime = System.currentTimeMillis();
			
			while (true) {			
				try {
					
					synchronized (this) {
						// 1. Recebe as perce��es e fazer o parsing delas
						// 2. Constroi uma representa��o de alto n�vel das percep��es
						//    2.1 Calcular a posi��o e orienta��o absoluta do jogador (self) 
						//    2.2 As posi��es absolutas dos objetos m�veis do campo (field)

						this.self  = (self == null) ? new PlayerPerception() : this.self;
						this.field = (field == null) ? new FieldPerception() : this.field;
						
						//boolean hasNewPerceptions = 
						//communicator.update(self, field); //os par�metros s�o alterados dentro desta chamada
						
						communicator.update(null, null); //para ignorar as percep��es lidas						
						
						boolean hasNewPerceptions = 
								perceiver.update(field, match);
						
						if (hasNewPerceptions) {
							self = field.getTeamPlayer(teamName, uniformNumber);
							
							this.selfConsumed = false;
							this.fieldConsumed = false;
							this.matchConsumed = false;
							
							//if (self.getDirection() != null) {
							this.viewDirection = self.getDirection();
							//}
						}

//						printLog();
		
						// 3. Executar o pr�ximo comando requisitado pelo cliente (se for guardar a proxima acao ou uma fila delas)
//						if (System.currentTimeMillis() >= nextActionTime) {
//							//envia para o servidor...
//							nextAction = null;
//							nextActionTime += SIMULATOR_CYCLE;
//						}
					}
					
					Thread.sleep(1);
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isActive() {
		return communicator.isActive();
	}
	
	public void printLog(){
		if(self != null && field != null && match != null){
			System.out.println("INFORMACOES DA PARTIDA ------------------");
			System.out.println("Tempo atual: "+match.getTime());
			System.out.println("Estado jogo: "+match.getState());
			System.out.println("Placar: "+match.getTeamAName() +" "+ match.getTeamAScore() +" x "+ match.getTeamBScore() +" "+ match.getTeamBName());
			System.out.println("Lado time A: "+match.getTeamASide());
			System.out.println("Lado time B: "+match.getTeamBSide());
			System.out.println();
			
			System.out.println("INFORMACOES DO JOGADOR ------------------");
			System.out.println("Tempo atual: "+ field.getTime());
			System.out.println("Numero jogador: "+ self.getUniformNumber());
			System.out.println("Time jogador: "+ self.getTeam());
			System.out.println("Lado campo: "+ self.getSide());
			System.out.println("Eh goleiro?: "+ self.isGoalie());
			if (self.getPosition()!= null)
				System.out.println("Posicao jogador: "+ self.getPosition());
			if (self.getDirection()!= null)
				System.out.println("Direcao jogador: "+ self.getDirection());
			System.out.println("Jogadores vistos: "+ field.getAllPlayers().size());
			System.out.println("Jogadores vistos do time: "+ field.getTeamPlayers(self.getTeam()).size());
			if(field.getBall().getPosition() != null)
				System.out.println("Posicao bola: "+ field.getBall().getPosition());
			System.out.println();
		}
	}
	
	synchronized public PlayerPerception perceiveSelf() {
		if (selfConsumed) {
			return null;
		}
		PlayerPerception s = this.self;
		//self = null;
		selfConsumed = true;
		return s; //.copy();
	}
	public PlayerPerception perceiveSelfBlocking() {
		PlayerPerception s = perceiveSelf();
		while (s == null) {   //atencao: risco de live lock, ideia: criar um semaforo apenas para as percep��es
			try {
				Thread.sleep(WAIT_TIME/2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			s = perceiveSelf();
		}
		return s;  
	}
	
	synchronized public FieldPerception perceiveField() {
		if (fieldConsumed) {
			return null;
		}
		FieldPerception f = this.field;
		//field = null;
		fieldConsumed = true;
		return f; //.copy();
	}
	public FieldPerception perceiveFieldBlocking() {
		FieldPerception f = perceiveField();
		while (f == null) {   //atencao: risco de live lock, ideia: criar um semaforo apenas para as percep��es
			try {
				Thread.sleep(WAIT_TIME/2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			f = perceiveField();
		}
		return f;
	}
	
	synchronized public MatchPerception perceiveMatch() {
		if (matchConsumed) {
			return null;
		}
		MatchPerception m = this.match;
		//field = null;
		matchConsumed = true;
		return m; //.copy();
	}
	public MatchPerception perceiveMatchBlocking() {
		MatchPerception m = perceiveMatch();
		while (m == null) {   //atencao: risco de live lock, ideia: criar um semaforo apenas para as percep��es
			try {
				Thread.sleep(WAIT_TIME/2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			m = perceiveMatch();
		}
		return m;
	}
	

// Pode remover...
//	/** Metodos temporarios, usados pelo Krislet. Remover quando tirar o Krislet. **/
//	public PlayerPerception getSelf() {
//		return self;
//	}
//
//	public void setSelf(PlayerPerception self) {
//		this.self = self;
//	}
//
//	public FieldPerception getField() {
//		return field;
//	}
//
//	public void setField(FieldPerception field) {
//		this.field = field;
//	}
//	/** Fim metodos temporarios **/

	
	/**
	 * O agente gira o corpo (e a vis�o) de modo que o futuro eixo de visao forme 
	 * o dado angulo em relacao ao eixo de visual atual.
	 */
	synchronized public boolean doTurn(double degreeAngle) {
		if (System.currentTimeMillis() < nextActionTime) {
			return false;
		}
		if (degreeAngle > 180.0) {
			degreeAngle -= 360;
		} else if (degreeAngle < -180.0) {
			degreeAngle += 360; 
		}
		communicator.turn(degreeAngle);		
		//nextActionTime += SIMULATOR_CYCLE;  //nao pode ser assim, por causa da situa��o em que o jogador ficou algum tempo sem agir
		nextActionTime = System.currentTimeMillis() + SIMULATOR_CYCLE;		
		return true;
	}
	
	public void doTurnBlocking(double degreeAngle) {
		while (System.currentTimeMillis() < nextActionTime) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		doTurn(degreeAngle);
	}

	/**
	 * Recebe o vetor de orienta��o do jogador ap�s a realiza��o desta a��o,
	 * considerando as coordenadas absolutas do campo. 
	 */
	synchronized public boolean doTurnToDirection(Vector2D orientation) {
		double angle = orientation.angleFrom(viewDirection);
		//System.out.println(" => DO TURN, angle = " + angle);
		return doTurn(angle);
	}
	
	public void doTurnToDirectionBlocking(Vector2D orientation) {
		while (System.currentTimeMillis() < nextActionTime) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
		doTurnToDirection(orientation);
	}
	
	/**
	 * Faz o agente virar para que seu eixo de visao alinhe ao ponto dado.
	 */
	synchronized public void doTurnToPoint(Vector2D referencePoint) {				
		Vector2D newDirection = referencePoint.sub(this.self.getPosition());
		doTurnToDirection(newDirection);
	}

	public void doTurnToPointBlocking(Vector2D referencePoint) {
		while (System.currentTimeMillis() < nextActionTime) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Vector2D newDirection = referencePoint.sub(this.viewDirection);
		doTurnToDirection(newDirection);
	}
		
	/**
	 * Acao de caminhar ou correr. O valor indica o esforco aplicado neste movimento.
	 */
	synchronized public boolean doDash(double intensity) {
		if (System.currentTimeMillis() < nextActionTime) {
			return false;
		}
		communicator.dash(intensity); 
		nextActionTime += SIMULATOR_CYCLE;
		return true;
	}
	
	public void doDashBlocking(double intensity) {
		while (System.currentTimeMillis() < nextActionTime) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
		doDash(intensity);
	}
	
	public void doDashBlocking(Vector2D orientation, double intensity) {
		doTurnToDirectionBlocking(orientation);
		doDashBlocking(intensity);
	}
	
	/**
	 * Chuta ou toca a bola. Recebe a intensidade e a direcao relativa do chute 
	 * (angulo em relacao � direcao de movimento/visao do jogador). 
	 */
	synchronized public boolean doKick(double intensity, double relativeAngle) {
		if (System.currentTimeMillis() < nextActionTime) {
			return false;
		}
		communicator.kick(intensity, relativeAngle); 
		nextActionTime += SIMULATOR_CYCLE;
		return true;
	}
	
	public void doKickBlocking(double intensity, double relativeAngle) {
		while (System.currentTimeMillis() < nextActionTime) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
		communicator.kick(intensity, relativeAngle);
	}

	/**
	 * Chuta ou toca a bola. Recebe a intensidade e a dire��o absoluta do chute. 
	 */
	synchronized public void doKick(double intensity, Vector2D direction) {
		//FAZER
		throw new Error("Not implemented");
	}

	public void doKickBlocking(double intensity, Vector2D direction) {
		while (System.currentTimeMillis() < nextActionTime) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
		doKick(intensity, direction);
	}
		
	/**
	 * Move o jogador para a coordenada dada. Isso s� pode ser feito em
	 * alguns momentos do jogo (por exemplo: antes do kickoff).
	 */
	synchronized public boolean doMove(double x, double y) {
		if (System.currentTimeMillis() < nextActionTime) {  //TESTAR tamb�m se PODE, dependendo do status da partida
			return false;
		}
		communicator.move(x, y); 
		nextActionTime += SIMULATOR_CYCLE;
		return true;
	}
	
	public void doMoveBlocking(double x, double y) {
		while (System.currentTimeMillis() < nextActionTime) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
		doMove(x, y);
	}
	
	public void disconnect() {
		communicator.bye();
	}
}

