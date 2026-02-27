<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.entity.Vehicule" %>
<%@ page import="com.entity.TypeCarburant" %>
<!DOCTYPE html>
<html lang="fr" data-theme="light">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Véhicules | Location Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/modern-style.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <style>
        :root { --font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif; }
    </style>
</head>
<body>
    <div class="app-layout" id="appLayout">
        <!-- Sidebar Overlay (Mobile) -->
        <div class="sidebar-overlay" onclick="toggleMobileSidebar()"></div>
        
        <!-- Sidebar Navigation -->
        <aside class="sidebar" id="sidebar">
            <div class="sidebar-header">
                <a href="${pageContext.request.contextPath}/" class="sidebar-logo">
                    <div class="sidebar-logo-icon">
                        <i class="fas fa-car"></i>
                    </div>
                    <div>
                        <div class="sidebar-logo-text">Location</div>
                        <div class="sidebar-logo-subtitle">Back Office</div>
                    </div>
                </a>
            </div>
            
            <!-- Sidebar Toggle Button -->
            <button class="sidebar-toggle" onclick="toggleSidebarCollapse()" title="Réduire/Agrandir">
                <i class="fas fa-chevron-left"></i>
            </button>
            
            <nav class="sidebar-nav">
                <div class="nav-section">
                    <div class="nav-section-title">Gestion</div>
                    <a href="${pageContext.request.contextPath}/vehicule/list" class="nav-item active" data-tooltip="Véhicules">
                        <span class="nav-icon"><i class="fas fa-car-side"></i></span>
                        <span class="nav-text">Véhicules</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/reservation/list" class="nav-item" data-tooltip="Réservations">
                        <span class="nav-icon"><i class="fas fa-clipboard-list"></i></span>
                        <span class="nav-text">Réservations</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/reservation/date/filter" class="nav-item" data-tooltip="Filtrer par date">
                        <span class="nav-icon"><i class="fas fa-calendar-alt"></i></span>
                        <span class="nav-text">Filtrer par date</span>
                    </a>
                </div>
            </nav>
            
            <div class="sidebar-footer">
                <div class="theme-toggle" onclick="toggleTheme()" data-tooltip="Mode sombre">
                    <span class="theme-toggle-text">Mode sombre</span>
                    <div class="theme-switch"></div>
                </div>
            </div>
        </aside>

        <!-- Main Content -->
        <main class="main-content">
            <header class="top-header">
                <button class="mobile-menu-btn" onclick="toggleMobileSidebar()">
                    <i class="fas fa-bars"></i>
                </button>
                <h1 class="page-title">Gestion des Véhicules</h1>
                <div class="header-actions">
                    <button class="btn btn-primary" onclick="openModal('vehicleModal')">
                        <i class="fas fa-plus"></i>
                        Nouveau Véhicule
                    </button>
                </div>
            </header>
            
            <div class="content-area">
                <%-- Messages --%>
                <%
                    String successMessage = (String) request.getAttribute("successMessage");
                    String errorMessage = (String) request.getAttribute("errorMessage");
                    
                    if (successMessage != null && !successMessage.isEmpty()) {
                %>
                    <div class="alert alert-success">
                        <span class="alert-icon"><i class="fas fa-check-circle"></i></span>
                        <div class="alert-content"><%= successMessage %></div>
                        <button class="alert-close" onclick="this.parentElement.remove()">&times;</button>
                    </div>
                <% } if (errorMessage != null && !errorMessage.isEmpty()) { %>
                    <div class="alert alert-error">
                        <span class="alert-icon"><i class="fas fa-exclamation-circle"></i></span>
                        <div class="alert-content"><%= errorMessage %></div>
                        <button class="alert-close" onclick="this.parentElement.remove()">&times;</button>
                    </div>
                <% } %>

                <!-- Stats -->
                <div class="stats-grid">
                    <div class="stat-card">
                        <div class="stat-icon primary"><i class="fas fa-car"></i></div>
                        <div class="stat-content">
                            <% List<Vehicule> vehiculesStats = (List<Vehicule>) request.getAttribute("vehicules"); %>
                            <div class="stat-value"><%= vehiculesStats != null ? vehiculesStats.size() : 0 %></div>
                            <div class="stat-label">Total Véhicules</div>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon success"><i class="fas fa-check"></i></div>
                        <div class="stat-content">
                            <div class="stat-value"><%= vehiculesStats != null ? vehiculesStats.size() : 0 %></div>
                            <div class="stat-label">Disponibles</div>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon info"><i class="fas fa-gas-pump"></i></div>
                        <div class="stat-content">
                            <div class="stat-value">3</div>
                            <div class="stat-label">Types Carburant</div>
                        </div>
                    </div>
                </div>

                <!-- Vehicle List Card -->
                <div class="card animate-slide-up">
                    <div class="card-header">
                        <div class="card-title">
                            <div class="card-title-icon"><i class="fas fa-list"></i></div>
                            Liste des Véhicules
                        </div>
                        <div class="search-bar" style="margin-bottom: 0;">
                            <div class="search-input-wrapper">
                                <span class="search-icon"><i class="fas fa-search"></i></span>
                                <input type="text" id="searchInput" class="search-input" placeholder="Rechercher un véhicule...">
                            </div>
                            <button class="btn btn-secondary" onclick="searchVehicules()">Rechercher</button>
                        </div>
                    </div>
                    <div class="card-body" style="padding: 0;">
                        <div class="table-wrapper">
                            <%
                                List<Vehicule> vehicules = (List<Vehicule>) request.getAttribute("vehicules");
                                if (vehicules != null && !vehicules.isEmpty()) {
                            %>
                                <table class="table">
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Référence</th>
                                            <th>Places</th>
                                            <th>Carburant</th>
                                            <th style="text-align: right;">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (Vehicule vehicule : vehicules) { %>
                                            <tr>
                                                <td><span class="text-muted">#<%= vehicule.getId() %></span></td>
                                                <td><strong><%= vehicule.getReference() %></strong></td>
                                                <td><span class="badge badge-info"><%= vehicule.getNbPlaces() %> places</span></td>
                                                <td><span class="badge badge-primary"><%= vehicule.getTypeCarburantLibelle() != null ? vehicule.getTypeCarburantLibelle() : "N/A" %></span></td>
                                                <td>
                                                    <div class="table-actions">
                                                        <button class="btn btn-ghost btn-sm" onclick="editVehicle(<%= vehicule.getId() %>, '<%= vehicule.getReference() %>', <%= vehicule.getNbPlaces() %>, <%= vehicule.getTypeCarburantId() %>)" title="Modifier">
                                                            <i class="fas fa-edit"></i>
                                                        </button>
                                                        <button class="btn btn-ghost btn-sm" onclick="confirmDelete(<%= vehicule.getId() %>, '<%= vehicule.getReference() %>')" title="Supprimer">
                                                            <i class="fas fa-trash-alt"></i>
                                                        </button>
                                                    </div>
                                                </td>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            <% } else { %>
                                <div class="empty-state">
                                    <div class="empty-state-icon"><i class="fas fa-car"></i></div>
                                    <div class="empty-state-title">Aucun véhicule</div>
                                    <p class="empty-state-text">Commencez par ajouter votre premier véhicule.</p>
                                    <button class="btn btn-primary" onclick="openModal('vehicleModal')">
                                        <i class="fas fa-plus"></i>
                                        Ajouter un véhicule
                                    </button>
                                </div>
                            <% } %>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>

    <!-- Modal: Add/Edit Vehicle -->
    <div class="modal-overlay" id="vehicleModal">
        <div class="modal">
            <div class="modal-header">
                <h2 class="modal-title" id="vehicleModalTitle">Nouveau Véhicule</h2>
                <button class="modal-close" onclick="closeModal('vehicleModal')">&times;</button>
            </div>
            <form action="${pageContext.request.contextPath}/vehicule/save" method="post" id="vehicleForm">
                <div class="modal-body">
                    <input type="hidden" name="id" id="vehicleId" value="">
                    
                    <div class="form-group">
                        <label class="form-label required" for="reference">Référence</label>
                        <input type="text" id="reference" name="reference" class="form-input" placeholder="Ex: BMW-X5-2024" required>
                    </div>
                    
                    <div class="form-group">
                        <label class="form-label required" for="nbPlaces">Nombre de places</label>
                        <input type="number" id="nbPlaces" name="nbPlaces" class="form-input" min="1" max="50" value="4" required>
                    </div>
                    
                    <div class="form-group">
                        <label class="form-label required" for="typeCarburantId">Type de carburant</label>
                        <select id="typeCarburantId" name="typeCarburantId" class="form-select" required>
                            <option value="">Sélectionner un type</option>
                            <%
                                List<TypeCarburant> typesCarburant = (List<TypeCarburant>) request.getAttribute("typesCarburant");
                                if (typesCarburant != null) {
                                    for (TypeCarburant type : typesCarburant) {
                            %>
                                <option value="<%= type.getId() %>"><%= type.getLibelle() %></option>
                            <% }} %>
                        </select>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" onclick="closeModal('vehicleModal')">Annuler</button>
                    <button type="submit" class="btn btn-primary" id="vehicleSubmitBtn">
                        <i class="fas fa-save"></i>
                        Créer le véhicule
                    </button>
                </div>
            </form>
        </div>
    </div>

    <!-- Modal: Confirm Delete -->
    <div class="modal-overlay" id="deleteModal">
        <div class="modal" style="max-width: 400px;">
            <div class="modal-header">
                <h2 class="modal-title">Confirmer la suppression</h2>
                <button class="modal-close" onclick="closeModal('deleteModal')">&times;</button>
            </div>
            <div class="modal-body text-center">
                <div class="empty-state-icon" style="background: var(--error-bg); color: var(--error);">
                    <i class="fas fa-trash-alt"></i>
                </div>
                <p>Êtes-vous sûr de vouloir supprimer le véhicule <strong id="deleteVehicleName"></strong> ?</p>
                <p class="text-muted">Cette action est irréversible.</p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeModal('deleteModal')">Annuler</button>
                <a href="#" id="deleteLink" class="btn btn-danger">
                    <i class="fas fa-trash-alt"></i>
                    Supprimer
                </a>
            </div>
        </div>
    </div>

    <script>
        // Theme Management
        function toggleTheme() {
            const html = document.documentElement;
            const currentTheme = html.getAttribute('data-theme');
            const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
            html.setAttribute('data-theme', newTheme);
            localStorage.setItem('theme', newTheme);
        }
        
        // Load saved theme
        const savedTheme = localStorage.getItem('theme') || 'light';
        document.documentElement.setAttribute('data-theme', savedTheme);

        // Sidebar Toggle (Mobile)
        function toggleMobileSidebar() {
            document.getElementById('sidebar').classList.toggle('open');
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

        // Modal Management
        function openModal(modalId) {
            document.getElementById(modalId).classList.add('active');
            document.body.style.overflow = 'hidden';
        }

        function closeModal(modalId) {
            document.getElementById(modalId).classList.remove('active');
            document.body.style.overflow = '';
            if (modalId === 'vehicleModal') {
                resetVehicleForm();
            }
        }

        // Close modal on overlay click
        document.querySelectorAll('.modal-overlay').forEach(overlay => {
            overlay.addEventListener('click', (e) => {
                if (e.target === overlay) {
                    closeModal(overlay.id);
                }
            });
        });

        // Close modal on Escape key
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                document.querySelectorAll('.modal-overlay.active').forEach(modal => {
                    closeModal(modal.id);
                });
            }
        });

        // Vehicle Form
        function resetVehicleForm() {
            document.getElementById('vehicleId').value = '';
            document.getElementById('reference').value = '';
            document.getElementById('nbPlaces').value = '4';
            document.getElementById('typeCarburantId').value = '';
            document.getElementById('vehicleModalTitle').textContent = 'Nouveau Véhicule';
            document.getElementById('vehicleSubmitBtn').innerHTML = '<i class="fas fa-save"></i> Créer le véhicule';
        }

        function editVehicle(id, reference, nbPlaces, typeCarburantId) {
            document.getElementById('vehicleId').value = id;
            document.getElementById('reference').value = reference;
            document.getElementById('nbPlaces').value = nbPlaces;
            document.getElementById('typeCarburantId').value = typeCarburantId;
            document.getElementById('vehicleModalTitle').textContent = 'Modifier le Véhicule';
            document.getElementById('vehicleSubmitBtn').innerHTML = '<i class="fas fa-save"></i> Enregistrer';
            openModal('vehicleModal');
        }

        // Delete Confirmation
        function confirmDelete(id, reference) {
            document.getElementById('deleteVehicleName').textContent = reference;
            document.getElementById('deleteLink').href = '${pageContext.request.contextPath}/vehicule/delete/' + id;
            openModal('deleteModal');
        }

        // Search
        function searchVehicules() {
            const searchTerm = document.getElementById('searchInput').value.trim();
            if (searchTerm) {
                window.location.href = '${pageContext.request.contextPath}/vehicule/list?search=' + encodeURIComponent(searchTerm);
            } else {
                window.location.href = '${pageContext.request.contextPath}/vehicule/list';
            }
        }
        
        document.getElementById('searchInput').addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchVehicules();
            }
        });

        // Auto-dismiss alerts
        setTimeout(() => {
            document.querySelectorAll('.alert').forEach(alert => {
                alert.style.opacity = '0';
                alert.style.transform = 'translateY(-10px)';
                setTimeout(() => alert.remove(), 300);
            });
        }, 5000);
    </script>
</body>
</html>
