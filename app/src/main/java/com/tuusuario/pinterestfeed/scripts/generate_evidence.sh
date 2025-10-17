#!/bin/bash
set -e

echo "════════════════════════════════════════════════════════"
echo "📊 Generando Evidencia de Rendimiento"
echo "════════════════════════════════════════════════════════"
echo

# --- Crear estructura de carpetas ---
echo "📁 Creando estructura de carpetas..."
mkdir -p docs/performance
mkdir -p releases
echo "✅ Carpetas creadas"
echo

# --- Verificar dispositivo conectado ---
echo "📱 Verificando dispositivo..."
adb start-server >/dev/null 2>&1
adb devices | grep -q "device$" && echo "✅ Dispositivo detectado" || { echo "❌ No se detectó dispositivo o emulador"; exit 1; }
echo

# --- Compilar APK ---
echo "🔨 Compilando APK debug..."
./gradlew assembleDebug || { echo "❌ Error al compilar APK"; exit 1; }
echo "✅ APK compilado exitosamente"
echo

# --- Copiar APK a releases ---
APK_SOURCE="app/build/outputs/apk/debug/app-debug.apk"
APK_TARGET="releases/pinterest-feed-v1.0-debug.apk"
cp "$APK_SOURCE" "$APK_TARGET"
echo "✅ APK copiado a $APK_TARGET"
echo

# --- Instalar APK en el dispositivo ---
echo "📲 Instalando app en dispositivo..."
adb install -r "$APK_TARGET" >/dev/null && echo "✅ App instalada" || echo "⚠️ Ya estaba instalada"
echo

# --- Lanzar app automáticamente ---
echo "🚀 Iniciando app..."
LAUNCHER="$(adb shell cmd package resolve-activity --brief com.tuusuario.pinterestfeed.debug 2>/dev/null | tail -n 1)"
if [[ -n "$LAUNCHER" && "$LAUNCHER" == */* ]]; then
  adb shell am start -n "$LAUNCHER"
else
  adb shell monkey -p com.tuusuario.pinterestfeed.debug -c android.intent.category.LAUNCHER 1
fi
echo "✅ App iniciada"
echo

# --- Capturar logs de rendimiento ---
echo "📊 Capturando logs de rendimiento..."
TS="$(date +%Y%m%d_%H%M%S)"
OUT="docs/performance/jankstats_${TS}.txt"

adb logcat -c
echo "⏱️  Haz scroll por 30 segundos en la app..."
echo "   Presiona Ctrl+C cuando termines"
adb logcat -v time | tee "$OUT"
echo
echo "✅ Logs capturados en $OUT"
echo

# --- (Opcional) Limpieza o pasos extra ---
# Aquí puedes agregar tareas como copiar screenshots, limpiar cache, etc.
