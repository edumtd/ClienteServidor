# Sistema Cliente-Servidor con XML-RPC

Este proyecto implementa un sistema cliente-servidor utilizando XML-RPC con funcionalidades de persistencia de sesión, reconexión y seguimiento de historial.

## Características

### 1. Persistencia de Sesión
- Los datos de sesión se guardan automáticamente en un archivo `session_data.ser`
- Al reiniciar el servidor, se cargan automáticamente los datos previos
- Incluye: contador actual, último cliente que incrementó, e historial de cada cliente

### 2. Reconexión de Clientes
- Los clientes pueden desconectarse y reconectarse en cualquier momento
- Al reconectarse, el sistema muestra automáticamente el historial previo del cliente
- Implementa reintentos automáticos (3 intentos con 2 segundos de espera) en caso de fallo de conexión

### 3. Seguimiento de Historial
- Cada cliente tiene su propio historial de valores obtenidos
- El historial se muestra al reconectar un cliente existente
- El historial persiste entre reinicios del servidor

## Compilación

### Servidor
```bash
cd Contador-Servidor
ant jar
```

### Cliente
```bash
cd Contador-Cliente
ant jar
```

## Ejecución

### Iniciar el Servidor
```bash
cd Contador-Servidor
java -cp "dist/Contador-Servidor.jar:lib/*" contador.servidor.Servidor
```

El servidor se iniciará en el puerto 8080 y cargará datos previos si existen.

### Iniciar un Cliente
```bash
cd Contador-Cliente
java -cp "dist/Contador-Cliente.jar:lib/*" contador.cliente.Cliente <clienteId>
```

Ejemplo:
```bash
java -cp "dist/Contador-Cliente.jar:lib/*" contador.cliente.Cliente Client1
```

## Ejemplos de Uso

### Primera Conexión
Cuando un cliente se conecta por primera vez:
```
Cliente 'Client1' iniciado, conectando al servidor...
Primera conexión (no hay historial previo).
Éxito. Nuevo valor: 1
```

### Reconexión con Historial
Cuando un cliente que ya tiene historial se reconecta:
```
Cliente 'Client1' iniciado, conectando al servidor...

--- Reconexión detectada ---
Historial previo para 'Client1':
Valores obtenidos anteriormente: [1, 3, 5]
Total de valores previos: 3
Continuando desde donde se quedó...

Éxito. Nuevo valor: 7
```

### Manejo de Errores de Conexión
Si el servidor no está disponible:
```
Error de conexión: Failed to read server's response: Connection refused
Intentando reconectar... (Intento 1/3)
Error de conexión: Failed to read server's response: Connection refused
Intentando reconectar... (Intento 2/3)
Error de conexión: Failed to read server's response: Connection refused
No se pudo reconectar después de 3 intentos.
```

## Arquitectura

### ContadorHandler.java
- `incrementar(String clienteId)`: Incrementa el contador y registra el valor para el cliente
- `obtenerHistorial(String clienteId)`: Retorna el historial de valores del cliente
- `guardarDatos()`: Guarda el estado actual en archivo
- `cargarDatos()`: Carga el estado desde archivo
- `inicializar()`: Inicializa el handler cargando datos previos

### Cliente.java
- Conecta al servidor XML-RPC en http://127.0.0.1:8080/xmlrpc
- Implementa lógica de reintentos con timeout de 5 segundos
- Recupera y muestra historial al reconectar
- Maneja errores de conexión de forma elegante

### Servidor.java
- Inicializa el servidor XML-RPC en puerto 8080
- Carga datos persistentes al iniciar
- Registra el ContadorHandler para manejar peticiones

## Requisitos
- Java 17 o superior
- Apache XML-RPC 3.1.3
- Apache Commons Logging 1.2
- Apache WS Commons Util 1.0.2

## Notas Importantes
- El archivo `session_data.ser` se crea automáticamente en el directorio del servidor
- Los clientes deben alternar turnos (no puede incrementar dos veces seguidas el mismo cliente)
- El contador se detiene al llegar a 100 y el servidor se apaga automáticamente
- Para ejecutar en red, cambiar la URL del servidor en Cliente.java de 127.0.0.1 a la IP del servidor
