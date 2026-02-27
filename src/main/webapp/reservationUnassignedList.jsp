<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.entity.Reservation" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html lang="fr" data-theme="light">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Réservations Non Assignées | Location Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/modern-style.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <style>
        :root { --font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif; }
        .btn-magic {
            background: linear-gradient(135deg, #6366f1 0%, #a855f7 100%);
            color: white;
            border: none;
            transition: all 0.3s ease;
        }
        .btn-magic:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(168, 85, 247, 0.4);
            color: white;
        }
        .loading-overlay {
            display: none;
            position: fixed;
            top: 0; left: 0; width: 100%; height: 100%;
            background: rgba(255,255,255,0.8);
            z-index: 9999;
            justify-content: center;
            align-items: center;
            flex-direction: column;
        }
        [data-theme='dark'] .loading-overlay { background: rgba(15, 23, 42, 0.8); }
    </style>
</head>
<body>
    <div id="loadingOverlay" class="loading-overlay">
        <div class="spinner-border text-primary mb-3" role="status">
            <i class="fas fa-circle-notch fa-spin fa-3x" style="color: #a855f7;"></i>
        </div>
        <p><strong>Calcul de l'optimisation temporelle...</strong></p>
        <p><small>Vérification du pooling et des fenêtres de mission.</small></p>
    </div>

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
            <button class="sidebar-toggle" onclick="toggleSidebarCollapse()"><i class="fas fa-chevron-left"></i></button>
            
            <nav class="sidebar-nav">
                <div class="nav-section">
                    <div class="nav-section-title">Gestion</div>
                    <a href="${pageContext.request.contextPath}/vehicule/list" class="nav-item">
                        <span class="nav-icon"><i class="fas fa-car-side"></i></span>
                        <span class="nav-text">Véhicules</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/reservation/list" class="nav-item">
                        <span class="nav-icon"><i class="fas fa-clipboard-list"></i></span>
                        <span class="nav-text">Réservations</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/reservation/date/filter" class="nav-item active">
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
            <%
                String dateDebut = (String) request.getAttribute("dateDebut");
                String dateFin = (String) request.getAttribute("dateFin");
                List<Reservation> reservations = (List<Reservation>) request.getAttribute("unassignedReservations");
            %>

            <header class="top-header">
                <button class="mobile-menu-btn" onclick="toggleMobileSidebar()"><i class="fas fa-bars"></i></button>
                <h1 class="page-title">Réservations en attente d'assignation</h1>
                
                <div class="header-actions">
                    <% if (reservations != null && !reservations.isEmpty() && dateDebut != null) { %>
                        <form action="${pageContext.request.contextPath}/reservation/assign-auto" method="post" onsubmit="return startAutoAssign()">
                            <input type="hidden" name="dateDebut" value="<%= dateDebut %>">
                            <input type="hidden" name="dateFin" value="<%= dateFin %>">
                            <button type="submit" class="btn btn-magic">
                                <i class="fas fa-wand-sparkles"></i> Lancer l'Algorithme
                            </button>
                        </form>
                    <% } %>
                    <a href="${pageContext.request.contextPath}/reservation/date/filter" class="btn btn-secondary">
                        <i class="fas fa-arrow-left"></i> Retour
                    </a>
                </div>
            </header>
            
            <div class="content-area">
                <% if (dateDebut != null && dateFin != null) { %>
                <div class="card animate-slide-up mb-4">
                    <div class="card-body" style="padding: 1rem 1.5rem; display: flex; justify-content: space-between; align-items: center;">
                        <div>
                            <strong><i class="fas fa-calendar-day"></i> Période sélectionnée :</strong> 
                            Du <%= dateDebut %> au <%= dateFin %>
                        </div>
                        <% if (reservations != null) { %>
                            <span class="badge <%= reservations.isEmpty() ? "badge-success" : "badge-warning" %>">
                                <%= reservations.size() %> demande(s) libre(s)
                            </span>
                        <% } %>
                    </div>
                </div>
                <% } %>
                
                <div class="card animate-slide-up">
                    <div class="card-header">
                        <div class="card-title">
                            <div class="card-title-icon"><i class="fas fa-list-ul"></i></div>
                            Détails des demandes
                        </div>
                    </div>
                    <div class="card-body" style="padding: 0;">
                        <div class="table-wrapper">
                            <%
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                                if (reservations != null && !reservations.isEmpty()) {
                            %>
                                <table class="table">
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Client</th>
                                            <th>Destination</th>
                                            <th>Passagers</th>
                                            <th>Date & Heure</th>
                                            <th>État</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (Reservation r : reservations) { %>
                                            <tr>
                                                <td><span class="text-muted">#<%= r.getId() %></span></td>
                                                <td><strong><%= r.getClient() %></strong></td>
                                                <td><span class="badge badge-secondary"><i class="fas fa-map-marker-alt"></i> <%= r.getLieuCode() != null ? r.getLieuCode() : "N/A" %></span></td>
                                                <td><span class="badge badge-info"><%= r.getNbPassager() %> pers.</span></td>
                                                <td><%= r.getDateHeure() != null ? r.getDateHeure().format(formatter) : "N/A" %></td>
                                                <td>
                                                    <span class="text-warning">
                                                        <i class="fas fa-exclamation-triangle"></i> À assigner
                                                    </span>
                                                </td>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            <% } else { %>
                                <div class="empty-state">
                                    <div class="empty-state-icon"><i class="fas fa-check-circle" style="color: #10b981;"></i></div>
                                    <div class="empty-state-title">Planning à jour</div>
                                    <p class="empty-state-text">Aucune réservation en attente n'a été trouvée pour ces dates.</p>
                                    <a href="${pageContext.request.contextPath}/reservation/date/assigned?dateDebut=<%=dateDebut%>&dateFin=<%=dateFin%>" class="btn btn-primary mt-3">
                                        Voir les missions déjà planifiées
                                    </a>
                                </div>
                            <% } %>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>

    <script>
        function startAutoAssign() {
            if (confirm("Lancer l'assignation automatique ?\n\nL'algorithme va :\n1. Grouper les passagers selon les fenêtres de 15min.\n2. Vérifier la disponibilité réelle des véhicules.\n3. Garantir qu'aucun trajet ne chevauche une mission future.")) {
                document.getElementById('loadingOverlay').style.display = 'flex';
                return true;
            }
            return false;
        }

        // --- Gestion Sidebar et Theme ---
        function toggleTheme() {
            const html = document.documentElement;
            const newTheme = html.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
            html.setAttribute('data-theme', newTheme);
            localStorage.setItem('theme', newTheme);
        }

        function toggleMobileSidebar() {
            document.querySelector('.sidebar').classList.toggle('open');
            document.querySelector('.sidebar-overlay').classList.toggle('active');
        }

        function toggleSidebarCollapse() {
            const sidebar = document.getElementById('sidebar');
            const isCollapsed = sidebar.classList.toggle('collapsed');
            document.getElementById('appLayout').classList.toggle('sidebar-collapsed', isCollapsed);
            localStorage.setItem('sidebarCollapsed', isCollapsed);
        }

        // Initialisation
        document.documentElement.setAttribute('data-theme', localStorage.getItem('theme') || 'light');
        if (localStorage.getItem('sidebarCollapsed') === 'true') {
            document.getElementById('sidebar').classList.add('collapsed');
            document.getElementById('appLayout').classList.add('sidebar-collapsed');
        }
    </script>
</body>
</html>