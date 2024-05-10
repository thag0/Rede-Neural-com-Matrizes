package jnn.core.tensor;

import java.util.Iterator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.DoubleBinaryOperator;

/**
 * <h2>
 *		Tensor multidimensional
 * </h2>
 * Implementação de um array multidimensional com finalidade de simplificar 
 * o uso de estrutura de dados dentro da biblioteca.
 * <p>
 * 		O tensor possui algumas funções próprias com intuito de aproveitar a
 * 		velocidade de processamento usando um único array contendo os dados do dele.
 * </p>
 * <h2>
 *		Exemplo de criação:
 * </h2>
 * <pre>
 *Tensor tensor = new Tensor(1, 1, 2, 2);
 *Tensor tensor = new Tensor(new int[]{2, 2});
 *Tensor tensor = new Tensor(2, 2);
 *tensor = [
 *  [[0.0, 0.0],
 *   [0.0, 0.0]]
 *]
 * </pre>
 * Algumas operações entre tensores são válidas desde que as dimensões
 * de ambos os tensores sejam iguais.
 * <pre>
 *Tensor a = new Tensor(2, 2);
 *a.preencer(1);
 *Tensor b = new Tensor(2, 2);
 *b.preencer(2);
 *a.add(b);//operação acontece dentro do tensor A
 *a = [
 *  [[3.0, 3.0],
 *   [3.0, 3.0]]
 *]
 * </pre>
 * 
 * @author Thiago Barroso, acadêmico de Engenharia da Computação pela
 * Universidade Federal do Pará, Campus Tucuruí. Maio/2024.
 */
public class Tensor implements Iterable<Variavel> {
    
	/**
	 * Dimensões do tensor.
	 */
	private int[] shape;

	/**
	 * Conjunto de elementos do tensor.
	 */
	public Variavel[] dados;

	/**
	 * Nome do tensor.
	 */
	private String nome = getClass().getSimpleName();

	/**
	 * Inicializa um tensor a partir de outra instância.
	 * <p>
	 *		O conteúdo do tensor recebido será copiado.
	 * </p>
	 * @param tensor tensor desejado.
	 */
    public Tensor(Tensor tensor) {
		if (tensor == null) {
			throw new IllegalArgumentException(
				"O tensor fornecido é nulo."
			);
		}

        this.shape = tensor.shape.clone();

        int n = tensor.tamanho();
        this.dados = inicializarDados(n);
		for (int i = 0; i < n; i++) {
			this.dados[i] = tensor.dados[i].clone();
		}
    }

	/**
	 * Inicializa um tensor a partir de um array quadridimensional primitivo.
	 * @param tensor tensor desejado.
	 */
	public Tensor(double[][][][] tensor) {
		if (tensor == null) {
			throw new IllegalArgumentException(
				"\nO tensor fornecido é nulo."
			);
		}

		this.shape = new int[]{
            tensor.length, 
            tensor[0].length, 
            tensor[0][0].length, 
            tensor[0][0][0].length
        };

		this.dados = inicializarDados(shape[0] * shape[1] * shape[2] * shape[3]);

		copiar(tensor);
	}

	/**
	 * Inicializa um tensor a partir de um array tridimensional primitivo.
	 * @param tensor tensor desejado.
	 */
	public Tensor(double[][][] tensor) {
		if (tensor == null) {
			throw new IllegalArgumentException(
				"\nO tensor fornecido é nulo."
			);
		}

		this.shape = new int[]{
            tensor.length, 
            tensor[0].length, 
            tensor[0][0].length,
        };

		this.dados = inicializarDados(shape[0] * shape[1] * shape[2]);

		copiar(tensor);
	}

	/**
	 * Inicializa um tensor a partir de um array bidimensional primitivo.
	 * @param mat matriz desejada.
	 */
	public Tensor(double[][] mat) {
		if (mat == null) {
			throw new IllegalArgumentException(
				"\nA matriz fornecida é nula."
			);
		}

		int col = mat[0].length;
		for (int i = 1; i < mat.length; i++) {
			if (mat[i].length != col) {
				throw new IllegalArgumentException(
					"\nA matriz deve conter a mesma quantidade de linhas para todas as colunas."
				);
			}
		}

		this.shape = copiarShape(new int[]{mat.length, mat[0].length});
		this.dados = inicializarDados(mat.length * mat[0].length);

		copiar(mat);
	}

	/**
	 * Inicializa um tensor a partir de um array.
	 * @param arr array desejado.
	 */
    public Tensor(Variavel[] arr) {
        shape = new int[]{arr.length};
        dados = inicializarDados(arr.length);
		for (int i = 0; i < arr.length; i++) {
			dados[i].set(arr[i]);
		}
    }

	/**
	 * Inicializar um tensor a partir de um conjunto de dados e formato
	 * pré-definidos.
	 * @param dados conjunto de dados desejado.
	 * @param shape formato do tensor.
	 */
	public Tensor(double[] dados, int... shape) {
		int[] s  = copiarShape(shape);
		int tam = calcularTamanho(s);
		if (tam != dados.length) {
			throw new IllegalArgumentException(
				"\nTamanho dos dados (" + dados.length + ") não corresponde ao " +
				"formato fornecido (" + tam + ")"
			);
		}
		this.shape = s;

		this.dados = inicializarDados(tam);
		for (int i = 0; i < tam; i++) {
			this.dados[i].set(dados[i]);
		}
	}

    /**
     * Inicializa um novo tensor vazio a partir de um formato especificado.
     * @param shape formato desejado.
     */
    public Tensor(int... shape) {
        if (shape == null) {
            throw new IllegalArgumentException(
                "\nShape fornecido é nulo."
            );
        }

        int tam = calcularTamanho(shape);

        this.shape = copiarShape(shape);
        dados = inicializarDados(tam);
    }

