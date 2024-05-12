package jnn.camadas;

import jnn.core.Utils;
import jnn.core.tensor.Tensor;
import jnn.core.tensor.Variavel;

/**
 * <h2>
 *    Camada de agrupamento máximo
 * </h2>
 * <p>
 *    A camada de agrupamento máximo é um componente utilizado para reduzir a 
 *    dimensionalidade espacial dos dados, preservando as características mais 
 *    importantes para a saída.
 * </p>
 * <p>
 *    Durante a operação de agrupamento máximo, a entrada é dividida em regiões 
 *    menores usando uma márcara e o valor máximo de cada região é salvo. 
 *    Essencialmente, a camada realiza a operação de subamostragem, mantendo apenas 
 *    as informações mais relevantes.
 * </p>
 * Exemplo simples de operação Max Pooling para uma região 2x2 com máscara 2x2:
 * <pre>
 *entrada = [
 *    [[1, 2],
 *     [3, 4]]
 *]
 * 
 *saida = [
 *    [4]
 *]
 * </pre>
 * <p>
 *    A camada de max pooling não possui parâmetros treináveis nem função de ativação.
 * </p>
 */
public class MaxPooling extends Camada implements Cloneable{

	/**
	 * Utilitario.
	 */
	Utils utils = new Utils();

	/**
	 * Dimensões dos dados de entrada (profundidade, altura, largura)
	 */
	private int[] formEntrada;

	/**
	 * Dimensões dos dados de saída (profundidade, altura, largura)
	 */
	private int[] formSaida;

	/**
	 * Tensor contendo os dados de entrada da camada.
	 * <p>
	 *    O formato da entrada é dado por:
	 * </p>
	 * <pre>
	 *    entrada = (profundidade, altura, largura)
	 * </pre>
	 */
	public Tensor _entrada;

	/**
	 * Tensor contendo os dados de saída da camada.
	 * <p>
	 *    O formato de entrada varia dependendo da configuração da
	 *    camada (filtro, strides) mas é dado como:
	 * </p>
	 * <pre>
	 *largura = (larguraEntrada = larguraFiltro) / larguraStride + 1;
	 *altura = (alturaEntrada = alturaFiltro) / alturaStride + 1;
	 * </pre>
	 * <p>
	 *    Com isso o formato de saída é dado por:
	 * </p>
	 * <pre>
	 *    saida = (profundidade, altura, largura)
	 * </pre>
	 * Essa relação é válida pra cada canal de entrada.
	 */
	public Tensor _saida;

	/**
	 * Tensor contendo os gradientes que serão
	 * retropropagados para as camadas anteriores.
	 * <p>
	 *    O formato do gradiente de entrada é dado por:
	 * </p>
	 * <pre>
	 *    entrada = (profundidadeEntrada, alturaEntrada, larguraEntrad)
	 * </pre>
	 */
	public Tensor _gradEntrada;

	/**
	 * Formato do filtro de pooling (altura, largura).
	 */
	private int[] formFiltro;

	/**
	 * Valores de stride (altura, largura).
	 */
	private int[] stride;

	/**
	 * Instancia uma nova camada de max pooling, definindo o formato do
	 * filtro que será aplicado em cada entrada da camada.
	 * <p>
	 *    O formato do filtro deve conter as dimensões da entrada da
	 *    camada (altura, largura).
	 * </p>
	 * <p>
	 *    Por padrão, os valores de strides serão os mesmo usados para
	 *    as dimensões do filtro, exemplo:
	 * </p>
	 * <pre>
	 *filtro = (2, 2)
	 *stride = (2, 2) // valor padrão
	 * </pre>
	 * @param formFiltro formato do filtro de max pooling.
	 * @throws IllegalArgumentException se o formato do filtro não atender as
	 * requisições.
	 */
	public MaxPooling(int[] formFiltro) {
		utils.validarNaoNulo(formFiltro, "\nO formato do filtro não pode ser nulo.");
		
		if (formFiltro.length != 2) {
			throw new IllegalArgumentException(
				"\nO formato do filtro deve conter dois elementos (altura, largura)."
			);
		}

		if (!utils.apenasMaiorZero(formFiltro)) {
			throw new IllegalArgumentException(
				"\nOs valores de dimensões do filtro devem ser maiores que zero."
			);
		}

		this.formFiltro = formFiltro;
		this.stride = new int[]{
			formFiltro[0],
			formFiltro[1]
		};
	}

