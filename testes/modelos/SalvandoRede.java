package testes.modelos;

import java.io.Serializable;

import ged.Dados;
import ged.Ged;
import rna.ativacoes.Sigmoid;
import rna.avaliacao.perda.ErroMedioQuadrado;
import rna.estrutura.Camada;
import rna.estrutura.Convolucional;
import rna.estrutura.Densa;
import rna.inicializadores.Xavier;
import rna.inicializadores.Zeros;
import rna.modelos.*;
import rna.otimizadores.*;
import rna.serializacao.Serializador;

@SuppressWarnings("unused")
public class SalvandoRede{
   public static void main(String[] args){
      Ged ged = new Ged();
      ged.limparConsole();
      String caminho = "./modelo-teste.txt";
      Serializador serializador = new Serializador();

      // Sequencial modelo = new Sequencial(new Camada[]{
      //    new Convolucional(new int[]{3, 3, 1}, new int[]{2, 2}, 2, "tanh")
      // });
      // modelo.compilar(new SGD(), new ErroMedioQuadrado(), new Zeros());
      // modelo.camada(0).inicializar(new Xavier(), new Xavier(), 0);
      // serializador.salvar(modelo, caminho);

      Sequencial modelo = serializador.lerSequencial(caminho);
      Convolucional camada = (Convolucional) modelo.camada(0);

      for(int i = 0; i < camada.numFiltros(); i++){
         camada.filtros[i][0].print("filtro " + i);
      }
      for(int i = 0; i < camada.bias.length; i++){
         camada.bias[i].print("bias " + i);
      }
   }
}
