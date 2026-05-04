package com.aptech.projectmgmt.util;

import com.aptech.projectmgmt.model.Account;
import com.aptech.projectmgmt.model.UserRole;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.StackPane;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChatbotUiContextUtil {

    private static final Set<String> CHATBOT_CONTROL_IDS = Set.of(
            "chatbotBtn", "chatWindow", "chatListView", "inputField", "robotIcon"
    );

    private static final Map<String, ScreenMetadata> SCREEN_CACHE = new HashMap<>();
    private static DatabaseSchemaSummary DATABASE_SCHEMA_CACHE;

    private ChatbotUiContextUtil() {
    }

    public static void updateCurrentScreen(String fxmlPath, Object controller) {
        SessionManager session = SessionManager.getInstance();
        session.setCurrentScreenFxmlPath(fxmlPath);
        session.setCurrentScreenController(controller != null ? controller.getClass().getSimpleName() : null);
        session.pushRecentScreenFxmlPath(fxmlPath);
    }

    public static String buildContextSummary(Scene scene) {
        SessionManager session = SessionManager.getInstance();
        Account account = session.getCurrentAccount();
        UserRole role = account != null ? account.getRole() : null;
        String currentScreen = session.getCurrentScreenFxmlPath();
        String currentController = session.getCurrentScreenController();

        StringBuilder context = new StringBuilder();
        context.append("Current role: ").append(role != null ? role.name() : "UNKNOWN").append('\n');
        context.append("Current screen: ").append(currentScreen != null ? currentScreen : "Unknown").append('\n');
        context.append("Current controller: ").append(currentController != null ? currentController : "Unknown").append('\n');
        context.append("Important: the user may ask about a different screen or workflow, not only the current one.\n");

        ScreenMetadata currentMetadata = loadScreenMetadata(currentScreen);
        if (currentMetadata != null) {
            appendScreenSummary(context, "Current screen summary", currentMetadata);
        }

        appendRecentScreens(context, session.getRecentScreenFxmlPaths(), currentScreen);
        appendRoleScreenCatalog(context, role, currentScreen);
        appendDatabaseSchemaSummary(context);

        if (scene != null && scene.getRoot() != null) {
            appendRuntimeControls(context, scene.getRoot());
        }

        return context.toString().trim();
    }

    private static void appendDatabaseSchemaSummary(StringBuilder context) {
        DatabaseSchemaSummary schemaSummary = loadDatabaseSchemaSummary();
        if (schemaSummary == null) {
            return;
        }

        context.append("Database schema summary:\n");
        context.append("- Tables available: ").append(String.join(", ", schemaSummary.tableNames().stream().limit(20).toList())).append('\n');
        context.append("- Important rule: only refer to tables and columns listed here; if something is not in the schema summary, say it is unavailable or unknown.\n");

        schemaSummary.tables().stream()
                .limit(16)
                .forEach(table -> {
                    context.append("- ").append(table.name());
                    if (!table.columns().isEmpty()) {
                        context.append(" columns=").append(String.join(", ", table.columns().stream().limit(8).toList()));
                    }
                    if (!table.references().isEmpty()) {
                        context.append(" references=").append(String.join(", ", table.references().stream().limit(4).toList()));
                    }
                    context.append('\n');
                });
    }

    private static void appendRecentScreens(StringBuilder context, List<String> recentScreens, String currentScreen) {
        List<String> previousScreens = recentScreens.stream()
                .filter(path -> path != null && !path.equals(currentScreen))
                .toList();
        if (previousScreens.isEmpty()) {
            return;
        }

        context.append("Recent screens in this session:\n");
        previousScreens.stream()
                .limit(4)
                .map(ChatbotUiContextUtil::loadScreenMetadata)
                .filter(metadata -> metadata != null)
                .forEach(metadata -> context.append("- ")
                        .append(metadata.displayName())
                        .append(" (").append(metadata.fxmlPath()).append(")\n"));
    }

    private static void appendRoleScreenCatalog(StringBuilder context, UserRole role, String currentScreen) {
        List<String> screenPaths = getRoleScreenPaths(role);
        if (screenPaths.isEmpty()) {
            return;
        }

        context.append("Other screens available for this role:\n");
        screenPaths.stream()
                .filter(path -> !path.equals(currentScreen))
                .limit(8)
                .map(ChatbotUiContextUtil::loadScreenMetadata)
                .filter(metadata -> metadata != null)
                .forEach(metadata -> {
                    context.append("- ")
                            .append(metadata.displayName())
                            .append(" (").append(metadata.fxmlPath()).append(")");
                    if (!metadata.keyActionLabels().isEmpty()) {
                        context.append(" actions=").append(String.join(", ", metadata.keyActionLabels().stream().limit(3).toList()));
                    }
                    if (!metadata.keyTexts().isEmpty()) {
                        context.append(" texts=").append(String.join(", ", metadata.keyTexts().stream().limit(2).toList()));
                    }
                    context.append('\n');
                });
    }

    private static void appendScreenSummary(StringBuilder context, String heading, ScreenMetadata metadata) {
        context.append(heading).append(":\n");
        context.append("- Name: ").append(metadata.displayName()).append('\n');
        context.append("- Path: ").append(metadata.fxmlPath()).append('\n');
        if (metadata.controllerName() != null && !metadata.controllerName().isBlank()) {
            context.append("- Controller: ").append(metadata.controllerName()).append('\n');
        }
        if (!metadata.keyTexts().isEmpty()) {
            context.append("- Key UI text: ").append(String.join(", ", metadata.keyTexts().stream().limit(5).toList())).append('\n');
        }
        if (!metadata.keyActionLabels().isEmpty()) {
            context.append("- Main actions: ").append(String.join(", ", metadata.keyActionLabels().stream().limit(6).toList())).append('\n');
        }
        if (!metadata.tableColumns().isEmpty()) {
            context.append("- Table columns: ").append(String.join(", ", metadata.tableColumns().stream().limit(6).toList())).append('\n');
        }
        appendDeclaredActions(context, metadata);
    }

    private static void appendDeclaredActions(StringBuilder context, ScreenMetadata metadata) {
        if (metadata.controls().isEmpty()) {
            return;
        }

        context.append("- Declared controls/actions:\n");
        metadata.controls().stream()
                .filter(control -> control.actionHandler() != null || isActionControl(control.type()))
                .limit(12)
                .forEach(control -> context.append("  * ")
                        .append(control.id() != null ? control.id() : control.type())
                        .append(" [").append(control.type()).append(']')
                        .append(control.label() != null && !control.label().isBlank() ? " label=\"" + control.label() + "\"" : "")
                        .append(control.actionHandler() != null ? " action=" + control.actionHandler() : "")
                        .append('\n'));
    }

    private static void appendRuntimeControls(StringBuilder context, Parent root) {
        List<RuntimeControlInfo> visibleControls = new ArrayList<>();
        collectVisibleControls(root, visibleControls, new HashSet<>());

        if (visibleControls.isEmpty()) {
            return;
        }

        visibleControls.sort(Comparator
                .comparing(RuntimeControlInfo::type)
                .thenComparing(info -> info.id() != null ? info.id() : "")
                .thenComparing(info -> info.label() != null ? info.label() : ""));

        context.append("Visible UI controls right now:\n");
        visibleControls.stream()
                .limit(30)
                .forEach(control -> context.append("- ")
                        .append(control.id() != null ? control.id() : control.type())
                        .append(" [").append(control.type()).append(']')
                        .append(control.label() != null && !control.label().isBlank() ? " text=\"" + control.label() + "\"" : "")
                        .append(control.prompt() != null && !control.prompt().isBlank() ? " prompt=\"" + control.prompt() + "\"" : "")
                        .append(control.selected() != null && !control.selected().isBlank() ? " selected=\"" + control.selected() + "\"" : "")
                        .append(control.disabled() ? " disabled=true" : " disabled=false")
                        .append('\n'));
    }

    private static void collectVisibleControls(Node node, List<RuntimeControlInfo> collector, Set<Node> visited) {
        if (node == null || visited.contains(node)) {
            return;
        }
        visited.add(node);

        if (!node.isVisible()) {
            return;
        }

        RuntimeControlInfo info = describeNode(node);
        if (info != null) {
            collector.add(info);
        }

        if (node instanceof TabPane tabPane) {
            Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
            String selectedTabText = selectedTab != null ? selectedTab.getText() : null;
            collector.add(new RuntimeControlInfo(tabPane.getId(), "TabPane", null, null, selectedTabText, tabPane.isDisable()));
            if (selectedTab != null) {
                collectVisibleControls(selectedTab.getContent(), collector, visited);
            }
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectVisibleControls(child, collector, visited);
            }
        }
    }

    private static RuntimeControlInfo describeNode(Node node) {
        String id = node.getId();
        if (id != null && CHATBOT_CONTROL_IDS.contains(id)) {
            return null;
        }

        if (node instanceof ButtonBase button) {
            return new RuntimeControlInfo(id, button.getClass().getSimpleName(), normalize(button.getText()), null, null, button.isDisable());
        }
        if (node instanceof TextInputControl input) {
            return new RuntimeControlInfo(id, input.getClass().getSimpleName(), null, normalize(input.getPromptText()), null, input.isDisable());
        }
        if (node instanceof ComboBoxBase<?> comboBox) {
            Object value = comboBox.getValue();
            return new RuntimeControlInfo(id, comboBox.getClass().getSimpleName(), null, null,
                    value != null ? normalize(String.valueOf(value)) : null, comboBox.isDisable());
        }
        if (node instanceof TableView<?> tableView) {
            return new RuntimeControlInfo(id, "TableView", summarizeTableColumns(tableView), null,
                    tableView.getSelectionModel().getSelectedItem() != null ? "row-selected" : "no-selection",
                    tableView.isDisable());
        }
        if (node instanceof ListView<?> listView) {
            return new RuntimeControlInfo(id, "ListView", null, null,
                    listView.getSelectionModel().getSelectedItem() != null ? "item-selected" : "no-selection",
                    listView.isDisable());
        }
        if (node instanceof Label label) {
            String text = normalize(label.getText());
            if (text == null || text.isBlank()) {
                return null;
            }
            return new RuntimeControlInfo(id, "Label", text, null, null, label.isDisable());
        }
        if (node instanceof StackPane && "contentArea".equals(id)) {
            return new RuntimeControlInfo(id, "StackPane", "content container", null, null, node.isDisable());
        }
        if (node instanceof Control control) {
            return new RuntimeControlInfo(id, control.getClass().getSimpleName(), null, null, null, control.isDisable());
        }
        return null;
    }

    private static String summarizeTableColumns(TableView<?> tableView) {
        List<String> columnNames = new ArrayList<>();
        for (TableColumn<?, ?> column : tableView.getColumns()) {
            String text = normalize(column.getText());
            if (text != null) {
                columnNames.add(text);
            }
        }
        return columnNames.isEmpty() ? null : String.join(", ", columnNames.stream().limit(5).toList());
    }

    private static List<String> getRoleScreenPaths(UserRole role) {
        if (role == null) {
            return List.of();
        }
        return switch (role) {
            case ADMIN -> List.of(
                    SceneManager.ADMIN_OVERVIEW,
                    SceneManager.CLASS_LIST,
                    SceneManager.ADMIN_TEACHER_LIST,
                    SceneManager.ADMIN_STAFF_LIST,
                    SceneManager.ADMIN_CLASS_CREATE_DIALOG,
                    SceneManager.ADMIN_TEACHER_CREATE_DIALOG,
                    SceneManager.ADMIN_STAFF_CREATE_DIALOG,
                    SceneManager.STUDENT_LIST
            );
            case STUDENT -> List.of(
                    SceneManager.STUDENT_OVERVIEW,
                    SceneManager.MY_PROJECT_LIST,
                    "/fxml/student/student-project-detail.fxml",
                    SceneManager.MY_TASK_LIST,
                    SceneManager.TASK_CREATE_DIALOG,
                    SceneManager.STUDENT_SUBMISSION,
                    SceneManager.MESSAGE_INBOX
            );
            case TEACHER -> List.of(
                    SceneManager.TEACHER_OVERVIEW,
                    SceneManager.CLASS_LIST,
                    SceneManager.PROJECT_LIST,
                    "/fxml/teacher/teacher-inbox.fxml",
                    SceneManager.PROJECT_DETAIL,
                    SceneManager.TASK_LIST
            );
            case STAFF -> List.of(
                    SceneManager.STAFF_OVERVIEW,
                    SceneManager.STUDENT_MANAGEMENT,
                    SceneManager.CLASS_LIST,
                    SceneManager.STUDENT_LIST,
                    SceneManager.PROJECT_LIST,
                    SceneManager.PROJECT_DETAIL,
                    SceneManager.GROUP_DETAIL,
                    SceneManager.TASK_LIST,
                    SceneManager.SUBMISSION_REQUEST_LIST,
                    SceneManager.TEACHER_LIST
            );
        };
    }

    private static ScreenMetadata loadScreenMetadata(String fxmlPath) {
        if (fxmlPath == null || fxmlPath.isBlank()) {
            return null;
        }
        if (SCREEN_CACHE.containsKey(fxmlPath)) {
            return SCREEN_CACHE.get(fxmlPath);
        }

        ScreenMetadata metadata = parseFxmlMetadata(fxmlPath);
        SCREEN_CACHE.put(fxmlPath, metadata);
        return metadata;
    }

    private static DatabaseSchemaSummary loadDatabaseSchemaSummary() {
        if (DATABASE_SCHEMA_CACHE != null) {
            return DATABASE_SCHEMA_CACHE;
        }

        try (InputStream inputStream = ChatbotUiContextUtil.class.getResourceAsStream("/db/database.sql")) {
            if (inputStream == null) {
                return null;
            }

            String sql = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, DbTableMetadata> tables = parseTables(sql);
            Map<String, List<String>> references = parseReferences(sql);

            List<DbTableMetadata> tableMetadataList = new ArrayList<>();
            for (Map.Entry<String, DbTableMetadata> entry : tables.entrySet()) {
                List<String> tableReferences = references.getOrDefault(entry.getKey(), List.of());
                tableMetadataList.add(new DbTableMetadata(entry.getKey(), entry.getValue().columns(), tableReferences));
            }

            DATABASE_SCHEMA_CACHE = new DatabaseSchemaSummary(new ArrayList<>(tables.keySet()), tableMetadataList);
            return DATABASE_SCHEMA_CACHE;
        } catch (Exception e) {
            return null;
        }
    }

    private static Map<String, DbTableMetadata> parseTables(String sql) {
        Map<String, DbTableMetadata> tables = new LinkedHashMap<>();
        Pattern tablePattern = Pattern.compile(
                "CREATE TABLE \\[dbo\\]\\.\\[(.+?)]\\((.*?)\\) ON \\[PRIMARY]",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        Matcher matcher = tablePattern.matcher(sql);
        while (matcher.find()) {
            String tableName = normalize(matcher.group(1));
            String body = matcher.group(2);
            List<String> columns = parseColumnNames(body);
            if (tableName != null) {
                tables.put(tableName, new DbTableMetadata(tableName, columns, List.of()));
            }
        }
        return tables;
    }

    private static List<String> parseColumnNames(String tableBody) {
        List<String> columns = new ArrayList<>();
        Pattern columnPattern = Pattern.compile("^\\s*\\[(.+?)]\\s+\\[", Pattern.MULTILINE);
        Matcher matcher = columnPattern.matcher(tableBody);
        while (matcher.find()) {
            String columnName = normalize(matcher.group(1));
            if (columnName != null && !columns.contains(columnName)) {
                columns.add(columnName);
            }
        }
        return columns;
    }

    private static Map<String, List<String>> parseReferences(String sql) {
        Map<String, List<String>> references = new LinkedHashMap<>();
        Pattern referencePattern = Pattern.compile(
                "ALTER TABLE \\[dbo\\]\\.\\[(.+?)]\\s+WITH CHECK ADD FOREIGN KEY\\(\\[(.+?)]\\)\\s*REFERENCES \\[dbo\\]\\.\\[(.+?)]\\s*\\(\\[(.+?)]\\)",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = referencePattern.matcher(sql.replace("\r", ""));
        while (matcher.find()) {
            String tableName = normalize(matcher.group(1));
            String columnName = normalize(matcher.group(2));
            String referencedTable = normalize(matcher.group(3));
            String referencedColumn = normalize(matcher.group(4));
            if (tableName == null || columnName == null || referencedTable == null || referencedColumn == null) {
                continue;
            }

            references.computeIfAbsent(tableName, ignored -> new ArrayList<>());
            String relation = columnName + "->" + referencedTable + "." + referencedColumn;
            if (!references.get(tableName).contains(relation)) {
                references.get(tableName).add(relation);
            }
        }
        return references;
    }

    private static ScreenMetadata parseFxmlMetadata(String fxmlPath) {
        try (InputStream inputStream = SceneManager.class.getResourceAsStream(fxmlPath)) {
            if (inputStream == null) {
                return null;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            Document document = factory.newDocumentBuilder().parse(inputStream);
            Element root = document.getDocumentElement();

            String controllerName = attribute(root, "fx:controller");
            Map<String, ScreenControlMetadata> controls = new LinkedHashMap<>();
            List<String> keyTexts = new ArrayList<>();
            List<String> actionLabels = new ArrayList<>();
            List<String> tableColumns = new ArrayList<>();

            NodeList elements = document.getElementsByTagName("*");
            for (int i = 0; i < elements.getLength(); i++) {
                org.w3c.dom.Node item = elements.item(i);
                if (!(item instanceof Element element)) {
                    continue;
                }

                String type = normalizeElementName(element.getTagName());
                String id = attribute(element, "fx:id");
                String label = firstNonBlank(attribute(element, "text"), attribute(element, "promptText"));
                String actionHandler = attribute(element, "onAction");
                String normalizedLabel = normalize(label);

                if ("Label".equals(type) || "Tab".equals(type)) {
                    addDistinct(keyTexts, normalizedLabel, 6);
                }
                if ("Button".equals(type)) {
                    addDistinct(actionLabels, normalizedLabel, 8);
                }
                if ("TableColumn".equals(type)) {
                    addDistinct(tableColumns, normalizedLabel, 8);
                }

                if (id == null && actionHandler == null && !isActionControl(type)) {
                    continue;
                }

                String key = id != null ? id : type + ":" + controls.size();
                controls.putIfAbsent(key, new ScreenControlMetadata(id, type, normalizedLabel, normalize(actionHandler)));
            }

            String displayName = firstNonBlank(firstItem(keyTexts), deriveDisplayName(fxmlPath));
            return new ScreenMetadata(
                    fxmlPath,
                    displayName,
                    controllerName,
                    new ArrayList<>(controls.values()),
                    keyTexts,
                    actionLabels,
                    tableColumns
            );
        } catch (Exception e) {
            return null;
        }
    }

    private static void addDistinct(List<String> collector, String value, int maxSize) {
        if (value == null || value.isBlank() || collector.contains(value) || collector.size() >= maxSize) {
            return;
        }
        collector.add(value);
    }

    private static String firstItem(List<String> values) {
        return values.isEmpty() ? null : values.get(0);
    }

    private static String deriveDisplayName(String fxmlPath) {
        String fileName = fxmlPath;
        int slashIndex = fileName.lastIndexOf('/');
        if (slashIndex >= 0) {
            fileName = fileName.substring(slashIndex + 1);
        }
        if (fileName.endsWith(".fxml")) {
            fileName = fileName.substring(0, fileName.length() - 5);
        }

        String[] tokens = fileName.split("[-_]");
        StringBuilder displayName = new StringBuilder();
        for (String token : tokens) {
            if (token.isBlank()) {
                continue;
            }
            if (!displayName.isEmpty()) {
                displayName.append(' ');
            }
            displayName.append(token.substring(0, 1).toUpperCase(Locale.ROOT))
                    .append(token.substring(1));
        }
        return displayName.toString();
    }

    private static String normalizeElementName(String tagName) {
        int index = tagName.indexOf(':');
        return index >= 0 ? tagName.substring(index + 1) : tagName;
    }

    private static String attribute(Element element, String name) {
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            org.w3c.dom.Node node = attributes.item(i);
            if (name.equals(node.getNodeName())) {
                return normalize(node.getNodeValue());
            }
        }
        return null;
    }

    private static boolean isActionControl(String type) {
        return "Button".equals(type) || "Hyperlink".equals(type) || "CheckBox".equals(type);
    }

    private static String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed.replace('\n', ' ');
    }

    private record ScreenMetadata(
            String fxmlPath,
            String displayName,
            String controllerName,
            List<ScreenControlMetadata> controls,
            List<String> keyTexts,
            List<String> keyActionLabels,
            List<String> tableColumns
    ) {
    }

    private record ScreenControlMetadata(
            String id,
            String type,
            String label,
            String actionHandler
    ) {
    }

    private record RuntimeControlInfo(
            String id,
            String type,
            String label,
            String prompt,
            String selected,
            boolean disabled
    ) {
    }

    private record DatabaseSchemaSummary(
            List<String> tableNames,
            List<DbTableMetadata> tables
    ) {
    }

    private record DbTableMetadata(
            String name,
            List<String> columns,
            List<String> references
    ) {
    }
}
