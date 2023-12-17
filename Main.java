import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import ged.*;
import geim.Geim;
import render.JanelaTreino;
import rna.avaliacao.perda.*;
import rna.estrutura.*;
import rna.inicializadores.*;
import rna.modelos.Modelo;
import rna.modelos.RedeNeural;
import rna.modelos.Sequencial;
import rna.otimizadores.*;

public class Main{
   static final int epocas = 5*1000;
   static final float escalaRender = 7f;
   static Ged ged = new Ged();
   static Geim geim = new Geim();

   public static void main(String[] args){      
      ged.limparConsole();

      int tamEntrada = 2;
      int tamSaida = 1;
      BufferedImage imagem = geim.lerImagem("/dados/mnist/treino/7/img_1.jpg");
      
      double[][] dados;
      if(tamSaida == 1) dados = geim.imagemParaDadosTreinoEscalaCinza(imagem);
      else if(tamSaida == 3) dados = geim.imagemParaDadosTreinoRGB(imagem);
      else return;

      double[][] in  = (double[][]) ged.separarDadosEntrada(dados, tamEntrada);
      double[][] out = (double[][]) ged.separarDadosSaida(dados, tamSaida);

      Modelo modelo = criarModelo(tamEntrada, tamSaida, false);
      System.out.println(modelo.info());

      //treinar e marcar tempo
      long t1, t2;
      long horas, minutos, segundos;

      System.out.println("Treinando.");
      t1 = System.nanoTime();
      treinoEmPainel(modelo, imagem.getWidth(), imagem.getHeight(), in, out);
      t2 = System.nanoTime();

      long tempoDecorrido = t2 - t1;
      long segundosTotais = TimeUnit.NANOSECONDS.toSeconds(tempoDecorrido);
      horas = segundosTotais / 3600;
      minutos = (segundosTotais % 3600) / 60;
      segundos = segundosTotais % 60;

      double precisao = (1 - modelo.avaliador.erroMedioAbsoluto(in, out))*100;
      double perda = modelo.avaliador.erroMedioQuadrado(in, out);
      System.out.println("Precisão = " + formatarDecimal(precisao, 2) + "%");
      System.out.println("Perda = " + perda);
      System.out.println("Tempo de treinamento: " + horas + "h " + minutos + "m " + segundos + "s");
      // exportarHistoricoPerda(rede, ged);
   }

   static Modelo criarModelo(int entradas, int saidas, boolean rna){
      Otimizador otm = new SGD(0.0001, 0.999);
      Perda perda = new ErroMedioQuadrado();
      Inicializador ini = new Xavier();

      if(rna){
         int[] arq = {entradas, 13, 13, saidas};
         RedeNeural modelo = new RedeNeural(arq);
         modelo.compilar(otm, perda, ini);
         modelo.configurarAtivacao("tanh");
         modelo.configurarAtivacao(modelo.camadaSaida(), "sigmoid");
         return modelo;
      
      }else{
         Sequencial modelo = new Sequencial();
         modelo.add(new Densa(entradas, 13, "tanh"));
         modelo.add(new Densa(13, saidas, "sigmoid"));
         modelo.compilar(otm, perda, ini);
         return modelo;
      }
   }

   /**
    * Treina e exibe o resultado da Rede Neural no painel.
    * @param modelo modelo de rede neural usado no treino.
    * @param altura altura da janela renderizada.
    * @param largura largura da janela renderizada.
    * @param entradas dados de entrada para o treino.
    * @param saidas dados de saída relativos a entrada.
    */
   static void treinoEmPainel(Modelo modelo, int altura, int largura, double[][] entradas, double[][] saidas){
      final int fps = 600;
      int epocasPorFrame = 30;

      //acelerar o processo de desenho
      //bom em situações de janelas muito grandes
      int n = Runtime.getRuntime().availableProcessors();
      int numThreads = (n > 1) ? (int)(n * 0.5) : 1;

      JanelaTreino jt = new JanelaTreino(largura, altura, escalaRender, numThreads);
      jt.desenharTreino(modelo, 0);
      
      //trabalhar com o tempo de renderização baseado no fps
      double intervaloDesenho = 1000000000/fps;
      double proximoTempoDesenho = System.nanoTime() + intervaloDesenho;
      double tempoRestante;
      
      int i = 0;
      while(i < epocas && jt.isVisible()){
         modelo.treinar(entradas, saidas, epocasPorFrame);
         jt.desenharTreino(modelo, i);
         i += epocasPorFrame;

         try{
            tempoRestante = proximoTempoDesenho - System.nanoTime();
            tempoRestante /= 1000000;
            if(tempoRestante < 0) tempoRestante = 0;

            Thread.sleep((long)tempoRestante);
            proximoTempoDesenho += intervaloDesenho;

         }catch(Exception e){ }
      }

      jt.dispose();
   }

   /**
    * Salva um arquivo csv com o historico de desempenho da rede.
    * @param rede rede neural.
    * @param ged gerenciador de dados.
    */
   static void exportarHistoricoPerda(Modelo rede, Ged ged){
      System.out.println("Exportando histórico de perda");
      double[] perdas = rede.historico();
      double[][] dadosPerdas = new double[perdas.length][1];

      for(int i = 0; i < dadosPerdas.length; i++){
         dadosPerdas[i][0] = perdas[i];
      }

      Dados dados = new Dados(dadosPerdas);
      ged.exportarCsv(dados, "historico-perda");
   }

   /**
    * Formata o valor recebido para a quantidade de casas após o ponto
    * flutuante.
    * @param valor valor alvo.
    * @param casas quantidade de casas após o ponto fluntuante.
    * @return
    */
   static String formatarDecimal(double valor, int casas){
      String valorFormatado = "";

      String formato = "#.";
      for(int i = 0; i < casas; i++) formato += "#";

      DecimalFormat df = new DecimalFormat(formato);
      valorFormatado = df.format(valor);

      return valorFormatado;
   }
}
