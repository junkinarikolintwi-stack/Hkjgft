# AniVault Android App

App que carga tu sitio con bloqueo TOTAL de popups y ads.
Funciona igual que tu código de Android Studio — `shouldOverrideUrlLoading` 
bloquea cualquier URL que no sea tu sitio o los servidores de video.

---

## ¿Cómo obtener el APK? (GRATIS, sin instalar nada)

### Paso 1 — Crear cuenta en GitHub
1. Ve a https://github.com
2. Crea una cuenta gratis

### Paso 2 — Subir este proyecto
1. En GitHub, clic en **"New repository"**
2. Nombre: `AniVaultApp`
3. Privado o público (da igual)
4. Clic **"Create repository"**
5. Sube todos los archivos de esta carpeta

### Paso 3 — El APK se compila solo
1. Ve a la pestaña **"Actions"** en tu repositorio
2. Verás el workflow **"Build AniVault APK"** corriendo
3. Espera ~3-5 minutos
4. Clic en el workflow completado ✅
5. Abajo en **"Artifacts"** → descarga **"AniVault-APK"**
6. Descomprime el ZIP → tienes el **app-debug.apk**

### Paso 4 — Instalar en tu celular
1. En tu Android: **Ajustes → Seguridad → Fuentes desconocidas** (activar)
2. Pasa el APK a tu celular (por WhatsApp, Drive, cable USB)
3. Ábrelo e instala

---

## ¿Cómo funciona el bloqueo?

El método `shouldOverrideUrlLoading` revisa CADA URL antes de cargarla:

- ✅ `momkjhhhh.blogspot.com` → permitido (tu sitio)
- ✅ `streamwish.to`, `voe.sx`, `dood.so`, etc. → permitidos (tus videos)
- ❌ Todo lo demás → BLOQUEADO, no se carga, no se abre

Y `onCreateWindow` retorna `false` → NINGÚN popup puede abrirse, jamás.

---

## Cambiar la URL de tu sitio

Edita `MainActivity.java` línea 32:
```java
private static final String SITE_URL = "https://momkjhhhh.blogspot.com/?m=1";
```

---

## Agregar más servidores de video

Edita `MainActivity.java` y agrega a la lista `VIDEO_HOSTS`:
```java
"nuevoservidor.com",
```
