#!/bin/bash

# Script para generar toda la evidencia de rendimiento
# Uso: ./scripts/generate_evidence.sh

echo "════════════════════════════════════════════════════════"
echo "📊 Generando Evidencia de Rendimiento"
echo "════════════════════════════════════════════════════════"
echo ""

# Crear estructura de carpetas
echo "📁 Creando estructura de carpetas..."
mkdir -p docs/performance
mkdir -p releases
echo "✅ Carpetas creadas"
echo ""

# Verificar que el dispositivo esté conectado
echo "📱 Verificando dispositivo..."
DEVICE=$(adb devices | grep -w "device" | wc -l)
if [ $DEVICE -eq 0 ]; then
    echo "❌ Error: No hay dispositivo conectado"
    echo "   Conecta un dispositivo o inicia un emulador"
    exit 1
fi
echo "✅ Dispositivo detectado"
echo ""

# Compilar APK debug
echo "🔨 Compilando APK debug..."
./gradlew assembleDebug
if [ $? -ne 0 ]; then
    echo "❌ Error al compilar APK"
    exit 1
fi
echo "✅ APK compilado exitosamente"
echo ""

# Copiar APK a releases
echo "📦 Copiando APK a releases/..."
cp app/build/outputs/apk/debug/app-debug.apk releases/pinterest-feed-v1.0-debug.apk
echo "✅ APK copiado a releases/pinterest-feed-v1.0-debug.apk"
echo ""

# Instalar app
echo "📲 Instalando app en dispositivo..."
adb install -r releases/pinterest-feed-v1.0-debug.apk
echo "✅ App instalada"
echo ""

# Iniciar app
echo "🚀 Iniciando app..."
adb shell am start -n com.tuusuario.pinterestfeed.debug/.MainActivity
sleep 3
echo "✅ App iniciada"
echo ""

# Capturar logs de JankStats
echo "📊 Capturando logs de rendimiento..."
echo "⏱️  Haz scroll por 30 segundos en la app..."
echo "   Presiona Ctrl+C cuando termines"
echo ""

# Crear archivo con timestamp
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
LOGFILE="docs/performance/jankstats_${TIMESTAMP}.txt"

echo "════════════════════════════════════════════════════════" > $LOGFILE
echo "JANKSTATS PERFORMANCE REPORT" >> $LOGFILE
echo "Fecha: $(date)" >> $LOGFILE
echo "════════════════════════════════════════════════════════" >> $LOGFILE
echo "" >> $LOGFILE

# Capturar logs (30 segundos)
timeout 30 adb logcat -s PerformanceMonitor:* Coil:D >> $LOGFILE 2>&1

echo "" >> $LOGFILE
echo "════════════════════════════════════════════════════════" >> $LOGFILE
echo "FIN DEL REPORTE" >> $LOGFILE
echo "════════════════════════════════════════════════════════" >> $LOGFILE

echo "✅ Logs capturados en $LOGFILE"
echo ""

# Capturar logs de Coil para cache
echo "📸 Capturando logs de cache de Coil..."
CACHEFILE="docs/performance/coil_cache_${TIMESTAMP}.txt"

echo "════════════════════════════════════════════════════════" > $CACHEFILE
echo "COIL CACHE REPORT" >> $CACHEFILE
echo "Fecha: $(date)" >> $CACHEFILE
echo "════════════════════════════════════════════════════════" >> $CACHEFILE
echo "" >> $CACHEFILE
echo "📊 Primera Pasada (Cache Cold)" >> $CACHEFILE
echo "----------------------------------------" >> $CACHEFILE

# Limpiar caché de la app
adb shell pm clear com.tuusuario.pinterestfeed.debug
sleep 2

# Reiniciar y capturar primera pasada
adb shell am start -n com.tuusuario.pinterestfeed.debug/.MainActivity
sleep 5
timeout 10 adb logcat -s Coil:D | grep -E "(cache|network)" >> $CACHEFILE 2>&1

echo "" >> $CACHEFILE
echo "📊 Segunda Pasada (Cache Warm)" >> $CACHEFILE
echo "----------------------------------------" >> $CACHEFILE

# Reiniciar sin limpiar caché
adb shell am force-stop com.tuusuario.pinterestfeed.debug
sleep 1
adb shell am start -n com.tuusuario.pinterestfeed.debug/.MainActivity
sleep 5
timeout 10 adb logcat -s Coil:D | grep -E "(cache|network)" >> $CACHEFILE 2>&1

echo "" >> $CACHEFILE
echo "════════════════════════════════════════════════════════" >> $CACHEFILE
echo "FIN DEL REPORTE" >> $CACHEFILE
echo "════════════════════════════════════════════════════════" >> $CACHEFILE

echo "✅ Logs de cache capturados en $CACHEFILE"
echo ""

# Instrucciones para Profiler
echo "════════════════════════════════════════════════════════"
echo "📊 SIGUIENTE PASO: Capturas del Profiler"
echo "════════════════════════════════════════════════════════"
echo ""
echo "1. Abre Android Studio"
echo "2. Ve a View > Tool Windows > Profiler"
echo "3. Selecciona el proceso de la app"
echo "4. Captura CPU y Memory por 30 segundos de scroll"
echo "5. Exporta las capturas a:"
echo "   - docs/performance/profiler_cpu.png"
echo "   - docs/performance/profiler_memory.png"
echo ""
echo "════════════════════════════════════════════════════════"
echo "✅ Evidencia generada exitosamente"
echo "════════════════════════════════════════════════════════"
echo ""
echo "Archivos generados:"
echo "  📄 $LOGFILE"
echo "  📄 $CACHEFILE"
echo "  📦 releases/pinterest-feed-v1.0-debug.apk"
echo ""
echo "Pendientes (manual):"
echo "  📸 docs/performance/profiler_cpu.png"
echo "  📸 docs/performance/profiler_memory.png"
echo "  🎥 Video demo de la app"
echo ""