package br.ufrpe;

public enum PlayerStatus {
	Idle,
	Attack,	// Leva a bola para o gol
	Steal,	// Oponente tem a bola
	Pursue,	// Bola ta sem ninguém
	// Defesa
	Patrol, // Patrulhando a área de defesa
	Defend	// Mover no seu campo
}