package jnn.modelos;

import jnn.avaliacao.Avaliador;
import jnn.avaliacao.perda.Perda;
import jnn.camadas.Camada;
import jnn.core.Utils;
import jnn.core.tensor.Tensor;
import jnn.core.tensor.Variavel;
import jnn.otimizadores.Otimizador;
import jnn.treinamento.Treinador;
import jnn.treinamento.Treino;
import jnn.treinamento.TreinoLote;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <h3>
 *    Modelo base
 * </h3>
 * Inteface para modelos criados dentro da biblioteca.
 */
public abstract class Modelo implements Cloneable {

	/**
	 * Nome da instância do modelo.
	 */
	protected String nome = getClass().getSimpleName();

	/**
	 * <h3> Não alterar </h3>
	 * Auxiliar no controle da compilação do modelo, ajuda a evitar uso 
	 * indevido caso ainda não tenha suas variáveis e dependências inicializadas 
	 * previamente.
	 */
	public boolean _compilado;

	/**
	 * Função de perda para avaliar o erro durante o treino.
	 */
	protected Perda _perda;

	/**
	 * Otimizador usado para ajuste de parâmetros treináveis.
	 */
	protected Otimizador _otimizador;

	/**
	 * Ponto inicial para os geradores aleatórios.
	 * <p>
	 *		Uma nova seed só é configurada se seu valor for
	 *		diferente de zero.
	 * </p>
	 */
	protected long seedInicial = 0;

	/**
	 * Gerenciador de treino do modelo.
	 */
	protected Treinador _treinador;

	/**
	 * Auxiliar na verificação de armazenagem do histórico
	 * de perda do modelo durante o treinamento.
	 */
	protected boolean calcularHistorico = false;

	/**
	 * Responsável pelo retorno de desempenho do modelo.
	 * Contém implementações de métodos tanto para cálculo de perdas
	 * quanto de métricas.
	 * <p>
	 *    Cada modelo possui seu próprio avaliador.
	 * </p>
	 */
	protected Avaliador _avaliador;

	/**
	 * Utilitário.
	 */
	protected Utils utils;
	
	/**
	 * Auxiliar de verificação da alteração do método de treino.
	 */
	protected boolean configTreino = false;

	/**
	 * Inicialização implicita de um modelo.
	 */
	protected Modelo() {
		_treinador = new Treino(this);
		_avaliador = new Avaliador(this);
		utils = new Utils();
	}

	/**
	 * <p>
	 *    Altera o nome do modelo.
	 * </p>
	 * O nome é apenas estético e não influencia na performance ou na 
	 * usabilidade do modelo.
	 * @param nome novo nome da rede.
	 */
	public void setNome(String nome) {
		if (nome != null) {
			String s = nome.trim();
			if (!s.isEmpty()) this.nome = s;
		}
	}

	/**
	 * Configura a nova seed inicial para os geradores de números aleatórios utilizados 
	 * durante o processo de inicialização de parâmetros treináveis do modelo.
	 * <p>
	 *    Configurações personalizadas de seed permitem fazer testes com diferentes
	 *    parâmetros, buscando encontrar um melhor ajuste para o modelo.
	 * </p>
	 * <p>
	 *    A configuração de seed deve ser feita antes da compilação do modelo para
	 *    surtir efeito.
	 * </p>
	 * @param seed nova seed.
	 */
	public void setSeed(Number seed) {
		utils.validarNaoNulo(seed, "Seed nula.");
		seedInicial = seed.longValue();
	}

	/**
	 * Define se, durante o processo de treinamento, o modelo irá salvar os dados 
	 * relacionados a função de perda de cada época.
	 * <p>
	 *    Calcular a perda é uma operação que pode ser computacionalmente cara 
	 *    dependendo do tamanho do modelo e do conjunto de dados, então deve ser 
	 *    bem avaliado querer habilitar ou não esse recurso.
	 * </p>
	 * <p>
	 *    {@code O valor padrão é false}
	 * </p>
	 * @param calc se verdadeiro, o modelo armazenará o histórico de perda 
	 * durante cada época de treinamento.
	 */
	public void setHistorico(boolean calc) {
		calcularHistorico = calc;
		_treinador.setHistorico(calc);
	}

	/**
	 * Configura a função de perda que será utilizada durante o processo
	 * de treinamento do modelo.
	 * @param perda nova função de perda.
	 */
	public void setPerda(Perda perda) {
		utils.validarNaoNulo(perda, "Função de perda nula.");
		_perda = perda;
	}

