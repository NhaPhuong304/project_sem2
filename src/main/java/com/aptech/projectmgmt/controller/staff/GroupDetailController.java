package com.aptech.projectmgmt.controller.staff;

import com.aptech.projectmgmt.controller.AvatarCellController;
import com.aptech.projectmgmt.controller.TextPromptDialogController;
import com.aptech.projectmgmt.model.GroupMember;
import com.aptech.projectmgmt.model.MemberRole;
import com.aptech.projectmgmt.model.MemberStatus;
import com.aptech.projectmgmt.model.Student;
import com.aptech.projectmgmt.service.GroupService;
import com.aptech.projectmgmt.service.ProjectService;
import com.aptech.projectmgmt.util.AlertUtil;
import com.aptech.projectmgmt.util.SceneManager;
import com.aptech.projectmgmt.util.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.Optional;

public class GroupDetailController {

    @FXML
    private Label groupNameLabel;
    @FXML
    private Button addMemberBtn;
    @FXML
    private TableView<GroupMember> memberTable;
    @FXML
    private TableColumn<GroupMember, String> avatarColumn;
    @FXML
    private TableColumn<GroupMember, String> studentCodeColumn;
    @FXML
    private TableColumn<GroupMember, String> studentNameColumn;
    @FXML
    private TableColumn<GroupMember, String> roleColumn;
    @FXML
    private TableColumn<GroupMember, String> statusColumn;
    @FXML
    private TableColumn<GroupMember, Integer> abandonCountColumn;
    @FXML
    private TableColumn<GroupMember, Void> actionColumn;

    private final GroupService groupService = new GroupService();
    private final ProjectService projectService = new ProjectService();

    private int groupId;
    private int classId;
    private String groupName;
    private final ObservableList<GroupMember> memberList = FXCollections.observableArrayList();
    private boolean readOnlyMode;
    private boolean projectHasStarted = true;

    @FXML
    public void initialize() {
        setupTableColumns();
        memberTable.setItems(memberList);

        groupNameLabel.setOnMouseClicked(event -> {
            if (!readOnlyMode && event.getClickCount() == 2) {
                handleRenameGroup();
            }
        });

        updateAccessMode();
    }

    public void initData(int groupId) {
        this.groupId = groupId;
        loadMembers();
    }

    public void initData(int groupId, int projectId, int classId, String groupName) {
        this.groupId = groupId;
        this.classId = classId;
        this.groupName = groupName;

        com.aptech.projectmgmt.model.Project project = projectService.getProjectById(projectId);
        if (project != null && project.getStartDate() != null
                && project.getStartDate().isAfter(java.time.LocalDate.now())) {
            this.projectHasStarted = false;
        } else {
            this.projectHasStarted = true;
        }

        loadMembers();
        updateAccessMode();
    }

    public void setReadOnlyMode(boolean readOnlyMode) {
        this.readOnlyMode = readOnlyMode;
        updateAccessMode();
    }

