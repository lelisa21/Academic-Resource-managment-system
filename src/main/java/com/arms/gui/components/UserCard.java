package com.arms.gui.components;

import org.controlsfx.control.GridCell;

import com.arms.domain.User;
import com.arms.domain.enums.UserStatus;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class UserCard extends GridCell<User> {

    private final VBox container;
    private final Label nameLabel;
    private final Label usernameLabel;
    private final Label roleLabel;
    private final Label statusLabel;
    private final Label emailLabel;

    public UserCard() {
        container = new VBox(8);
        container.setPadding(new Insets(12));
        container.setAlignment(Pos.TOP_LEFT);
        container.getStyleClass().add("user-card");

        nameLabel = new Label();
        nameLabel.setFont(Font.font(14));
        nameLabel.getStyleClass().add("user-name");

        usernameLabel = new Label();
        usernameLabel.setFont(Font.font(12));
        usernameLabel.getStyleClass().add("user-username");

        roleLabel = new Label();
        roleLabel.setFont(Font.font(11));
        roleLabel.getStyleClass().add("user-role");

        statusLabel = new Label();
        statusLabel.setFont(Font.font(11));
        statusLabel.getStyleClass().add("user-status");

        emailLabel = new Label();
        emailLabel.setFont(Font.font(11));
        emailLabel.setWrapText(true);
        emailLabel.getStyleClass().add("user-email");

        container.getChildren().addAll(nameLabel, usernameLabel, roleLabel, statusLabel, emailLabel);
    }

    @Override
    protected void updateItem(User user, boolean empty) {
        super.updateItem(user, empty);

        if (empty || user == null) {
            setGraphic(null);
        } else {
            nameLabel.setText(user.getFullName() != null ? user.getFullName().trim() : "(no name)");
            usernameLabel.setText("@" + (user.getUsername() != null ? user.getUsername() : "unknown"));
            roleLabel.setText("Role: " + (user.getRole() != null ? user.getRole().name() : "UNKNOWN"));
            statusLabel.setText("Status: " + (user.getStatus() != null ? user.getStatus().name() : "UNKNOWN"));
            emailLabel.setText(user.getEmail() != null ? user.getEmail() : "No email");

            // Update colors based on role and status
            Color backgroundColor = Color.LIGHTGRAY;
            if (user.getRole() != null) {
                switch (user.getRole()) {
                    case ADMIN:
                    case SUPER_ADMIN:
                        backgroundColor = Color.LIGHTPINK;
                        break;
                    case TEACHER:
                        backgroundColor = Color.LIGHTBLUE;
                        break;
                    case STUDENT:
                        backgroundColor = Color.LIGHTGREEN;
                        break;
                    default:
                        backgroundColor = Color.LIGHTGRAY;
                }
            }

            // Dim if inactive (treat null as not active)
            if (!UserStatus.ACTIVE.equals(user.getStatus())) {
                backgroundColor = backgroundColor.darker();
                container.setOpacity(0.7);
            } else {
                container.setOpacity(1.0);
            }

            container.setBackground(new Background(new BackgroundFill(
                    backgroundColor, new CornerRadii(6), Insets.EMPTY)));

            setGraphic(container);
        }
    }
}
