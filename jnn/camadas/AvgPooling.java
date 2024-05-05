package jnn.camadas;

import jnn.core.Utils;
import jnn.core.tensor.Tensor4D;

/**
 * <h1>
 *    Camada de agrupamento médio
 * </h1>
 * <p>
 *    A camada de agrupamento médio é um componente utilizado para reduzir a 
 *    dimensionalidade espacial dos dados, preservando as características mais 
 *    importantes para a saída.
 * </p>
 * <p>
 *    Durante a operação de agrupamento médio, a entrada é dividida em regiões 
 *    menores usando uma máscara e a média de cada região é calculada e salva. 
 *    Essencialmente, a camada realiza a operação de subamostragem, calculando a 
 *    média das informações em cada região.
 * </p>
 * Exemplo simples de operação Avg Pooling para uma região 2x2 com máscara 2x2:
 * <pre>
 *entrada = [
 *    1, 2
 *    3, 4
 *]
 * 
 *saida = 2.5
 * </pre>
 * <p>
 *    A camada de avg pooling não possui parâmetros treináveis nem função de ativação.
 * </p>
 */
public class AvgPooling extends Camada {

	/**
	 * Utilitario.
	 */
	Utils utils = new Utils();

	/**
	 * Dimensões dos dados de entrada (1, profundidade, altura, largura)
	 */
	private int[] formEntrada;

	/**
	 * Dimensões dos dados de saída (1, profundidade, altura, largura)
	 */
	private int[] formSaida;

	/**
	 * Tensor contendo os dados de entrada da camada.
	 * <p>
	 *    O formato da entrada é dado por:
	 * </p>
	 * <pre>
	 *    entrada = (1, profundidade, altura, largura)
	 * </pre>
	 */
	public Tensor4D _entrada;

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
	 *    saida = (1, profundidade, altura, largura)
	 * </pre>
	 * Essa relação é válida pra cada canal de entrada.
	 */
	public Tensor4D _saida;

	/**
	 * Tensor contendo os gradientes que serão
	 * retropropagados para as camadas anteriores.
	 * <p>
	 *    O formato do gradiente de entrada é dado por:
	 * </p>
	 * <pre>
	 *    entrada = (1, profundidadeEntrada, alturaEntrada, larguraEntrad)
	 * </pre>
	 */
	public Tensor4D _gradEntrada;

	/**
	 * Formato do filtro de pooling (altura, largura).
	 */
	private int[] formFiltro;

	/**
	 * Valores de stride (altura, largura).
	 */
	private int[] stride;