    private void setupTableColumns() {
        avatarColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentPhotoUrl()));
        avatarColumn.setCellFactory(col -> new TableCell<>() {
            private final Parent avatarView;
            private final AvatarCellController controller;

            {
                try {
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource(SceneManager.AVATAR_CELL));
                    avatarView = loader.load();
                    controller = loader.getController();
                } catch (Exception ex) {
                    throw new IllegalStateException("Unable to load the group avatar cell", ex);
                }
            }

            @Override
            protected void updateItem(String photoUrl, boolean empty) {
                super.updateItem(photoUrl, empty);
                GroupMember member = empty || getTableRow() == null ? null : getTableRow().getItem();
                if (member == null) {
                    setGraphic(null);
                    return;
                }
                controller.setPhoto(photoUrl);
                setGraphic(avatarView);
            }
        });

        studentCodeColumn.setCellValueFactory(new PropertyValueFactory<>("studentCode"));
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("studentFullName"));

        roleColumn.setCellValueFactory(c -> {
            MemberRole r = c.getValue().getRole();
            return new SimpleStringProperty(r == MemberRole.LEADER ? "Truong nhom" : "Thanh vien");
        });

        statusColumn.setCellValueFactory(c -> {
            MemberStatus s = c.getValue().getStatus();
            return new SimpleStringProperty(s == MemberStatus.ACTIVE ? "Active" : "Removed.");
        });

        abandonCountColumn.setCellValueFactory(new PropertyValueFactory<>("abandonCount"));

        actionColumn.setCellFactory(col -> createActionCell());

        addMemberBtn.setOnAction(e -> handleAddMember());
    }

    private TableCell<GroupMember, Void> createActionCell() {
        return new TableCell<>() {
            private final Button primaryBtn = new Button();
            private final Button secondaryBtn = new Button();
            private final HBox box = new HBox(6, primaryBtn, secondaryBtn);

            {
                box.setAlignment(Pos.CENTER);

                primaryBtn.setPrefWidth(90);
                secondaryBtn.setPrefWidth(90);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                GroupMember member = empty || getTableRow() == null ? null : getTableRow().getItem();

                if (member == null || readOnlyMode) {
                    setGraphic(null);
                    return;
                }

                primaryBtn.setOnAction(null);
                secondaryBtn.setOnAction(null);

                if (member.getStatus() == MemberStatus.EXCLUDED) {
                    primaryBtn.setText("Reactivate.");
                    primaryBtn.setOnAction(e -> handleReactivate(member));
                    primaryBtn.setVisible(true);
                    primaryBtn.setManaged(true);

                    secondaryBtn.setVisible(false);
                    secondaryBtn.setManaged(false);
                } else if (member.getRole() == MemberRole.LEADER) {
                    primaryBtn.setText(projectHasStarted ? "Revoke access" : "Delete");
                    primaryBtn.setOnAction(e -> {
                        if (projectHasStarted) {
                            handleExclude(member);
                        } else {
                            handleHardDelete(member);
                        }
                    });
                    primaryBtn.setVisible(true);
                    primaryBtn.setManaged(true);

                    secondaryBtn.setVisible(false);
                    secondaryBtn.setManaged(false);
                } else {
                    primaryBtn.setText("Change Leader");
                    primaryBtn.setOnAction(e -> handleChangeLeader(member));
                    primaryBtn.setVisible(true);
                    primaryBtn.setManaged(true);

                    secondaryBtn.setText(projectHasStarted ? "Revoke access" : "Delete");
                    secondaryBtn.setOnAction(e -> {
                        if (projectHasStarted) {
                            handleExclude(member);
                        } else {
                            handleHardDelete(member);
                        }
                    });
                    secondaryBtn.setVisible(true);
                    secondaryBtn.setManaged(true);
                }

                setGraphic(box);
            }
        };
    }

    private void handleChangeLeader(GroupMember newLeader) {
        if (readOnlyMode) {
            AlertUtil.showError("Teacher accounts can only view group information");
            return;
        }

        if (newLeader.getStatus() != MemberStatus.ACTIVE) {
            AlertUtil.showError("Only active members can be selected");
            return;
        }

        if (newLeader.getRole() == MemberRole.LEADER) {
            AlertUtil.showError("This member is already the leader");
            return;
        }

        var currentStaff = SessionManager.getInstance().getCurrentStaff();
        if (currentStaff == null) {
            AlertUtil.showError("Khong xac dinh duoc staff");
            return;
        }

        if (!AlertUtil.showConfirm("Ban co chac muon doi " + newLeader.getStudentFullName() + " thanh leader moi?")) {
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                groupService.changeLeader(groupId, newLeader.getMemberId(), currentStaff.getStaffId());
                return null;
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            AlertUtil.showSuccess("Leader changed successfully");
            loadMembers();
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Loi doi leader: " + (ex != null ? ex.getMessage() : ""));
        }));

        new Thread(task).start();
    }

    private void handleReactivate(GroupMember member) {
        if (readOnlyMode) {
            AlertUtil.showError("Teacher accounts can only view group information");
            return;
        }

        var currentStaff = SessionManager.getInstance().getCurrentStaff();
        if (currentStaff == null) {
            AlertUtil.showError("Unable to identify the staff member");
            return;
        }

        if (!AlertUtil.showConfirm("Reactivate member " + member.getStudentFullName() + "?")) {
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                groupService.reactivateMember(member.getMemberId(), currentStaff.getStaffId());
                return null;
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            AlertUtil.showSuccess("Member reactivated successfully.");
            loadMembers();
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Error reactivating member.: " + (ex != null ? ex.getMessage() : ""));
        }));

        new Thread(task).start();
    }

    private void loadMembers() {
        Task<List<GroupMember>> task = new Task<>() {
            @Override
            protected List<GroupMember> call() {
                return groupService.getMembersByGroup(groupId);
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            memberList.setAll(task.getValue());
            updateGroupNameLabel();
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Error loading members: " + (ex != null ? ex.getMessage() : ""));
        }));

        new Thread(task).start();
    }

    private void updateGroupNameLabel() {
        String displayName = groupName != null && !groupName.isBlank() ? groupName : "Group";
        String suffix = readOnlyMode ? "" : " Double-click to rename";
        groupNameLabel.setText("Detail " + displayName + " (" + memberList.size() + " member)" + suffix);
    }

    private void handleRenameGroup() {
        Optional<String> renameResult = showRenameDialog();
        if (renameResult.isEmpty()) {
            return;
        }

        String newName = renameResult.get();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                groupService.renameGroup(groupId, newName);
                return null;
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            groupName = newName;
            updateGroupNameLabel();
            AlertUtil.showSuccess("Group name changed successfully");
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Group rename error: " + (ex != null ? ex.getMessage() : ""));
        }));

        new Thread(task).start();
    }

    private void handleExclude(GroupMember member) {
        if (readOnlyMode) {
            AlertUtil.showError("Tai khoan giao vien chi duoc xem thong tin nhom");
            return;
        }

        var currentStaff = SessionManager.getInstance().getCurrentStaff();
        if (currentStaff == null) {
            AlertUtil.showError("Unable to identify staff");
            return;
        }

        if (!AlertUtil.showConfirm("Confirm revoking participation for " + member.getStudentFullName() + "?")) {
            return;
        }

        Optional<String> reasonResult = showReasonDialog(member.getStudentFullName());
        if (reasonResult.isEmpty()) {
            return;
        }

        String reason = reasonResult.get();

        if (member.getRole() == MemberRole.LEADER && member.getStatus() == MemberStatus.ACTIVE) {
            List<GroupMember> candidates = groupService.getActiveMembersExcept(groupId, member.getMemberId());
            if (candidates.isEmpty()) {
                AlertUtil.showError("Cannot revoke the leader's participation because the group has no active member available to replace them.");
                return;
            }

            Optional<GroupMember> newLeaderResult = showChooseNewLeaderDialog(candidates);
            if (newLeaderResult.isEmpty()) {
                return;
            }

            GroupMember newLeader = newLeaderResult.get();

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    groupService.excludeLeaderAndTransfer(
                            member.getMemberId(),
                            newLeader.getMemberId(),
                            currentStaff.getStaffId(),
                            reason
                    );
                    return null;
                }
            };

            task.setOnSucceeded(e -> Platform.runLater(() -> {
                AlertUtil.showSuccess("Leader transferred and previous member participation revoked");
                loadMembers();
            }));

            task.setOnFailed(e -> Platform.runLater(() -> {
                Throwable ex = task.getException();
                AlertUtil.showError("Error: " + (ex != null ? ex.getMessage() : ""));
            }));

            new Thread(task).start();
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                groupService.excludeMember(member.getMemberId(), currentStaff.getStaffId(), reason);
                return null;
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            AlertUtil.showSuccess("Participation revoked");
            loadMembers();
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Error:" + (ex != null ? ex.getMessage() : ""));
        }));

        new Thread(task).start();
    }

    private void handleHardDelete(GroupMember member) {
        if (readOnlyMode) {
            AlertUtil.showError("Teacher accounts are only allowed to view group information");
            return;
        }

        if (!AlertUtil.showConfirm("Are you sure you want to delete " + member.getStudentFullName() + "from the group?")) {
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                groupService.removeMember(member.getMemberId());
                return null;
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            AlertUtil.showSuccess("Member removed from the group");
            loadMembers();
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Error removing member: " + (ex != null ? ex.getMessage() : ""));
        }));

        new Thread(task).start();
    }

    private Optional<GroupMember> showChooseNewLeaderDialog(List<GroupMember> candidates) {
        ComboBox<GroupMember> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(candidates);
        comboBox.getSelectionModel().selectFirst();

        comboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(GroupMember member) {
                return member == null ? "" : member.getStudentFullName() + " - " + member.getStudentCode();
            }

            @Override
            public GroupMember fromString(String string) {
                return null;
            }
        });

        Dialog<GroupMember> dialog = new Dialog<>();
        dialog.setTitle("Chose New Leader");
        dialog.setHeaderText("This member is the group leader. Please choose a new leader before revoking their participation.");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(comboBox);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return comboBox.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private void handleAddMember() {
        if (readOnlyMode) {
            AlertUtil.showError("Teacher accounts are only allowed to view group information");
            return;
        }

        Task<List<Student>> task = new Task<>() {
            @Override
            protected List<Student> call() {
                return groupService.getAvailableStudentsForClass(classId);
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> showAddMemberDialog(task.getValue())));

        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Unable to load the student list: " + (ex != null ? ex.getMessage() : ""));
        }));

        new Thread(task).start();
    }

    private void showAddMemberDialog(List<Student> students) {
        if (students == null || students.isEmpty()) {
            AlertUtil.showError("There are no students left in the class to add to the group.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.GROUP_MEMBER_CREATE_DIALOG));
            Parent content = loader.load();
            GroupMemberCreateDialogController controller = loader.getController();

            boolean hasLeader = groupService.hasActiveLeader(groupId);
            boolean isFirstMember = groupService.countActiveMembers(groupId) == 0;

            controller.setAvailableStudents(students);
            controller.configureRoleOptions(isFirstMember, hasLeader);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Add student to group");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().setPrefWidth(500);

            var result = dialog.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }

            Student selectedStudent = controller.getSelectedStudent();
            MemberRole selectedRole = controller.getSelectedRole();

            Task<Void> addTask = new Task<>() {
                @Override
                protected Void call() {
                    groupService.addMemberToGroup(groupId, selectedStudent.getStudentId(), selectedRole);
                    return null;
                }
            };

            addTask.setOnSucceeded(e -> Platform.runLater(() -> {
                AlertUtil.showSuccess("Student added to the group successfully");
                loadMembers();
            }));

            addTask.setOnFailed(e -> Platform.runLater(() -> {
                Throwable ex = addTask.getException();
                AlertUtil.showError("Error adding student to the group: " + (ex != null ? ex.getMessage() : ""));
            }));

            new Thread(addTask).start();
        } catch (Exception e) {
            AlertUtil.showError("Unable to open the add-student-to-group form: " + e.getMessage());
        }
    }

    private Optional<String> showReasonDialog(String studentName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.TEXT_PROMPT_DIALOG));
            Parent content = loader.load();
            TextPromptDialogController controller = loader.getController();
            controller.initData("Reason for revoking participation", "Enter the reason for revoking participation of " + studentName, "");

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Revoke participation");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().setPrefWidth(520);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return Optional.empty();
            }

            String reason = controller.getContent();
            if (reason.isBlank()) {
                AlertUtil.showError("Vui long nhap ly do huy quyen tham gia");
                return Optional.empty();
            }

            return Optional.of(reason);
        } catch (Exception ex) {
            AlertUtil.showError("Unable to open the reason input form: " + ex.getMessage());
            return Optional.empty();
        }
    }

    private Optional<String> showRenameDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.TEXT_PROMPT_DIALOG));
            Parent content = loader.load();
            TextPromptDialogController controller = loader.getController();
            controller.initData("Rename group", "Enter the new group name", groupName);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Rename group");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().setPrefWidth(520);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return Optional.empty();
            }

            String name = controller.getContent();
            if (name.isBlank()) {
                AlertUtil.showError("Group name cannot be empty");
                return Optional.empty();
            }

            return Optional.of(name);
        } catch (Exception ex) {
            AlertUtil.showError("Unable to open the rename-group form: " + ex.getMessage());
            return Optional.empty();
        }
    }

    private void updateAccessMode() {
        if (addMemberBtn != null) {
            boolean canAdd = !readOnlyMode && !projectHasStarted;
            addMemberBtn.setVisible(canAdd);
            addMemberBtn.setManaged(canAdd);
        }

        if (actionColumn != null) {
            actionColumn.setVisible(!readOnlyMode);
        }

        if (groupNameLabel != null) {
            updateGroupNameLabel();
        }
    }
}