	/**
	 * Auxiliar na inicialização do conjunto de dados do tensor.
	 * @param tamanho tamanho desejado.
	 * @return array de dados alocado.
	 */
	private Variavel[] inicializarDados(int tamanho) {
		Variavel[] d = new Variavel[tamanho];
		for (int i = 0; i < tamanho; i++) {
			d[i] = new Variavel(0.0d);
		}

		return d;
	}

	/**
	 * Calcula a quantidade de elementos de acordo com o formato informado.
	 * @param shape formato desejado.
	 * @return tamanho do array de elementos necessário.
	 */
    private int calcularTamanho(int[] shape) {
        if (shape.length == 0) return 0;

        int tam = 1;
        for (int i = 0; i < shape.length; i++) {
            if (shape[i] < 1) {
                throw new IllegalArgumentException(
                    "\nArray de formato deve conter valores maiores que 1."
                );
            }

            tam *= shape[i];
        }

        return tam;
    }

    /**
     * Copia valores relevantes para o formato do tensor.
     * @param shape shape desejado.
     * @return shape com valores úteis.
     */
	private int[] copiarShape(int[] shape) {
		if (shape.length == 0) {
			throw new IllegalArgumentException(
				"\nShape vazio."
			);
		}

		return shape.clone();
		
		//TODO reconsiderar isso aqui futuramente
		// int inicio = 0;
		// boolean difUmEncontrado = false;
		
		// for (int i = 0; i < shape.length; i++) {
		// 	if (shape[i] != 1) {
		// 		inicio = i;
		// 		difUmEncontrado = true;
		// 		break;
		// 	}
		// }
	
		// if (!difUmEncontrado) {
		// 	return new int[]{1};
		// }
	
		// int[] s = new int[shape.length - inicio];
		// System.arraycopy(shape, inicio, s, 0, s.length);
	
		// return s;
	}

	/**
	 * Configura o novo formato para o tensor.
	 * <p>
	 * A configuração não altera o conteúdo do tensor, e sim a forma
	 * como os dados são tratados e acessados.
	 * </p>
	 * Exemplo:
	 * <pre>
	 *tensor = [
	 *    [[1, 2],
	 *     [3, 4]]
	 *]
	 *
	 *r = tensor.reshape(4);
	 *
	 *r = [
	 *    [1, 2, 3, 4]
	 *]
	 * </pre>
	 * @param dim array contendo as novas dimensões (dim1, dim2, dim3, dim4).
	 * @return instância local alterada.
	 */
	public Tensor reshape(int... dims) {
		int tamInicial = calcularTamanho(shape);

		int[] dimsUteis = copiarShape(dims);
		int tamDesejado = calcularTamanho(dimsUteis);

		if (tamInicial != tamDesejado) {
			throw new IllegalArgumentException(
				"\nA quatidade de elementos com as novas dimensões (" + tamDesejado +
				") deve ser igual a quantidade de elementos do tensor (" + tamanho() + ")."
			);
		}

		Tensor novo = new Tensor(dimsUteis);
		novo.copiarElementos(dados);

		return novo;
	}

	/**
	 * Transpõe o conteúdo do tensor.
	 * @return {@code Tensor} transposto.
	 */
    public Tensor transpor() {
        if (shape.length == 1) {
			//transpor tensor coluna
			Tensor t = new Tensor(shape[0], 1);
			t.copiarElementos(dados);
			return t;
        }
		
		if (shape.length == 2 && shape[1] == 1) {
			Tensor t = new Tensor(shape[0]);
			t.copiarElementos(dados);
			return t;
		}

        int[] novoShape = new int[shape.length];
        for (int i = 0; i < shape.length; i++) {
            novoShape[i] = shape[shape.length - i - 1];
        }
		
        Tensor t = new Tensor(novoShape);

        int[] idsOriginais = new int[shape.length];
        int[] idsTranspostos = new int[shape.length];
        for (int i = 0; i < dados.length; i++) {
            int temp = i;
            for (int j = shape.length - 1; j >= 0; j--) {
                idsOriginais[j] = temp % shape[j];
                temp /= shape[j];
            }

            for (int j = 0; j < shape.length; j++) {
                idsTranspostos[j] = idsOriginais[shape.length - j - 1];
            }

			int indiceTransposto = t.indice(idsTranspostos);

            t.dados[indiceTransposto] = dados[i];
        }

        return t;
    }

	/**
	 * 
	 * @param n
	 * @param ids
	 * @return
	 */
	public Tensor bloco(int n, int... ids) {
		if (numDim() > 1) {
			throw new UnsupportedOperationException(
				"\nSem suporte para tensor com mais de uma dimensão."
			);
		}

		int elementos = tamanho();

		Variavel[] arr = new Variavel[elementos * n];
		for (int i = 0; i < n; i++){
			System.arraycopy(dados, 0, arr, i*elementos, elementos);
		}

		Tensor bloco = new Tensor(n, elementos);
		bloco.copiarElementos(arr);

		return bloco;
	}

    /**
     * Calcula o índice de um elementos dentro do conjunto de dados do tensor.
     * @param dims índices desejados.
     * @return índice correspondente no array de elementos do tensor.
     */
    private int indice(int... dims) {
        if (numDim() != dims.length) {
            throw new IllegalArgumentException(
				"\nNúmero de dimensões fornecidas " + dims.length + 
				" não corresponde às " + numDim() + " do tensor."
			);
        }
    
        int id = 0;
        int multiplicador = 1;
    
        for (int i = shape.length - 1; i >= 0; i--) {
            if (dims[i] < 0 || dims[i] >= shape[i]) {
                throw new IllegalArgumentException(
					"\nÍndice " + dims[i] + " fora dos limites para a dimensão " + i +
					" (tamanho = " + shape[i] + ");"
				);
            }
            id += dims[i] * multiplicador;
            multiplicador *= shape[i];
        }
    
        return id;
    }

