package rna.serializacao;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import rna.estrutura.Convolucional;

public class UtilsConv{

   public void serializar(Convolucional camada, BufferedWriter bw){
      try{
         //nome da camada pra facilitar
         bw.write(camada.getClass().getSimpleName());
         bw.newLine();

         //formato de entrada
         int[] entrada = camada.formatoEntrada();
         for(int i = 0; i < entrada.length; i++){
            bw.write(entrada[i] + " ");
         }
         bw.newLine();
         
         //formato de saída
         int[] saida = camada.formatoSaida();
         for(int i = 0; i < saida.length; i++){
            bw.write(saida[i] + " ");
         }
         bw.newLine();
         
         //função de ativação
         bw.write(String.valueOf(camada.obterAtivacao().getClass().getSimpleName()));
         bw.newLine();

         //bias
         bw.write(String.valueOf(camada.temBias()));
         bw.newLine();

         //filtros
         for(int i = 0; i < camada.filtros.length; i++){
            for(int j = 0; j < camada.filtros[i].length; j++){
               for(int k = 0; k < camada.filtros[i][j].lin(); k++){
                  for(int l = 0; l < camada.filtros[i][j].col(); l++){
                     double peso = camada.filtros[i][j].dado(k, l);
                     bw.write(String.valueOf(peso));
                     bw.newLine();
                  }
               }
            }
         }
         
         if(camada.temBias()){
            for(int i = 0; i < camada.bias.length; i++){
               for(int j = 0; j < camada.bias[i].lin(); j++){
                  for(int k = 0; k < camada.bias[i].col(); k++){
                     double bias = camada.bias[i].dado(j, k);
                     bw.write(String.valueOf(bias));
                     bw.newLine();
                  }
               }
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }

   public Convolucional lerConfig(BufferedReader br){
      try{
         //formato de entrada
         String[] sEntrada = br.readLine().split(" ");
         int[] entrada = new int[sEntrada.length];
         for(int i = 0; i < sEntrada.length; i++){
            entrada[i] = Integer.parseInt(sEntrada[i]);
         }

         //formato de saída
         String[] sSaida = br.readLine().split(" ");
         int[] saida = new int[sSaida.length];
         for(int i = 0; i < sSaida.length; i++){
            saida[i] = Integer.parseInt(sSaida[i]);
         }
         
         //função de ativação
         String ativacao = br.readLine();

         //bias
         boolean bias = Boolean.valueOf(br.readLine());
         
         //inicialização da camada
         int[] formFiltro = {saida[0], saida[1]};
         int numFiltros = saida[2];

         Convolucional camada = new Convolucional(formFiltro, numFiltros, ativacao);
         camada.configurarBias(bias);
         camada.construir(entrada);
         return camada;
      }catch(Exception e){
         throw new RuntimeException(e);
      }
   }

   public void lerPesos(Convolucional camada, BufferedReader br){
      try{
         for(int i = 0; i < camada.filtros.length; i++){
            for(int j = 0; j < camada.filtros[i].length; j++){
               for(int k = 0; k < camada.filtros[i][j].lin(); k++){
                  for(int l = 0; l < camada.filtros[i][j].col(); l++){
                     double filtro = Double.parseDouble(br.readLine());
                     camada.filtros[i][j].editar(k, l, filtro);
                  }
               }
            }
         }
         
         if(camada.temBias()){
            for(int i = 0; i < camada.bias.length; i++){
               for(int j = 0; j < camada.bias[i].lin(); j++){
                  for(int k = 0; k < camada.bias[i].col(); k++){
                     double bias = Double.parseDouble(br.readLine());
                     camada.bias[i].editar(j, k, bias);
                  }
               }
            }
         }
      }catch(Exception e){
         throw new RuntimeException(e);
      }
   }
}