	/**
	 * Instancia uma nova camada de average pooling, definindo o formato do
	 * filtro que será aplicado em cada entrada da camada.
	 * <p>
	 *    O formato do filtro deve conter as dimensões da entrada da
	 *    camada (altura, largura).
	 * </p>
	 * <p>
	 *    Por padrão, os valores de strides serão os mesmos usados para
	 *    as dimensões do filtro, exemplo:
	 * </p>
	 * <pre>
	 *filtro = (2, 2)
	 *stride = (2, 2) // valor padrão
	 * </pre>
	 * @param formFiltro formato do filtro de average pooling.
	 * @throws IllegalArgumentException se o formato do filtro não atender as
	 * requisições.
	 */
	public AvgPooling(int[] formFiltro) {
		utils.validarNaoNulo(formFiltro, "O formato do filtro não pode ser nulo.");

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
	 * Instancia uma nova camada de average pooling, definindo o formato do filtro 
	 * e os strides (passos) que serão aplicados em cada entrada da camada.
	 * <p>
	 *    O formato do filtro e dos strides devem conter as dimensões da entrada 
	 *    da camada (altura, largura).
	 * </p>
	 * @param formFiltro formato do filtro de average pooling.
	 * @param stride strides que serão aplicados ao filtro.
	 * @throws IllegalArgumentException se o formato do filtro não atender as
	 * requisições.
	 * @throws IllegalArgumentException se os strides não atenderem as requisições.
	 */
	public AvgPooling(int[] formFiltro, int[] stride) {
		utils.validarNaoNulo(formFiltro, "O formato do filtro não pode ser nulo.");

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

		utils.validarNaoNulo(stride, "O formato dos strides não pode ser nulo.");

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
	 * Instancia uma nova camada de average pooling, definindo o formato do filtro, 
	 * formato de entrada e os strides (passos) que serão aplicados em cada entrada 
	 * da camada.
	 * <p>
	 *    O formato do filtro e dos strides devem conter as dimensões da entrada 
	 *    da camada (altura, largura).
	 * </p>
	 * A camada será automaticamente construída usando o formato de entrada especificado.
	 * @param formEntrada formato de entrada para a camada.
	 * @param formFiltro formato do filtro de average pooling.
	 * @param stride strides que serão aplicados ao filtro.
	 * @throws IllegalArgumentException se o formato do filtro não atender as
	 * requisições.
	 * @throws IllegalArgumentException se os strides não atenderem as requisições.
	 */
	public AvgPooling(int[] formEntrada, int[] formFiltro, int[] stride) {
		this(formFiltro, stride);
		construir(formEntrada);
	}

	/**
	 * Constroi a camada AvgPooling, inicializando seus atributos.
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
		utils.validarNaoNulo(entrada, "Formato de entrada fornecido para camada " + nome() + " é nulo.");

		if (!(entrada instanceof int[])) {
			throw new IllegalArgumentException(
				"\nObjeto esperado para entrada da camada " + nome() + " é do tipo int[], " +
				"objeto recebido é do tipo " + entrada.getClass().getTypeName()
			);
		}
		
		int[] e = (int[]) entrada;
		if (e.length == 4) {
			this.formEntrada = new int[]{1, e[1], e[2], e[3]};
		
		} else if (e.length == 3) {
			this.formEntrada = new int[]{1, e[0], e[1], e[2]};
		
		} else {         
			throw new IllegalArgumentException(
				"\nO formato de entrada deve conter três elementos (profundidade, altura, largura) ou " +
				"quatro elementos (primeiro elementos desconsiderado)" +
				"formato recebido possui " + e.length + " elementos."
			);
		}

		this.formSaida = new int[4];
		formSaida[0] = 1;
		formSaida[1] = formEntrada[1];//profundidade
		formSaida[2] = (formEntrada[2] - formFiltro[0]) / this.stride[0] + 1;//altura
		formSaida[3] = (formEntrada[3] - formFiltro[1]) / this.stride[1] + 1;//largura
		
		_entrada = new Tensor4D(formEntrada);
		_gradEntrada = new Tensor4D(_entrada);
		_saida = new Tensor4D(formSaida);

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
	public Tensor4D forward(Object entrada) {
		verificarConstrucao();

		if (entrada instanceof Tensor4D) {
			Tensor4D e = (Tensor4D) entrada;

			if (!(_entrada.comparar3D(e))) {
				throw new IllegalArgumentException(
					"\nDimensões da entrada recebida " + e.shapeStr() +
					" incompatíveis com a entrada da camada " + this._entrada.shapeStr()
				);
			}

			_entrada.copiar(e);
			
		} else if (entrada instanceof double[][][]) {
			double[][][] e = (double[][][]) entrada;
			_entrada.copiar(e, 0);

		} else {
			throw new IllegalArgumentException(
				"\nTipo de entrada \"" + entrada.getClass().getTypeName() + "\" não suportada."
			);
		}

		int profundidade = formEntrada[1];
		for (int i = 0; i < profundidade; i++) {
			aplicar(_entrada, _saida, i);
		}

		return _saida;
	}

	/**
	 * Calcula a média dos valores encontrados na entrada de acordo com as
	 * configurações de filtro e strides.
	 * @param entrada tensor de entrada.
	 * @param saida tensor de destino.
	 * @param prof índice de profundidade da operação.
	 */
	private void aplicar(Tensor4D entrada, Tensor4D saida, int prof) {
		int alturaEntrada = entrada.dim3();
		int larguraEntrada = entrada.dim4();
		int alturaSaida = saida.dim3();
		int larguraSaida = saida.dim4();
  
		for (int i = 0; i < alturaSaida; i++) {
			int linInicio = i * stride[0];
			int linFim = Math.min(linInicio + formFiltro[0], alturaEntrada);
			for (int j = 0; j < larguraSaida; j++) {
				int colInicio = j * stride[1];
				int colFim = Math.min(colInicio + formFiltro[1], larguraEntrada);
				double soma = 0;
				int cont = 0;

				for (int lin = linInicio; lin < linFim; lin++) {
					for (int col = colInicio; col < colFim; col++) {
						soma += entrada.get(0, prof, lin, col);
						cont++;
					}
				}

				saida.set((soma/cont), 0, prof, i, j);
			}
		}
	}

	@Override
	public Tensor4D backward(Object grad) {
		verificarConstrucao();

		if (grad instanceof Tensor4D) {
			Tensor4D g = (Tensor4D) grad;
			int profundidade = formEntrada[1];   
			for (int i = 0; i < profundidade; i++) {
				gradAvgPool(_entrada, g, _gradEntrada, i);
			}
		
		} else {
			throw new IllegalArgumentException(
				"Formato de gradiente \" "+ grad.getClass().getTypeName() +" \" não " +
				"suportado para camada de AvgPooling."
			);
		}

		return _gradEntrada;
	}

	/**
	 * Calcula e atualiza os gradientes da camada de Avg Pooling em relação à entrada.
	 * <p>
	 *    Retroropaga os gradientes da camada seguinte para a camada de Avg Pooling, considerando 
	 *    a operação de agrupamento médio. Ela calcula os gradientes em relação à entrada para as 
	 *    camadas anteriores.
	 * </p>
	 * @param entrada entrada da camada.
	 * @param gradSeguinte gradiente da camada seguinte.
	 * @param gradEntrada gradiente de entrada da camada de Avg pooling.
	 * @param prof índice de profundidade da operação.
	 */
	private void gradAvgPool(Tensor4D entrada, Tensor4D gradSeguinte, Tensor4D gradEntrada, int prof) {
		int alturaEntrada = entrada.dim3();
		int larguraEntrada = entrada.dim4();
		int alturaGradSeguinte = gradSeguinte.dim3();
		int larguraGradSeguinte = gradSeguinte.dim4();

		for (int i = 0; i < alturaGradSeguinte; i++) {
			int linInicio = i * stride[0];
			int linFim = Math.min(linInicio + formFiltro[0], alturaEntrada);
			for (int j = 0; j < larguraGradSeguinte; j++) {
				int colInicio = j * stride[1];
				int colFim = Math.min(colInicio + formFiltro[1], larguraEntrada);

				double grad = gradSeguinte.get(0, prof, i, j);
				double mediaGrad = grad / (formFiltro[0] * formFiltro[1]);

				for (int lin = linInicio; lin < linFim; lin++) {
					for (int col = colInicio; col < colFim; col++) {
						gradEntrada.set(mediaGrad, 0, prof, lin, col);
					}
				}
			}
		}
	}

	@Override
	public Tensor4D saida() {
		verificarConstrucao();
		return _saida;
	}

	@Override
	public int[] formatoSaida() {
		verificarConstrucao();
		return formSaida;
	}

	@Override
	public int[] formatoEntrada() {
		verificarConstrucao();
		return formEntrada;
	}

	/**
	 * Retorna o formato do filtro (altura, largura) usado pela camada.
	 * @return formato do filtro da camada.
	 */
	public int[] formatoFiltro() {
		verificarConstrucao();
		return new int[]{
			formFiltro[0],
			formFiltro[1]
		};
	}

	/**
	 * Retorna o formato dos strides (altura, largura) usado pela camada.
	 * @return formato dos strides da camada.
	 */
	public int[] formatoStride() {
		verificarConstrucao();
		return new int[]{
			stride[0],
			stride[1]
		};
	}

	@Override
	public int numParametros() {
		return 0;
	}

	@Override
	public Tensor4D gradEntrada() {
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
	
}