	/**
	 * Retorna o elemento do tensor de acordo com os índices fornecidos.
	 * @param ids índices desejados para busca.
	 * @return valor de acordo com os índices.
	 */
    public double get(int... ids) {
        return dados[indice(ids)].get();
    }

	/**
	 * Edita o valor do tensor usando o valor informado.
	 * @param ids índices para atribuição.
	 * @param valor valor desejado.
	 */
    public void set(double valor, int... ids) {
        dados[indice(ids)].set(valor);
    }

	/**
	 * Preenche todo o conteúdo do tensor com o valor fornecido.
	 * @param valor valor desejado.
	 * @return instância local alterada.
	 */
	public Tensor preencher(double valor) {
		final int n = tamanho();
		for (int i = 0; i < n; i++) {
			dados[i].set(valor);
		}

		return this;
	}

	/**
	 * Preenche o conteúdo do tensor usando um contador iniciado com
	 * valor 1 que é alterado a cada elemento.
	 * @param cres contador crescente (1, 2, 3, ...), caso falso o
	 * contador é decrescente (-1, -2, -3, ...).
	 * @return instância local alterada.
	 */
	public Tensor preencherContador(boolean cres) {
		int tam = tamanho();

		if (cres) {
			for (int i = 0; i < tam; i++) {
				dados[i].set(i+1);
			}

		} else {
			for (int i = 0; i < tam; i++) {
				dados[i].set(tam-i-1);
			}
		}

		return this;
	}

	/**
	 * Zera todo o conteúdo o tensor.
	 * @return instância local alterada.
	 */
	public Tensor zerar() {
        final int n = tamanho();
		for (int i = 0; i < n; i++) {
			dados[i].set(0.0d);
		}

		return this;
	}

	/**
	 * Copia todo o conteúdo do tensor na instância local.
	 * @param tensor {@code Tensor} desejado.
	 * @return instância local alterada.
	 */
	public Tensor copiar(Tensor tensor) {
		if (!compararShape(tensor)) {
			throw new IllegalArgumentException(
				"\nDimensões " + shapeStr() + " incompatíveis com as do" +
				" tensor recebido " + tensor.shapeStr()
			);
		}

		System.arraycopy(tensor.dados, 0, this.dados, 0, tamanho());

		return this;
	}

	/**
	 * Copia todo o conteúdo do array na instância local.
	 * @param arr array desejado.
	 * @return instância local alterada.
	 */
    public Tensor copiar(double[][][][] arr) {
        if (numDim() != 4) {
            throw new IllegalArgumentException(
                "\nTensor tem " + numDim() + " dimensões, mas deve" +
                " ter 4."
            );
        }

        int d1 = shape[0];
        int d2 = shape[1];
        int d3 = shape[2];
        int d4 = shape[3];

		if ((d1 != arr.length) ||
			(d2 != arr[0].length) ||
			(d3 != arr[0][0].length) ||
			(d4 != arr[0][0][0].length)) {
			throw new IllegalArgumentException(
				"\nDimensões do tensor " + shapeStr() +
				" incompatíveis com as do array recebido ("
				+ arr.length + ", " + arr[0].length + ", " + arr[0][0].length + ", " + arr[0][0][0].length
				+ ")."
			);
		}

		int cont = 0;
		for (int i = 0; i < d1; i++) {
			for (int j = 0; j < d2; j++) {
				for (int k = 0; k < d3; k++) {
					for (int l = 0; l < d4; l++) {
						this.dados[cont++].set(arr[i][j][k][l]);
					}
				}
			}
		}

		return this;
    }

	/**
	 * Copia todo o conteúdo do array na instância local.
	 * @param arr array desejado.
	 * @return instância local alterada.
	 */
    public Tensor copiar(double[][][] arr) {
        if (numDim() != 3) {
            throw new IllegalArgumentException(
                "\nTensor tem " + numDim() + " dimensões, mas deve" +
                " ter 3."
            );
        }

        int d1 = shape[0];
        int d2 = shape[1];
        int d3 = shape[2];

		if ((d1 != arr.length) ||
			(d2 != arr[0].length) ||
			(d3 != arr[0][0].length)) {
			throw new IllegalArgumentException(
				"\nDimensões do tensor " + shapeStr() +
				" incompatíveis com as do array recebido ("
				+ arr.length + ", " + arr[0].length + ", " + arr[0][0].length + ")."
			);
		}

		int cont = 0;
		for (int i = 0; i < d1; i++) {
			for (int j = 0; j < d2; j++) {
				for (int k = 0; k < d3; k++) {
					this.dados[cont++].set(arr[i][j][k]);
				}
			}
		}

		return this;
    }

	/**
	 * Copia todo o conteúdo do array na instância local.
	 * @param arr array desejado.
	 * @return instância local alterada.
	 */
    public Tensor copiar(double[][] arr) {
        if (numDim() > 2) {
            throw new IllegalArgumentException(
                "\nTensor tem " + numDim() + " dimensões, mas deve" +
                " ter 2."
            );
        }

        int lin = (numDim() == 1) ? 1: shape[0];
        int col = (numDim() == 1) ? shape[0] : shape[1];

		if ((lin != arr.length) ||
			(col != arr[0].length)) {
			throw new IllegalArgumentException(
				"\nDimensões do tensor " + shapeStr() +
				" incompatíveis com as do array recebido ("
				+ arr.length + ", " + arr[0].length + ")."
			);
		}

		int id = 0;
		for (int i = 0; i < lin; i++) {
			for (int j = 0; j < col; j++) {
				dados[id++].set(arr[i][j]);
			}
		}

		return this;
    }

