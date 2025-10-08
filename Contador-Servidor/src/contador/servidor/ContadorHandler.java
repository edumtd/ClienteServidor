package contador.servidor; // <-- Verifica tu paquete

import org.apache.xmlrpc.webserver.WebServer;
import java.io.*;
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
    private static final String PERSISTENCE_FILE = "session_data.ser";

    // Método estático para inicializar el handler (cargar datos persistentes)
    public static void inicializar() {
        cargarDatos();
    }

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

            // Guardar datos después de cada incremento exitoso
            guardarDatos();

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

    // Método para obtener el historial de un cliente
    public Object[] obtenerHistorial(String clienteId) {
        synchronized (ContadorHandler.class) {
            List<Integer> historial = registroLlamadas.get(clienteId);
            if (historial == null || historial.isEmpty()) {
                return new Object[0];
            }
            return historial.toArray();
        }
    }

    // Método para guardar los datos en archivo
    private static void guardarDatos() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PERSISTENCE_FILE))) {
            // Crear un objeto de datos para serializar
            SessionData data = new SessionData();
            data.contador = contador;
            data.ultimoClienteId = ultimoClienteId;
            data.registroLlamadas = new HashMap<>(registroLlamadas);
            oos.writeObject(data);
        } catch (IOException e) {
            System.err.println("Error al guardar datos: " + e.getMessage());
        }
    }

    // Método para cargar los datos desde archivo
    private static void cargarDatos() {
        File file = new File(PERSISTENCE_FILE);
        if (!file.exists()) {
            System.out.println("No hay datos previos. Iniciando con valores por defecto.");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            SessionData data = (SessionData) ois.readObject();
            contador = data.contador;
            ultimoClienteId = data.ultimoClienteId;
            registroLlamadas.clear();
            registroLlamadas.putAll(data.registroLlamadas);
            System.out.println("Datos cargados: Contador=" + contador + ", Último cliente='" + ultimoClienteId + "'");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error al cargar datos: " + e.getMessage());
        }
    }

    // Clase interna para serialización de datos de sesión
    private static class SessionData implements Serializable {
        private static final long serialVersionUID = 1L;
        int contador;
        String ultimoClienteId;
        Map<String, List<Integer>> registroLlamadas;
    }
}