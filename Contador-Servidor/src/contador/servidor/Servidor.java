package contador.servidor; // <-- Verifica tu paquete

import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.webserver.WebServer;

public class Servidor {

    public static void main(String[] args) {
        try {
            int puerto = 8080;
            System.out.println("Iniciando servidor XML-RPC en el puerto " + puerto);

            // Inicializar el handler (cargar datos persistentes)
            ContadorHandler.inicializar();

            WebServer webServer = new WebServer(puerto);
            XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();

            PropertyHandlerMapping phm = new PropertyHandlerMapping();
            
            // Le pasamos la CLASE, como quería originalmente el compilador
            phm.addHandler("contador", ContadorHandler.class);
            
            xmlRpcServer.setHandlerMapping(phm);

            // Le pasamos la referencia del servidor al handler para que pueda apagarlo
            ContadorHandler.setWebServer(webServer);

            webServer.start();
            System.out.println(">>> Servidor listo. Esperando peticiones...");

        } catch (Exception e) {
            System.err.println("Error en el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}