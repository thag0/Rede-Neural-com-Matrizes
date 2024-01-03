package rna.otimizadores;

import rna.estrutura.Camada;

/**
 * Classe base para implementações de otimizadores do treino da Rede Neural.
 * <p>
 *		O otimizador já deve levar em consideração que os gradientes para todas
 *		as camadas foram calculados previamente.
 * </p>
 * <p>
 *		Novos otimizadores devem implementar (pelo menos) os métodos {@code inicialziar()} 
 *		e {@code atualizar()} que são chamados obrigatoriamente no momento da compilação e 
 *		treino da Rede Neural.
 * </p>
 * <p>
 *		O método {@code inicialziar()} é útil para aqueles otimizadores que possuem atributos 
 *		especiais, como o coeficiente de momentum por exemplo.
 * </p>
 */
public abstract class Otimizador{

	/**
	 * Informações sobre o otimizador.
	 */
	String info;

	/**
	 * Espaçamento para uma melhor formatação das informações do otimizador
	 */
	String pad = " ".repeat(4);

	/**
	 * Inicializa os parâmetros do otimizador para a as camadas da Rede Neural.
	 * @param redec lista de camadas densas da Rede Neural.
	 */
	public void inicializar(Camada[] redec){
		throw new UnsupportedOperationException(
			"Inicialização do otimizador não implementada."
		);
	}

	/**
	 * Atualiza os parâmetros treináveis da Rede Neural de acordo com o 
	 * otimizador configurado.
	 * <p>
	 *		A atualização de pesos é feita uma única vez em todos os 
	 *		parâmetros da rede.
	 * </p>
	 * @param redec lista de camadas densas da Rede Neural.
	 */
	public void atualizar(Camada[] redec){
		throw new UnsupportedOperationException(
			"Implementar método de atualização do otimizador."
		);
	}

	/**
 	 * Exibe as opções de configurações do otimizador.
	 * @return buffer formatado.
	 */
	public String info(){
		return this.info;
	}

	/**
	 * Inicializa o valor padrão para informações do otimizador, informando
	 * seu nome.
	 */
	protected void construirInfo(){
		this.info = "";
		this.info += this.pad +  "Otimizador: " + this.nome() + "\n";
	}

	/**
	 * Adiciona uma nova linha de informação do otimizador.
	 * @param info informação adicional do otimizador
	 */
	protected void addInfo(String info){
		this.info += this.pad + info + "\n";
	}

	/**
	 * Retorna o nome do otimizador.
	 * @return nome do otimizador.
	 */
	public String nome(){
		return getClass().getSimpleName();
	}
}
