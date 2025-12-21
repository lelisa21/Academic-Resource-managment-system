package com.arms.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Admin extends User {

    private String adminId;
    private String accessLevel;
    private List<String> permissions;

    public Admin() {
        super();
        this.permissions = new ArrayList<>();
    }

    public Admin(String adminId, String accessLevel, List<String> permissions) {
        super();
        this.adminId = adminId;
        this.accessLevel = accessLevel;
        this.permissions = permissions == null ? new ArrayList<>() : permissions;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public boolean hasPermission(String permission) {
        return permissions != null && (permissions.contains(permission) || permissions.contains("ALL"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Admin)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        Admin admin = (Admin) o;
        return Objects.equals(adminId, admin.adminId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), adminId);
    }
}
