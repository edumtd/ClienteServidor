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
    // Mapa para rastrear el último valor que recibió cada cliente
    private static final Map<String, Integer> ultimoValorCliente = Collections.synchronizedMap(new HashMap<>());
    // Mapa para rastrear todas las sesiones históricas de cada cliente
    private static final Map<String, List<List<Integer>>> historialSesiones = Collections.synchronizedMap(new HashMap<>());
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
            ultimoValorCliente.put(clienteId, contador);

            System.out.println("Cliente '" + clienteId + "' -> Nuevo valor: " + contador);

            if (contador == 100) {
                System.out.println("--- CONTADOR LLEGO A 100! APAGANDO SERVIDOR... ---");
                // Guardar la sesión actual antes de cerrar
                guardarSesionActual();
                new Thread(() -> {
                    if (webServer != null) {
                        webServer.shutdown();
                    }
                }).start();
            }
            return contador;
        }
    }
    
    // Método para obtener el historial completo de un cliente (todas las sesiones)
    public List<Integer> obtenerHistorial(String clienteId) {
        synchronized (ContadorHandler.class) {
            List<Integer> historialCompleto = new ArrayList<>();
            
            // Añadir datos de sesiones anteriores
            if (historialSesiones.containsKey(clienteId)) {
                for (List<Integer> sesion : historialSesiones.get(clienteId)) {
                    historialCompleto.addAll(sesion);
                }
            }
            
            // Añadir datos de la sesión actual
            if (registroLlamadas.containsKey(clienteId)) {
                historialCompleto.addAll(registroLlamadas.get(clienteId));
            }
            
            return historialCompleto;
        }
    }
    
    // Método para cerrar sesión de un cliente y guardar su historial
    public void cerrarSesion(String clienteId) {
        synchronized (ContadorHandler.class) {
            if (registroLlamadas.containsKey(clienteId) && !registroLlamadas.get(clienteId).isEmpty()) {
                historialSesiones.putIfAbsent(clienteId, new ArrayList<>());
                // Crear una copia de la sesión actual
                List<Integer> sesionActual = new ArrayList<>(registroLlamadas.get(clienteId));
                historialSesiones.get(clienteId).add(sesionActual);
                
                System.out.println("Sesión cerrada para cliente '" + clienteId + "' - Total valores en esta sesión: " + sesionActual.size());
                mostrarHistorialCliente(clienteId);
                
                // Limpiar la sesión actual pero mantener el último valor
                registroLlamadas.put(clienteId, new ArrayList<>());
            }
        }
    }
    
    // Método auxiliar para guardar todas las sesiones actuales
    private static void guardarSesionActual() {
        for (Map.Entry<String, List<Integer>> entry : registroLlamadas.entrySet()) {
            String clienteId = entry.getKey();
            List<Integer> valoresActuales = entry.getValue();
            
            if (!valoresActuales.isEmpty()) {
                historialSesiones.putIfAbsent(clienteId, new ArrayList<>());
                historialSesiones.get(clienteId).add(new ArrayList<>(valoresActuales));
            }
        }
        
        // Mostrar resumen final de todos los clientes
        System.out.println("\n=== RESUMEN FINAL DE TODOS LOS CLIENTES ===");
        for (String clienteId : historialSesiones.keySet()) {
            mostrarHistorialCliente(clienteId);
        }
    }
    
    // Método auxiliar para mostrar el historial de un cliente
    private static void mostrarHistorialCliente(String clienteId) {
        System.out.println("\n--- Historial completo de cliente '" + clienteId + "' ---");
        
        if (historialSesiones.containsKey(clienteId)) {
            List<List<Integer>> sesiones = historialSesiones.get(clienteId);
            for (int i = 0; i < sesiones.size(); i++) {
                List<Integer> sesion = sesiones.get(i);
                System.out.println("  Sesión " + (i + 1) + ": " + sesion.size() + " valores - " + sesion);
            }
        }
        
        // Sesión actual si existe
        if (registroLlamadas.containsKey(clienteId) && !registroLlamadas.get(clienteId).isEmpty()) {
            List<Integer> sesionActual = registroLlamadas.get(clienteId);
            int numeroSesion = (historialSesiones.containsKey(clienteId) ? historialSesiones.get(clienteId).size() : 0) + 1;
            System.out.println("  Sesión " + numeroSesion + " (actual): " + sesionActual.size() + " valores - " + sesionActual);
        }
        
        int totalValores = 0;
        if (historialSesiones.containsKey(clienteId)) {
            for (List<Integer> sesion : historialSesiones.get(clienteId)) {
                totalValores += sesion.size();
            }
        }
        if (registroLlamadas.containsKey(clienteId)) {
            totalValores += registroLlamadas.get(clienteId).size();
        }
        System.out.println("  Total de valores obtenidos: " + totalValores);
    }
}