	/**
	 * Copia todo o conteúdo do array na instância local.
	 * @param arr array desejado.
	 * @return instância local alterada.
	 */
    public Tensor copiar(double[] arr) {
        if (numDim() != 1) {
            throw new IllegalArgumentException(
                "\nTensor tem " + numDim() + " dimensões, mas deve" +
                " ter 1."
            );
        }

		if ((tamanho() != arr.length)) {
			throw new IllegalArgumentException(
				"\nDimensões do tensor " + shapeStr() +
				" incompatíveis com as do array recebido (" + arr.length + ")."
			);
		}

		for (int i = 0; i < arr.length; i++) {
			dados[i].set(arr[i]);
		}

		return this;
    }

	/**
	 * Copia apenas os dados contidos no array, sem levar em consideração
	 * as dimensões do tensor.
	 * <p>
	 * Ainda é necessário que a quantidade de elementos do array seja igual
	 * a quantidade de elementos do tensor.
	 * </p>
	 * @param elementos array de elementos desejado.
	 * @return instância local alterada.
	 */
	public Tensor copiarElementos(Variavel[] elementos) {
		if (elementos == null) {
			throw new IllegalArgumentException(
				"\nArray de elementos não pode ser nulo."
			);
		}

		if (elementos.length != tamanho()) {
			throw new IllegalArgumentException(
				"\nTamanho do array fornecido (" + elementos.length + ") inconpatível" +
				"com os elementos do tensor (" + tamanho() + ")."
			);
		}

		System.arraycopy(elementos, 0, dados, 0, tamanho());

		return this;
	}

	/**
	 * Copia apenas os dados contidos no array, sem levar em consideração
	 * as dimensões do tensor.
	 * <p>
	 * Ainda é necessário que a quantidade de elementos do array seja igual
	 * a quantidade de elementos do tensor.
	 * </p>
	 * @param elementos array de elementos desejado.
	 * @return instância local alterada.
	 */
	public Tensor copiarElementos(double[] elementos) {
		if (elementos == null) {
			throw new IllegalArgumentException(
				"\nArray de elementos não pode ser nulo."
			);
		}

		if (elementos.length != tamanho()) {
			throw new IllegalArgumentException(
				"\nTamanho do array fornecido (" + elementos.length + ") inconpatível" +
				"com os elementos do tensor (" + tamanho() + ")."
			);
		}

		for (int i = 0; i < elementos.length; i++) {
			dados[i].set(elementos[i]);
		}
		
		return this;
	}

	/**
	 * Adiciona todo o conteúdo {@code elemento a elemento} usando o tensor recebido,
	 * seguindo a expressão:
	 * <pre>
	 *  this += tensor
	 * </pre>
	 * @param tensor {@code Tensor} com conteúdo.
	 * @return instância local alterada.
	 */
    public Tensor add(Tensor tensor) {
        if (!compararShape(tensor)) {
            throw new IllegalArgumentException(
                "\nTensor fornecido possui shape " + tensor.shapeStr() +
				", shape esperado " + shapeStr()
            );
        }

        int n = tamanho();
        for (int i = 0; i < n; i++) {
            dados[i].add(tensor.dados[i]);
        }

        return this;
    }

	public Tensor add(double valor, int... ids) {
		dados[indice(ids)].add(valor);
		return this;
	}

	/**
	 * Subtrai todo o conteúdo {@code elemento a elemento} usando o tensor recebido,
	 * seguindo a expressão:
	 * <pre>
	 *  this -= tensor
	 * </pre>
	 * @param tensor {@code Tensor} com conteúdo.
	 * @return instância local alterada.
	 */
    public Tensor sub(Tensor tensor) {
        if (!compararShape(tensor)) {
            throw new IllegalArgumentException(
                "\nTensor fornecido deve conter o mesmo shape."
            );
        }

        int n = tamanho();
        for (int i = 0; i < n; i++) {
            dados[i].sub(tensor.dados[i]);
        }

        return this;
    }

	/**
	 * Multiplica todo o conteúdo {@code elemento a elemento} usando o tensor recebido,
	 * seguindo a expressão:
	 * <pre>
	 *  this *= tensor
	 * </pre>
	 * @param tensor {@code Tensor} com conteúdo.
	 * @return instância local alterada.
	 */
    public Tensor mult(Tensor tensor) {
        if (!compararShape(tensor)) {
            throw new IllegalArgumentException(
                "\nTensor fornecido deve conter o mesmo shape."
            );
        }

        int n = tamanho();
        for (int i = 0; i < n; i++) {
            dados[i].mult(tensor.dados[i]);
        }

        return this;
    }

	/**
	 * Divide todo o conteúdo {@code elemento a elemento} usando o tensor recebido,
	 * seguindo a expressão:
	 * <pre>
	 *  this /= tensor
	 * </pre>
	 * @param tensor {@code Tensor} com conteúdo.
	 * @return instância local alterada.
	 */
    public Tensor div(Tensor tensor) {
        if (!compararShape(tensor)) {
            throw new IllegalArgumentException(
                "\nTensor fornecido deve conter o mesmo shape."
            );
        }

        int n = tamanho();
        for (int i = 0; i < n; i++) {
            dados[i].div(tensor.dados[i]);
        }

        return this;
    }

