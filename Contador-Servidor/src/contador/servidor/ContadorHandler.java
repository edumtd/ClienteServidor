package contador.servidor; // <-- Verifica tu paquete

import org.apache.xmlrpc.webserver.WebServer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContadorHandler {

    // Variables estáticas: compartidas por todas las instancias
    private static int contador = 0;
    private static String ultimoClienteId = "";
    private static final Map<String, List<Integer>> registroLlamadas = Collections.synchronizedMap(new HashMap<>());
    private static WebServer webServer;

    // Método estático para que el servidor nos pase su referencia
    public static void setWebServer(WebServer server) {
        webServer = server;
    }

    // El método de incremento ya no necesita ser estático
    public int incrementar(String clienteId) {
        // Sincronizamos sobre la clase, ya que las variables son estáticas
        synchronized (ContadorHandler.class) {
            if (contador >= 100) {
                return -1;
            }
            if (clienteId.equals(ultimoClienteId)) {
                return 0;
            }

            contador++;
            ultimoClienteId = clienteId;
            registroLlamadas.putIfAbsent(clienteId, new ArrayList<>());
            registroLlamadas.get(clienteId).add(contador);

            System.out.println("Cliente '" + clienteId + "' -> Nuevo valor: " + contador);

            if (contador == 100) {
                System.out.println("--- CONTADOR LLEGO A 100! APAGANDO SERVIDOR... ---");
                new Thread(() -> {
                    if (webServer != null) {
                        webServer.shutdown();
                    }
                }).start();
            }
            return contador;
        }
    }
}