	/**
	 * Configura o novo otimizador do modelo com base numa nova instância 
	 * de otimizador.
	 * <p>
	 *    Configurando o otimizador informando diretamente uma nova instância 
	 *    permite configurar os hiperparâmetros do otimizador fora dos valores 
	 *    padrão, o que pode ajudar a melhorar o desempenho de aprendizado do 
	 *    modelo em cenários específicos.
	 * </p>
	 * Otimizadores disponíveis.
	 * <ol>
	 *    <li> GD (Gradient Descent) </li>
	 *    <li> SGD (Stochastic Gradient Descent) </li>
	 *    <li> AdaGrad </li>
	 *    <li> RMSProp </li>
	 *    <li> Adam  </li>
	 *    <li> Nadam </li>
	 *    <li> AMSGrad </li>
	 *    <li> Adadelta </li>
	 *    <li> Lion </li>
	 * </ol>
	 * @param otm novo otimizador.
	 */
	public void setOtimizador(Otimizador otm) {
		utils.validarNaoNulo(otm, "Otimizador nulo.");
		_otimizador = otm;
	}

	/**
	 * Configura um novo treinador para o modelo.
	 * @param t {@code Treinador} novo.
	 */
	public void setTreinador(Treinador t) {
		utils.validarNaoNulo(t, "Treinador nulo.");
		_treinador = t;
		configTreino = true;
	}

	/**
	 * Inicializa os parâmetros necessários para a criação do modelo,
	 * além de gerar os valores iniciais para os kernels e bias.
	 * <p>
	 *    Caso nenhuma configuração inicial seja feita, o modelo será 
	 *    compilado com os valores padrões. 
	 * </p>
	 * <p>
	 *    Otimizadores podem ser recebidos usando instâncias pré configuradas, 
	 *    essas intâncias dão a liberdade de inicializar o otimizador com valores
	 *    personalizáveis para seus parâmetros (como taxa de aprenziado, por exemplo).
	 * </p>
	 * <p>
	 *    Para treinar o modelo deve-se fazer uso da função função {@code treinar()} 
	 *    informando os dados necessários para treino.
	 * </p>
	 * @param otimizador otimizador usando para ajustar os parâmetros treinavéis do 
	 * modelo, pode ser uma {@code String} referente ao nome ou uma {@code instância} 
	 * já inicializada.
	 * @param perda função de perda usada para avaliar o erro do modelo durante o 
	 * treino, pode ser uma {@code String} referente ao nome ou uma {@code instância} 
	 * já inicializada.
	 */
	public abstract void compilar(Object otimizador, Object perda);

	/**
	 * Auxiliar na verificação da compilação do modelo.
	 */
	protected void validarCompilacao() {
		if (!_compilado) {
			throw new IllegalStateException(
				"\nO modelo deve ser compilado."
			);
		}
	}

	/**
	 * Alimenta o modelo com os dados de entrada.
	 * @param x {@code Tensor} contendo dados de entrada que 
	 * serão propagados através do modelo.
	 * @return {@code Tensor} contendo a saída prevista pelo modelo.
	 */
	public abstract Tensor forward(Tensor x);

	/**
	 * Alimenta o modelo com vários dados de entrada.
	 * @param xs array de {@code Tensor} contendo múltiplas 
	 * entradas para o modelo.
	 * @return array de {@code Tensor} contendo as previsões 
	 * correspondentes.
	 */
	public Tensor[] forward(Tensor[] xs) {
		validarCompilacao();

		utils.validarNaoNulo(xs, "Dados de entrada nulos.");

		final int numEntradas = xs.length;
		int numThreads = Runtime.getRuntime().availableProcessors();
		if (numThreads > numEntradas) numThreads = numEntradas;

		Tensor[] prevs = new Tensor[numEntradas];
		Modelo[] clones = new Modelo[numThreads];

		for (int i = 0; i < numThreads; i++) {
			clones[i] = clone();
		}

		int lote = numEntradas / numThreads;
		try (ExecutorService exec = Executors.newFixedThreadPool(numThreads)) {
			for (int i = 0; i < numThreads; i++) {
				final int id = i;
				final int inicio = i * lote;
				final int fim = (i == numThreads - 1) ? numEntradas : (i + 1) * lote;
	
				exec.execute(() -> {
					for (int j = inicio; j < fim; j++) {
						prevs[j] = clones[id].forward(xs[j]);
					}
				});
			}
		}

		return prevs;
	}

