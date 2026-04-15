=========================================================
PROJECT MANAGEMENT SYSTEM - APTECH SEMESTER 2
JavaFX 21 + SceneBuilder + SQL Server 2019 + Maven
=========================================================

1. GIOI THIEU
Project nay la desktop app quan ly du an sinh vien Aptech.
He thong hien tai ho tro 3 nhom nguoi dung:
- Admin (Giao vu)
- Teacher (Giao vien)
- Student (Sinh vien)

Chuc nang chinh:
- Dang nhap / OTP / doi mat khau lan dau
- Quan ly lop
- Quan ly sinh vien
- Quan ly giao vien
- Quan ly project
- Quan ly nhom
- Quan ly task
- Hop thu den cho sinh vien
- Upload / doi avatar
- Splash screen truoc khi vao login


=========================================================
2. CONG NGHE VA THU VIEN SU DUNG
=========================================================

2.1. Ngon ngu va UI
- Java 21
- JavaFX 21
- SceneBuilder

2.2. Database
- SQL Server 2019 hoac moi hon
- Port local mac dinh: 1433

2.3. Build tool
- Maven 3.9+

2.4. Thu vien Maven dang dung
Khai bao trong file:
- [pom.xml]

Danh sach:
- org.openjfx:javafx-controls:21
- org.openjfx:javafx-fxml:21
- org.openjfx:javafx-graphics:21
- com.microsoft.sqlserver:mssql-jdbc:12.6.1.jre11
- com.zaxxer:HikariCP:5.1.0
- com.sun.mail:jakarta.mail:2.0.1
- org.mindrot:jbcrypt:0.4
- org.slf4j:slf4j-simple:1.7.36


=========================================================
3. YEU CAU MOI TRUONG
=========================================================

Ban can cai:
- JDK 21
- Maven
- SQL Server 2019+
- Eclipse IDE for Java Developers
- SceneBuilder
- DBeaver hoac SQL Server Management Studio de chay file SQL

Khuyen nghi:
- Eclipse 2023-03 tro len
- SceneBuilder ban moi


=========================================================
4. CAU TRUC PROJECT
=========================================================

Thu muc quan trong:
- [src/main/java/com/aptech/projectmgmt](/Users/yennhn/eclipse-workspace/project-sem2/src/main/java/com/aptech/projectmgmt)
- [src/main/resources/fxml](/Users/yennhn/eclipse-workspace/project-sem2/src/main/resources/fxml)
- [src/main/resources/css/style.css](/Users/yennhn/eclipse-workspace/project-sem2/src/main/resources/css/style.css)
- [src/main/resources/db/ProjectManagement_DDL_v3.sql](/Users/yennhn/eclipse-workspace/project-sem2/src/main/resources/db/ProjectManagement_DDL_v3.sql)
- [src/main/resources/database.properties](/Users/yennhn/eclipse-workspace/project-sem2/src/main/resources/database.properties)
- [src/main/resources/application.properties](/Users/yennhn/eclipse-workspace/project-sem2/src/main/resources/application.properties)
- [uploads/avatars](/Users/yennhn/eclipse-workspace/project-sem2/uploads/avatars)

Entry point:
- Maven JavaFX run: `com.aptech.projectmgmt.Main`
- Eclipse Java Application nen chay: [AppLauncher.java](/Users/yennhn/eclipse-workspace/project-sem2/src/main/java/com/aptech/projectmgmt/AppLauncher.java)


=========================================================
5. CAC FILE CAU HINH QUAN TRONG
=========================================================

5.1. Cau hinh database
File:
- [src/main/resources/database.properties](/Users/yennhn/eclipse-workspace/project-sem2/src/main/resources/database.properties)

Format hien tai:
  db.url=jdbc:sqlserver://localhost:1433;databaseName=ProjectManagementDB;encrypt=false;trustServerCertificate=true
  db.username=sa
  db.password=Password123!
  db.poolSize=10

Ban phai doi:
- `db.username`
- `db.password`
cho dung voi may cua ban.

5.2. Cau hinh mail OTP
File:
- [src/main/resources/application.properties](/Users/yennhn/eclipse-workspace/project-sem2/src/main/resources/application.properties)

