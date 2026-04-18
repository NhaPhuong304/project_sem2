package com.aptech.projectmgmt.controller;

import com.aptech.projectmgmt.model.Account;
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

    private int currentUserRole = 2; // Default role is Student

    @FXML
    public void initialize() {
        Account currentAcc = SessionManager.getInstance().getCurrentAccount();
        if (currentAcc != null && currentAcc.getRole() != null) {
            currentUserRole = currentAcc.getRole().getValue();
        }

        configureChatListView();

        TranslateTransition t = new TranslateTransition(Duration.millis(1200), chatbotBtn);
        t.setByY(-15);
        t.setAutoReverse(true);
        t.setCycleCount(TranslateTransition.INDEFINITE);
        t.play();

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

        chatListView.getItems().add("Ban: " + userText);
        inputField.clear();
        chatListView.scrollTo(chatListView.getItems().size() - 1);

        int placeholderIndex = addAiPlaceholder();
        callGeminiApiAsync(userText, placeholderIndex);
    }

    private int addAiPlaceholder() {
        chatListView.getItems().add("AI: Dang tra loi...");
        chatListView.scrollTo(chatListView.getItems().size() - 1);
        return chatListView.getItems().size() - 1;
    }

    private void callGeminiApiAsync(String userMessage, int placeholderIndex) {
        String rolePrompt = getSystemPromptForRole(currentUserRole);
        String requestBody = String.format("""
            {
              "prompt": "%s",
              "rolePrompt": "%s"
            }
            """, escapeJson(userMessage), escapeJson(rolePrompt));

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
                return "Da co loi khi ket noi chatbot.";
            }
        }).thenAccept(botResponse -> Platform.runLater(() -> {
            animateAiResponse(placeholderIndex, "AI: " + botResponse);
        }));
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
                    Ban la tro ly AI cho vai tro Admin trong he thong quan ly project sinh vien tai Aptech.
                    Nhiem vu chinh cua ban la ho tro van hanh he thong, quan ly tai khoan, quan ly giao vu, giao vien, lop hoc va tinh on dinh cua quy trinh.
                    Uu tien cac chu de: phan quyen, kich hoat/khoa tai khoan, reset mat khau, quy trinh tao lop, phan cong giao vu, tao giao vien va kiem soat du lieu.
                    Khi tra loi:
                    - Viet ngan gon, ro rang, chia thanh cac buoc neu huong dan thao tac.
                    - Uu tien goc nhin quan tri va kiem soat rui ro.
                    - Neu nguoi dung hoi van de ky thuat, bao mat, quyen truy cap hoac logic he thong thi tra loi cu the.
                    - Khong boi ra du lieu dang co trong database. Neu thieu thong tin thi noi ro ban dang dua tren mo ta chung cua he thong.
                    - Neu cau hoi lien quan den project sinh vien, hay quy ve anh huong quan tri: lop nao, giao vien nao, giao vu nao, trang thai nao can theo doi.
                    """;
            case 2 -> """
                    Ban la Mentor AI than thien cho Sinh vien trong he thong Student Project.
                    Ban ho tro sinh vien lam project theo nhom, chia task, theo doi deadline, nop bao cao, trao doi voi giao vien va xu ly cac tinh huong bi tre task.
                    Khi tra loi:
                    - Xung ho la Mentor va Ban.
                    - Giai thich de hieu, than thien, co tinh huong thuc te cua do an nhom.
                    - Uu tien cac chu de: len ke hoach project, chia task hop ly, phoi hop nhom, cach bao cao tien do, cach xin ho tro giao vien, cach tranh tre deadline.
                    - Neu Ban dang gap van de voi task, hay de xuat hanh dong cu the theo thu tu uu tien.
                    - Khuyen khich tinh chu dong, trung thuc ve tien do, va ton trong quy tac lam viec nhom.
                    - Khong gia vo nhu dang doc duoc du lieu task that trong he thong neu nguoi dung chua cung cap.
                    """;
            case 3 -> """
                    Ban la tro ly AI cho Giao vien huong dan project sinh vien.
                    Ban ho tro theo doi tien do nhom, danh gia task, dua nhan xet chuyen mon, nhac sinh vien bam sat muc tieu project va deadline.
                    Khi tra loi:
                    - Van phong trang trong, chuyen nghiep, ngan gon.
                    - Uu tien cac chu de: danh gia tien do, nhan dien rui ro tre han, cach giao task, cach review bai lam, cach nhac sinh vien cap nhat trang thai.
                    - Neu nguoi dung hoi cach xu ly mot nhom cham tien do, hay dua ra huong xu ly co thu tu: kiem tra task, xac dinh nguoi phu trach, hen moc cap nhat, gui nhac nho.
                    - Neu lien quan chat luong hoc thuat, hay dua ra tieu chi ro rang va co the ap dung duoc.
                    - Khong tu nhan biet diem so, lich su task hay du lieu thuc te neu chua duoc cung cap.
                    """;
            case 4 -> """
                    Ban la tro ly AI cho Giao vu trong he thong quan ly project sinh vien.
                    Ban ho tro to chuc lop, sap xep nhom, quan ly danh sach sinh vien, phoi hop voi giao vien, va theo doi van de hanh chinh lien quan den project.
                    Khi tra loi:
                    - Van phong ro rang, thao tac duoc ngay, nghieng ve quy trinh.
                    - Uu tien cac chu de: tao lop, chuyen lop, them sinh vien, chia nhom, phan cong giao vien huong dan, nhac nho quy dinh va deadline.
                    - Neu co xung dot hoac thay doi nhom/lop, hay dua ra cach xu ly theo huong cong bang va de theo doi.
                    - Neu cau hoi lien quan task, hay tap trung vao goc nhin dieu phoi va nhac viec, khong di sau vao danh gia hoc thuat nhu giao vien.
                    - Khong bo sung thong tin he thong neu nguoi dung chua cung cap.
                    """;
            default -> """
                    Ban la tro ly AI than thien cho he thong quan ly project sinh vien.
                    Hay tra loi ro rang, dung trong tam, khong boi ra du lieu khong co.
                    """;
        };
    }

    private String getWelcomeMessageForRole(int role) {
        return switch (role) {
            case 1 -> "Xin chao Admin. Toi co the ho tro quan ly tai khoan, lop hoc, phan quyen va quy trinh van hanh project.";
            case 2 -> "Xin chao Ban. Mentor co the giup Ban chia task, len ke hoach project, xu ly tre deadline va chuan bi bao cao.";
            case 3 -> "Xin chao Giang vien. Toi co the ho tro theo doi tien do nhom, danh gia task va goi y cach nhac sinh vien.";
            case 4 -> "Xin chao Giao vu. Toi co the ho tro ve lop hoc, chia nhom, phan cong va cac quy trinh dieu phoi project.";
            default -> "Xin chao! Toi co the giup gi cho ban hom nay?";
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
                return "AI chua tra ve noi dung.";
            }

            String trimmed = responseBody.trim();
            String lowerCaseBody = trimmed.toLowerCase();
            if (lowerCaseBody.startsWith("<!doctype html") || lowerCaseBody.startsWith("<html")) {
                return "Chatbot proxy dang tra ve HTML thay vi JSON. Hay kiem tra lai Apps Script deployment va quyen truy cap /exec.";
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
            return "Khong the doc phan hoi tu AI.";
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
