import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { ApiConfigService } from '../../core/api-config.service';
import { SessionService } from '../../core/session.service';
import { homePorRol, estadoStyle, etiquetaEstado } from '../../core/constants';
import * as M from '../../core/models';
import { IconComponent } from '../../shared/icon.component';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, RouterLink, IconComponent],
  template: `
    <div class="landing-wrap">
      <!-- ================= HEADER / TOPBAR ================= -->
      <header class="landing-header">
        <div class="header-inner">
          <div class="brand" routerLink="/">
            <div class="logo-box">C</div>
            <div class="brand-text">
              <span class="brand-title">ciudad<b>activa</b></span>
              <span class="brand-sub">Comunidad Urbana</span>
            </div>
          </div>

          <nav class="header-nav">
            <a href="#descarga" class="nav-link">
              <app-icon name="smartphone" [size]="16" /> App Móvil
            </a>
            <a href="#metricas" class="nav-link">
              <app-icon name="chart" [size]="16" /> Métricas
            </a>
            <a href="#muro" class="nav-link">
              <app-icon name="map" [size]="16" /> Incidencias
            </a>
            <a [href]="api.urlSwaggerDocs()" target="_blank" rel="noopener" class="nav-link">
              <app-icon name="server" [size]="16" /> API Docs
            </a>
          </nav>

          <div class="header-actions">
            @if (session.isLoggedIn()) {
              <div class="user-pill" [routerLink]="homePorRol(session.rol())">
                <div class="avatar-sm">{{ session.user()?.nombre?.charAt(0) || 'U' }}</div>
                <div class="user-info">
                  <span class="user-name">{{ session.user()?.nombre }}</span>
                  <span class="user-role">{{ session.rol() }}</span>
                </div>
                <app-icon name="chevron" [size]="14" />
              </div>
            } @else {
              <a routerLink="/login" class="btn ghost btn-sm">Iniciar Sesión</a>
              <a routerLink="/register" class="btn copper btn-sm">Registrarse</a>
            }
          </div>
        </div>
      </header>

      <!-- ================= HERO SECTION ================= -->
      <section class="hero-section">
        <div class="hero-bg-glow"></div>
        <div class="hero-content">
          <div class="hero-badge">
            <span class="pulse-dot"></span>
            <span>Release Oficial v0.2.2 Disponible para Android</span>
          </div>

          <h1 class="hero-title">
            Transforma tu ciudad,<br>
            <span class="gradient-text">un reporte a la vez</span>
          </h1>

          <p class="hero-desc">
            Plataforma integral para el reporte ciudadano, validación técnica, asignación operativa y
            seguimiento en tiempo real de incidencias urbanas. Desarrollada bajo metodología <b>RUP</b> con
            georreferenciación GPS, mapas satelitales y soporte de notificaciones por correo.
          </p>

          <!-- BOTONES DE ACCIÓN HERO -->
          <div class="hero-cta-group" id="descarga">
            <a [href]="api.urlDescargarApk()" class="btn-hero-primary" download="CiudadActiva-v0.2.2.apk">
              <div class="btn-hero-icon">
                <app-icon name="download" [size]="24" />
              </div>
              <div class="btn-hero-text">
                <span class="btn-label">Descargar App Android</span>
                <span class="btn-meta">v0.2.2 Oficial · APK 12.3 MB · Sin costo</span>
              </div>
            </a>

            @if (session.isLoggedIn()) {
              <a [routerLink]="homePorRol(session.rol())" class="btn-hero-secondary">
                <app-icon name="dashboard" [size]="18" />
                <span>Ir a mi Panel de Control</span>
              </a>
            } @else {
              <a routerLink="/login" class="btn-hero-secondary">
                <app-icon name="user" [size]="18" />
                <span>Acceder a la Web</span>
              </a>
            }
          </div>

          <!-- STATUS LIVE CLOUD -->
          <div class="cloud-status-chip">
            <app-icon name="server" [size]="14" />
            <span>Conectado a Cloud Backend: <code>{{ cfg.baseUrl() }}</code></span>
            <span class="status-live">● EN LÍNEA</span>
          </div>
        </div>

        <!-- HERO MOCKUP / CARD PREVIEW -->
        <div class="hero-preview">
          <div class="phone-mockup">
            <div class="phone-notch"></div>
            <div class="phone-screen">
              <div class="mockup-top">
                <div class="mockup-brand">
                  <span class="m-c">C</span>
                  <b>ciudad<span>activa</span></b>
                </div>
                <div class="mockup-badge">v0.2.2</div>
              </div>

              <div class="mockup-card hero-gradient">
                <small>INCIDENCIAS URBANAS</small>
                <h3>Reporta en 3 pasos con fotos y mapa GPS</h3>
                <div class="mockup-tags">
                  <span>📍 GPS Exacto</span>
                  <span>📷 Evidencia</span>
                  <span>🔔 OTP Verificado</span>
                </div>
              </div>

              <div class="mockup-list">
                <div class="mockup-item">
                  <div class="dot amber"></div>
                  <div>
                    <strong>Bache en calzada principal</strong>
                    <small>Calle 15 con Cra 8 · PENDIENTE</small>
                  </div>
                </div>
                <div class="mockup-item">
                  <div class="dot blue"></div>
                  <div>
                    <strong>Luminaria apagada parque</strong>
                    <small>Barrio El Centro · EN PROCESO</small>
                  </div>
                </div>
                <div class="mockup-item">
                  <div class="dot green"></div>
                  <div>
                    <strong>Escombros retirados</strong>
                    <small>Av. Las Palmas · ATENDIDO</small>
                  </div>
                </div>
              </div>

              <div class="mockup-footer">
                <a [href]="api.urlDescargarApk()" class="mockup-download-btn">
                  <app-icon name="download" [size]="15" /> Instalar en tu Celular
                </a>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- ================= MÉTRICAS EN VIVO DE LA BD ================= -->
      <section class="metrics-section" id="metricas">
        <div class="section-container">
          <div class="section-head center">
            <span class="kicker">DATOS DE LA BASE DE DATOS EN TIEMPO REAL</span>
            <h2>Impacto y Gestión en Cifras</h2>
            <p class="muted">Información sincronizada directamente desde PostgreSQL / Supabase</p>
          </div>

          <div class="metrics-grid">
            <div class="metric-card">
              <div class="metric-icon teal">
                <app-icon name="file" [size]="24" />
              </div>
              <div class="metric-val">{{ stats()?.totalReportes ?? 0 }}</div>
              <div class="metric-name">Reportes Totales</div>
              <div class="metric-sub">Registrados por la ciudadanía</div>
            </div>

            <div class="metric-card">
              <div class="metric-icon green">
                <app-icon name="check-circle" [size]="24" />
              </div>
              <div class="metric-val">{{ stats()?.reportesAtendidosOCerrados ?? 0 }}</div>
              <div class="metric-name">Casos Resueltos</div>
              <div class="metric-sub">Con evidencia fotográfica de solución</div>
            </div>

            <div class="metric-card">
              <div class="metric-icon blue">
                <app-icon name="clock" [size]="24" />
              </div>
              <div class="metric-val">{{ stats()?.reportesPorEstado?.['EN_PROCESO'] ?? 0 }}</div>
              <div class="metric-name">En Proceso Activo</div>
              <div class="metric-sub">Atendidos por cuadrillas técnicas</div>
            </div>

            <div class="metric-card">
              <div class="metric-icon copper">
                <app-icon name="layers" [size]="24" />
              </div>
              <div class="metric-val">{{ categorias().length }}</div>
              <div class="metric-name">Categorías Urbanas</div>
              <div class="metric-sub">Malla vial, luminarias, aseo y más</div>
            </div>
          </div>
        </div>
      </section>

      <!-- ================= ROLES / MÓDULOS RUP ================= -->
      <section class="roles-section">
        <div class="section-container">
          <div class="section-head center">
            <span class="kicker">ARQUITECTURA RUP (RATIONAL UNIFIED PROCESS)</span>
            <h2>Una Solución Integral para Cada Rol</h2>
            <p class="muted">Diseñada para una interacción armónica entre la ciudadanía y la administración pública</p>
          </div>

          <div class="roles-grid">
            <div class="role-card">
              <div class="role-icon">👤</div>
              <h3>Ciudadano</h3>
              <p>Genera reportes georreferenciados con evidencia fotográfica. Realiza seguimiento con línea de tiempo y comenta en tiempo real.</p>
              <ul class="role-features">
                <li><app-icon name="check" [size]="14" /> Asistente en 3 pasos</li>
                <li><app-icon name="check" [size]="14" /> Mapa interactivo con semáforos</li>
                <li><app-icon name="check" [size]="14" /> Notificaciones OTP seguras</li>
              </ul>
            </div>

            <div class="role-card">
              <div class="role-icon">📋</div>
              <h3>Gestor Municipal</h3>
              <p>Módulo de Triage para inspeccionar y validar incidencias reportadas. Asigna prioridades y envía órdenes de trabajo a operadores.</p>
              <ul class="role-features">
                <li><app-icon name="check" [size]="14" /> Bandeja de validación técnica</li>
                <li><app-icon name="check" [size]="14" /> Asignación de cuadrillas</li>
                <li><app-icon name="check" [size]="14" /> Dashboard de estadísticas</li>
              </ul>
            </div>

            <div class="role-card">
              <div class="role-icon">🛠️</div>
              <h3>Operador de Campo</h3>
              <p>Recibe tareas asignadas en terreno. Registra avances de obra y sube fotografía de la solución para el cierre formal del reporte.</p>
              <ul class="role-features">
                <li><app-icon name="check" [size]="14" /> Bitácora de avances</li>
                <li><app-icon name="check" [size]="14" /> Subida de foto de solución</li>
                <li><app-icon name="check" [size]="14" /> Cierre verificado de tareas</li>
              </ul>
            </div>

            <div class="role-card">
              <div class="role-icon">🛡️</div>
              <h3>Administrador</h3>
              <p>Supervisa la plataforma, gestiona usuarios y roles institucionales, configura el catálogo de categorías y consulta la auditoría inmutable.</p>
              <ul class="role-features">
                <li><app-icon name="check" [size]="14" /> Control total de usuarios</li>
                <li><app-icon name="check" [size]="14" /> Registro de auditoría</li>
                <li><app-icon name="check" [size]="14" /> Catálogo de categorías</li>
              </ul>
            </div>
          </div>
        </div>
      </section>

      <!-- ================= MURO DE REPORTES EN VIVO ================= -->
      <section class="feed-section" id="muro">
        <div class="section-container">
          <div class="section-head between row">
            <div>
              <span class="kicker">COMUNIDAD ACTIVA</span>
              <h2>Últimas Incidencias Reportadas</h2>
              <p class="muted">Reportes públicos registrados directamente por los ciudadanos</p>
            </div>
            <a routerLink="/login" class="btn ghost btn-sm">Ver Mapa Completo</a>
          </div>

          @if (cargandoReportes()) {
            <div class="skeleton-list mt">
              <div class="skeleton"></div>
              <div class="skeleton"></div>
              <div class="skeleton"></div>
            </div>
          } @else if (reportes().length === 0) {
            <div class="empty card mt">
              <div class="empty-emoji">🏙️</div>
              <strong>No hay reportes recientes registrados aún</strong>
              <p class="muted small">Sé el primero en reportar una incidencia comunitaria.</p>
              <a routerLink="/register" class="btn copper btn-sm mt">Crear Primer Reporte</a>
            </div>
          } @else {
            <div class="grid grid-auto mt">
              @for (r of reportes().slice(0, 6); track r.id) {
                <div class="card reporte-item clickable" routerLink="/login">
                  @if (r.imagenUrl) {
                    <img [src]="r.imagenUrl" [alt]="r.titulo" class="card-thumb" />
                  } @else {
                    <div class="card-thumb-placeholder">
                      <app-icon name="camera" [size]="28" />
                      <span>Sin evidencia fotográfica</span>
                    </div>
                  }
                  <div class="card-body">
                    <div class="row between gap-sm mb">
                      <span class="chip chip-sm" [style.background]="estadoStyle(r.estado).bg" [style.color]="estadoStyle(r.estado).text" [style.borderColor]="estadoStyle(r.estado).border">
                        {{ etiquetaEstado(r.estado) }}
                      </span>
                      <small class="muted">{{ r.categoriaNombre || 'General' }}</small>
                    </div>
                    <h4>{{ r.titulo }}</h4>
                    <p class="muted small line-clamp">{{ r.descripcion }}</p>
                    <div class="card-meta">
                      <small><app-icon name="pin" [size]="12" /> {{ r.direccion || 'Ubicación registrada' }}</small>
                    </div>
                  </div>
                </div>
              }
            </div>
          }
        </div>
      </section>

      <!-- ================= BANNER DESCARGA DETALLADA ================= -->
      <section class="download-banner">
        <div class="section-container">
          <div class="download-card">
            <div class="download-info">
              <span class="kicker text-copper">INSTALACIÓN FÁCIL Y SEGURA</span>
              <h2>Instala CiudadActiva en tu Celular Android</h2>
              <p class="muted">Lleva el poder del reporte ciudadano en tu bolsillo. Compatible con dispositivos Android 7.0 (Nougat) en adelante.</p>

              <div class="install-steps">
                <div class="i-step">
                  <span class="step-num">1</span>
                  <div>
                    <strong>Descarga el archivo APK</strong>
                    <small>Presiona el botón de descarga directa (12.3 MB).</small>
                  </div>
                </div>
                <div class="i-step">
                  <span class="step-num">2</span>
                  <div>
                    <strong>Acepta la instalación</strong>
                    <small>Si tu dispositivo lo solicita, activa "Permitir desde esta fuente".</small>
                  </div>
                </div>
                <div class="i-step">
                  <span class="step-num">3</span>
                  <div>
                    <strong>Inicia sesión o regístrate</strong>
                    <small>Valida tu cuenta con el código OTP enviado a tu correo.</small>
                  </div>
                </div>
              </div>

              <div class="row gap mt-lg">
                <a [href]="api.urlDescargarApk()" class="btn primary" download="CiudadActiva-v0.2.2.apk">
                  <app-icon name="download" [size]="18" /> Descargar APK v0.2.2 (12.3 MB)
                </a>
                <a href="https://github.com/Juanpbb6811/CiudadActiva" target="_blank" rel="noopener" class="btn ghost">
                  Ver Código en GitHub
                </a>
              </div>
            </div>

            <div class="download-qr-box">
              <div class="qr-frame">
                <div class="qr-header">
                  <div class="logo-box-sm">C</div>
                  <span>Escanea o Descarga</span>
                </div>
                <!-- Visual QR placeholder -->
                <div class="qr-visual">
                  <app-icon name="smartphone" [size]="64" />
                  <span class="qr-text">Android Release Oficial</span>
                  <span class="qr-sub">v0.2.2 · Firmada</span>
                </div>
                <a [href]="api.urlDescargarApk()" class="btn copper block btn-sm mt-sm">Descargar Ahora</a>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- ================= FOOTER ================= -->
      <footer class="landing-footer">
        <div class="section-container">
          <div class="footer-top row between wrap gap">
            <div class="footer-brand">
              <div class="brand">
                <div class="logo-box">C</div>
                <div class="brand-text">
                  <span class="brand-title">ciudad<b>activa</b></span>
                  <span class="brand-sub">Sistema de Incidencias Urbanas</span>
                </div>
              </div>
              <p class="muted small mt-sm" style="max-width:340px">
                Plataforma comunitaria desarrollada bajo la metodología RUP. Facultad de Ingeniería de Sistemas,
                Universidad de Cundinamarca.
              </p>
            </div>

            <div class="footer-links-group row gap-lg">
              <div>
                <strong>Plataforma</strong>
                <ul class="clean-list mt-sm small muted">
                  <li><a routerLink="/login">Iniciar Sesión</a></li>
                  <li><a routerLink="/register">Registro Ciudadano</a></li>
                  <li><a [href]="api.urlDescargarApk()">Descarga APK Android</a></li>
                </ul>
              </div>

              <div>
                <strong>Backend & Cloud</strong>
                <ul class="clean-list mt-sm small muted">
                  <li><a [href]="api.urlSwaggerDocs()" target="_blank">Swagger API UI</a></li>
                  <li><a [href]="cfg.baseUrl() + '/api/health'" target="_blank">Health Check</a></li>
                  <li><a href="https://dashboard.render.com" target="_blank">Render Cloud</a></li>
                </ul>
              </div>
            </div>
          </div>

          <div class="footer-bottom row between wrap gap mt-lg">
            <small class="muted">© 2026 CiudadActiva. Todos los derechos reservados. Universidad de Cundinamarca.</small>
            <small class="muted">Versión 0.2.2 · Spring Boot 3.4 & Angular 19</small>
          </div>
        </div>
      </footer>
    </div>
  `,
  styles: [`
    .landing-wrap {
      background: var(--bg);
      color: var(--text);
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      overflow-x: hidden;
    }

    /* Header */
    .landing-header {
      position: sticky;
      top: 0;
      z-index: 50;
      background: rgba(255, 255, 255, 0.92);
      backdrop-filter: blur(12px);
      border-bottom: 1px solid var(--border);
      padding: 12px 24px;
    }
    .header-inner {
      max-width: 1180px;
      margin: 0 auto;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
    }
    .brand {
      display: flex;
      align-items: center;
      gap: 10px;
      cursor: pointer;
      text-decoration: none;
    }
    .logo-box {
      width: 38px;
      height: 38px;
      border-radius: 12px;
      background: linear-gradient(135deg, var(--teal-light), var(--copper));
      color: #fff;
      font-size: 20px;
      font-weight: 800;
      display: grid;
      place-items: center;
      box-shadow: 0 4px 12px rgba(17, 50, 51, 0.15);
    }
    .logo-box-sm {
      width: 26px;
      height: 26px;
      border-radius: 8px;
      background: linear-gradient(135deg, var(--teal-light), var(--copper));
      color: #fff;
      font-size: 14px;
      font-weight: 800;
      display: grid;
      place-items: center;
    }
    .brand-text { display: flex; flex-direction: column; line-height: 1.15; }
    .brand-title { font-size: 18px; font-weight: 700; color: var(--teal-dark); }
    .brand-title b { color: var(--copper); }
    .brand-sub { font-size: 10.5px; color: var(--muted); letter-spacing: 0.05em; text-transform: uppercase; }

    .header-nav { display: flex; gap: 20px; align-items: center; }
    .nav-link {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      color: var(--teal-dark);
      font-weight: 600;
      font-size: 13.5px;
      padding: 6px 10px;
      border-radius: 8px;
      transition: background 0.15s, color 0.15s;
    }
    .nav-link:hover { background: #eef4f2; color: var(--teal); }

    .header-actions { display: flex; gap: 10px; align-items: center; }
    .user-pill {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 4px 12px 4px 6px;
      border: 1px solid var(--border);
      background: #fff;
      border-radius: 999px;
      cursor: pointer;
      transition: box-shadow 0.15s;
    }
    .user-pill:hover { box-shadow: var(--shadow); }
    .avatar-sm {
      width: 28px;
      height: 28px;
      border-radius: 50%;
      background: linear-gradient(135deg, var(--teal-light), var(--copper));
      color: #fff;
      display: grid;
      place-items: center;
      font-weight: 700;
      font-size: 12px;
    }
    .user-info { display: flex; flex-direction: column; line-height: 1.1; font-size: 11.5px; }
    .user-name { font-weight: 700; color: var(--teal-dark); }
    .user-role { color: var(--muted); font-size: 10px; }

    /* Hero */
    .hero-section {
      position: relative;
      padding: 60px 24px 80px;
      max-width: 1180px;
      margin: 0 auto;
      display: grid;
      grid-template-columns: 1.15fr 0.85fr;
      gap: 48px;
      align-items: center;
    }
    .hero-content { display: flex; flex-direction: column; gap: 18px; z-index: 2; }
    .hero-badge {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      padding: 6px 14px;
      border-radius: 999px;
      background: #eef6f3;
      border: 1px solid #c8e0d7;
      color: var(--teal);
      font-size: 12px;
      font-weight: 700;
      width: fit-content;
    }
    .pulse-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #10b981;
      box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.25);
    }
    .hero-title {
      font-size: 42px;
      line-height: 1.15;
      font-weight: 800;
      color: var(--teal-dark);
      letter-spacing: -0.02em;
    }
    .gradient-text {
      background: linear-gradient(135deg, var(--teal), var(--copper));
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
    .hero-desc {
      font-size: 16px;
      line-height: 1.6;
      color: var(--muted);
      max-width: 560px;
    }
    .hero-desc b { color: var(--teal-dark); }

    .hero-cta-group {
      display: flex;
      flex-wrap: wrap;
      gap: 14px;
      align-items: center;
      margin-top: 10px;
    }
    .btn-hero-primary {
      display: inline-flex;
      align-items: center;
      gap: 14px;
      padding: 14px 22px;
      border-radius: 16px;
      background: linear-gradient(135deg, #113233, #1e4c4e);
      color: #fff;
      text-decoration: none;
      box-shadow: 0 10px 25px rgba(17, 50, 51, 0.28);
      transition: transform 0.15s, box-shadow 0.15s;
    }
    .btn-hero-primary:hover {
      transform: translateY(-2px);
      box-shadow: 0 14px 30px rgba(17, 50, 51, 0.38);
    }
    .btn-hero-icon {
      width: 42px;
      height: 42px;
      border-radius: 12px;
      background: linear-gradient(135deg, var(--teal-light), var(--copper));
      display: grid;
      place-items: center;
      color: #fff;
    }
    .btn-hero-text { display: flex; flex-direction: column; text-align: left; }
    .btn-hero-text .btn-label { font-size: 15px; font-weight: 700; line-height: 1.2; }
    .btn-hero-text .btn-meta { font-size: 11.5px; opacity: 0.8; }

    .btn-hero-secondary {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      padding: 14px 22px;
      border-radius: 16px;
      background: #fff;
      border: 1.5px solid var(--border);
      color: var(--teal-dark);
      font-weight: 700;
      font-size: 14.5px;
      text-decoration: none;
      box-shadow: var(--shadow);
      transition: background 0.15s, transform 0.15s;
    }
    .btn-hero-secondary:hover { background: #f1f5f4; transform: translateY(-1px); }

    .cloud-status-chip {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      font-size: 11.5px;
      color: var(--muted);
      margin-top: 6px;
    }
    .cloud-status-chip code {
      background: #eef2f1;
      padding: 2px 6px;
      border-radius: 4px;
      font-family: monospace;
      color: var(--teal);
    }
    .status-live { color: #10b981; font-weight: 700; font-size: 11px; }

    /* Phone Mockup */
    .hero-preview {
      display: flex;
      justify-content: center;
      align-items: center;
    }
    .phone-mockup {
      width: 310px;
      height: 560px;
      border-radius: 36px;
      background: #113233;
      padding: 10px;
      box-shadow: 0 25px 60px rgba(17, 50, 51, 0.35);
      border: 4px solid #335c59;
      position: relative;
    }
    .phone-notch {
      width: 110px;
      height: 18px;
      background: #113233;
      border-radius: 0 0 12px 12px;
      position: absolute;
      top: 10px;
      left: 50%;
      transform: translateX(-50%);
      z-index: 10;
    }
    .phone-screen {
      background: #f8fafc;
      width: 100%;
      height: 100%;
      border-radius: 28px;
      overflow: hidden;
      display: flex;
      flex-direction: column;
      padding: 24px 14px 14px;
      box-sizing: border-box;
      gap: 12px;
    }
    .mockup-top {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 6px;
    }
    .mockup-brand { display: flex; align-items: center; gap: 6px; font-size: 14px; color: var(--teal-dark); }
    .mockup-brand .m-c {
      width: 22px; height: 22px; border-radius: 6px; background: var(--copper); color:#fff;
      display:grid; place-items:center; font-weight:800; font-size:11px;
    }
    .mockup-brand span { color: var(--copper); }
    .mockup-badge { font-size: 10px; font-weight: 700; background: #eef2f1; padding: 2px 6px; border-radius: 6px; color: var(--teal); }

    .mockup-card.hero-gradient {
      background: linear-gradient(135deg, var(--teal-dark), var(--teal));
      color: #fff;
      border-radius: 16px;
      padding: 14px;
      display: flex;
      flex-direction: column;
      gap: 4px;
    }
    .mockup-card.hero-gradient small { font-size: 9.5px; opacity: 0.8; letter-spacing: 0.05em; font-weight: 700; }
    .mockup-card.hero-gradient h3 { font-size: 13.5px; line-height: 1.3; font-weight: 700; }
    .mockup-tags { display: flex; flex-wrap: wrap; gap: 4px; margin-top: 6px; }
    .mockup-tags span { background: rgba(255, 255, 255, 0.18); font-size: 9.5px; padding: 2px 6px; border-radius: 6px; }

    .mockup-list { display: flex; flex-direction: column; gap: 8px; flex: 1; }
    .mockup-item {
      display: flex;
      align-items: center;
      gap: 8px;
      background: #fff;
      border: 1px solid var(--border);
      border-radius: 12px;
      padding: 8px 10px;
    }
    .mockup-item .dot { width: 8px; height: 8px; border-radius: 50%; }
    .mockup-item .dot.amber { background: #f59e0b; }
    .mockup-item .dot.blue { background: #3b82f6; }
    .mockup-item .dot.green { background: #10b981; }
    .mockup-item strong { display: block; font-size: 11px; color: var(--teal-dark); line-height: 1.2; }
    .mockup-item small { color: var(--muted); font-size: 9.5px; }

    .mockup-footer { padding-top: 4px; }
    .mockup-download-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      background: var(--teal-dark);
      color: #fff;
      border-radius: 10px;
      padding: 8px;
      font-size: 11.5px;
      font-weight: 700;
      text-decoration: none;
    }

    /* Sections */
    .section-container { max-width: 1180px; margin: 0 auto; padding: 60px 24px; }
    .section-head { margin-bottom: 32px; }
    .section-head h2 { font-size: 28px; font-weight: 800; color: var(--teal-dark); margin: 6px 0; }

    /* Metrics */
    .metrics-section { background: #fff; border-top: 1px solid var(--border); border-bottom: 1px solid var(--border); }
    .metrics-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
    .metric-card {
      background: var(--bg);
      border: 1px solid var(--border);
      border-radius: 18px;
      padding: 24px 18px;
      text-align: center;
      transition: transform 0.15s;
    }
    .metric-card:hover { transform: translateY(-3px); }
    .metric-icon {
      width: 48px;
      height: 48px;
      border-radius: 14px;
      margin: 0 auto 12px;
      display: grid;
      place-items: center;
      color: #fff;
    }
    .metric-icon.teal { background: var(--teal); }
    .metric-icon.green { background: #10b981; }
    .metric-icon.blue { background: #3b82f6; }
    .metric-icon.copper { background: var(--copper); }
    .metric-val { font-size: 36px; font-weight: 800; color: var(--teal-dark); line-height: 1; }
    .metric-name { font-size: 14.5px; font-weight: 700; color: var(--teal-dark); margin: 6px 0 2px; }
    .metric-sub { font-size: 11.5px; color: var(--muted); }

    /* Roles */
    .roles-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
    .role-card {
      background: #fff;
      border: 1px solid var(--border);
      border-radius: 18px;
      padding: 24px 20px;
      display: flex;
      flex-direction: column;
      box-shadow: var(--shadow);
      transition: transform 0.15s;
    }
    .role-card:hover { transform: translateY(-3px); }
    .role-icon { font-size: 32px; margin-bottom: 12px; }
    .role-card h3 { font-size: 17px; font-weight: 700; color: var(--teal-dark); margin-bottom: 8px; }
    .role-card p { font-size: 13px; line-height: 1.5; color: var(--muted); margin-bottom: 16px; flex: 1; }
    .role-features { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 6px; font-size: 12px; color: var(--teal-dark); }
    .role-features li { display: flex; align-items: center; gap: 6px; }
    .role-features app-icon { color: #10b981; }

    /* Muro Feed */
    .feed-section { background: var(--bg); }
    .reporte-item { padding: 0; overflow: hidden; display: flex; flex-direction: column; }
    .card-thumb { width: 100%; height: 160px; object-fit: cover; }
    .card-thumb-placeholder {
      width: 100%;
      height: 160px;
      background: #eef2f1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 6px;
      color: var(--muted);
      font-size: 12px;
    }
    .card-body { padding: 14px; display: flex; flex-direction: column; flex: 1; }
    .card-body h4 { font-size: 14px; font-weight: 700; color: var(--teal-dark); margin: 0 0 4px; }
    .line-clamp {
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      margin-bottom: 10px;
    }
    .card-meta { margin-top: auto; padding-top: 8px; border-top: 1px solid var(--border); font-size: 11px; color: var(--muted); }

    /* Download Banner */
    .download-banner { background: #fff; border-top: 1px solid var(--border); border-bottom: 1px solid var(--border); }
    .download-card {
      background: linear-gradient(135deg, #113233, #1e4c4e);
      border-radius: 28px;
      padding: 44px 40px;
      color: #fff;
      display: grid;
      grid-template-columns: 1.3fr 0.7fr;
      gap: 36px;
      align-items: center;
      box-shadow: 0 20px 50px rgba(17, 50, 51, 0.25);
    }
    .download-info h2 { font-size: 28px; font-weight: 800; margin: 8px 0; }
    .text-copper { color: var(--copper-light); }
    .download-info p { color: #cfe3dc; font-size: 14.5px; line-height: 1.5; margin-bottom: 24px; }

    .install-steps { display: flex; flex-direction: column; gap: 14px; }
    .i-step { display: flex; align-items: flex-start; gap: 12px; }
    .step-num {
      width: 26px;
      height: 26px;
      border-radius: 50%;
      background: var(--copper);
      color: #fff;
      font-weight: 700;
      font-size: 12px;
      display: grid;
      place-items: center;
      flex-shrink: 0;
    }
    .i-step strong { display: block; font-size: 13.5px; }
    .i-step small { color: #a4c7bd; font-size: 12px; }

    .download-qr-box { display: flex; justify-content: center; }
    .qr-frame {
      background: #fff;
      color: var(--teal-dark);
      border-radius: 20px;
      padding: 20px;
      width: 210px;
      text-align: center;
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
    }
    .qr-header { display: flex; align-items: center; justify-content: center; gap: 6px; font-weight: 700; font-size: 12px; margin-bottom: 12px; }
    .qr-visual {
      background: #f1f7f5;
      border: 2px dashed #b9d8cd;
      border-radius: 14px;
      padding: 20px 10px;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 6px;
      color: var(--teal);
    }
    .qr-text { font-size: 12px; font-weight: 700; }
    .qr-sub { font-size: 10px; color: var(--muted); }

    /* Footer */
    .landing-footer { background: #0c2425; color: #fff; padding: 48px 24px 28px; }
    .landing-footer .brand-title { color: #fff; }
    .clean-list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 8px; }
    .clean-list a { color: #9bbab0; transition: color 0.15s; }
    .clean-list a:hover { color: #fff; }
    .footer-bottom { border-top: 1px solid rgba(255, 255, 255, 0.1); padding-top: 20px; }

    /* Responsive */
    @media (max-width: 900px) {
      .hero-section { grid-template-columns: 1fr; gap: 32px; padding: 36px 16px; }
      .hero-title { font-size: 32px; }
      .metrics-grid, .roles-grid { grid-template-columns: 1fr 1fr; }
      .download-card { grid-template-columns: 1fr; padding: 28px 20px; }
      .header-nav { display: none; }
    }
    @media (max-width: 600px) {
      .metrics-grid, .roles-grid { grid-template-columns: 1fr; }
    }
  `],
})
export class LandingComponent implements OnInit {
  api = inject(ApiService);
  cfg = inject(ApiConfigService);
  session = inject(SessionService);
  private router = inject(Router);

  stats = signal<M.Estadisticas | null>(null);
  reportes = signal<M.Reporte[]>([]);
  categorias = signal<M.Categoria[]>([]);
  cargandoReportes = signal(true);

  homePorRol = homePorRol;
  estadoStyle = estadoStyle;
  etiquetaEstado = etiquetaEstado;

  ngOnInit(): void {
    // 1. Cargar Estadísticas en vivo de la base de datos
    this.api.estadisticas().subscribe({
      next: (data) => this.stats.set(data),
      error: () => {},
    });

    // 2. Cargar Categorías activas
    this.api.listarCategorias().subscribe({
      next: (cats) => this.categorias.set(cats),
      error: () => {},
    });

    // 3. Cargar Reportes recientes de la comunidad
    this.api.listarReportes().subscribe({
      next: (list) => {
        this.reportes.set(list);
        this.cargandoReportes.set(false);
      },
      error: () => {
        this.cargandoReportes.set(false);
      },
    });
  }
}