	/**
	 * Instancia uma nova camada de max pooling, definindo o formato do filtro 
	 * e os strides (passos) que serão aplicados em cada entrada da camada.
	 * <p>
	 *    O formato do filtro e dos strides devem conter as dimensões da entrada 
	 *    da camada (altura, largura).
	 * </p>
	 * @param formFiltro formato do filtro de max pooling.
	 * @param stride strides que serão aplicados ao filtro.
	 * @throws IllegalArgumentException se o formato do filtro não atender as
	 * requisições.
	 * @throws IllegalArgumentException se os strides não atenderem as requisições.
	 */
	public MaxPooling(int[] formFiltro, int[] stride) {
		utils.validarNaoNulo(formFiltro, "\nO formato do filtro não pode ser nulo.");

		if (formFiltro.length != 2) {
			throw new IllegalArgumentException(
				"\nO formato do filtro deve conter três elementos (altura, largura)."
			);
		}

		if (!utils.apenasMaiorZero(formFiltro)) {
			throw new IllegalArgumentException(
				"\nOs valores de dimensões do filtro devem ser maiores que zero."
			);
		}

		utils.validarNaoNulo(stride, "\nO formato do filtro não pode ser nulo.");
		
		if (stride.length != 2) {
			throw new IllegalArgumentException(
				"\nO formato para os strides deve conter dois elementos (altura, largura)."
			);
		}

		if (!utils.apenasMaiorZero(stride)) {
			throw new IllegalArgumentException(
				"\nOs valores para os strides devem ser maiores que zero."
			);
		}

		this.formFiltro = formFiltro;
		this.stride = stride;
	}

	/**
	 * Instancia uma nova camada de max pooling, definindo o formato do filtro, 
	 * formato de entrada e os strides (passos) que serão aplicados em cada entrada 
	 * da camada.
	 * <p>
	 *    O formato do filtro e dos strides devem conter as dimensões da entrada 
	 *    da camada (altura, largura).
	 * </p>
	 * A camada será automaticamente construída usando o formato de entrada especificado.
	 * @param formEntrada formato de entrada para a camada.
	 * @param formFiltro formato do filtro de max pooling.
	 * @param stride strides que serão aplicados ao filtro.
	 * @throws IllegalArgumentException se o formato do filtro não atender as
	 * requisições.
	 * @throws IllegalArgumentException se os strides não atenderem as requisições.
	 */
	public MaxPooling(int[] formEntrada, int[] formFiltro, int[] stride) {
		this(formFiltro, stride);
		construir(formEntrada);
	}

	/**
	 * Constroi a camada MaxPooling, inicializando seus atributos.
	 * <p>
	 *    O formato de entrada da camada deve seguir o padrão:
	 * </p>
	 * <pre>
	 *    formEntrada = (profundidade, altura, largura)
	 * </pre>
	 * <h3>
	 *    Nota
	 * </h3>
	 * <p>
	 *    Caso o formato de entrada contenha quatro elementos, o primeiro
	 *    valor é descondiderado.
	 * </p>
	 * @param entrada formato dos dados de entrada para a camada.
	 */
	@Override
	public void construir(Object entrada) {
		utils.validarNaoNulo(entrada, "\nFormato de entrada fornecida para camada MaxPooling é nulo.");
		
		if (!(entrada instanceof int[])) {
			throw new IllegalArgumentException(
				"\nObjeto esperado para entrada da camada " + nome() +" é do tipo int[], " +
				"objeto recebido é do tipo " + entrada.getClass().getTypeName()
			);
		}
		
		int[] e = (int[]) entrada;
		if (e.length == 4) {
			this.formEntrada = new int[]{e[1], e[2], e[3]};
		
		}else if (e.length == 3) {
			this.formEntrada = new int[]{e[0], e[1], e[2]};

		}else {         
			throw new IllegalArgumentException(
				"\nO formato de entrada deve conter três elementos (profundidade, altura, largura) ou " +
				"quatro elementos (primeiro elementos desconsiderado)" +
				"formato recebido possui " + e.length + " elementos."
			);
		}

		this.formSaida = new int[3];
		formSaida[0] = formEntrada[0];//profundidade
		formSaida[1] = (formEntrada[1] - formFiltro[0]) / this.stride[0] + 1;//altura
		formSaida[2] = (formEntrada[2] - formFiltro[1]) / this.stride[1] + 1;//largura
		
		_entrada = new Tensor(formEntrada);
		_gradEntrada = new Tensor(_entrada);
		_saida = new Tensor(formSaida);

		setNomes();

		_construida = true;//camada pode ser usada
	}

