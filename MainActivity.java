package com.anivault.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {

    private WebView webView;
    private ProgressBar progressBar;
    private View fullscreenView;
    private FrameLayout container;

    // Tu sitio — SOLO este URL puede cargar
    private static final String SITE_URL = "https://momkjhhhh.blogspot.com/?m=1";
    private static final String SITE_HOST = "momkjhhhh.blogspot.com";

    // Dominios de video permitidos (los embeds de tus capítulos)
    private static final List<String> VIDEO_HOSTS = Arrays.asList(
        "streamwish.to", "streamwish.com", "alions.pro", "embedwish.com",
        "filemoon.sx", "filemoon.in", "moonplayer.to",
        "voe.sx", "voe.la",
        "dood.so", "dood.la", "doodstream.com", "ds2play.com",
        "streamtape.com", "streamtape.net", "stp.network",
        "mp4upload.com",
        "videovard.sx", "vidhide.com", "vidhideplus.com",
        "uqload.com", "uqload.co",
        "sendvid.com",
        "viewsb.com",
        "hqq.to", "hqq.tv",
        "ok.ru", "odnoklassniki.ru",
        "sibnet.ru",
        "mixdrop.co", "mixdrop.to",
        // CDNs de video (para que carguen los m3u8 y mp4)
        "akamaized.net", "cloudfront.net", "fastly.net",
        "jwplatform.com", "jwpcdn.com",
        // Google fonts (para el HTML)
        "fonts.googleapis.com", "fonts.gstatic.com",
        // TMDb imágenes
        "image.tmdb.org",
        // Blogspot recursos
        "blogger.com", "blogspot.com", "bp.blogspot.com",
        "www.blogger.com", "www.blogspot.com",
        "cdnjs.cloudflare.com", "cdn.jsdelivr.net"
    );

    // Dominios BLOQUEADOS (ads, trackers, popups)
    private static final List<String> BLOCKED_HOSTS = Arrays.asList(
        "doubleclick.net", "googlesyndication.com", "googleadservices.com",
        "google-analytics.com", "analytics.google.com", "adservice.google.com",
        "pagead2.googlesyndication.com",
        "facebook.net", "connect.facebook.net",
        "adnxs.com", "rubiconproject.com", "openx.net", "pubmatic.com",
        "criteo.com", "taboola.com", "outbrain.com", "mgid.com",
        "popads.net", "popcash.net", "hilltopads.net", "propellerads.com",
        "trafficjunky.com", "exoclick.com", "juicyads.com", "trafficstars.com",
        "plugrush.com", "hotjar.com", "clarity.ms",
        "amazon-adsystem.com", "advertising.com"
    );

    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Pantalla completa sin barra de título
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        // Layout principal
        container = new FrameLayout(this);
        setContentView(container);

        // Progress bar
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, 8));
        progressBar.setMax(100);
        container.addView(progressBar);

        // WebView
        webView = new WebView(this);
        webView.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));
        container.addView(webView);

        configurarWebView();
        webView.loadUrl(SITE_URL);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configurarWebView() {
        WebSettings settings = webView.getSettings();

        // Habilitar JavaScript (necesario para tu sitio)
        settings.setJavaScriptEnabled(true);

        // Configuración de caché y almacenamiento
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // Media
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);

        // Zoom
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        // User agent normal (no revelar que es WebView)
        settings.setUserAgentString(
            "Mozilla/5.0 (Linux; Android 13; Pixel 7) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/120.0.0.0 Mobile Safari/537.36"
        );

        // WebViewClient — controla navegación
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                String host = request.getUrl().getHost();
                if (host == null) host = "";
                host = host.toLowerCase().replace("www.", "");

                // ── BLOQUEO PRINCIPAL ──────────────────────────────
                // Si el host no es tu sitio ni un servidor de video = BLOQUEADO
                // Esto bloquea TODOS los popups y redirecciones de ads

                // Permitir tu sitio
                if (host.contains(SITE_HOST) || host.contains("blogspot.com") || host.contains("blogger.com")) {
                    return false; // Cargar normalmente
                }

                // Verificar si es un servidor de video permitido
                for (String videoHost : VIDEO_HOSTS) {
                    if (host.contains(videoHost) || videoHost.contains(host)) {
                        return false; // Permitir cargar el embed
                    }
                }

                // Bloquear ads conocidos explícitamente
                for (String blocked : BLOCKED_HOSTS) {
                    if (host.contains(blocked)) {
                        return true; // BLOQUEADO
                    }
                }

                // TODO lo demás que no esté en la lista = BLOQUEADO
                // Esto incluye links de ads, popups, redirecciones
                android.util.Log.d("AniVault", "BLOQUEADO: " + url);
                return true;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String host = request.getUrl().getHost();
                if (host == null) return null;
                host = host.toLowerCase();

                // Bloquear recursos de dominios de ads (imágenes, scripts, etc.)
                for (String blocked : BLOCKED_HOSTS) {
                    if (host.contains(blocked)) {
                        // Devolver respuesta vacía
                        return new WebResourceResponse("text/plain", "utf-8",
                            new java.io.ByteArrayInputStream("".getBytes()));
                    }
                }
                return null;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(0);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);

                // Inyectar JS bloqueador adicional después de cargar
                view.evaluateJavascript(
                    "(function(){" +
                    "  window.open = function(){ return null; };" +
                    "  document.querySelectorAll('a[target=\"_blank\"]').forEach(function(a){" +
                    "    a.setAttribute('target','_self');" +
                    "  });" +
                    "})()", null
                );
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed(); // Permitir certificados para los servidores de video
            }
        });

        // WebChromeClient — controla popups, fullscreen, permisos
        webView.setWebChromeClient(new WebChromeClient() {

            // BLOQUEAR POPUPS — onCreateWindow nunca crea ventanas nuevas
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog,
                                          boolean isUserGesture, android.os.Message resultMsg) {
                // No crear ninguna ventana nueva = popups bloqueados
                android.util.Log.d("AniVault", "Popup bloqueado");
                return false;
            }

            // Pantalla completa para el video
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (fullscreenView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                fullscreenView = view;
                container.addView(fullscreenView, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
                webView.setVisibility(View.GONE);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }

            @Override
            public void onHideCustomView() {
                if (fullscreenView == null) return;
                container.removeView(fullscreenView);
                fullscreenView = null;
                webView.setVisibility(View.VISIBLE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }

            // Permisos para reproducción de video
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }

            // Progress bar
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                if (newProgress == 100) progressBar.setVisibility(View.GONE);
            }
        });
    }

    // Botón atrás navega en el historial del WebView
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.destroy();
    }
}