	/**
	 * Remove as dimensões que tem tamanho igual a 1.
	 * @param dim índice da dimensão desejada.
	 * @return instância local, talvez alterada.
	 */
	public Tensor squeeze(int dim) {
		if (numDim() == 1) return this; // Não fazer nada com tensores escalares
	
		if (dim < 0 || dim >= shape.length) {
			throw new IllegalArgumentException("\nDimensão especificada inválida");
		}
	
		if (shape[dim] != 1) {
			return this; // A dimensão especificada já possui tamanho diferente de 1
		}
	
		int[] novoShape = new int[shape.length - 1];
		int id = 0;
		for (int i = 0; i < shape.length; i++) {
			if (i != dim) {
				novoShape[id++] = shape[i];
			}
		}
	
		shape = novoShape;
	
		return this;
	}

	/**
	 * Adiciona uma nova dimensão com tamanho 1.
	 * @param dim índice da dimensão que será adicionada.
	 * @return instância local alterada.
	 */
    public Tensor unsqueeze(int dim) {
        if (dim < 0 || dim > shape.length) {
            throw new IllegalArgumentException(
				"\nEixo " + dim + " fora de alcance"
			);
        }
        
        int[] novoShape = new int[shape.length + 1];
        
		for (int i = 0; i < dim; i++) {
            novoShape[i] = shape[i];
        }
        novoShape[dim] = 1;
        for (int i = dim; i < shape.length; i++) {
            novoShape[i + 1] = shape[i];
        }

        this.shape = novoShape;

		return this;
    }

	/**
	 * Fatia o conteúdo do tensor de acordo com os índices especificados.
	 * <p>
	 *		Exemplo:
	 * </p>
	 * <pre>
	 *tensor [
	 *	[[1, 2, 3],
	 *	 [4, 5, 6]]
	 *]
	 *
	 *slice = tensor.slice(new int[]{0, 0}, new int[]{1, 3});
	 * 
	 *slice = [
	 * 	[[1, 2, 3]]
	 *]
	 * </pre>
	 * @param idsInicio índices de incio do fatiamento (inclusivo).
	 * @param idsFim índices do fim do fatiamento (exclusivos).
	 * @return {@code Tensor} fatiado.
	 */
	public Tensor slice(int[] idsInicio, int[] idsFim) {
		if (idsInicio.length != shape.length || idsFim.length != shape.length) {
			throw new IllegalArgumentException(
				"\nNúmero de índices de início/fim não corresponde às dimensões do tensor (" + numDim() + ")."
			);
		}
	
		int[] novoShape = new int[shape.length];
		for (int i = 0; i < shape.length; i++) {
			if (idsInicio[i] < 0 || idsInicio[i] >= shape[i] ||
				idsFim[i] < 0 || idsFim[i] > shape[i] || idsFim[i] <= idsInicio[i]) {
				throw new IllegalArgumentException(
					"\nÍndices de início/fim inválidos para a dimensão " + i
				);
			}
			novoShape[i] = idsFim[i] - idsInicio[i];
		}
	
		Tensor slice = new Tensor(novoShape);
	
		int[] indices = new int[shape.length];
		final int tam = tamanho();
		for (int i = 0; i < tam; i++) {
			int id = i;
			for (int j = shape.length - 1; j >= 0; j--) {
				indices[j] = id % shape[j];
				id /= shape[j];
			}
	
			boolean dentroSlice = true;
			for (int j = 0; j < shape.length; j++) {
				if (indices[j] < idsInicio[j] || indices[j] >= idsFim[j]) {
					dentroSlice = false;
					break;
				}
			}
	
			if (dentroSlice) {
				int[] idsSlice = new int[shape.length];
				for (int j = 0; j < shape.length; j++) {
					idsSlice[j] = indices[j] - idsInicio[j];
				}

				//por padrão compartilhar as mesma variáveis
				slice.dados[indice(idsSlice)] = dados[indice(indices)];
			}
		}
	
		return slice;
	}

	/**
	 * Aplica a função recebida em todos os elementos do tensor.
	 * <p>
	 *      Exemplo:
	 * </p>
	 * <pre>
	 * tensor.aplicar(x -> Math.random());
	 * </pre>
	 * Onde {@code x} representa cada elemento dentro do tensor.
	 * 
	 * @param fun função desejada.
	 * @return instância local alterada.
	 */
    public Tensor aplicar(DoubleUnaryOperator fun) {
		if (fun == null) {
			throw new IllegalArgumentException(
				"\nFunção recebida é nula."
			);
		}

		for (int i = 0; i < dados.length; i++) {
			dados[i].set(fun.applyAsDouble(dados[i].get()));
		}

		return this;
	}

	/**
	 * Aplica a função recebida em todos os elementos do tensor.
	 * <p>
	 *      Exemplo:
	 * </p>
	 * <pre>
	 * tensor.aplicar(x -> Math.random());
	 * </pre>
	 * Onde {@code x} representa cada elemento dentro do tensor fornecido.
	 * @param tensor {@code Tensor} base.
	 * @param fun função para aplicar no tensor base.
	 * @return instância local alterada.
	 */
    public Tensor aplicar(Tensor tensor, DoubleUnaryOperator fun) {
		if (tensor == null) {
			throw new IllegalArgumentException(
				"\nTensor fornecido é nulo."
			);
		}
		if (!compararShape(tensor)) {
			throw new IllegalArgumentException(
				"\nAs dimensões do tensor fornecido " + tensor.shapeStr() +
				" e as da instância local " + shapeStr() + " devem ser iguais."
			);
		}
		if (fun == null) {
			throw new IllegalArgumentException(
				"\nFunção recebida é nula."
			);
		}

		for (int i = 0; i < dados.length; i++) {
			dados[i].set(fun.applyAsDouble(tensor.dados[i].get()));
		}

		return this;
	}