	@Override
	public void inicializar() {}

	@Override
	protected void setNomes() {
		_entrada.nome("entrada");
		_gradEntrada.nome("gradiente entrada");
		_saida.nome("saída");
	}

	@Override
	public Tensor forward(Object entrada) {
		verificarConstrucao();

		if (entrada instanceof Tensor) {
			Tensor e = (Tensor) entrada;

			if (!_entrada.compararShape(e)) {
				throw new IllegalArgumentException(
					"\nDimensões da entrada recebida " + e.shapeStr() +
					" incompatíveis com a entrada da camada " + _entrada.shapeStr()
				);
			}

			_entrada.copiar(e);
			
		}else if (entrada instanceof double[][][]) {
			double[][][] e = (double[][][]) entrada;
			_entrada.copiar(e);

		}else {
			throw new IllegalArgumentException(
				"\nTipo de entrada \"" + entrada.getClass().getTypeName() + "\" não suportada."
			);
		}

		int profundidade = formEntrada[0];
		for (int i = 0; i < profundidade; i++) {
			aplicar(_entrada, _saida, i);
		}

		return _saida;
	}

	/**
	 * Agrupa os valores máximos encontrados na entrada de acordo com as 
	 * configurações de filtro e strides.
	 * @param entrada tensor de entrada.
	 * @param saida tensor de destino.
	 * @param prof índice de profundidade da operação.
	 */
	private void aplicar(Tensor entrada, Tensor saida, int prof) {
		int[] shapeEntrada = entrada.shape();
		int[] shapeSaida = saida.shape();

		int altEntrada  = shapeEntrada[shapeEntrada.length-1];
		int largEntrada = shapeEntrada[shapeEntrada.length-2];
		int altSaida  = shapeSaida[shapeSaida.length-1];
		int largSaida = shapeSaida[shapeSaida.length-2];

		int nDims = entrada.numDim();
  
		for (int i = 0; i < altSaida; i++) {
			int linInicio = i * stride[0];
			int linFim = Math.min(linInicio + formFiltro[0], altEntrada);
			for (int j = 0; j < largSaida; j++) {
				int colInicio = j * stride[1];
				int colFim = Math.min(colInicio + formFiltro[1], largEntrada);
				double maxValor = Double.MIN_VALUE;

				for (int lin = linInicio; lin < linFim; lin++) {
					for (int col = colInicio; col < colFim; col++) {
						double valor = (nDims == 2) ? entrada.get(lin, col) : entrada.get(prof, lin, col);
						if (valor > maxValor) maxValor = valor;
					}
				}
				saida.set(maxValor, 
					(nDims == 2) ? new int[]{i, j} : new int[]{prof, i, j}
				);
			}
		}
	}

	@Override
	public Tensor backward(Object grad) {
		verificarConstrucao();

		if (grad instanceof Tensor) {
			Tensor g = (Tensor) grad;
			int profundidade = formEntrada[0];   
			for (int i = 0; i < profundidade; i++) {
				gradMaxPool(_entrada, g, _gradEntrada, i);
			}
		
		}else {
			throw new IllegalArgumentException(
				"Formato de gradiente \" "+ grad.getClass().getTypeName() +" \" não " +
				"suportado para camada de MaxPooling."
			);
		}

		return _gradEntrada;
	}
	
