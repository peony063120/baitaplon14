import os

# Create directory structure
dirs = [
    r"client\src\main\resources\com\auction\client\view",
    r"client\src\main\resources\com\auction\client\styles"
]

for dir_path in dirs:
    os.makedirs(dir_path, exist_ok=True)
    print(f"Created: {dir_path}")

# Create login.fxml
login_fxml = '''<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.geometry.Insets?>
<?import javafx.scene.control.Button?>
<?import javafx.scene.control.ComboBox?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.control.PasswordField?>
<?import javafx.scene.control.TextField?>
<?import javafx.scene.layout.BorderPane?>
<?import javafx.scene.layout.VBox?>
<?import javafx.scene.layout.HBox?>

<BorderPane maxHeight="-Infinity" maxWidth="-Infinity" minHeight="-Infinity" minWidth="-Infinity" prefHeight="600.0" prefWidth="500.0" xmlns="http://javafx.com/javafx/21" xmlns:fx="http://javafx.com/fxml/1" fx:controller="com.auction.client.controller.LoginController">
    <center>
        <VBox alignment="CENTER" spacing="15.0" style="-fx-padding: 30;">
            <Label styleClass="title" text="Auction System Login" />
            
            <VBox spacing="5.0">
                <Label text="Username:" />
                <TextField fx:id="usernameField" promptText="Enter your username" />
            </VBox>
            
            <VBox spacing="5.0">
                <Label text="Password:" />
                <PasswordField fx:id="passwordField" promptText="Enter your password" />
            </VBox>
            
            <VBox spacing="5.0">
                <Label text="Role:" />
                <ComboBox fx:id="roleCombo" prefWidth="150.0" promptText="Select a role" />
            </VBox>
            
            <Label fx:id="errorLabel" style="-fx-text-fill: #cc0000;" />
            
            <HBox alignment="CENTER" spacing="10.0">
                <Button onAction="#handleLogin" prefWidth="100.0" style="-fx-padding: 10;" text="Login" />
                <Button onAction="#handleRegister" prefWidth="100.0" style="-fx-padding: 10;" text="Register" />
            </HBox>
        </VBox>
    </center>
</BorderPane>'''

register_fxml = '''<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.geometry.Insets?>
<?import javafx.scene.control.Button?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.control.PasswordField?>
<?import javafx.scene.control.TextField?>
<?import javafx.scene.layout.BorderPane?>
<?import javafx.scene.layout.VBox?>
<?import javafx.scene.layout.HBox?>

<BorderPane maxHeight="-Infinity" maxWidth="-Infinity" minHeight="-Infinity" minWidth="-Infinity" prefHeight="700.0" prefWidth="500.0" xmlns="http://javafx.com/javafx/21" xmlns:fx="http://javafx.com/fxml/1" fx:controller="com.auction.client.controller.RegisterController">
    <center>
        <VBox alignment="TOP_CENTER" spacing="15.0" style="-fx-padding: 30;">
            <Label styleClass="title" text="Create New Account" />
            
            <VBox spacing="5.0">
                <Label text="Username:" />
                <TextField fx:id="usernameField" promptText="Choose a username" />
            </VBox>
            
            <VBox spacing="5.0">
                <Label text="Email:" />
                <TextField fx:id="emailField" promptText="Enter your email address" />
            </VBox>
            
            <VBox spacing="5.0">
                <Label text="Full Name:" />
                <TextField fx:id="fullNameField" promptText="Enter your full name" />
            </VBox>
            
            <VBox spacing="5.0">
                <Label text="Password:" />
                <PasswordField fx:id="passwordField" promptText="Enter a strong password" />
            </VBox>
            
            <VBox spacing="5.0">
                <Label text="Confirm Password:" />
                <PasswordField fx:id="confirmPasswordField" promptText="Confirm your password" />
            </VBox>
            
            <Label fx:id="errorLabel" style="-fx-text-fill: #cc0000;" />
            
            <HBox alignment="CENTER" spacing="10.0">
                <Button onAction="#handleRegister" prefWidth="100.0" style="-fx-padding: 10;" text="Register" />
                <Button onAction="#handleCancel" prefWidth="100.0" style="-fx-padding: 10;" text="Cancel" />
            </HBox>
        </VBox>
    </center>
</BorderPane>'''

