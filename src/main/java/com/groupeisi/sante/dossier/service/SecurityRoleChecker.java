package com.groupeisi.sante.dossier.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class SecurityRoleChecker {

    public boolean isMedecin() {
        return hasRole("ROLE_MEDECIN");
    }

    public boolean isInfirmier() {
        return hasRole("ROLE_INFIRMIER");
    }

    public boolean isSecretaire() {
        return hasRole("ROLE_SECRETAIRE");
    }

    public boolean isAdminSecurite() {
        return hasRole("ROLE_ADMIN_SECURITE");
    }

    /** Secrétaire sans rôle soignant : masquage des identifiants de couverture. */
    public boolean isSecretaireOnly() {
        return isSecretaire() && !isMedecin() && !isInfirmier();
    }

    public boolean canViewClinicalNotes() {
        return isMedecin();
    }

    private boolean hasRole(String role) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        return authorities.stream().anyMatch(a -> role.equals(a.getAuthority()));
    }
}
