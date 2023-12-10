package rna.ativacoes;

import rna.estrutura.Convolucional;
import rna.estrutura.Densa;

/**
 * Implementação da função de ativação Linear para uso dentro 
 * da {@code Rede Neural}.
 */
public class Linear extends Ativacao{

   /**
    * Instancia a função de ativação Linear.
    */
   public Linear(){

   }

   @Override
   public void calcular(Densa camada){
      int i, j;
      
      for(i = 0; i < camada.saida.lin; i++){
         for(j = 0; j < camada.saida.col; j++){
            camada.saida.editar(i, j, camada.somatorio.dado(i, j));
         }
      }
   }

   @Override
   public void derivada(Densa camada){
      int i, j;
      double grad;

      for(i = 0; i < camada.derivada.lin; i++){
         for(j = 0; j < camada.derivada.col; j++){
            grad = camada.gradSaida.dado(i, j);
            camada.derivada.editar(i, j, (1 * grad));
         }
      }
   }

   @Override
   public void calcular(Convolucional camada){
      int i, j, k;
    
      for(i = 0; i < camada.saida.length; i++){
         for(j = 0; j < camada.saida[i].lin; j++){
            for(k = 0; k < camada.saida[i].col; k++){
               camada.saida[i].editar(j, k, camada.somatorio[i].dado(j, k));
            }
         }
      }
   }

   @Override
   public void derivada(Convolucional camada){
      int i, j, k;
      double grad;
    
      for(i = 0; i < camada.gradSaida.length; i++){
         for(j = 0; j < camada.gradSaida[i].lin; j++){
            for(k = 0; k < camada.gradSaida[i].col; k++){
               grad = camada.gradSaida[i].dado(j, k);
               camada.derivada[i].editar(j, k, grad);
            }
         }
      }
   }
}
