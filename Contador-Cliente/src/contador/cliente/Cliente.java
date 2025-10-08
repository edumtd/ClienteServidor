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
            System.out.println("Uso: java Cliente 1");
            return;
        }
        String clienteId = args[0];

        try {
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            // IMPORTANTE: Si ejecutas el cliente en otra PC, cambia 127.0.0.1
            // por la IP local del servidor.
            config.setServerURL(new URL("http://127.0.0.1:8080/xmlrpc"));
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);
            
            System.out.println("Cliente '" + clienteId + "' iniciado, conectando al servidor...");
            
            // Obtener historial de sesiones anteriores
            List<Integer> misValores = new ArrayList<>();
            try {
                Object[] params = new Object[]{clienteId};
                Object historialObj = client.execute("contador.obtenerHistorial", params);
                
                if (historialObj instanceof Object[]) {
                    Object[] historialArray = (Object[]) historialObj;
                    for (Object obj : historialArray) {
                        if (obj instanceof Integer) {
                            misValores.add((Integer) obj);
                        }
                    }
                }
                
                if (!misValores.isEmpty()) {
                    System.out.println("*** RECONECTADO - Recuperando datos de sesiones anteriores ***");
                    System.out.println("Valores obtenidos en sesiones anteriores: " + misValores.size());
                    System.out.println("Historial completo: " + misValores);
                    System.out.println("*** Continuando desde donde se quedó... ***\n");
                }
            } catch (Exception e) {
                System.out.println("Primera conexión de este cliente.");
            }
            
            Random random = new Random();
            
            // Agregar hook para cerrar sesión cuando se termine el programa
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Object[] params = new Object[]{clienteId};
                    client.execute("contador.cerrarSesion", params);
                } catch (Exception e) {
                    // Ignorar errores al cerrar (puede que el servidor ya no esté disponible)
                }
            }));

            while (true) {
                Object[] params = new Object[]{clienteId};
                Integer resultado = (Integer) client.execute("contador.incrementar", params);

                if (resultado > 0) {
                    misValores.add(resultado);
                    System.out.println("Éxito. Nuevo valor: " + resultado + " (Total acumulado: " + misValores.size() + ")");
                } else if (resultado == 0) {
                    System.out.println("Turno repetido, esperando...");
                } else if (resultado == -1) {
                    System.out.println("El contador ha finalizado.");
                    break;
                }
                
                Thread.sleep(100 + random.nextInt(200));
            }
            
            System.out.println("\n--- Registro final para '" + clienteId + "' ---");
            System.out.println("Total de valores obtenidos (todas las sesiones): " + misValores.size());
            System.out.println("Lista completa de valores: " + misValores);

        } catch (Exception e) {
            System.err.println("Error en el cliente: " + e.getMessage());
        }
    }
}