Can co cac key:
  smtp.host=smtp.gmail.com
  smtp.port=587
  smtp.username=your_email@gmail.com
  smtp.password=your_gmail_app_password
  smtp.from=your_email@gmail.com

Luu y:
- Khong nen de mail/password that khi chia se project
- Neu khong cau hinh SMTP dung, cac chuc nang OTP va gui mail thong bao se loi


=========================================================
6. HUONG DAN CHAY DATABASE TU DAU
=========================================================

File SQL chinh:
- [ProjectManagement_DDL_v3.sql](/Users/yennhn/eclipse-workspace/project-sem2/src/main/resources/db/ProjectManagement_DDL_v3.sql)

File nay se:
- Tao database `ProjectManagementDB`
- Tao tat ca bang, index, trigger, stored procedure, view
- Tao san account test de dang nhap ngay

6.1. Cach chay
1. Mo DBeaver hoac SQL Server Management Studio
2. Ket noi toi SQL Server local
3. Mo file:
   `src/main/resources/db/ProjectManagement_DDL_v3.sql`
4. Chay toan bo file

6.2. Luu y quan trong
- File nay co `CREATE DATABASE ProjectManagementDB`
- Neu may ban da co database trung ten roi thi can xoa DB cu hoac doi ten DB truoc khi chay lai

6.3. Sau khi chay xong
Kiem tra da co:
- Database `ProjectManagementDB`
- Bang `Account`, `Staff`, `Student`, `Project`, `Task`, ...
- Stored procedure `sp_GenerateOtp`, `sp_VerifyOtp`, `sp_ResetOverdueTasks`


=========================================================
7. TAI KHOAN TEST CO SAN SAU KHI CHAY FILE SQL
=========================================================

Mat khau cho tat ca tai khoan test:
- `123`

7.1. Admin
- username: `admin`
- password: `123`

7.2. Teacher
- username: `gv001`
- password: `123`

- username: `gv002`
- password: `123`

7.3. Student
- username: `st001`
- password: `123`
- username: `st002`
- password: `123`
- username: `st003`
- password: `123`
- username: `st004`
- password: `123`
- username: `st005`
- password: `123`

Luu y:
- Cac tai khoan seed trong DDL dang de `IsFirstLogin = 0`
- Nghia la login test truc tiep duoc, khong bi ep OTP lan dau


=========================================================
8. HUONG DAN IMPORT VA CHAY TRONG ECLIPSE
=========================================================

8.1. Import project
1. Mo Eclipse
2. Chon:
   File > Import > Maven > Existing Maven Projects
3. Chon thu muc project:
   `/Users/yennhn/eclipse-workspace/project-sem2`
4. Finish
5. Chuot phai project > Maven > Update Project

8.2. Chon dung JDK
1. Chuot phai project > Properties
2. Vao Java Build Path
3. Dam bao project dang dung JDK 21
4. Vao Java Compiler
5. Dam bao compiler level la 21

8.3. Cau hinh SceneBuilder
1. Cai SceneBuilder
2. Trong Eclipse vao:
   Window > Preferences > JavaFX
   - Help > Eclipse Marketplace > e(fx)clipse > Restart Eclipse
3. Set duong dan SceneBuilder executable

Vi du macOS:
  /Applications/SceneBuilder.app/Contents/MacOS/SceneBuilder

8.4. Mo file FXML bang SceneBuilder
- Chuot phai file `.fxml`
- Open With > SceneBuilder
Bạn cần cài trước:
	Mở trình duyệt
	Vào trang tải Scene Builder của Gluon:
	https://gluonhq.com/products/scene-builder/
	Tải bản Windows
	Cài xong, thường sẽ có file:
	C:\Program Files\SceneBuilder\SceneBuilder.exe
	Sau đó quay lại Eclipse:
	
	Chuột phải file .fxml
	Open With > Other...
	Chọn External programs
	Browse...
	Trỏ tới:
	C:\Program Files\SceneBuilder\SceneBuilder.exe
	Tick Use it for all '*.fxml' files
	OK


=========================================================
9. HUONG DAN CHAY APP
=========================================================

