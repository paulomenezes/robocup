package br.ufrpe;

public enum PlayerStatus {
	Idle,
	Attack,	// Leva a bola para o gol
	Steal,	// Oponente tem a bola
	Pursue,	// Bola ta sem ningu�m
	// Defesa
	Patrol, // Patrulhando a �rea de defesa
	Defend	// Mover no seu campo
}