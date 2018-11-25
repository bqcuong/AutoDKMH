package net.bqc.autodkmh;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sau quá trình tồn tại lâu dài, mang lại cơ hội học tập cho nhiều thế hệ sv VNU.
 * Tool AutoDKMH sẽ được lược bỏ các phần mã nguồn quan trọng,
 * thay vào đó là các chỉ dẫn giúp bạn tự định hướng để xây dựng tool của riêng mình.
 *
 * Nói cách khác, từ bây giờ AutoDKMH sẽ là "một bài tập" để những bạn thực sự cần đến
 * nó sử dụng và hoàn thành.
 *
 * Tư tưởng của tool AutoDKMH này là mô phỏng lại các bước trong quá trình đăng ký môn học của bạn.
 * Hoạt động tương tự như cách một bạn sinh viên sử dụng trình duyệt đăng ký nhưng tận dụng tốc độ khủng khiếp và khả
 * năng "có sẵn" (availability) 24/7 của máy tính.
 *
 * Để có thể thực hiện "bài tập" này, các bạn cần nắm rõ các kiến thức:
 * 1. Http request là gì? Cách sử dụng nó trong Java như thế nào? Tại bài tập này, các bạn chỉ cần biết GET/POST là đủ.
 *    Hướng dẫn sử dụng HttpURLConnection của Java để thực hiện các GET/POST request.
 * 2. Cookie là gì? Tại sao cần Cookie? Làm sao để kích hoạt Cookie trong HttpURLConnection của Java?
 * 3. CSRF là gì? Hiểu được tại sao khi submit một form POST lại cần đính kèm một CSRF token.
 * 4. Làm sao để trích xuất thông tin từ HTML thô trong Java? Gợi ý sử dụng thư viện Jsoup.
 *
 * @Created by cuong on 2/12/2015.
 * @Updated by cuong on 25/11/2018.
 */
public class AutoDKMH {

    /**
     * Địa chỉ máy chủ đăng ký, có 2 máy chủ, có thể chuyển sang máy chủ phụ khi cần thiết
     */
    public final static String HOST = "http://dangkyhoc.vnu.edu.vn";

    /**
     * Path tới API đăng nhập (mình tạm gọi là API)
     */
    public final static String LOGIN_URL = HOST + "/dang-nhap";

    /**
     * Path tới API đăng xuất
     */
    public final static String LOGOUT_URL = HOST + "/Account/Logout";

    /**
     * Path tới API lấy dữ liệu danh sách môn học chuyên ngành
     */
    public final static String AVAILABLE_COURSES_DATA_URL_MAJOR = HOST + "/danh-sach-mon-hoc/1/1";

    /**
     * Path tới API lấy dữ liệu danh sách môn học toàn trường
     */
    public final static String AVAILABLE_COURSES_DATA_URL_ALL = HOST + "/danh-sach-mon-hoc/1/2";

    /**
     * Path tới API lấy dữ liệu môn học đã đăng ký
     */
    public final static String REGISTERED_COURSES_DATA_URL = HOST + "/danh-sach-mon-hoc-da-dang-ky/1";

    /**
     * Path tới API kiểm tra điều kiện tiên quyết, "%s" sẽ được thay thế bởi "data-crdid" của môn học
     */
    public final static String CHECK_PREREQUISITE_COURSES_URL = HOST + "/kiem-tra-tien-quyet/%s/1";

    /**
     * Path tới API chọn môn học đăng ký, "%s" sẽ được thay thế bởi "data-rowindex" của môn học
     */
    public final static String CHOOSE_COURSE_URL = HOST + "/chon-mon-hoc/%s/1/1";

    /**
     * Path tới API ghi nhận đăng ký
     */
    public final static String SUBMIT_URL = HOST + "/xac-nhan-dang-ky/1";

    /**
     * Connection dùng chung cho tất cả http request,
     * nên dùng chung một connection bởi vì liên quan đến việc sử dụng cookie
     */
    private HttpURLConnection con;

    /**
     * Tên tài khoản
     */
    private String user;