9.1. Cach khuyen dung: Maven
Mo terminal tai root project va chay:
  mvn clean javafx:run

Hoac:
  mvn javafx:run

9.2. Chay trong Eclipse bang Maven Build
1. Chuot phai project
2. Run As > Maven build...
3. Goals:
   clean javafx:run
4. Run

9.3. Chay trong Eclipse bang Java Application
Neu muon bam Run truc tiep trong Eclipse:
- Hay chay [AppLauncher.java](/Users/yennhn/eclipse-workspace/project-sem2/src/main/java/com/aptech/projectmgmt/AppLauncher.java)
- Khong nen chay truc tiep `Main.java` neu launch config cua Eclipse chua dung

Main class nen chon:
  com.aptech.projectmgmt.AppLauncher

Ly do:
- Tranh loi `JavaFX runtime components are missing`


=========================================================
10. LUONG KHOI DONG APP
=========================================================

Khi chay app:
1. Splash screen hien logo Aptech
2. Thanh progress chay tu 0 den 100%
3. Sau khoang 5 giay moi vao man login

File lien quan:
- [Main.java](/Users/yennhn/eclipse-workspace/project-sem2/src/main/java/com/aptech/projectmgmt/Main.java)
- [SplashController.java](/Users/yennhn/eclipse-workspace/project-sem2/src/main/java/com/aptech/projectmgmt/controller/SplashController.java)
- [splash.fxml](/Users/yennhn/eclipse-workspace/project-sem2/src/main/resources/fxml/splash.fxml)


=========================================================
11. CAC ROLE VA QUYEN HIEN TAI
=========================================================

11.1. Admin
- Dang nhap vao dashboard admin
- Quan ly lop
- Quan ly sinh vien
- Quan ly giao vien
- Quan ly project
- Quan ly nhom
- Xem va quan ly task

11.2. Teacher
- Dang nhap vao dashboard teacher rieng
- Chi xem duoc lop va project ma minh huong dan
- Khong tao lop, khong them sinh vien, khong tao project nhu admin

11.3. Student
- Dang nhap vao dashboard sinh vien
- Xem project cua minh
- Xem task
- Tao task neu la truong nhom va du dieu kien
- Xem hop thu den


=========================================================
12. AVATAR
=========================================================

12.1. Avatar mac dinh
- He thong dung `no-image.jpg` neu account chua co avatar

12.2. Khi doi avatar
- User bam vao avatar o header
- Chon anh tu may
- App se copy anh vao:
  [uploads/avatars](/Users/yennhn/eclipse-workspace/project-sem2/uploads/avatars)
- Ten file duoc tao ngau nhien, khong trung
- Neu user da co avatar cu do project quan ly thi app se xoa file cu
- DB chi luu path tuong doi, vi du:
  `uploads/avatars/avatar_acc_3_xxxxxxxxxxxxx.jpg`

12.3. Khi copy project sang may khac
Neu muon giu avatar da upload:
- Phai copy ca thu muc:
  `uploads/avatars`


=========================================================
13. LUU Y VE MAIL / OTP
=========================================================

Tinh nang can mail:
- Quen mat khau
- OTP doi mat khau lan dau
- Gui thong bao khi tao sinh vien moi
- Gui thong bao khi tao giao vien moi
- Gui nhac nho task / mail lien quan task

Neu mail khong gui duoc:
- Kiem tra `application.properties`
- Kiem tra Gmail App Password
- Kiem tra ket noi Internet


=========================================================
14. CAC MAN HINH CHINH
=========================================================

Admin:
- Login
- OTP
- Staff Dashboard
- Quan ly lop
- Quan ly sinh vien
- Quan ly giao vien
- Quan ly project
- Chi tiet project
- Chi tiet nhom
- Danh sach task

Teacher:
- Teacher Dashboard
- Lop cua toi
- Project huong dan

Student:
- Student Dashboard
- Du an cua toi
- Cong viec cua toi
- Hop thu den


=========================================================
15. CAU TRUC MA NGUON
=========================================================

