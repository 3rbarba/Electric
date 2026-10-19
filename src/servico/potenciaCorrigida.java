package servico;

public class potenciaCorrigida extends demandaEquip {
    public static int corrigirTab6(int potencia){
            return (int) (potencia / demandaTab7(potencia));
    }
}