	/**
	 * Calcula e atualiza os gradientes da camada de Max Pooling em relação à entrada.
	 * <p>
	 *    Retroropaga os gradientes da camada seguinte para a camada de Max Pooling, considerando 
	 *    a operação de agrupamento máximo. Ela calcula os gradientes em relação à entrada para as 
	 *    camadas anteriores.
	 * </p>
	 * @param entrada entrada da camada.
	 * @param gradSeguinte gradiente da camada seguinte.
	 * @param gradEntrada gradiente de entrada da camada de max pooling.
	 * @param prof índice de profundidade da operação.
	 */
	 private void gradMaxPool(Tensor entrada, Tensor gradSeguinte, Tensor gradEntrada, int prof) {
		int[] shapeEntrada = entrada.shape();
		int[] shapeGradS = gradSeguinte.shape();

		int altEntrada  = shapeEntrada[shapeEntrada.length-2];
		int largEntrada = shapeEntrada[shapeEntrada.length-1];

		int altGradSeguinte  = shapeGradS[shapeGradS.length-2];
		int largGradSeguinte = shapeGradS[shapeGradS.length-1];
  
		for (int i = 0; i < altGradSeguinte; i++) {
			int linInicio = i * stride[0];
			int linFim = Math.min(linInicio + formFiltro[0], altEntrada);
			for (int j = 0; j < largGradSeguinte; j++) {
				int colInicio = j * stride[1];
				int colFim = Math.min(colInicio + formFiltro[1], largEntrada);
  
				int[] posicaoMaximo = posicaoMaxima(entrada, prof, linInicio, colInicio, linFim, colFim);
				int linMaximo = posicaoMaximo[0];
				int colMaximo = posicaoMaximo[1];
  
				double grad = gradSeguinte.get(prof, i, j);
				gradEntrada.set(grad, prof, linMaximo, colMaximo);
			}
		}
	}
  
  
	/**
	 * Encontra a posição do valor máximo em uma submatriz do tensor.
	 * <p>
	 *    Se houver múltiplos elementos com o valor máximo, a função retorna as coordenadas 
	 *    do primeiro encontrado.
	 * </p>
	 * @param tensor tensor alvo.
	 * @param linInicio índice inicial para linha.
	 * @param colInicio índice final para a linha.
	 * @param linFim índice inicial para coluna (exclusivo).
	 * @param colFim índice final para coluna (exclusivo).
	 * @param prof índice de profundidade da operação.
	 * @return array representando as coordenadas (linha, coluna) do valor máximo
	 * na submatriz.
	 */
	private int[] posicaoMaxima(Tensor tensor, int prof, int linInicio, int colInicio, int linFim, int colFim) {
		int[] posMaximo = {0, 0};
		double valMaximo = Double.NEGATIVE_INFINITY;
  
		for (int i = linInicio; i < linFim; i++) {
			for (int j = colInicio; j < colFim; j++) {
				if (tensor.get(prof, i, j) > valMaximo) {
					valMaximo = tensor.get(prof, i, j);
					posMaximo[0] = i;
					posMaximo[1] = j;
				}
			}
		}
  
		return posMaximo;
	}
 
	@Override
	public int[] formatoEntrada() {
		verificarConstrucao();
		return formEntrada.clone();
	}

	@Override
	public int[] formatoSaida() {
		verificarConstrucao();
		return formSaida.clone();
	}

	@Override
	public int tamanhoSaida() {
		verificarConstrucao();
		return _saida.tamanho();
	}

	/**
	 * Retorna o formato do filtro usado pela camada.
	 * @return dimensões do filtro de pooling.
	 */
	public int[] formatoFiltro() {
		return formFiltro.clone();
	}
		
	/**
	 * Retorna o formato dos strides usado pela camada.
	 * @return dimensões dos strides.
	 */
	public int[] formatoStride() {
		return stride.clone();
	}

	@Override
	public int numParametros() {
		return 0;
	}

	@Override
	public Tensor saida() {
		verificarConstrucao();
		return _saida;
	}

	@Override
	public Variavel[] saidaParaArray() {
		verificarConstrucao();
		return saida().paraArray();
	}

	@Override
	public Tensor gradEntrada() {
		verificarConstrucao();
		return _gradEntrada;
	}

	@Override
	public String info() {
		verificarConstrucao();

		StringBuilder sb = new StringBuilder();
		String pad = " ".repeat(4);
		
		sb.append(nome() + " (id " + this.id + ") = [\n");

		sb.append(pad).append("Entrada: " + utils.shapeStr(formEntrada) + "\n");
		sb.append(pad).append("Filtro: " + utils.shapeStr(formFiltro) + "\n");
		sb.append(pad).append("Strides: " + utils.shapeStr(stride) + "\n");
		sb.append(pad).append("Saída: " + utils.shapeStr(formatoSaida()) + "\n");

		sb.append("]\n");

		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(info());
		int tamanho = sb.length();

		sb.delete(tamanho-1, tamanho);//remover ultimo "\n"    
		
		sb.append(" <hash: " + Integer.toHexString(hashCode()) + ">");
		sb.append("\n");
		
		return sb.toString();
	}

	@Override
	public MaxPooling clone() {
		MaxPooling clone = (MaxPooling) super.clone();

		clone._treinavel = this._treinavel;
		clone.treinando = this.treinando;
		clone._construida = this._construida;

		clone.formEntrada = this.formEntrada.clone();
		clone.formFiltro = this.formFiltro.clone();
		clone.formSaida = this.formSaida.clone();
		clone.stride = this.stride.clone();
		
		clone._entrada = this._entrada.clone();
		clone._saida = this._saida.clone();
		clone._gradEntrada = this._gradEntrada.clone();

		return clone;
	}
}