	/**
	 * Zera os gradientes acumulados do modelo.
	 * <p>
	 *    Apenas camadas treináveis são afetadas.
	 * </p>
	 */
	public abstract void gradZero();

	/**
	 * Realiza as verificações necessárias nos dados usados pelo modelo.
	 * @param <T> tipo de dados, comumente usando {@code Tensor}.
	 * @param xs array contendos dados de entrada.
	 * @param ys array contendos dados de saída.
	 */
	private <T> void validarDados(T[] xs, T[] ys) {
		utils.validarNaoNulo(xs, "Dados de entrada nulos.");
		utils.validarNaoNulo(ys, "Dados de saida nulos.");
 
		if (xs.length != ys.length) {
			throw new IllegalArgumentException(
				"\nDados de entrada e saída devem conter o mesmo tamanho, " +
				"recebido x = " + xs.length + " e y = " + ys.length
			);
		}
	}

	/**
	 * Treina o modelo de acordo com as configurações predefinidas.
	 * @param xs dados de entrada do treino (features).
	 * @param ys dados de saída correspondente a entrada (classes).
	 * @param epochs quantidade de épocas de treinamento.
	 * @param logs logs para perda durante as épocas de treinamento.
	 */
	public void treinar(Tensor[] xs, Tensor[] ys, int epochs, boolean logs) {
		treinar(xs, ys, epochs, 1, logs);
	}
	
	/**
	 * Treina o modelo de acordo com as configurações predefinidas utilizando o
	 * treinamento em lotes.
	 * @param xs {@code Tensores} contendos os dados de entrada.
	 * @param ys {@code Tensores} contendos os dados de saída (rótulos).
	 * @param epochs quantidade de épocas de treinamento.
	 * @param tamLote tamanho do lote de treinamento.
	 * @param logs logs para perda durante as épocas de treinamento.
	 */
	public void treinar(Tensor[] xs, Tensor[] ys, int epochs, int tamLote, boolean logs) {
		validarCompilacao();
		validarDados(xs, ys);

		if (epochs < 1) {
			throw new IllegalArgumentException(
				"\nValor de épocas deve ser maior que zero, recebido = " + epochs
			);
		}

		if (tamLote < 1) {
			throw new IllegalArgumentException(
				"\nValor de lote deve ser maior que zero, recebido = " + tamLote
			);
		}

		if (!configTreino && tamLote > 1) {
			_treinador = new TreinoLote(this, tamLote);
			_treinador.setHistorico(calcularHistorico);
		}
		
		_treinador.executar(xs, ys, epochs, logs);
	}

	/**
	 * Configura o modelo para o modo de treino
	 * @param treinando {@code true} caso o modelo deva estar no modo treino,
	 * {@code false} caso contrário.
	 */
	public abstract void treino(boolean treinando);

	/**
	 * Avalia o modelo, calculando o seu valor de perda fazendo uso da função 
	 * de perda que foi configurada.
	 * <p>
	 *    É possível utilizar outras funções de perda mesmo que sejam diferentes
	 *    da que o modelo usa, através de:
	 * </p>
	 * <pre>
	 * modelo.avaliador()
	 * </pre>
	 * @param xs {@code Tensores} contendo dados de entrada.
	 * @param ys {@code Tensores} contendo dados de saída correspondente as entradas fornecidas.
	 * @return {@code Tensor} contendo valor de perda do modelo.
	 */
	public Tensor avaliar(Tensor[] xs, Tensor[] ys) {
		validarCompilacao();
		validarDados(xs, ys);

		Tensor[] prevs = forward(xs);
		
		Tensor res = new Tensor(1);
		int n = prevs.length;
		for (int i = 0; i < n; i++) {
			res.add(_perda.forward(prevs[i], ys[i]));
		}

		return res.div(n);
	}

	/**
	 * Retorna o avaliador do modelo, 
	 * <p>
	 *    O avaliador contém diferentes métodos de métricas úteis
	 *    para medir seu desempenho.
	 * </p>
	 * @return avaliador do modelo.
	 */
	public Avaliador avaliador() {
		return _avaliador;
	}

	/**
	 * Retorna o treinador do modelo.
	 * @return treinador do modelo.
	 */
	public Treinador treinador() {
		return _treinador;
	}