	/**
	 * Aplica a função recebida em todos os elementos do tensor de acordo com a operação
	 * entre A e B.
	 * <p>
	 *      Exemplo:
	 * </p>
	 * <pre>
	 *Tensor a = new Tensor(2, 2);
	 *Tensor b = new Tensor(2, 2);
	 *Tensor c = new Tensor(2, 2);
	 *c.aplicar(a, b, (x, y) -> x + y);
	 * </pre>
	 * Onde:
	 * <p>{@code x} representa cada elemento dentro do tensor A.
	 * <p>{@code y} representa cada elemento dentro do tensor B.
	 * <p>
	 *		É necessário que todos os tensores possuam o mesmo formato.
	 * </p>
	 * @param a {@code Tensor} A.
	 * @param b {@code Tensor} B.
	 * @param fun função para aplicar no tensor local.
	 * @return instância local alterada.
	 */
    public Tensor aplicar(Tensor a, Tensor b, DoubleBinaryOperator fun) {
		if (a == null || b == null) {
			throw new IllegalArgumentException(
				"\nOs tesores fornecidos não podem ser nulos."
			);
		}
		if (!compararShape(a)) {
			throw new IllegalArgumentException(
				"\nAs dimensões do tensor A " + a.shapeStr() +
				" e as da instância local " + shapeStr() + " devem ser iguais."
			);
		}
		if (!compararShape(b)) {
			throw new IllegalArgumentException(
				"\nAs dimensões do tensor B " + b.shapeStr() +
				" e as da instância local " + shapeStr() + " devem ser iguais."
			);
		}
		if (fun == null) {
			throw new IllegalArgumentException(
				"\nFunção recebida é nula."
			);
		}

		for (int i = 0; i < dados.length; i++) {
			dados[i].set(fun.applyAsDouble(a.dados[i].get(), b.dados[i].get()));
		}

		return this;
	}

	/**
	 * Aplica a função recebida em todos os elementos do tensor.
	 * <p>
	 *      Exemplo:
	 * </p>
	 * <pre>
	 * tensor.aplicar(x -> Math.random());
	 * </pre>
	 * Onde {@code x} representa cada elemento dentro do tensor.
	 * 
	 * @param fun função desejada.
	 * @param inicio índice inicial (inclusivo).
	 * @param fim índice final (exclusivo).
	 * @return instância local alterada.
	 */
    public Tensor aplicar(int d1, int d2, DoubleUnaryOperator fun) {
		if (fun == null) {
			throw new IllegalArgumentException(
				"\nFunção recebida é nula."
			);
		}

		int d3 = shape[shape.length-3];
		int d4 = shape[shape.length-2];
		
		int inicio = indice(d1, d2, 0, 0);
		int fim = inicio + (d3 * d4);

		for (int i = inicio; i < fim; i++) {
			dados[i].set(fun.applyAsDouble(dados[i].get()));
		}

		return this;
	}

	/**
	 * Retorna o valor contido no tensor, caso ele possua apenas um elemento.
	 * @return valor contido no tensor.
	 */
	public double item() {
		if (tamanho() > 1) {
			throw new IllegalArgumentException(
				"\nO tensor deve conter apenas um elemento."
			);
		}

		return dados[0].get();
	}

	/**
	 * Aplica a função recebida em todos os elementos do tensor.
	 * <p>
	 *		Exemplo:
	 * </p>
	 * <pre>
	 * tensor.map(x -> Math.random());
	 * </pre>
	 * Onde {@code x} representa cada elemento dentro do tensor local.
	 * @param fun função desejada.
	 * @return {@code Tensor} contendo o resultado.
	 */
	public Tensor map(DoubleUnaryOperator fun) {
		if (fun == null) {
			throw new IllegalArgumentException(
				"\nFunção recebida é nula."
			);
		}

		Tensor t = new Tensor(shape());

		for (int i = 0; i < t.tamanho(); i++) {
			t.dados[i].set(fun.applyAsDouble(dados[i].get()));
		}

		return t;
	}

	/**
	 * Aplica a função recebida em todos os elementos do tensor.
	 * <p>
	 *		Exemplo:
	 * </p>
	 * <pre>
	 *a = {1, 2, 3};
	 *b = {1, 2, 3};
	 *
	 *r = a.map(b, (x, y) -> x+y);
	 *r = {2, 4, 6};
	 *  </pre>
	 * Onde {@code x} representa cada elemento dentro do tensor local.
	 * @param tensor segundo {@code Tensor} para aplicar a função.
	 * @param fun função desejada.
	 * @return {@code Tensor} contendo o resultado.
	 */
	public Tensor map(Tensor tensor, DoubleBinaryOperator fun) {
		if (fun == null) {
			throw new IllegalArgumentException(
				"\nFunção recebida é nula."
			);
		}

		Tensor t = new Tensor(shape());

		for (int i = 0; i < t.tamanho(); i++) {
			t.dados[i].set(fun.applyAsDouble(dados[i].get(), tensor.dados[i].get()));
		}

		return t;
	}

	/**
	 * Reduz os elementos do tensor para um, aplicando a função de recebida.
	 * <p>
	 * Exemplo:
	 * </p>
	 * <pre>
	 *tensor = {1, 2, 3, 4, 5};
	 *res = tensor.reduce(0, (x, y) -> x+y);//tensor = {15}
	 * </pre>
	 * @param in valor inicial.
	 * @param fun função desejada.
	 * @return {@code Tensor} contendo o resultado.
	 */
	public Tensor reduce(double in, DoubleBinaryOperator fun) {
		if (fun == null) {
			throw new IllegalArgumentException(
				"\nFunção de redução não pode ser nula."
			);
		}

		Variavel res = new Variavel(in);
		for (Variavel val : dados) {
			res.set(fun.applyAsDouble(res.get(), val.get()));
		}

		return new Tensor(new Variavel[]{ res });
	}