    /**
     * Mật khẩu tài khoản
     */
    private String password;

    /**
     * Danh sách mã môn học cần đăng ký
     */
    private List<String> courseCodes;

    /**
     * Phương thức khởi tạo, làm nhiệm vụ load thông tin cấu hình cho tool
     */
    public AutoDKMH() {
        this.courseCodes = new ArrayList<>();
        loadInitialParameters("config.properties");
    }

    /**
     * Phương thức main
     */
    public static void main(String args[]) throws InterruptedException {
        AutoDKMH tool = new AutoDKMH();

        logn("/******************************************/");
        logn("//! Username = " + tool.user);
        logn("//! Password = " + "********");
        logn("//! Course Codes = " + tool.courseCodes);
        logn("/******************************************/");

        tool.run();
    }

    /**
     * Load thông tin từ tệp cấu hình cấu hình (properties file).
     * Thông tin gồm tài khoản đăng nhập và danh sách mã môn học cần đăng ký.
     * Tên tài khoản gán vào thuộc tính username
     * Mật khẩu tài khoản gán vào thuộc tính password
     * Danh sách mã môn học cần đăng ký gán vào thuộc tính courseCodes
     *
     * @param   filePath
     *          đường dẫn đến tệp cấu hình
     */
    private void loadInitialParameters(String filePath) {
    }

    /**
     * The entrance to dark world...
     */
    private void run() throws InterruptedException {
        Calendar cal = Calendar.getInstance();

        /*
         * Thực hiện các đợt đăng ký liên tiếp, chỉ dừng lại khi tất cả môn học đã được đăng ký
         */
        while (true) {
            logn("\n/******************************************/");
            logn("Try on: " + cal.getTime().toString());

            // đăng nhập bao giờ thành công thì mới thực hiện các bước tiếp theo
            try {
                login();
            }
            catch (UnsupportedOperationException e) {
                errn("login() is not implemented. Exit.");
                System.exit(1);
            }
            catch (Exception e) {
                errn("\nEncountered exception " + e.getMessage());
                logn("Try again...");
                continue;
            }

            // lọc và chỉ giữ lại những môn chưa được đăng ký trên hệ thống
            log("Filtering desired courses...");
            String registeredCoursesData = sendPost(REGISTERED_COURSES_DATA_URL, "");
            courseCodes = courseCodes.stream()
                    .filter(code -> !registeredCoursesData.contains(code))
                    .collect(Collectors.toList());
            logn("[Done]");
            logn("Filtered courses: " + courseCodes);

            /* trong trường hợp danh sách môn học sau khi lọc trống, có nghĩa rằng bạn đã đăng ký được tất cả
             * môn học mong muốn rồi. Lúc này tool ngừng hoạt động.
             */
            if (courseCodes.isEmpty()) {
                logn("\nCourses have been already registered!\n[Exit]");
                System.exit(1);
            }

            // lưu ý: bạn bắt buộc phải lấy dữ liệu danh sách môn học chuyên ngành thì hệ thống mới cho phép bạn đăng ký vào ghi nhận
            sendPost(AVAILABLE_COURSES_DATA_URL_MAJOR, "");

            log("Get raw courses data...");
            // lấy dữ liệu danh sách môn học toàn trường (luôn đầy đủ các môn học)
            String coursesData = sendPost(AVAILABLE_COURSES_DATA_URL_ALL, "");
            logn("[Done]");

            // thực hiện đăng ký các môn học theo các mã môn học từ tệp cấu hình
            for (Iterator<String> it = courseCodes.iterator(); it.hasNext();) {
                String courseCode = it.next();

                log("\nGetting course information for [" + courseCode + "]...");
                // lấy thông tin data-crdid và data-rowindex của môn học
                String courseDetails[] = getCourseDetailsFromCoursesData(coursesData, courseCode);
                logn("[Done]");

                if (courseDetails != null) {
                    log("Checking prerequisite courses...");
                    // thực hiện kiểm tra điền kiện tiên quyết
                    String res = sendPost(String.format(CHECK_PREREQUISITE_COURSES_URL, courseDetails[0]), "");
                    logn("[Done]");
                    logn("Response: " + res);

                    // thực hiện chọn môn học
                    log("Choose [" + courseCodes + "] for queue...");
                    res = sendPost(String.format(CHOOSE_COURSE_URL, courseDetails[1]), "");
                    logn("[Done]");
                    logn("Response: " + res);

                    // bỏ mã môn học ra danh sách cần đăng ký khi đã thành công
                    if (res.contains("thành công"))
                        it.remove();
                }
            }

            log("Submitting...");
            // thực hiện ghi nhận đăng ký
            String res = sendPost(String.format(SUBMIT_URL, ""), "");
            logn("[Done]");
            logn("Response: " + res);

            log("Logging out...");
            // đăng xuất khỏi hệ thống
            sendGet(LOGOUT_URL);
            logn("[Success]");

            // nếu đăng ký thành công tất cả môn học, thoát
            if (courseCodes.isEmpty()) {
                logn("\nRegistered all!\n[Exit]");
                System.exit(1);
            }

            logn("/******************************************/");

            // tạm nghỉ 2s để thực hiện đợt đăng ký tiếp theo (chưa đăng ký được hết các môn)
            Thread.sleep(2000);
        }
    }

