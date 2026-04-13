/**
 * Slide 9 — Administrador
 */
export const meta = {
  index: 9,
  title: "Administrador - InspectionsApp",
};

export const styles = `* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

body {
    margin: 0;
    padding: 0;
    overflow: hidden;
    font-family: 'Roboto', sans-serif;
    background: var(--color-body-bg);
}

.slide-container {
    position: relative;
    width: 1280px;
    height: 720px;
    overflow: hidden;
    background: linear-gradient(135deg, var(--slide-bg-1) 0%, var(--slide-bg-2) 50%, var(--slide-bg-3) 100%);
}

.bg-pattern {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-image:
        radial-gradient(circle at 20% 80%, rgba(var(--color-success-rgb), 0.1) 0%, transparent 50%),
        radial-gradient(circle at 80% 20%, rgba(var(--color-success-light-rgb), 0.1) 0%, transparent 50%);
    opacity: 0.6;
}

.grid-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-image:
        linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
        linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
    background-size: 50px 50px;
    opacity: 0.3;
}

.content-wrapper {
    position: relative;
    width: 100%;
    height: 100%;
    padding: 40px 60px;
    z-index: 10;
}

.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.title-section {
    display: flex;
    align-items: center;
    gap: 15px;
}

.title-icon {
    width: 50px;
    height: 50px;
    background: linear-gradient(135deg, var(--color-success) 0%, var(--color-success-light) 100%);
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 24px;
}

.main-title {
    font-family: 'Montserrat', sans-serif;
    font-size: 32px;
    font-weight: 700;
    color: white;
    margin: 0;
}

.subtitle {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.7);
    margin-top: 5px;
}

.stats {
    display: flex;
    gap: 20px;
}

.stat-item {
    display: flex;
    align-items: center;
    gap: 8px;
    background: rgba(255, 255, 255, 0.1);
    padding: 8px 16px;
    border-radius: 20px;
}

.stat-value {
    font-size: 14px;
    font-weight: 600;
    color: white;
}

.stat-label {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.7);
}

.screenshots-area {
    display: flex;
    flex-direction: column;
    gap: 18px;
    align-items: center;
}

.phone-row {
    display: flex;
    justify-content: center;
    gap: 40px;
}

.screenshot-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
}

.screenshot-card img {
    border-radius: 16px;
    border: 2px solid rgba(255, 255, 255, 0.12);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.4);
    object-fit: contain;
}

.screenshot-card img.phone-img {
    height: 360px;
    width: auto;
}

.screenshot-card img.landscape-img {
    width: 680px;
    height: auto;
    border-radius: 10px;
}

.screenshot-label {
    font-family: 'Montserrat', sans-serif;
    font-size: 14px;
    font-weight: 600;
    color: white;
    text-align: center;
}

.floating-elements {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    overflow: hidden;
}

.floating-icon {
    position: absolute;
    color: rgba(var(--color-success-light-rgb), 0.1);
    font-size: 20px;
    animation: float 6s ease-in-out infinite;
}

@keyframes float {
    0%, 100% { transform: translateY(0) rotate(0deg); }
    50% { transform: translateY(-20px) rotate(5deg); }
}`;

export const html = `<div class="slide-container">
    <div class="bg-pattern"></div>
    <div class="grid-overlay"></div>
    <div class="floating-elements">
        <i class="fas fa-user-shield floating-icon" style="top: 15%; left: 8%;"></i>
        <i class="fas fa-shield-alt floating-icon" style="top: 25%; right: 12%;"></i>
        <i class="fas fa-users-cog floating-icon" style="bottom: 18%; left: 15%;"></i>
    </div>
    <div class="content-wrapper">
        <div class="header">
            <div class="title-section">
                <div class="title-icon">
                    <i class="fas fa-user-shield"></i>
                </div>
                <div>
                    <div class="main-title">Administrador</div>
                    <div class="subtitle">Funcionalidades exclusivas del rol ADMIN</div>
                </div>
            </div>
            <div class="stats">
                <div class="stat-item">
                    <span class="stat-value">4</span>
                    <span class="stat-label">vistas</span>
                </div>
                <div class="stat-item">
                    <span class="stat-value">ADMIN</span>
                    <span class="stat-label">rol</span>
                </div>
            </div>
        </div>
        <div class="screenshots-area">
            <div class="phone-row">
                <div class="screenshot-card">
                    <img class="phone-img" src="assets/vista-admin.png" alt="Vista Admin">
                    <div class="screenshot-label">Vista Admin</div>
                </div>
                <div class="screenshot-card">
                    <img class="phone-img" src="assets/gestion-usuarios.png" alt="Admin Gestion de Usuarios">
                    <div class="screenshot-label">Admin Gesti\u00f3n de Usuarios</div>
                </div>
                <div class="screenshot-card">
                    <img class="phone-img" src="assets/log-auditoria.png" alt="Log para Auditoria">
                    <div class="screenshot-label">Log para Auditor\u00eda</div>
                </div>
            </div>
            <div class="screenshot-card">
                <img class="landscape-img" src="assets/informe-pdf.png" alt="Informe de Auditoria en PDF">
                <div class="screenshot-label">Informe de Auditor\u00eda en PDF</div>
            </div>
        </div>
    </div>
</div>`;