	/**
	 * Retorna um {@code Tensor} contendo a soma dos elementos da 
     * instância local.
	 * @return {@code Tensor} resultado.
	 */
    public Tensor soma() {
        double soma = 0.0d;
        final int n = tamanho();
        for (int i = 0; i < n; i++) {
            soma += dados[i].get();
        }

        return new Tensor(new Variavel[]{ new Variavel(soma) });
    }

	/**
	 * Retorna um {@code Tensor} contendo a média aritmética dos 
     * elementos da instância local.
	 * @return {@code Tensor} resultado.
	 */
	public Tensor media() {
        double media = soma().item() / tamanho();
        return new Tensor(new Variavel[]{ new Variavel(media) });
    }

	/**
	 * Retorna um {@code Tensor} contendo o valor máximo dentro dos 
     * elementos da instância local.
	 * @return {@code Tensor} resultado.
	 */
	public Tensor maximo() {
		double max = dados[0].get();
		final int tam = tamanho();

		for (int i = 1; i < tam; i++) {
			if (dados[i].get() > max) max = dados[i].get();
		}

		return new Tensor(new Variavel[]{ new Variavel(max) });
	}

	/**
	 * Retorna um {@code Tensor} contendo o valor mínimo dentro dos 
     * elementos da instância local.
	 * @return {@code Tensor} resultado.
	 */
	public Tensor minimo() {
		double min = dados[0].get();
		final int tam = tamanho();

		for (int i = 1; i < tam; i++) {
			if (dados[i].get() < min) min = dados[i].get();
		}

		return new Tensor(new Variavel[]{ new Variavel(min) });
	}

	/**
	 * Retorna um {@code Tensor} contendo o desvio padrão de acordo com os
     * elementos da instância local.
	 * @return {@code Tensor} resultado.
     */
	public Tensor desvp() {
		double media = media().item();
		double soma = 0.0d;
        final int n = tamanho();

		for (int i = 0; i < n; i++) {
			soma += Math.pow(dados[i].get() - media, 2);
		}

		return new Tensor(new Variavel[]{ 
			new Variavel(Math.sqrt(soma / tamanho()))
		});
	}

	/**
	 * Normaliza os valores do tensor dentro do intervalo especificado.
	 * @param min valor mínimo do intervalo.
	 * @param max valor máximo do intervalo.
	 * @return instância local alterada.
	 */
	public Tensor normalizar(double min, double max) {
		double valMin = minimo().item();
		double valMax = maximo().item();

		double intOriginal = valMax - valMin;
		double intNovo = max - min;

        final int n = tamanho();
		for (int i = 0; i < n; i++) {
			dados[i].set(((dados[i].get() - valMin) / intOriginal) * intNovo + min);
		}

		return this;
	}

	/**
	 * Aplica a função de ativação {@code ReLU} em todos os
	 * elementos do tensor.
	 * @return instância local alterada.
	 */
	public Tensor relu() {
		return aplicar(x -> x > 0 ? x : 0);
	}

	/**
	 * Aplica a função de ativação {@code Sigmoid} em todos os
	 * elementos do tensor.
	 * @return instância local alterada.
	 */
	public Tensor sigmoid() {
		return aplicar(x -> 1 / (1 + Math.exp(-x)));
	}

	/**
	 * Aplica a função de ativação {@code TanH} (Tangente Hiperbólica)
	 * em todos os elementos do tensor.
	 * @return instância local alterada.
	 */
	public Tensor tanh() {
		return aplicar(x -> 2 / (1 + Math.exp(-2 * x)) - 1);
	}

	/**
	 * Aplica a função de ativação {@code Atan} (Arco Tangente)
	 * em todos os elementos do tensor.
	 * @return instância local alterada.
	 */
	public Tensor atan() {
		return aplicar(x -> Math.atan(x));
	}

	/**
	 * Calcula o valor {@code seno} de todos os elementos do tensor.
	 * @return instância local alterada.
	 */
	public Tensor sin() {
		return aplicar(x -> Math.sin(x));
	}

	/**
	 * Calcula o valor {@code cosseno} de todos os elementos do tensor.
	 * @return instância local alterada.
	 */
	public Tensor cos() {
		return aplicar(x -> Math.cos(x));
	}

	/**
	 * Calcula o valor {@code tangente} de todos os elementos do tensor.
	 * @return instância local alterada.
	 */
	public Tensor tan() {
		return aplicar(x -> Math.tan(x));
	}

	/**
	 * Calcula o valor {@code absoluto} de cada elemento do do tensor.
	 * @return instância local alterada.
	 */
	public Tensor abs() {
		return aplicar(x -> Math.abs(x));
	}

	/**
	 * Calcula o valor {@code exponencial} de cada elemento do do tensor.
	 * @return instância local alterada.
	 */
	public Tensor exp() {
		return aplicar(x -> Math.exp(x));
	}

	/**
	 * Calcula o valor {@code logaritmo natural} de cada elemento do do tensor.
	 * @return instância local alterada.
	 */
	public Tensor log() {
		return aplicar(x -> Math.log(x));
	}

    /**
     * Retorna a quantidade de dimensões do tensor.
     * @return quantidade de dimensões do tensor.
     */
    public int numDim() {
        return shape.length;
    }

	/**
	 * Retorna um array contendo as dimensões do tensor.
	 * @return dimensões do tensor.
	 */
    public int[] shape() {
        return shape;
    }

