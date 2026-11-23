# ⚡ LoadFlow — Minecraft Load Tester

**LoadFlow** es una plataforma de pruebas de carga y estrés para servidores **Minecraft Java Edition** diseñada como **aplicación de escritorio nativa en Java** con interfaz gráfica moderna en modo oscuro (*FlatLaf Dark*).

A diferencia de generadores HTTP genéricos (como k6 o JMeter), LoadFlow establece conexiones reales con el protocolo de red de Minecraft, simulando jugadores que se autentican, reciben estado de juego, responden a *keep-alive*, transmiten paquetes de posición y miden la latencia exacta de ida y vuelta (RTT).

---

## 🚀 Características Principales

- **Aplicación de Escritorio 100% Autónoma:** No requiere Docker, PostgreSQL ni Redis. Todo corre en un único ejecutable local `.jar`.
- **Protocolo Minecraft 1.20.5 (Java Edition):**
  - Soporte para fases de Handshake, Login (`online-mode=false`), Configuración (1.20.2+) y Play.
  - Compresión Zlib automática compatible con el umbral (*compression-threshold*) del servidor.
- **Rendimiento Ultraligero (Netty Asíncrono):**
  - Cada bot consume solo unos pocos megabytes de memoria RAM.
  - Permite simular cientos de bots concurrentes en tu propia máquina sin saturar la CPU.
- **Dashboard en Tiempo Real:**
  - Tarjetas KPI: Bots conectados, latencia media (RTT), percentil 95 (p95), tasa de éxito y tráfico de paquetes.
  - Monitor en vivo con tabla detallada de cada bot, su estado, latencia individual y causas de desconexión.
- **Control de Seguridad y Circuit Breaker:**
  - Botón prominente de parada de emergencia **STOP ALL** para desconectar todos los bots al instante.
  - Circuit Breaker configurable: detiene la prueba automáticamente si la tasa de errores supera el umbral permitido.
- **Perfiles de Comportamiento:**
  - **AFK / Anti-inactividad:** Envío periódico de rotación para evitar expulsión del servidor por inactividad.
  - **Explorer:** Movimiento y pasos aleatorios dentro del mundo de prueba.
- **Exportación de Informes:**
  - Exporta resúmenes completos en formato **JSON** y hojas de datos en **CSV**.

---

## 📋 Requisitos del Sistema

- **Sistema Operativo:** Windows 10/11 (o Linux / macOS).
- **Java:** OpenJDK 17 o superior.

---

## 🏃 Modo de Uso Rápido

### Opción 1: Ejecutable Nativo de Windows (.exe) (Recomendada)
Entra a la carpeta:
```
dist/LoadFlow/LoadFlow.exe
```
O extrae el paquete portable:
```
dist/LoadFlow-Portable-Windows.zip
```
> **Nota:** Esta versión contiene su propio runtime integrado; funciona en cualquier PC con Windows incluso si no tiene Java instalado.

### Opción 2: Script directo
Haz doble clic en:
```
run.bat
```

### Opción 3: Desde el código fuente con Gradle
```powershell
.\gradlew.bat run
```

---

## 🛠️ Cómo Compilar el Proyecto

Para regenerar el ejecutable nativo `.exe`:
```powershell
.\gradlew.bat packageExe
```

Para regenerar el archivo `.jar` ejecutable:
```powershell
.\gradlew.bat fatJar
```
El archivo resultante se generará en:  
`build/libs/LoadFlow-1.0.0-all.jar` (aprox. 6.7 MB).

Para ejecutar los tests unitarios:
```powershell
.\gradlew.bat test
```

---

## 📖 Guía de Pruebas con tu Servidor Paper 1.20.5

1. **Configuración del Servidor Paper:**
   - Asegúrate de que `server.properties` tenga `online-mode=false`.
   - Verifica el puerto (por defecto `25565`).
2. **Configuración en LoadFlow:**
   - Ve a la pestaña **⚙️ Configuración**.
   - Coloca la IP y puerto de tu servidor Paper.
   - Define el número de bots objetivo (ej. 50 para empezar).
   - Ajusta la rampa (ej. 5 bots nuevos por segundo para evitar sobrecargar el handshake inicial).
   - Haz clic en **▶ Guardar y Lanzar Prueba de Carga**.
3. **Monitoreo:**
   - En la pestaña **📊 Dashboard** verás la latencia promedio y los bots activos en tiempo real.
   - En la pestaña **🤖 Monitor de Bots** puedes ver cada cliente individual y filtrar por estado.
   - Si necesitas cancelar de inmediato, presiona **⏹ STOP ALL**.
4. **Exportar Resultados:**
   - Haz clic en **📥 Exportar Informe** para guardar el informe JSON y CSV y comparar tus optimizaciones de Paper (*paper.yml*, *bukkit.yml*).