main_fxml = '''<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.geometry.Insets?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.control.Tab?>
<?import javafx.scene.control.TabPane?>
<?import javafx.scene.layout.BorderPane?>
<?import javafx.scene.layout.HBox?>
<?import javafx.scene.layout.VBox?>
<?import javafx.scene.control.Button?>

<BorderPane maxHeight="-Infinity" maxWidth="-Infinity" minHeight="-Infinity" minWidth="-Infinity" prefHeight="800.0" prefWidth="1000.0" xmlns="http://javafx.com/javafx/21" xmlns:fx="http://javafx.com/fxml/1" fx:controller="com.auction.client.controller.MainController">
    <top>
        <HBox alignment="CENTER_RIGHT" spacing="20.0" style="-fx-padding: 15; -fx-background-color: #f0f0f0;">
            <Label fx:id="userLabel" text="Welcome, User" />
            <Button onAction="#handleLogout" text="Logout" />
        </HBox>
    </top>
    
    <center>
        <TabPane tabClosingPolicy="UNAVAILABLE">
            <Tab text="Available Auctions" closable="false">
                <VBox fx:id="auctionsContainer" spacing="10.0" style="-fx-padding: 15;">
                    <Label text="Available auctions will be displayed here" />
                </VBox>
            </Tab>
            
            <Tab text="My Auctions" closable="false">
                <VBox fx:id="myAuctionsContainer" spacing="10.0" style="-fx-padding: 15;">
                    <Label text="Your auctions will be displayed here" />
                </VBox>
            </Tab>
            
            <Tab text="Profile" closable="false">
                <VBox spacing="15.0" style="-fx-padding: 15;">
                    <Label styleClass="section-title" text="User Profile" />
                    <VBox fx:id="profileInfo" spacing="10.0">
                        <Label text="Profile information will be displayed here" />
                    </VBox>
                </VBox>
            </Tab>
        </TabPane>
    </center>
</BorderPane>'''

main_css = '''/* General Styles */
.root {
    -fx-font-family: "Segoe UI", "Helvetica", sans-serif;
    -fx-font-size: 12;
}

/* Labels */
.title {
    -fx-font-size: 24;
    -fx-font-weight: bold;
    -fx-text-fill: #1a1a1a;
}

.section-title {
    -fx-font-size: 18;
    -fx-font-weight: bold;
    -fx-text-fill: #1a1a1a;
}

Label {
    -fx-text-fill: #333333;
}

/* Text Fields and Password Fields */
.text-field,
.password-field {
    -fx-padding: 8;
    -fx-border-color: #cccccc;
    -fx-border-width: 1;
    -fx-border-radius: 3;
    -fx-font-size: 12;
}

.text-field:focused,
.password-field:focused {
    -fx-border-color: #0078d4;
    -fx-border-width: 2;
}

/* ComboBox */
.combo-box {
    -fx-padding: 8;
    -fx-border-color: #cccccc;
    -fx-border-width: 1;
    -fx-border-radius: 3;
    -fx-font-size: 12;
}

.combo-box:focused {
    -fx-border-color: #0078d4;
    -fx-border-width: 2;
}

/* Buttons */
.button {
    -fx-padding: 10 20;
    -fx-font-size: 12;
    -fx-border-radius: 3;
    -fx-background-color: #0078d4;
    -fx-text-fill: white;
    -fx-cursor: hand;
}

.button:hover {
    -fx-background-color: #106ebe;
}

.button:pressed {
    -fx-background-color: #005a9e;
}

/* TabPane */
.tab-pane {
    -fx-padding: 10;
}

.tab {
    -fx-padding: 10 20;
}

.tab-header-background {
    -fx-background-color: #f0f0f0;
}

.tab:selected {
    -fx-background-color: white;
}

/* VBox and HBox */
.vbox,
.hbox {
    -fx-spacing: 10;
}

/* Scroll Pane */
.scroll-pane {
    -fx-padding: 10;
}

/* Error Messages */
.error {
    -fx-text-fill: #cc0000;
    -fx-font-weight: bold;
}

/* Success Messages */
.success {
    -fx-text-fill: #008000;
    -fx-font-weight: bold;
}'''

# Write files
files = {
    r"client\src\main\resources\com\auction\client\view\login.fxml": login_fxml,
    r"client\src\main\resources\com\auction\client\view\register.fxml": register_fxml,
    r"client\src\main\resources\com\auction\client\view\main.fxml": main_fxml,
    r"client\src\main\resources\com\auction\client\styles\main.css": main_css,
}

for file_path, content in files.items():
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"Created: {file_path}")

print("\nAll files and directories created successfully!")