Java source:
- [src/main/java/com/aptech/projectmgmt](/Users/yennhn/eclipse-workspace/project-sem2/src/main/java/com/aptech/projectmgmt)

Package chinh:
- `config`     : cau hinh DB
- `model`      : model va enum
- `repository` : query SQL
- `service`    : business logic
- `controller` : controller JavaFX
- `util`       : SceneManager, SessionManager, AlertUtil, AvatarUtil...

Resource:
- [src/main/resources/fxml](/Users/yennhn/eclipse-workspace/project-sem2/src/main/resources/fxml)
- [src/main/resources/css/style.css](/Users/yennhn/eclipse-workspace/project-sem2/src/main/resources/css/style.css)
- [src/main/resources/images](/Users/yennhn/eclipse-workspace/project-sem2/src/main/resources/images)
- [src/main/resources/db](/Users/yennhn/eclipse-workspace/project-sem2/src/main/resources/db)


=========================================================
16. LENH MAVEN HUU ICH
=========================================================

Compile:
  mvn compile

Compile bo qua test:
  mvn -q -DskipTests compile

Chay app:
  mvn javafx:run

Clean:
  mvn clean

Clean va run:
  mvn clean javafx:run


=========================================================
17. CACH CHUYEN PROJECT SANG MAY KHAC
=========================================================

17.1. Copy source code
Copy toan bo thu muc project, bao gom:
- source code
- resources
- uploads/avatars neu muon giu avatar da upload

17.2. Tren may moi, can cai:
- JDK 21
- Maven
- SQL Server
- Eclipse
- SceneBuilder

17.3. Chay database
1. Mo SQL tool
2. Chay file:
   [ProjectManagement_DDL_v3.sql](/Users/yennhn/eclipse-workspace/project-sem2/src/main/resources/db/ProjectManagement_DDL_v3.sql)
3. File nay tao DB va seed luon account test

17.4. Sua cau hinh
Sua file:
- [database.properties](/Users/yennhn/eclipse-workspace/project-sem2/src/main/resources/database.properties)
- [application.properties](/Users/yennhn/eclipse-workspace/project-sem2/src/main/resources/application.properties)

17.5. Import vao Eclipse
- Import Maven project
- Update Project
- Chay bang Maven hoac `AppLauncher`


=========================================================
18. NHUNG LOI THUONG GAP
=========================================================

18.1. Loi khong ket noi DB
Kiem tra:
- SQL Server da mo chua
- dung port 1433 chua
- username/password trong `database.properties` dung chua

18.2. Loi JavaFX runtime components are missing
Cach khac phuc:
- Chay bang `mvn javafx:run`
hoac
- Chay [AppLauncher.java](/Users/yennhn/eclipse-workspace/project-sem2/src/main/java/com/aptech/projectmgmt/AppLauncher.java) thay vi `Main.java`

18.3. Khong gui duoc mail
Kiem tra:
- `application.properties`
- Gmail App Password
- ket noi Internet

18.4. Login dung ma khong vao duoc dashboard
Kiem tra:
- file SQL da chay xong chua
- DB da co `Account`, `Staff`, `Student`
- role co dung khong


=========================================================
19. GHI CHU QUAN TRONG
=========================================================

- Project nay dung JavaFX + FXML + SceneBuilder, khong dung Swing
- Khong query DB tren FX Thread
- Repository chi chua SQL
- Service chua business logic
- Scene chuyen bang `SceneManager`
- Session dang nhap luu trong `SessionManager`
- Password luu bang BCrypt
- Splash hien truoc login
- Avatar upload luu trong project


=========================================================
20. TOM TAT NHANH DE CHAY DUOC NGAY
=========================================================

Neu ban chi muon chay nhanh tren may moi:
1. Cai JDK 21
2. Cai Maven
3. Cai SQL Server
4. Chay file:
   `src/main/resources/db/ProjectManagement_DDL_v3.sql`
5. Sua:
   `src/main/resources/database.properties`
6. Neu can OTP/mail thi sua:
   `src/main/resources/application.properties`
7. Mo terminal tai project root
8. Chay:
   `mvn clean javafx:run`
9. Login bang:
   `admin / 123`

=========================================================
END OF README
=========================================================