	/**
	 * Retorna uma String contendo as dimensões do tensor.
	 * @return dimensões do tensor em formato de String.
	 */
    public String shapeStr() {
        StringBuilder sb = new StringBuilder();

        sb.append("(");
        for (int i = 0; i < shape.length; i++) {
            sb.append(shape[i]).append(", ");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.deleteCharAt(sb.length()-1);
        sb.append(")");

        return sb.toString();
    }

	/**
	 * Compara todo o conteúdo da instância local, isso inclui as {@code dimensões}
	 * de cada tensor e seus {@code elementos individuais}.
	 * @param tensor {@code Tensor} desejado.
	 * @return {@code true} caso sejam iguais, {@code false} caso contrário.
	 */
	public boolean comparar(Tensor tensor) {
		if (!compararShape(tensor)) return false;

		for (int i = 0; i < dados.length; i++) {
			if (dados[i].get() != tensor.dados[i].get()) return false;
		}

		return true;
	}

    /**
     * Verifica se o shape do tensor fornecido é igual ao shape
     * da instância local.
     * @param tensor {@code Tensor} desejado.
     * @return {@code true} caso as dimensões de ambos os tensores sejam
     * iguais, {@code false} caso contrário.
     */
    public boolean compararShape(Tensor tensor) {
        int n = shape.length;
        if (n != tensor.shape.length) return false;

        for (int i = 0; i < n; i++) {
            if (shape[i] != tensor.shape[i]) return false;
        }

        return true;
    }

	/**
	 * Retorna a quantidade total de elementos no tensor.
	 * @return número elementos do tensor.
	 */
	public int tamanho() {
		return dados.length;
	}

	/**
	 * Retorna o conteúdo do tensor no formato de array
	 * @return conteúdo do tensor.
	 */
	public Variavel[] paraArray() {
		return dados;
	}

	/**
	 * Retorna o conteúdo do tensor no formato de array
	 * @return conteúdo do tensor.
	 */
	public double[] paraArrayPrimitivo() {
		double[] arr = new double[dados.length];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = dados[i].get();
		}

		return arr;
	}

	/**
	 * Configura o nome do tensor.
	 * @param nome novo nome.
	 * @return instância local alterada.
	 */
	public Tensor nome(String nome) {
		if (nome != null) {
			nome = nome.trim();
			if (!nome.isEmpty()) this.nome = nome;
		}

		return this;
	}

	/**
	 * Retorna o nome do tensor.
	 * @return nome do tensor.
	 */
	public String nome() {
		return this.nome;
	}

    @Override
    public String toString() {
        return construirPrint();
    }

	/**
	 * Exibe, via terminal, todo o conteúdo do tensor.
	 */
	public void print() {
		System.out.println(construirPrint());
	}

	/**
	 * Monta as informações de exibição do tensor.
	 * @return string formatada.
	 */
    private String construirPrint() {
		final String identacao = " ".repeat(4);
        int tamMaximo = -1;
        for (Variavel valor : dados) {
            int tamValor = String.format("%f", valor.get()).length();
            if (tamValor > tamMaximo) tamMaximo = tamValor;
        }

        StringBuilder sb = new StringBuilder();

        int[] indices = new int[shape.length];
        boolean[] parentesisAbertos = new boolean[shape.length];

		sb.append(nome()).append(" ").append(shapeStr()).append(" = [").append("\n");

        sb.append(identacao);
        for (int n = 0; n < tamanho(); n++) {
            for (int i = 0; i < indices.length; i++) {
                if (!parentesisAbertos[i]) {
                    sb.append("[");
                    parentesisAbertos[i] = true;
                }
            }

            final String valorStr = String.format("%f", get(indices));
            sb.append(" ".repeat(tamMaximo - valorStr.length()))
				.append(valorStr);

            final int idUltimaDim = shape.length - 1;
            if (indices[idUltimaDim] < shape[idUltimaDim] - 1) {
                sb.append(", ");
            }

            boolean qualquerParentesisAberto = false;
            int numParentesisFechados = 0;

            for (int i = indices.length - 1; i >= 0; i--) {
                indices[i] += 1;
                if (indices[i] >= shape[i]) {
                    indices[i] = 0;

                    sb.append("]");
                    if (i > 0 && indices[i - 1] < shape[i - 1] - 1) {
                        sb.append(",");
                    }

                    parentesisAbertos[i] = false;
                    qualquerParentesisAberto = true;
                    numParentesisFechados++;
                } else {
                    break;
                }
            }

            if (qualquerParentesisAberto) {
                if (numParentesisFechados > 1) {
                    sb.append("\n");
                }
                sb.append("\n").append(identacao);
                sb.append(" ".repeat(shape.length - numParentesisFechados));
            }
        }

		// arrumar ultima linha antes do fim do print
		int n = identacao.length();
		if (numDim() > 1) n += 1;
        for (int i = 0; i < n; i++) {
            sb.deleteCharAt(sb.length()-1);
        }

		sb.append("]").append("\n");

        return sb.toString().trim();
	}

	/**
	 * Clona o conteúdo do tensor numa instância separada.
	 * @return clone da instância local.
	 */
    @Override
	public Tensor clone() {
		return new Tensor(this);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Tensor4D) && comparar((Tensor) obj);
	}

	@Override
	public Iterator<Variavel> iterator() {
		return new TensorIterator();
	}

	/**
	 * Iterador para usar com o tensor, usando para percorrer
	 * os elementos do tensor sequencialmente.
	 */
	class TensorIterator implements Iterator<Variavel> {

		/**
		 * Contador do índice atual.
		 */
		private int indice = 0;

		@Override
		public boolean hasNext() {
			return indice < tamanho();
		}

		@Override
		public Variavel next() {
			return dados[indice++];
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
				"\nSem suporte."
			);
		}
	}

}