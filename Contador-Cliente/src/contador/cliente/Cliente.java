/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package contador.cliente;

/**
 *
 * @author araul
 */


import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class Cliente {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java Cliente <id>");
            return;
        }
        String clienteId = args[0];

        try {
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            // IMPORTANTE: Si ejecutas el cliente en otra PC, cambia 127.0.0.1
            // por la IP local del servidor.
            config.setServerURL(new URL("http://172.31.10.27:8080/xmlrpc"));
            config.setConnectionTimeout(5000);  // Timeout de 5 segundos
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);
            
            System.out.println("Cliente '" + clienteId + "' iniciado, conectando al servidor...");
            
            // Intentar obtener historial previo al reconectar
            intentarRecuperarHistorial(client, clienteId);
            
            List<Integer> misValores = new ArrayList<>();
            Random random = new Random();
            int intentosReconexion = 0;
            int maxIntentosReconexion = 3;

            while (true) {
                try {
                    Object[] params = new Object[]{clienteId};
                    Integer resultado = (Integer) client.execute("contador.incrementar", params);

                    // Resetear contador de reconexión en caso de éxito
                    intentosReconexion = 0;

                    if (resultado > 0) {
                        misValores.add(resultado);
                        System.out.println("Éxito. Nuevo valor: " + resultado);
                    } else if (resultado == 0) {
                        System.out.println("Turno repetido, esperando...");
                    } else if (resultado == -1) {
                        System.out.println("El contador ha finalizado.");
                        break;
                    }
                    
                    Thread.sleep(100 + random.nextInt(200));
                    
                } catch (Exception e) {
                    intentosReconexion++;
                    System.err.println("Error de conexión: " + e.getMessage());
                    
                    if (intentosReconexion >= maxIntentosReconexion) {
                        System.err.println("No se pudo reconectar después de " + maxIntentosReconexion + " intentos.");
                        break;
                    }
                    
                    System.out.println("Intentando reconectar... (Intento " + intentosReconexion + "/" + maxIntentosReconexion + ")");
                    Thread.sleep(2000);  // Esperar 2 segundos antes de reintentar
                }
            }
            
            System.out.println("\n--- Registro final para '" + clienteId + "' ---");
            System.out.println("Total de valores obtenidos en esta sesión: " + misValores.size());

        } catch (Exception e) {
            System.err.println("Error en el cliente: " + e.getMessage());
        }
    }

    // Método para intentar recuperar el historial previo del cliente
    private static void intentarRecuperarHistorial(XmlRpcClient client, String clienteId) {
        try {
            Object[] params = new Object[]{clienteId};
            Object[] historial = (Object[]) client.execute("contador.obtenerHistorial", params);
            
            if (historial != null && historial.length > 0) {
                System.out.println("\n--- Reconexión detectada ---");
                System.out.println("Historial previo para '" + clienteId + "':");
                System.out.print("Valores obtenidos anteriormente: [");
                for (int i = 0; i < historial.length; i++) {
                    System.out.print(historial[i]);
                    if (i < historial.length - 1) System.out.print(", ");
                }
                System.out.println("]");
                System.out.println("Total de valores previos: " + historial.length);
                System.out.println("Continuando desde donde se quedó...\n");
            }
        } catch (Exception e) {
            System.out.println("Primera conexión (no hay historial previo).");
        }
    }
}