	/**
	 * Retorna o otimizador configurado para o treino do modelo modelo.
	 * @return otimizador atual do modelo.
	 */
	public Otimizador otm() {
		validarCompilacao();
		return _otimizador;
	}

	/**
	 * Retorna a função de perda configurada do modelo.
	 * @return função de perda atual do modelo.
	 */
	public Perda perda() {
		validarCompilacao();
		return _perda;
	}

	/**
	 * Retorna a {@code camada} do Modelo correspondente ao índice fornecido.
	 * @param id índice da busca.
	 * @return camada baseada na busca.
	 */
	public abstract Camada camada(int id);

	/**
	 * Retorna todo o conjunto de camadas presente no modelo.
	 * @return conjunto de camadas do modelo.
	 */
	public abstract Camada[] camadas();

	/**
	 * Retorna a {@code camada de saída}, ou última camada, do modelo.
	 * @return camada de saída.
	 */
	public abstract Camada camadaSaida();

	/**
	 * Retorna o conjunto de parâmetros do modelo.
	 * <p>
	 *		Os parâmetros de um modelo incluem {@code kernels} e {@code bias}
	 *		de cada camada, todos sendo do tipo {@code Tensor}.
	 * </p>
	 * <p>
	 * 		A sequência fornecida dos parâmetros é dada por:
	 * </p>
	 * <pre>
	 * 	params = [k1, b1, k2, b2, k3, b3, ...]
	 * </pre>
	 * Onde: {@code k = kernel} e {@code b = bias}.
	 * @return array de {@code Tensor} contendo os parâmetros do modelo.
	 */
	public abstract Tensor[] params();

	/**
	 * Retorna o conjunto de gradientes em relação aos parâmetros do modelo.
	 * <p>
	 * 		A sequência fornecida dos gradientes é dada por:
	 * </p>
	 * <pre>
	 * 	params = [gk1, gb1, gk2, gb2, gk3, gb3, ...]
	 * </pre>
	 * Onde: {@code gk = gradKernel} e {@code gb = gradBias}.
	 * @return array de {@code Tensor} contendo os gradientes do modelo.
	 */
	public abstract Tensor[] grads();
	
	/**
	 * Retorna um array contendo a saída serializada do modelo.
	 * @return saída do modelo.
	 */
	public abstract Variavel[] saidaParaArray();

	/**
	 * Copia os dados de saída da última camada do modelo para o array.
	 * @param arr array para cópia.
	 */
	public void copiarDaSaida(double[] arr) {
		utils.validarNaoNulo(arr, "Array nulo.");
		
		Variavel[] saida = saidaParaArray();
		
		if (saida.length != arr.length) {
			throw new IllegalArgumentException(
				"\nIncompatibilidade de dimensões entre o array fornecido (" + arr.length + 
				") e o array gerado pela saída da última camada (" + saida.length + ")."
			);
		}

		for (int i = 0; i < saida.length; i++) {
			arr[i] = saida[i].get();
		}
	}

	/**
	 * Informa o nome configurado do modelo.
	 * @return nome do modelo.
	 */
	public String nome() {
		return nome;
	}

	/**
	 * Retorna a quantidade total de parâmetros do modelo.
	 * <p>
	 *    isso inclui todos os kernels e bias (caso configurados).
	 * </p>
	 * @return quantidade de parâmetros total do modelo.
	 */
	public abstract int numParams();

	/**
	 * Retorna a quantidade de camadas presente no modelo.
	 * @return quantidade de camadas do modelo.
	 */
	public abstract int numCamadas();

	/**
	 * Disponibiliza o histórico da função de perda do modelo durante cada época
	 * de treinamento.
	 * <p>
	 *    O histórico será o do ultimo processo de treinamento usado, seja ele 
	 *    sequencial ou em lotes.
	 * </p>
	 * @return array contendo o valor de perda durante cada época de treinamento 
	 * do modelo.
	 */
	public double[] hist() {
		return _treinador.hist();
	}

	/**
	 * Gera uma string representando as características do modelo.
	 * @return {@code String} representando o modelo.
	 */
	protected abstract String construirInfo();

	/**
	 * Exibe, via console, as informações do modelo.
	 */
	public abstract void print();

	@Override
	public String toString() {
		validarCompilacao();
		return construirInfo();
	}

	/**
	 * Clona as características principais do modelo.
	 * @return clone do modelo.
	 */
	@Override
	public Modelo clone() {
		try {
			Modelo clone = (Modelo) super.clone(); 
			return clone;

		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
