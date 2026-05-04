package com.aptech.projectmgmt.controller;

import com.aptech.projectmgmt.model.Account;
import com.aptech.projectmgmt.util.ChatbotUiContextUtil;
import com.aptech.projectmgmt.util.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class AIChatBotController {
    private static final String CHATBOT_PROXY_URL =
            "https://script.google.com/macros/s/AKfycby7aLkD_Cd9J6RsOz0L6oZON5PoJaYhH3fQBZW4_X157uHK_2ugr6CUKeWFJJaN6IzT/exec";

    @FXML private Button chatbotBtn;
    @FXML private VBox chatWindow;
    @FXML private ListView<String> chatListView;
    @FXML private TextField inputField;

    private int currentUserRole = 2;

    @FXML
    public void initialize() {
        Account currentAcc = SessionManager.getInstance().getCurrentAccount();
        if (currentAcc != null && currentAcc.getRole() != null) {
            currentUserRole = currentAcc.getRole().getValue();
        }

        configureChatListView();

        TranslateTransition transition = new TranslateTransition(Duration.millis(1200), chatbotBtn);
        transition.setByY(-15);
        transition.setAutoReverse(true);
        transition.setCycleCount(TranslateTransition.INDEFINITE);
        transition.play();

        chatListView.getItems().add("AI: " + getWelcomeMessageForRole(currentUserRole));
    }

    @FXML
    private void toggleChat() {
        chatWindow.setVisible(!chatWindow.isVisible());
        if (chatWindow.isVisible()) {
            inputField.requestFocus();
        }
    }

    @FXML
    private void sendMessage() {
        String userText = inputField.getText().trim();
        if (userText.isEmpty()) {
            return;
        }

        chatListView.getItems().add("You: " + userText);
        inputField.clear();
        chatListView.scrollTo(chatListView.getItems().size() - 1);

        int placeholderIndex = addAiPlaceholder();
        callGeminiApiAsync(userText, placeholderIndex);
    }

    private int addAiPlaceholder() {
        chatListView.getItems().add("AI: Thinking...");
        chatListView.scrollTo(chatListView.getItems().size() - 1);
        return chatListView.getItems().size() - 1;
    }

    private void callGeminiApiAsync(String userMessage, int placeholderIndex) {
        String rolePrompt = getSystemPromptForRole(currentUserRole);
        String uiContext = ChatbotUiContextUtil.buildContextSummary(chatListView.getScene());
        String requestBody = String.format("""
            {
              "prompt": "%s",
              "rolePrompt": "%s",
              "uiContext": "%s"
            }
            """, escapeJson(userMessage), escapeJson(rolePrompt), escapeJson(uiContext));

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CHATBOT_PROXY_URL))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return parseGeminiResponse(response.body());
            } catch (Exception e) {
                e.printStackTrace();
                return "An error occurred while connecting to the chatbot.";
            }
        }).thenAccept(botResponse -> Platform.runLater(() ->
                animateAiResponse(placeholderIndex, "AI: " + botResponse)));
    }

    private void configureChatListView() {
        chatListView.setFixedCellSize(-1);
        chatListView.setCellFactory(listView -> new ListCell<>() {
            private final StackPane bubble = new StackPane();

            {
                bubble.setStyle("-fx-padding: 8 10; -fx-background-radius: 10;");
                setPrefWidth(0);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                bubble.getChildren().setAll(createFormattedMessage(item, Math.max(220, listView.getWidth() - 30)));
                setGraphic(bubble);
            }
        });
    }

    private TextFlow createFormattedMessage(String message, double width) {
        TextFlow textFlow = new TextFlow();
        textFlow.setPrefWidth(width);
        textFlow.setMaxWidth(width);
        textFlow.setLineSpacing(2);

        boolean bold = false;
        String[] parts = message.split("\\*\\*", -1);
        for (String part : parts) {
            if (!part.isEmpty()) {
                Text text = new Text(part);
                text.setFont(Font.font("System", bold ? FontWeight.BOLD : FontWeight.NORMAL, 14));
                textFlow.getChildren().add(text);
            }
            bold = !bold;
        }
        return textFlow;
    }

    private void animateAiResponse(int index, String fullMessage) {
        if (index < 0 || index >= chatListView.getItems().size()) {
            chatListView.getItems().add(fullMessage);
            chatListView.scrollTo(chatListView.getItems().size() - 1);
            return;
        }

        chatListView.getItems().set(index, "AI:");
        final int[] cursor = {3};
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(10), event -> {
            cursor[0] = Math.min(cursor[0] + 3, fullMessage.length());
            chatListView.getItems().set(index, fullMessage.substring(0, cursor[0]));
            chatListView.scrollTo(index);
        }));
        timeline.setCycleCount(Math.max(1, (int) Math.ceil((fullMessage.length() - 3) / 3.0)));
        timeline.play();
    }

    private String getSystemPromptForRole(int role) {
        return switch (role) {
            case 1 -> """
                    You are an AI assistant for the Admin role in the Aptech student project management system.
                    Your main responsibility is to support system operations, account management, staff and teacher coordination, class administration, and process stability.
                    Prioritize topics such as permissions, account activation/deactivation, password resets, class creation workflows, staff assignment, teacher creation, and data control.
                    When responding:
                    - Be concise, clear, and use steps when giving instructions.
                    - Prioritize an administrative and risk-control perspective.
                    - If the user asks about technical issues, security, permissions, or system logic, answer specifically.
                    - Do not invent data that is supposedly in the database. If information is missing, clearly say you are responding based on the general system description.
                    - If the question is about student projects, frame it in terms of administrative impact: which class, which teacher, which staff member, and which status needs attention.
                    - The UI context that accompanies each message is real application state. Use the current role, current screen, other available screens for that role, and visible controls to guide the user step by step.
                    - Only guide the user through buttons and actions that are actually visible in the provided UI context.
                    - Do not treat the whole database or whole application as one flat area. Distinguish between account administration, class management, project operations, and detail screens.
                    """;
            case 2 -> """
                    You are a friendly AI mentor for students in the Student Project system.
                    You help students work on group projects, divide tasks, track deadlines, submit reports, communicate with teachers, and handle delayed tasks.
                    When responding:
                    - Use a supportive and easy-to-understand tone.
                    - Prioritize topics such as project planning, sensible task assignment, team coordination, progress reporting, asking teachers for help, and avoiding missed deadlines.
                    - If the user is struggling with a task, suggest specific actions in priority order.
                    - Encourage initiative, honesty about progress, and respect for team collaboration rules.
                    - Do not pretend you can read real task data from the system unless the user provides it.
                    - The UI context that accompanies each message is real application state. Use the current role, current screen, other available student screens, and visible controls to explain where to click next.
                    - If a button is hidden or disabled in the UI context, explain that clearly instead of suggesting impossible steps.
                    - The user may ask about a screen other than the current one, so use the role screen catalog when answering navigation questions.
                    """;
            case 3 -> """
                    You are an AI assistant for teachers supervising student projects.
                    You help monitor team progress, evaluate tasks, provide professional feedback, and remind students to stay aligned with project goals and deadlines.
                    When responding:
                    - Be professional, respectful, and concise.
                    - Prioritize topics such as progress evaluation, deadline risk detection, task assignment, work review, and student status updates.
                    - If the user asks how to handle a delayed group, provide a structured action plan: review tasks, identify the responsible member, set an update checkpoint, and send reminders.
                    - If academic quality is involved, provide clear and actionable criteria.
                    - Do not claim to know real grades, task history, or actual system data unless it has been provided.
                    - The UI context that accompanies each message is real application state. Use the current screen, the teacher screen catalog, and visible controls when giving navigation or workflow guidance.
                    - The user may ask about another teacher workflow even when standing on a different screen.
                    """;
            case 4 -> """
                    You are an AI assistant for staff members in the student project management system.
                    You help organize classes, arrange groups, manage student lists, coordinate with teachers, and track administrative issues related to projects.
                    When responding:
                    - Be clear, actionable, and process-oriented.
                    - Prioritize topics such as class creation, class transfers, student enrollment, group assignment, teacher assignment, and deadline reminders.
                    - If there is a conflict or a class/group change, suggest a fair and traceable handling approach.
                    - If the question is about tasks, focus on coordination and reminders rather than academic evaluation.
                    - Do not add system facts that the user has not provided.
                    - The UI context that accompanies each message is real application state. Use the current screen, the staff screen catalog, and visible controls to tell the user which action is available now and where related workflows live.
                    - Do not treat all data areas as one flat space. Keep class, student, group, project, submission, and task workflows separate.
                    """;
            default -> """
                    You are a friendly AI assistant for the student project management system.
                    Reply clearly, stay on topic, and do not invent missing data.
                    Use the supplied UI context when it is available.
                    The current screen may not be the same as the screen the user is asking about.
                    """;
        };
    }

    private String getWelcomeMessageForRole(int role) {
        return switch (role) {
            case 1 -> "Hello Admin. I can help with accounts, classes, permissions, and project operations.";
            case 2 -> "Hello. I can help you split tasks, plan the project, manage delays, and prepare reports.";
            case 3 -> "Hello Teacher. I can help track team progress, review tasks, and suggest follow-up actions for students.";
            case 4 -> "Hello Staff. I can help with classes, group assignment, coordination, and project administration workflows.";
            default -> "Hello. How can I help you today?";
        };
    }

    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }

    private String parseGeminiResponse(String responseBody) {
        try {
            if (responseBody == null || responseBody.isBlank()) {
                return "The AI did not return any content.";
            }

            String trimmed = responseBody.trim();
            String lowerCaseBody = trimmed.toLowerCase();
            if (lowerCaseBody.startsWith("<!doctype html") || lowerCaseBody.startsWith("<html")) {
                return "The chatbot proxy returned HTML instead of JSON. Please check the Apps Script deployment and /exec access permissions.";
            }

            if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
                return trimmed;
            }

            String[] markers = {
                    "\"reply\": \"",
                    "\"reply\":\"",
                    "\"response\": \"",
                    "\"response\":\"",
                    "\"message\": \"",
                    "\"message\":\"",
                    "\"text\": \"",
                    "\"text\":\""
            };

            for (String marker : markers) {
                int startIndex = trimmed.indexOf(marker);
                if (startIndex == -1) {
                    continue;
                }

                startIndex += marker.length();
                int endIndex = findJsonStringEnd(trimmed, startIndex);
                return trimmed.substring(startIndex, endIndex)
                        .replace("\\n", "\n")
                        .replace("\\\"", "\"");
            }

            return trimmed;
        } catch (Exception e) {
            return "Could not parse the AI response.";
        }
    }

    private int findJsonStringEnd(String text, int startIndex) {
        boolean escaped = false;
        for (int i = startIndex; i < text.length(); i++) {
            char current = text.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (current == '\\') {
                escaped = true;
                continue;
            }
            if (current == '"') {
                return i;
            }
        }
        return text.length();
    }
}
