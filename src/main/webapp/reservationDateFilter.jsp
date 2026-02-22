<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.entity.Reservation" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html lang="fr" data-theme="light">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Filtrer par Date | Location Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/modern-style.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <style>
        :root { --font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif; }
    </style>
</head>
<body>
    <div class="app-layout" id="appLayout">
        <div class="sidebar-overlay" onclick="toggleMobileSidebar()"></div>
        
        <aside class="sidebar" id="sidebar">
            <div class="sidebar-header">
                <a href="${pageContext.request.contextPath}/" class="sidebar-logo">
                    <div class="sidebar-logo-icon"><i class="fas fa-car"></i></div>
                    <div>
                        <div class="sidebar-logo-text">Location</div>
                        <div class="sidebar-logo-subtitle">Back Office</div>
                    </div>
                </a>
            </div>
            <button class="sidebar-toggle" onclick="toggleSidebarCollapse()" title="Réduire/Agrandir"><i class="fas fa-chevron-left"></i></button>
            
            <nav class="sidebar-nav">
                <div class="nav-section">
                    <div class="nav-section-title">Gestion</div>
                    <a href="${pageContext.request.contextPath}/vehicule/list" class="nav-item" data-tooltip="Véhicules">
                        <span class="nav-icon"><i class="fas fa-car-side"></i></span>
                        <span class="nav-text">Véhicules</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/reservation/list" class="nav-item" data-tooltip="Réservations">
                        <span class="nav-icon"><i class="fas fa-clipboard-list"></i></span>
                        <span class="nav-text">Réservations</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/reservation/date/filter" class="nav-item active" data-tooltip="Filtrer par date">
                        <span class="nav-icon"><i class="fas fa-calendar-alt"></i></span>
                        <span class="nav-text">Filtrer par date</span>
                    </a>
                </div>
            </nav>
            
            <div class="sidebar-footer">
                <div class="theme-toggle" onclick="toggleTheme()">
                    <span class="theme-toggle-text">Mode sombre</span>
                    <div class="theme-switch"></div>
                </div>
            </div>
        </aside>

        <main class="main-content">
            <header class="top-header">
                <button class="mobile-menu-btn" onclick="toggleMobileSidebar()">
                    <i class="fas fa-bars"></i>
                </button>
                <h1 class="page-title">Filtrer par Date</h1>
                <div class="header-actions">
                    <a href="${pageContext.request.contextPath}/reservation/list" class="btn btn-secondary">
                        <i class="fas fa-arrow-left"></i> Retour
                    </a>
                </div>
            </header>
            
            <div class="content-area">
                <!-- Filter Form -->
                <div class="card animate-slide-up mb-4">
                    <div class="card-header">
                        <div class="card-title">
                            <div class="card-title-icon"><i class="fas fa-filter"></i></div>
                            Filtrer les réservations par date
                        </div>
                    </div>
                    <div class="card-body">
                        <div class="form-row" style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; max-width: 500px;">
                            <div class="form-group">
                                <label class="form-label" for="dateDebut">
                                    <i class="fas fa-calendar-alt"></i> Date de début
                                </label>
                                <input type="date" id="dateDebut" class="form-input">
                            </div>
                            <div class="form-group">
                                <label class="form-label" for="dateFin">
                                    <i class="fas fa-calendar-check"></i> Date de fin
                                </label>
                                <input type="date" id="dateFin" class="form-input">
                            </div>
                        </div>
                        <div class="form-actions" style="justify-content: flex-start; gap: 1rem; margin-top: 1.5rem;">
                            <button type="button" class="btn btn-primary" onclick="viewAssigned()">
                                <i class="fas fa-check-circle"></i> Réservations Assignées
                            </button>
                            <button type="button" class="btn btn-warning" onclick="viewUnassigned()">
                                <i class="fas fa-clock"></i> Réservations Non Assignées
                            </button>
                        </div>
                    </div>
                </div>

            </div>
        </main>
    </div>

    <script>
        const contextPath = '${pageContext.request.contextPath}';
        
        function validateDates() {
            const dateDebut = document.getElementById('dateDebut').value;
            const dateFin = document.getElementById('dateFin').value;
            
            if (!dateDebut || !dateFin) {
                alert('Veuillez sélectionner une date de début et une date de fin');
                return null;
            }
            
            if (dateDebut > dateFin) {
                alert('La date de début doit être antérieure ou égale à la date de fin');
                return null;
            }
            
            return { dateDebut, dateFin };
        }
        
        function viewAssigned() {
            const dates = validateDates();
            if (!dates) return;
            window.location.href = contextPath + '/reservation/date/assigned?dateDebut=' + dates.dateDebut + '&dateFin=' + dates.dateFin;
        }
        
        function viewUnassigned() {
            const dates = validateDates();
            if (!dates) return;
            window.location.href = contextPath + '/reservation/date/unassigned?dateDebut=' + dates.dateDebut + '&dateFin=' + dates.dateFin;
        }
        
        function toggleTheme() {
            const html = document.documentElement;
            const newTheme = html.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
            html.setAttribute('data-theme', newTheme);
            localStorage.setItem('theme', newTheme);
        }
        document.documentElement.setAttribute('data-theme', localStorage.getItem('theme') || 'light');

        function toggleMobileSidebar() {
            document.querySelector('.sidebar').classList.toggle('open');
            document.querySelector('.sidebar-overlay').classList.toggle('active');
        }

        // Sidebar Collapse Toggle (Desktop)
        function toggleSidebarCollapse() {
            const sidebar = document.getElementById('sidebar');
            const appLayout = document.getElementById('appLayout');
            const isCollapsed = sidebar.classList.toggle('collapsed');
            appLayout.classList.toggle('sidebar-collapsed', isCollapsed);
            localStorage.setItem('sidebarCollapsed', isCollapsed);
        }

        // Load saved sidebar state
        const sidebarCollapsed = localStorage.getItem('sidebarCollapsed') === 'true';
        if (sidebarCollapsed) {
            document.getElementById('sidebar').classList.add('collapsed');
            document.getElementById('appLayout').classList.add('sidebar-collapsed');
        }
        
        // Default to today's date
        const today = new Date().toISOString().split('T')[0];
        document.getElementById('dateDebut').value = today;
        document.getElementById('dateFin').value = today;
    </script>
</body>
</html>