    /**
     * Thực hiện đăng nhập vào hệ thống.
     *
     * Gợi ý: Đầu tiên gửi GET request đến API đăng nhập để lấy được CSRF token,
     * sau đó gửi POST request đến API đăng nhập để thực hiện đăng nhập.
     * Các tham số cần gửi trong POST request gồm LoginName, Password, __RequestVerificationToken
     *
     * Lưu ý: Nhớ kích hoạt Cookie để có thể "giữ liên lạc" với máy chủ trong những request sau này
     */
    private void login() {
        throw new UnsupportedOperationException();
    }

    /**
     * Lấy thông tin về data-crdid và data-rowindex của môn học muốn đăng ký.
     * Hai thông tin này được sử dụng khi gọi vào các API "kiểm tra điều kiện tiên quyết" và "chọn môn học đăng ký".
     *
     * Gợi ý: Sử dụng thư viện Jsoup để trích xuất các thông tin này từ dữ liệu HTML.
     *
     * @param   coursesDataHtml
     *          dữ liệu HTML thô (lấy được khi gửi yêu cầu tới các API lấy dữ liệu danh sách môn học),
     *          xem mẫu dữ liệu html thô này ở tệp data/data.html.zip
     *
     * @param   courseCode
     *          mã của môn học muốn đăng ký
     *
     * @return  Mảng String, trong đó phần tử đầu tiên là data-crdid, phần tử thứ hai là data-rowindex. Trong trường
     *          hợp không tìm thấy thông tin của môn học, trả về null.
     */
    private String[] getCourseDetailsFromCoursesData(String coursesDataHtml, String courseCode) {
        return null;
    }

    /**
     * Thực hiện POST request
     *
     * @param   urlStr
     *          URL mong muốn gửi POST request đến
     * @param   postParams
     *          các tham số muốn đính kèm theo request, ngăn cách nhau bởi kí tự '&'
     *
     * @return  Kết quả trả về từ máy chủ, biểu diễn ở dạng String
     */
    private String sendPost(String urlStr, String postParams) {
        return null;
    }

    /**
     * Thực hiện GET request
     *
     * @param   urlStr
     *          URL mong muốn gửi GET request đến
     *
     * @return  Kết quả trả về từ máy chủ, biểu diễn ở dạng String
     */
    private String sendGet(String urlStr) {
        return null;
    }

    /**
     * Ghi log
     * @param   message
     *          nội dung để ghi log
     */
    private static void log(String message) {
        System.out.print(message);
    }

    /**
     * Ghi log và tạo dòng mới
     * @param   message
     *          nội dung để ghi log
     */
    private static void logn(String message) {
        log(message + "\n");
    }

    /**
     * Ghi lỗi và tạo dòng mới
     * @param   message
     *          nội dung để ghi lỗi
     */
    private static void errn(String message) {
        System.err.println(message);
    }
}