# AgentesIA

Aplicación de escritorio en Java que conecta agentes de IA con un modelo local servido por [Ollama](https://ollama.com/). Incluye dos agentes —generación de código y traducción de texto al español— y una interfaz Swing para seleccionarlos, enviar una entrada y ver la respuesta.

> Proyecto del **Taller de Agentes de IA** dictado por **Emilio Mayer** en la **Universidad Provincial de Córdoba (UPC)**, desarrollado como evolución del [proyecto base del taller](https://github.com/EmilioM1983/taller-aplicaciones-con-ia).

## Agentes disponibles

| Agente | Entrada | Salida |
|---|---|---|
| Generador de código | Especificaciones en lenguaje natural | Código, sin explicaciones adicionales |
| Traductor de textos al español | Texto en cualquier idioma | Traducción al castellano |

## Arquitectura

```
AgentUI (Swing)
   │ selecciona una opción y ejecuta
   ▼
AgentFactory ──► IAgent (Strategy)
                    ├── GeneratorCodeAgent
                    └── TranslatorTextAgent
                           │ construye su prompt
                           ▼
                      LLMClient ──HTTP POST──► Ollama · /api/generate
                           ▲
                Config ◄── application.properties
```

| Capa | Clase | Responsabilidad |
|---|---|---|
| Interfaz | `AgentUI` | Selección de agente, entrada, resultado y estado de progreso |
| Creación | `AgentFactory` | Instancia el agente según la opción elegida |
| Contrato | `IAgent` | Interfaz común de ejecución: `String execute(String text)` |
| Agentes | `GeneratorCodeAgent`, `TranslatorTextAgent` | Orquestan prompt + cliente |
| Prompts | `GeneratorCodePrompt`, `TranslatorTextPrompt` | Instrucciones del sistema, aisladas del agente |
| Cliente | `LLMClient` | Serialización JSON, llamada HTTP y lectura de la respuesta |
| Configuración | `Config` | Carga `application.properties` desde el classpath |
| DTOs | `RequestDTO`, `ResponseDTO` | Mapeo del contrato de la API |

## Requisitos

- **JDK 21**
- **Maven 3.9+** (necesario para compilar por consola; IntelliJ IDEA trae uno embebido)
- **Ollama** instalado y en ejecución
- Modelo descargado: `ollama pull llama3.2:3b`

## Puesta en marcha

1. Instalar Ollama y verificar que el modelo está disponible:

   ```bash
   ollama list
   ```

   Si no aparece `llama3.2:3b`, descargarlo con `ollama pull llama3.2:3b`.

2. Clonar el repositorio:

   ```bash
   git clone https://github.com/ChiaraMadera/agentes-ia.git
   cd agentesia
   ```

3. Ejecutar la aplicación. La forma más simple es abrir el proyecto en IntelliJ IDEA y correr la clase `org.example.Main`. Por consola, con Maven instalado:

   ```bash
   mvn compile
   mvn exec:java -Dexec.mainClass="org.example.Main"
   ```

4. Verificación esperada: se abre una ventana titulada *Agentes IA*, con un selector de agente, un área de entrada, otra de resultado y los botones **Ejecutar** y **Limpiar**. Al elegir *Generador de código* e ingresar una especificación, la barra de progreso indica que hay una consulta en curso y el resultado aparece en el panel inferior.

## Configuración

La configuración vive en `src/main/resources/application.properties`:

| Propiedad | Valor por defecto | Descripción |
|---|---|---|
| `ollama.url` | `http://localhost:11434/api/generate` | Endpoint de generación de Ollama |
| `ollama.model` | `llama3.2:3b` | Modelo consultado; debe estar descargado localmente |

Para usar otro modelo, descargarlo con `ollama pull` y actualizar `ollama.model`.

## Estructura del proyecto

```
src/main/java/org/example/
├── Main.java                 # Punto de entrada (lanza la UI)
├── agents/                   # IAgent y agentes concretos por dominio
│   ├── desarrollo/
│   └── texto/
├── client/LLMClient.java     # Cliente HTTP del LLM
├── config/Config.java        # Lectura de application.properties
├── dtos/                     # RequestDTO y ResponseDTO
├── factory/AgentFactory.java # Creación de agentes por opción
├── prompts/                  # Instrucciones por agente
└── ui/                       # AgentUI (Swing) y Console (alternativa)
```

## Decisiones de diseño

- **Strategy + Factory**: `IAgent` define el contrato y `AgentFactory` centraliza la creación. Agregar un agente nuevo implica una clase de agente, su prompt y una línea en la fábrica; el resto del sistema no se toca.
- **Prompts separados de los agentes**: las instrucciones viven en `prompts/` para poder ajustarlas sin modificar la lógica de ejecución.
- **Cliente aislado y configuración externa**: `LLMClient` es el único punto que conoce HTTP y JSON, y el modelo o endpoint se cambian desde `application.properties` sin recompilar.
- **UI no bloqueante**: la llamada al modelo corre en un `SwingWorker`, así la ventana responde mientras se espera la respuesta.

## Limitaciones y próximos pasos

- No incluye tests automatizados.
- Los errores del cliente se devuelven como texto (`"Error: ..."`) en lugar de propagarse como excepción; conviene modelarlos con una excepción propia y mostrarlos en un diálogo.
- Las respuestas se solicitan sin streaming (`stream: false`), por lo que el resultado aparece completo al final.
- Las opciones de agente están fijas en la UI y el mapeo es posicional (índice + 1 en la fábrica); un enum o registro explícito sería más robusto.
- `Console` quedó como alternativa por línea de comandos, desactivada en `Main`.

## Sobre el taller

Este proyecto forma parte del **Taller de Agentes de IA** dictado por **Emilio Mayer** en la **Universidad Provincial de Córdoba (UPC)**. El [repositorio base del taller](https://github.com/EmilioM1983/taller-aplicaciones-con-ia) contiene el cliente de consola original; esta versión lo extiende con una interfaz gráfica, una capa de agentes intercambiables y configuración externalizada.

## Autor

**Chiara Madera** — proyecto de taller.


